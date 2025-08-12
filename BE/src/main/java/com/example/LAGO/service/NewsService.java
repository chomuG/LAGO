package com.example.LAGO.service;

import com.example.LAGO.ai.sentiment.FinBertSentimentService;
import com.example.LAGO.domain.News;
import com.example.LAGO.domain.StockInfo;
import com.example.LAGO.domain.WatchList;  // 관심종목 엔티티
import com.example.LAGO.dto.response.NewsResponse;
import com.example.LAGO.repository.NewsRepository;
import com.example.LAGO.repository.StockInfoRepository;
import com.example.LAGO.repository.WatchListRepository;  // 관심종목 Repository
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NewsService {

    private final NewsRepository newsRepository;
    private final StockInfoRepository stockInfoRepository;
    private final WatchListRepository watchListRepository;  // 관심종목 Repository
    private final RestTemplate restTemplate;
    private final NewsSaver newsSaver;

    @Value("${finbert.url:http://localhost:8000}")
    private String finbertUrl;

    // 수집 상태 추적용
    private final Map<String, LocalDateTime> lastCollectionTime = new ConcurrentHashMap<>();

    /**
     * Google RSS 실시간 뉴스 수집
     * 스케줄링은 NewsScheduler에서 관리
     */
    public void collectRealtimeNews() {
        log.info("=== Google RSS 실시간 뉴스 수집 시작 ===");

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");

            Map<String, Object> requestBody = Map.of("limit", 10);  // 병렬 처리 효과를 위해 10개로 증가
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    finbertUrl + "/collect/realtime",
                    HttpMethod.POST,
                    entity,
                    Map.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                processNewsResponse(response.getBody(), "REALTIME", null);
                lastCollectionTime.put("REALTIME", LocalDateTime.now());
            } else {
                log.warn("Google RSS 실시간 뉴스 수집 실패: {}", response.getStatusCode());
            }

        } catch (Exception e) {
            log.error("Google RSS 실시간 뉴스 수집 중 오류 발생: {}", e.getMessage(), e);
        }
    }

    /**
     * DB에서 관심종목 조회 후 뉴스 수집
     * 트랜잭션을 개별 뉴스 저장 단위로 분리하여 장시간 락 방지
     */
    public void collectWatchlistNewsFromDB() {
        log.info("=== DB 기반 관심종목 뉴스 수집 시작 ===");

        try {
            // 사용자가 하트 누른 관심종목 조회
            List<WatchList> watchListItems = watchListRepository.findAllActive();

            if (watchListItems.isEmpty()) {
                log.info("활성화된 관심종목이 없습니다.");
                return;
            }

            log.info("관심종목 {}개 발견", watchListItems.size());

            // 종목별로 순차 처리
            for (WatchList watchItem : watchListItems) {
                try {
                    StockInfo stock = watchItem.getStock();

                    if (stock == null) {
                        log.warn("관심종목에 연결된 종목 정보가 없습니다: {}", watchItem.getId());
                        continue;
                    }

                    log.info("관심종목 뉴스 수집: {} ({})", stock.getCompanyName(), stock.getStockCode());

                    collectSingleWatchlistNews(
                            stock.getStockCode(),
                            stock.getCompanyName(),
                            getAliasesForStock(stock),
                            5  // 종목당 5개 뉴스
                    );

                    // API 부하 방지를 위한 지연
                    Thread.sleep(2000); // 2초 대기

                } catch (Exception e) {
                    log.error("관심종목 뉴스 수집 실패 - ID: {}, 오류: {}",
                            watchItem.getId(), e.getMessage());
                }
            }

            lastCollectionTime.put("WATCHLIST", LocalDateTime.now());
            log.info("=== DB 기반 관심종목 뉴스 수집 완료 ===");

        } catch (Exception e) {
            log.error("관심종목 뉴스 수집 중 오류 발생: {}", e.getMessage(), e);
        }
    }

    /**
     * 단일 관심종목 뉴스 수집
     */
    public void collectSingleWatchlistNews(String symbol, String companyName,
                                           List<String> aliases, int limit) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");

            Map<String, Object> requestBody = Map.of(
                    "symbol", symbol,
                    "company_name", companyName,
                    "aliases", aliases != null ? aliases : new ArrayList<>(),
                    "limit", limit
            );

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    finbertUrl + "/collect/watchlist",
                    HttpMethod.POST,
                    entity,
                    Map.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                processNewsResponse(response.getBody(), "WATCHLIST", symbol);
                log.info("관심종목 {} 뉴스 수집 성공", companyName);
            } else {
                log.warn("관심종목 {} 뉴스 수집 실패: {}", companyName, response.getStatusCode());
            }

        } catch (Exception e) {
            log.error("관심종목 {} 뉴스 수집 중 오류: {}", companyName, e.getMessage());
        }
    }

    /**
     * 뉴스 응답 처리 및 DB 저장
     */
    private void processNewsResponse(Map<String, Object> responseBody,
                                     String collectionType, String stockCode) {
        if (responseBody == null) {
            log.warn("응답 본문이 null입니다.");
            return;
        }

        Integer count = (Integer) responseBody.get("count");

        if (responseBody.get("news") instanceof List) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> newsList = (List<Map<String, Object>>) responseBody.get("news");

            int savedCount = 0;
            int duplicateCount = 0;
            int failedCount = 0;

            for (Map<String, Object> newsData : newsList) {
                try {
                    // 종목 코드 설정 (관심종목인 경우)
                    if (stockCode != null) {
                        newsData.put("symbol", stockCode);
                    }

                    // 수집 타입 태깅
                    newsData.put("collection_type", collectionType);

                    News news = convertToNewsEntity(newsData);

                    // 중복 체크 (URL 기반)
                    if (isDuplicateNews(news.getUrl())) {
                        duplicateCount++;
                        log.debug("중복 뉴스 스킵: {}", news.getUrl());
                        continue;
                    }
                    
                    // URL 확인 로그
                    log.info("뉴스 저장 준비 - 제목: {}, URL: {}", 
                            news.getTitle().substring(0, Math.min(news.getTitle().length(), 50)),
                            news.getUrl());

                    // StockInfo 연결 (종목 코드가 있는 경우)
                    if (news.getStockCode() != null && !news.getStockCode().isEmpty()) {
                        stockInfoRepository.findByStockCode(news.getStockCode())
                                .ifPresent(news::setStock);
                    }

                    // 독립 트랜잭션으로 저장
                    boolean saved = newsSaver.upsertNews(news);
                    if (saved) {
                        savedCount++;
                    } else {
                        failedCount++;
                    }

                } catch (Exception e) {
                    failedCount++;
                    log.error("뉴스 처리 실패 - URL: {}, 오류: {}",
                            getStringValue(newsData, "url"), e.getMessage());
                }
            }

            log.info("[{}] 뉴스 처리 완료 - 성공: {}, 중복: {}, 실패: {} (전체: {})",
                    collectionType, savedCount, duplicateCount, failedCount, newsList.size());
        }
    }

    /**
     * 중복 뉴스 체크
     */
    private boolean isDuplicateNews(String url) {
        if (url == null || url.isEmpty()) {
            return false;
        }
        return newsRepository.existsByUrl(url);
    }

    /**
     * 종목별 검색 별칭 조회
     */
    private List<String> getAliasesForStock(StockInfo stock) {
        List<String> aliases = new ArrayList<>();

        // 회사명 변형 추가
        String companyName = stock.getCompanyName();
        if (companyName != null) {
            // 기본 회사명
            aliases.add(companyName);

            // 영문명이 있으면 추가
            if (stock.getCompanyNameEn() != null) {
                aliases.add(stock.getCompanyNameEn());
            }

            // 일반적인 약칭 생성
            if (companyName.endsWith("전자")) {
                aliases.add(companyName.replace("전자", ""));
            }
            if (companyName.endsWith("화학")) {
                aliases.add(companyName.replace("화학", ""));
            }
            if (companyName.endsWith("홀딩스")) {
                aliases.add(companyName.replace("홀딩스", ""));
            }
            if (companyName.endsWith("에너지솔루션")) {
                aliases.add(companyName.replace("에너지솔루션", "에너지"));
            }
        }

        // 중복 제거
        return aliases.stream().distinct().collect(Collectors.toList());
    }

    /**
     * 종목별 뉴스 조회
     */
    public Page<NewsResponse> getNewsByStock(String stockCode, Pageable pageable) {
        Page<News> newsPage = newsRepository.findByStockCodeOrderByPublishedDateDesc(stockCode, pageable);
        return newsPage.map(NewsResponse::from);
    }

    /**
     * 관심종목 뉴스 조회
     */
    public Page<NewsResponse> getWatchlistNews(Long userId, Pageable pageable) {
        // 사용자의 관심종목 코드 조회
        List<String> stockCodes = watchListRepository.findActiveStockCodesByUserId(userId);

        if (stockCodes.isEmpty()) {
            return Page.empty(pageable);
        }

        // 관심종목 뉴스 조회
        Page<News> newsPage = newsRepository.findByStockCodeInOrderByPublishedDateDesc(stockCodes, pageable);
        return newsPage.map(NewsResponse::from);
    }

    /**
     * 뉴스 목록 조회 (페이징)
     */
    public Page<NewsResponse> getNewsList(Pageable pageable) {
        Page<News> newsPage = newsRepository.findAllByOrderByCreatedAtDesc(pageable);
        return newsPage.map(NewsResponse::from);
    }

    /**
     * 특정 뉴스 조회
     */
    public NewsResponse getNewsById(Long id) {
        News news = newsRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("뉴스를 찾을 수 없습니다: " + id));

        return NewsResponse.from(news);
    }

    /**
     * 감성별 뉴스 통계 (종목별 옵션)
     */
    public Map<String, Object> getNewsSentimentStats(String stockCode) {
        List<News> newsList;

        if (stockCode != null && !stockCode.isEmpty()) {
            newsList = newsRepository.findByStockCode(stockCode);
        } else {
            newsList = newsRepository.findAll();
        }

        long positive = newsList.stream().filter(n -> "POSITIVE".equals(n.getSentiment())).count();
        long negative = newsList.stream().filter(n -> "NEGATIVE".equals(n.getSentiment())).count();
        long neutral = newsList.stream().filter(n -> "NEUTRAL".equals(n.getSentiment())).count();

        Map<String, Object> stats = new HashMap<>();
        stats.put("positive", positive);
        stats.put("negative", negative);
        stats.put("neutral", neutral);
        stats.put("total", (long) newsList.size());

        if (stockCode != null) {
            stats.put("stockCode", stockCode);
        }

        // 감성 점수 계산 (긍정 비율)
        if (!newsList.isEmpty()) {
            double sentimentScore = (double) positive / newsList.size() * 100;
            stats.put("sentimentScore", String.format("%.1f", sentimentScore));
        }

        return stats;
    }

    /**
     * 수집 상태 조회
     */
    public Map<String, Object> getCollectionStatus() {
        Map<String, Object> status = new HashMap<>();

        status.put("lastRealtimeCollection", lastCollectionTime.get("REALTIME"));
        status.put("lastWatchlistCollection", lastCollectionTime.get("WATCHLIST"));

        // 다음 수집 예정 시간
        if (lastCollectionTime.containsKey("REALTIME")) {
            LocalDateTime nextRealtime = lastCollectionTime.get("REALTIME").plusMinutes(20);
            status.put("nextRealtimeCollection", nextRealtime);
        }

        if (lastCollectionTime.containsKey("WATCHLIST")) {
            LocalDateTime nextWatchlist = lastCollectionTime.get("WATCHLIST").plusMinutes(20);
            status.put("nextWatchlistCollection", nextWatchlist);
        }

        return status;
    }

    /**
     * 역사적 챌린지 뉴스 수집 (날짜만 지정 - 기본 종목들)
     */
    public void collectHistoricalNews(String date) {
        log.info("=== Google RSS 역사적 챌린지 뉴스 수집 시작 (기본 종목): {} ===", date);
        
        // DB에서 주요 종목들 조회 (시가총액 상위 10개)
        List<StockInfo> majorStocks = stockInfoRepository.findTop10ByOrderByMarketCapDesc();
        
        for (StockInfo stock : majorStocks) {
            try {
                collectHistoricalNewsForStock(stock.getStockCode(), stock.getCompanyName(), 
                                            date, getAliasesForStock(stock));
                
                // API 부하 방지를 위한 지연
                Thread.sleep(2000);
            } catch (Exception e) {
                log.error("역사적 뉴스 수집 실패 - 종목: {}, 오류: {}", 
                         stock.getCompanyName(), e.getMessage());
            }
        }
        
        log.info("=== Google RSS 역사적 챌린지 뉴스 수집 완료: {} ===", date);
    }
    
    /**
     * 특정 종목의 역사적 뉴스 수집
     */
    public void collectHistoricalNewsForStock(String symbol, String companyName, 
                                              String date, List<String> aliases) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");
            
            Map<String, Object> requestBody = Map.of(
                "symbol", symbol,
                "company_name", companyName,
                "date", date,
                "aliases", aliases != null ? aliases : new ArrayList<>(),
                "limit", 10
            );
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            
            ResponseEntity<Map> response = restTemplate.exchange(
                finbertUrl + "/collect/historical",
                HttpMethod.POST,
                entity,
                Map.class
            );
            
            if (response.getStatusCode().is2xxSuccessful()) {
                processNewsResponse(response.getBody(), "HISTORICAL", symbol);
                log.info("역사적 뉴스 수집 성공: {} ({}) - {}", companyName, symbol, date);
            }
            
        } catch (Exception e) {
            log.error("역사적 뉴스 수집 실패: {} - {}", companyName, e.getMessage());
        }
    }
    
    /**
     * 오래된 뉴스 정리 (30일 이상)
     * 스케줄링은 NewsScheduler에서 관리
     */
    @Transactional
    public int cleanupOldNews() {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(30);
        List<News> oldNews = newsRepository.findByCreatedAtBefore(cutoffDate);

        if (!oldNews.isEmpty()) {
            newsRepository.deleteAll(oldNews);
            log.info("{}개의 오래된 뉴스를 정리했습니다", oldNews.size());
            return oldNews.size();
        }

        return 0;
    }

    /**
     * FinBERT 데이터를 News 엔티티로 변환
     */
    private News convertToNewsEntity(Map<String, Object> newsData) {
        // NOT NULL 필드들에 대한 기본값 보정
        String title = getStringValue(newsData, "title");
        if (title == null || title.trim().isEmpty()) {
            title = "제목 없음";
        }

        String contentText = getStringValue(newsData, "content_text");
        String summaryText = getStringValue(newsData, "summary_text");

        String content = null;
        if (contentText != null && !contentText.trim().isEmpty()) {
            content = contentText;
        } else if (summaryText != null && !summaryText.trim().isEmpty()) {
            content = summaryText;
        } else {
            content = getStringValue(newsData, "content");
            if (content == null || content.trim().isEmpty()) {
                content = "내용 없음";
            }
        }

        // URL 처리 - 실제 URL 사용 (RSS URL이 아닌)
        String url = getStringValue(newsData, "url");
        String originalUrl = getStringValue(newsData, "original_url");
        
        // 디버깅용 로그 추가
        if (url != null && originalUrl != null) {
            log.debug("URL 매핑 - 실제 URL: {}, RSS URL: {}", url, originalUrl);
        }
        
        if (url == null || url.trim().isEmpty()) {
            // url이 없으면 original_url이라도 사용 (fallback)
            url = originalUrl != null ? originalUrl : "";
            log.warn("실제 URL이 없어 RSS URL 사용: {}", url);
        }

        // 길이 제한 적용
        if (title.length() > 500) {
            title = title.substring(0, 497) + "...";
        }

        if (url.length() > 1000) {
            url = url.substring(0, 1000);
        }

        String mainImageUrl = getStringValue(newsData, "main_image_url");

        News news = News.builder()
                .title(title)
                .content(content)
                .summary(getStringArrayAsString(newsData, "summary_lines"))
                .url(url)
                .source(getStringValue(newsData, "source"))
                .publishedDate(parsePublishedDate(newsData))
                .stockCode(getStringValue(newsData, "symbol"))
                .sentiment(getStringValue(newsData, "label"))
                .confidenceLevel(getStringValue(newsData, "confidence_level"))
                .tradingSignal(getStringValue(newsData, "trading_signal"))
                .images(getStringArray(newsData, "images"))
                .collectionType(getStringValue(newsData, "collection_type"))
                .build();

        return news;
    }

    /**
     * Map에서 String 값 안전하게 추출
     */
    private String getStringValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? value.toString() : null;
    }

    /**
     * Map에서 String 배열을 하나의 문자열로 변환
     */
    private String getStringArrayAsString(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value instanceof List) {
            @SuppressWarnings("unchecked")
            List<String> list = (List<String>) value;
            return String.join(" ", list);
        }
        return value != null ? value.toString() : null;
    }

    /**
     * Map에서 String 배열을 List<String>으로 변환
     */
    private List<String> getStringArray(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value instanceof List) {
            @SuppressWarnings("unchecked")
            List<String> list = (List<String>) value;
            return list;
        }
        return null;
    }

    /**
     * 다양한 날짜 형식을 파싱하여 LocalDateTime으로 변환
     */
    private LocalDateTime parsePublishedDate(Map<String, Object> newsData) {
        String dateStr = getStringValue(newsData, "published_at");
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return LocalDateTime.now();
        }

        try {
            return java.time.OffsetDateTime.parse(dateStr).toLocalDateTime();
        } catch (Exception e1) {
            try {
                return java.time.ZonedDateTime.parse(dateStr).toLocalDateTime();
            } catch (Exception e2) {
                try {
                    return LocalDateTime.parse(dateStr);
                } catch (Exception e3) {
                    try {
                        return java.time.Instant.parse(dateStr)
                                .atZone(java.time.ZoneId.systemDefault())
                                .toLocalDateTime();
                    } catch (Exception e4) {
                        log.warn("published_at 파싱 실패, 현재 시간으로 대체 - 입력값: {}", dateStr);
                        return LocalDateTime.now();
                    }
                }
            }
        }
    }
}