package com.example.LAGO.service;

import com.example.LAGO.dto.response.NaverNewsResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NaverFinanceNewsClient {
    
    // 네이버 증권 뉴스 URL
    private static final String NAVER_FINANCE_NEWS_URL = "https://finance.naver.com/news/news_list.naver";
    private static final String NAVER_STOCK_NEWS_URL = "https://finance.naver.com/item/news_news.naver?code=";
    
    /**
     * 네이버 증권 섹션에서 최신 뉴스 크롤링
     */
    public NaverNewsResponse crawlFinanceNews(int display) {
        NaverNewsResponse response = new NaverNewsResponse();
        List<NaverNewsResponse.NewsItem> items = new ArrayList<>();
        
        try {
            // 네이버 증권 뉴스 페이지 접속
            // 마켓 뉴스 페이지로 변경 (더 명확한 구조)
            String marketNewsUrl = "https://finance.naver.com/news/maket_news.naver";
            Document doc = Jsoup.connect(NAVER_FINANCE_NEWS_URL)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .timeout(10000)
                    .get();
            
            // 뉴스 목록 선택자 - 네이버 증권 페이지 구조에 맞게
            Elements newsElements = doc.select("div.articleSubject > a, dd.articleSubject > a");
            
            int count = 0;
            for (Element element : newsElements) {
                if (count >= display) break;
                
                NaverNewsResponse.NewsItem item = new NaverNewsResponse.NewsItem();
                
                // 제목
                String title = element.text().trim();
                item.setTitle(title);
                
                // 링크 - 줄바꿈 제거
                String link = element.attr("href").trim().replaceAll("\\s+", "");
                if (!link.startsWith("http")) {
                    link = "https://finance.naver.com" + link;
                }
                item.setLink(link);
                
                // 설명 - 제목을 설명으로도 사용
                item.setDescription(title);
                
                // 날짜 - 현재 시간으로 설정 (실제 날짜는 상세 페이지에서 가져와야 함)
                item.setPubDate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss +0900")));
                
                items.add(item);
                count++;
                
                log.debug("크롤링한 뉴스: {}", title);
            }
            
            response.setItems(items);
            response.setTotal(items.size());
            response.setDisplay(items.size());
            
            log.info("네이버 증권에서 {}개 뉴스 크롤링 완료", items.size());
            
        } catch (IOException e) {
            log.error("네이버 증권 뉴스 크롤링 실패: {}", e.getMessage());
        }
        
        return response;
    }
    
    /**
     * 특정 종목의 뉴스 크롤링
     */
    public NaverNewsResponse crawlStockNews(String stockCode, int display) {
        NaverNewsResponse response = new NaverNewsResponse();
        List<NaverNewsResponse.NewsItem> items = new ArrayList<>();
        
        try {
            // 종목별 뉴스 페이지 접속
            String url = NAVER_STOCK_NEWS_URL + stockCode;
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .timeout(10000)
                    .get();
            
            // 종목 뉴스 목록 선택자
            Elements newsElements = doc.select("table.type5 tbody tr");
            
            int count = 0;
            for (Element tr : newsElements) {
                if (count >= display) break;
                
                // 제목과 링크를 가진 요소
                Element titleElement = tr.select("td.title a").first();
                if (titleElement == null) continue;
                
                NaverNewsResponse.NewsItem item = new NaverNewsResponse.NewsItem();
                
                // 제목
                String title = titleElement.text().trim();
                item.setTitle(title);
                
                // 링크 - 줄바꿈 제거
                String link = titleElement.attr("href").trim().replaceAll("\\s+", "");
                if (!link.startsWith("http")) {
                    link = "https://finance.naver.com" + link;
                }
                item.setLink(link);
                
                // 설명
                item.setDescription(title);
                
                // 날짜
                Element dateElement = tr.select("td.date").first();
                String dateStr = dateElement != null ? dateElement.text() : "";
                item.setPubDate(formatDate(dateStr));
                
                items.add(item);
                count++;
                
                log.debug("종목 {} 뉴스 크롤링: {}", stockCode, title);
            }
            
            response.setItems(items);
            response.setTotal(items.size());
            response.setDisplay(items.size());
            
            log.info("종목 {}에서 {}개 뉴스 크롤링 완료", stockCode, items.size());
            
        } catch (IOException e) {
            log.error("종목 {} 뉴스 크롤링 실패: {}", stockCode, e.getMessage());
        }
        
        return response;
    }
    
    /**
     * 날짜 문자열 포맷팅
     */
    private String formatDate(String dateStr) {
        try {
            // "2024.01.01 10:30" 형식을 API 형식으로 변환
            if (dateStr.contains(".")) {
                return LocalDateTime.now().format(DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss +0900"));
            }
            return dateStr;
        } catch (Exception e) {
            return LocalDateTime.now().format(DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss +0900"));
        }
    }
}