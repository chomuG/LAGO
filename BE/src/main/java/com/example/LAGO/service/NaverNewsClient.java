package com.example.LAGO.service;

import com.example.LAGO.dto.response.NaverNewsResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

@Slf4j
@Service
@RequiredArgsConstructor
public class NaverNewsClient {
    
    private final RestTemplate restTemplate;
    
    @Value("${naver.api.client-id:}")
    private String naverClientId;
    
    @Value("${naver.api.client-secret:}")
    private String naverClientSecret;
    
    private static final String NAVER_NEWS_API_URL = "https://openapi.naver.com/v1/search/news.json";
    
    public NaverNewsResponse searchNews(String query, int display, int start) {
        try {
            // URL 인코딩을 직접 처리
            String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8.toString());
            
            // URL 파라미터를 직접 구성
            String url = NAVER_NEWS_API_URL + 
                    "?query=" + encodedQuery +
                    "&display=" + display +
                    "&start=" + start +
                    "&sort=date";
            
            HttpHeaders headers = createHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            log.info("네이버 뉴스 API 호출 - URL: {}, 원본 검색어: {}", url, query);
            
            ResponseEntity<NaverNewsResponse> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, NaverNewsResponse.class);
            
            NaverNewsResponse result = response.getBody();
            if (result != null && result.getItems() != null) {
                log.info("네이버 뉴스 API 응답 - 총 {}개 뉴스 검색, {}개 반환", 
                        result.getTotal(), result.getItems().size());
            }
            
            return result;
            
        } catch (Exception e) {
            log.error("네이버 뉴스 API 호출 실패 - 검색어: {}, 오류: {}", query, e.getMessage());
            return new NaverNewsResponse(); // 빈 응답 반환
        }
    }
    
    public NaverNewsResponse searchGeneralInvestmentNews(int display, int start) {
        // 네이버 API는 최대 100개까지 한번에 가능
        int maxDisplay = 100; // 최대로 가져와서 필터링
        
        // 더 구체적인 금융 관련 키워드 시도
        String[] keywords = {
            "코스피 지수",  // 더 구체적인 키워드
            "코스닥 시장",
            "주식 거래",
            "증권사",
            "금리 인상",
            "환율",
            "삼성전자 주가",
            "경제 전망"
        };
        
        NaverNewsResponse allNews = new NaverNewsResponse();
        allNews.setItems(new ArrayList<>());
        
        for (String keyword : keywords) {
            NaverNewsResponse response = searchNews(keyword, 20, start); // 각 키워드로 20개씩
            if (response.getItems() != null && !response.getItems().isEmpty()) {
                log.info("키워드 '{}' 로 뉴스 검색 성공 - {}개 반환", keyword, response.getItems().size());
                allNews.getItems().addAll(response.getItems());
                
                // 충분히 모았으면 중단
                if (allNews.getItems().size() >= display) {
                    break;
                }
            }
        }
        
        if (allNews.getItems().isEmpty()) {
            log.warn("모든 키워드로 뉴스 검색 실패");
        }
        
        return allNews;
    }
    
    public NaverNewsResponse searchStockNews(String stockName, int display, int start) {
        // 종목명으로 직접 검색
        return searchNews(stockName, display, start);
    }
    
    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Naver-Client-Id", naverClientId);
        headers.set("X-Naver-Client-Secret", naverClientSecret);
        headers.set("Content-Type", "application/json");
        return headers;
    }
}