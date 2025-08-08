package com.example.LAGO.service;

import com.example.LAGO.ai.sentiment.FinBertSentimentService;
import com.example.LAGO.domain.News;
import com.example.LAGO.domain.StockInfo;
import com.example.LAGO.dto.response.FinBertResponse;
import com.example.LAGO.dto.response.NaverNewsResponse;
import com.example.LAGO.dto.response.NewsAnalysisResult;
import com.example.LAGO.repository.NewsRepository;
import com.example.LAGO.repository.StockInfoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NewsService {
    
    private final NewsRepository newsRepository;
    private final StockInfoRepository stockInfoRepository;
    private final NaverNewsClient naverNewsClient;
    private final NaverFinanceNewsClient naverFinanceNewsClient;
    private final ClaudeClient claudeClient;
    private final FinBertSentimentService finBertSentimentService;
    
    // 일반 투자 뉴스 키워드
    private static final List<String> GENERAL_KEYWORDS = Arrays.asList(
            "주식", "투자", "증권", "코스피", "코스닥", "경제"
    );
    
    @Transactional
    public void collectAndSaveGeneralNews() {
        log.info("=== 일반 투자 뉴스 수집 시작 (네이버 증권 페이지) ===");
        
        try {
            log.info("네이버 증권 페이지 크롤링 시작...");
            // 네이버 증권 페이지에서 직접 크롤링
            NaverNewsResponse response = naverFinanceNewsClient.crawlFinanceNews(30);
            
            if (response.getItems() != null) {
                int savedCount = 0;
                
                for (NaverNewsResponse.NewsItem item : response.getItems()) {
                    if (!newsRepository.existsByUrl(item.getLink())) {
                        News news = convertToGeneralNews(item);
                        if (news != null) {
                            newsRepository.save(news);
                            savedCount++;
                        }
                    }
                }
                
                log.info("일반 투자 뉴스 {}개 수집 완료", savedCount);
            }
        } catch (Exception e) {
            log.error("일반 투자 뉴스 수집 실패: {}", e.getMessage());
        }
    }
    
    @Transactional
    public void collectAndSaveStockNews() {
        log.info("=== 관심종목 뉴스 수집 시작 ===");
        
        try {
            // 상위 20개 주요 종목 가져오기
            List<StockInfo> majorStocks = stockInfoRepository.findTop20ByOrderByStockInfoIdAsc();
            
            if (majorStocks.isEmpty()) {
                log.warn("STOCK_INFO 테이블에 데이터가 없어 관심종목 뉴스 수집을 건너뜁니다.");
                return;
            }
            
            int totalSaved = 0;
            
            for (StockInfo stock : majorStocks) {
                try {
                    NaverNewsResponse response = naverNewsClient.searchStockNews(stock.getName(), 10, 1);
                    
                    if (response.getItems() != null) {
                        for (NaverNewsResponse.NewsItem item : response.getItems()) {
                            if (!newsRepository.existsByUrl(item.getLink())) {
                                News news = convertToStockNews(item, stock);
                                if (news != null) {
                                    newsRepository.save(news);
                                    totalSaved++;
                                }
                            }
                        }
                    }
                    
                    // API 호출 제한을 위한 잠시 대기
                    Thread.sleep(100);
                    
                } catch (Exception e) {
                    log.warn("종목 {} 뉴스 수집 실패: {}", stock.getName(), e.getMessage());
                }
            }
            
            log.info("관심종목 뉴스 {}개 수집 완료", totalSaved);
            
        } catch (Exception e) {
            log.error("관심종목 뉴스 수집 실패: {}", e.getMessage());
        }
    }
    
    private News convertToGeneralNews(NaverNewsResponse.NewsItem item) {
        try {
            String cleanTitle = cleanHtmlTags(item.getTitle());
            String cleanDescription = cleanHtmlTags(item.getDescription());
            
            // 네이버 증권 페이지에서 가져온 뉴스는 이미 금융 뉴스이므로 필터링 제거
            
            // FinBERT URL 기반 종합 분석 (본문 + 이미지 + 감정분석)
            NewsAnalysisResult analysisResult;
            try {
                log.info("FinBERT URL 기반 분석 시작: {}", item.getLink());
                analysisResult = finBertSentimentService.analyzeNewsByUrl(item.getLink());
                log.info("FinBERT URL 분석 완료 - 제목: {}, 본문: {}자, 이미지: {}개, 감정: {}", 
                    analysisResult.getTitle() != null ? analysisResult.getTitle().substring(0, Math.min(30, analysisResult.getTitle().length())) : "없음",
                    analysisResult.getContent() != null ? analysisResult.getContent().length() : 0,
                    analysisResult.getImages() != null ? analysisResult.getImages().size() : 0,
                    analysisResult.getLabel());
            } catch (Exception e) {
                log.warn("FinBERT URL 분석 실패, 기본값 사용: {}", e.getMessage());
                // 기본값 설정
                analysisResult = createDefaultNewsAnalysisResult();
            }
            
            // 실제 추출된 제목과 본문이 있으면 사용, 없으면 네이버 API 데이터 사용
            String finalTitle = (analysisResult.getTitle() != null && !analysisResult.getTitle().isEmpty()) ? 
                analysisResult.getTitle() : cleanTitle;
            String finalContent = (analysisResult.getContent() != null && !analysisResult.getContent().isEmpty()) ? 
                analysisResult.getContent() : cleanDescription;
            
            // Claude로 요약 생성 - 서비스 실패시 기본값 사용
            String summary;
            try {
                summary = claudeClient.summarizeNews(finalTitle, finalContent);
            } catch (Exception e) {
                log.warn("Claude API 호출 실패, 기본 요약 사용: {}", e.getMessage());
                summary = finalContent.length() > 200 ? finalContent.substring(0, 200) + "..." : finalContent;
            }
            
            return News.builder()
                    .title(finalTitle)
                    .content(finalContent)
                    .summary(summary)
                    .url(item.getLink())
                    .publishedDate(parsePublishedDate(item.getPubDate()))
                    .source(extractSource(item.getLink()))
                    .stockCode(null) // 일반 뉴스는 특정 종목 없음
                    .stockName(null)
                    .keywords(String.join(", ", GENERAL_KEYWORDS))
                    .images(analysisResult.getImages()) // FinBERT에서 추출한 이미지들
                    // FinBERT 감정분석 핵심 결과만 저장
                    .sentiment(analysisResult.getLabel()) // 호재악재 표시
                    .confidenceLevel(analysisResult.getConfidenceLevel()) // 신뢰도
                    .tradingSignal(analysisResult.getTradingSignal()) // 거래신호
                    .build();
                    
        } catch (Exception e) {
            log.warn("일반 뉴스 변환 실패: {}", e.getMessage());
            return null;
        }
    }
    
    private News convertToStockNews(NaverNewsResponse.NewsItem item, StockInfo stock) {
        try {
            String cleanTitle = cleanHtmlTags(item.getTitle());
            String cleanDescription = cleanHtmlTags(item.getDescription());
            
            // FinBERT URL 기반 종합 분석 (본문 + 이미지 + 감정분석)
            NewsAnalysisResult analysisResult;
            try {
                log.info("FinBERT URL 기반 분석 시작: {}", item.getLink());
                analysisResult = finBertSentimentService.analyzeNewsByUrl(item.getLink());
                log.info("FinBERT URL 분석 완료 - 제목: {}, 본문: {}자, 이미지: {}개, 감정: {}", 
                    analysisResult.getTitle() != null ? analysisResult.getTitle().substring(0, Math.min(30, analysisResult.getTitle().length())) : "없음",
                    analysisResult.getContent() != null ? analysisResult.getContent().length() : 0,
                    analysisResult.getImages() != null ? analysisResult.getImages().size() : 0,
                    analysisResult.getLabel());
            } catch (Exception e) {
                log.warn("FinBERT URL 분석 실패, 기본값 사용: {}", e.getMessage());
                // 기본값 설정
                analysisResult = createDefaultNewsAnalysisResult();
            }
            
            // 실제 추출된 제목과 본문이 있으면 사용, 없으면 네이버 API 데이터 사용
            String finalTitle = (analysisResult.getTitle() != null && !analysisResult.getTitle().isEmpty()) ? 
                analysisResult.getTitle() : cleanTitle;
            String finalContent = (analysisResult.getContent() != null && !analysisResult.getContent().isEmpty()) ? 
                analysisResult.getContent() : cleanDescription;
            
            // Claude로 요약 생성 - 서비스 실패시 기본값 사용
            String summary;
            try {
                summary = claudeClient.summarizeNews(finalTitle, finalContent);
            } catch (Exception e) {
                log.warn("Claude API 호출 실패, 기본 요약 사용: {}", e.getMessage());
                summary = finalContent.length() > 200 ? finalContent.substring(0, 200) + "..." : finalContent;
            }
            
            return News.builder()
                    .title(finalTitle)
                    .content(finalContent)
                    .summary(summary)
                    .url(item.getLink())
                    .publishedDate(parsePublishedDate(item.getPubDate()))
                    .source(extractSource(item.getLink()))
                    .stockCode(stock.getCode())
                    .stockName(stock.getName())
                    .stockInfo(stock) // StockInfo 관계 설정
                    .keywords(stock.getName() + ", 주식")
                    .images(analysisResult.getImages()) // FinBERT에서 추출한 이미지들
                    // FinBERT 감정분석 핵심 결과만 저장
                    .sentiment(analysisResult.getLabel()) // 호재악재 표시
                    .confidenceLevel(analysisResult.getConfidenceLevel()) // 신뢰도
                    .tradingSignal(analysisResult.getTradingSignal()) // 거래신호
                    .build();
                    
        } catch (Exception e) {
            log.warn("종목 뉴스 변환 실패: {}", e.getMessage());
            return null;
        }
    }
    
    private String cleanHtmlTags(String text) {
        if (text == null) return "";
        return text.replaceAll("<[^>]*>", "").trim();
    }
    
    private LocalDateTime parsePublishedDate(String pubDate) {
        try {
            // 네이버 API 날짜 형식: "Mon, 08 Aug 2025 10:30:00 +0900"
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss Z");
            return LocalDateTime.parse(pubDate, formatter);
        } catch (DateTimeParseException e) {
            log.warn("날짜 파싱 실패: {} - 현재 시간으로 설정", pubDate);
            return LocalDateTime.now();
        }
    }
    
    private String extractSource(String url) {
        try {
            if (url.contains("naver.com")) return "네이버뉴스";
            if (url.contains("chosun.com")) return "조선일보";
            if (url.contains("joongang.co.kr")) return "중앙일보";
            if (url.contains("donga.com")) return "동아일보";
            if (url.contains("hani.co.kr")) return "한겨레";
            if (url.contains("mk.co.kr")) return "매일경제";
            if (url.contains("hankyung.com")) return "한국경제";
            if (url.contains("newsis.com")) return "뉴시스";
            if (url.contains("yonhapnews.co.kr")) return "연합뉴스";
            return "기타언론사";
        } catch (Exception e) {
            return "알수없음";
        }
    }
    
    // 뉴스 조회 메서드들
    public Page<News> getGeneralInvestmentNews(Pageable pageable) {
        return newsRepository.findGeneralInvestmentNews(
                "%주식%", "%투자%", "%증권%", "%코스피%", "%코스닥%", "%경제%", pageable);
    }
    
    public Page<News> getStockNews(List<String> stockCodes, Pageable pageable) {
        return newsRepository.findByStockCodesOrderByPublishedDateDesc(stockCodes, pageable);
    }
    
    public Page<News> searchNews(String keyword, Pageable pageable) {
        return newsRepository.findByKeywordSearch(keyword, pageable);
    }
    
    /**
     * FinBERT 서비스 호출 실패 시 사용할 기본값 생성
     */
    private FinBertResponse createDefaultSentimentResult() {
        FinBertResponse response = new FinBertResponse();
        response.setLabel("NEUTRAL");
        response.setScore(0.0);
        response.setRawScore(0.0);
        response.setKeywordAdjustment(0.0);
        response.setConfidence(0.5);
        response.setConfidenceLevel("medium");
        response.setTradingSignal("HOLD");
        response.setSignalStrength(0.0);
        return response;
    }
    
    /**
     * FinBERT URL 분석 실패 시 사용할 기본값 생성
     */
    private NewsAnalysisResult createDefaultNewsAnalysisResult() {
        NewsAnalysisResult result = new NewsAnalysisResult();
        result.setTitle("제목을 가져올 수 없습니다");
        result.setContent("본문을 가져올 수 없습니다");
        result.setImages(new ArrayList<>());
        result.setLabel("NEUTRAL");
        result.setScore(0.0);
        result.setConfidence(0.5);
        result.setTradingSignal("HOLD");
        result.setConfidenceLevel("medium");
        return result;
    }
    
    /**
     * 뉴스가 금융/경제 관련 내용인지 확인
     */
    private boolean isFinanceRelated(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }
        
        String lowerText = text.toLowerCase();
        
        // 금융/경제 관련 키워드
        String[] financeKeywords = {
            "경제", "증권", "금융", "투자", "주식", "주가", "코스피", "코스닥", 
            "증시", "상승", "하락", "거래", "매수", "매도", "시장", "환율",
            "금리", "채권", "펀드", "자산", "수익", "손실", "배당", "상장",
            "기업", "실적", "매출", "영업이익", "순이익", "적자", "흑자",
            "은행", "보험", "증권사", "정부", "정책", "부동산", "원자재",
            "달러", "엔화", "위안", "유로", "원화", "통화", "인플레이션",
            "gdp", "경기", "침체", "회복", "성장", "수출", "수입", "무역"
        };
        
        // 제외할 키워드 (패션, 연예 등 - 스포츠는 경제 섹션에 포함될 수 있으므로 제외)
        String[] excludeKeywords = {
            "패션", "스타일", "메이크업", "화장", "뷰티", "코스메틱",
            "연예", "드라마", "영화", "가수", "배우", "아이돌", "셀럽", "연예인",
            "요리", "레시피", "맛집", "카페"
        };
        
        // 제외 키워드가 포함되어 있으면 false
        for (String keyword : excludeKeywords) {
            if (lowerText.contains(keyword)) {
                return false;
            }
        }
        
        // 금융 키워드가 포함되어 있으면 true
        for (String keyword : financeKeywords) {
            if (lowerText.contains(keyword)) {
                return true;
            }
        }
        
        return false;
    }
}