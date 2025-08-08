package com.example.LAGO.controller;

import com.example.LAGO.dto.response.NewsResponse;
import com.example.LAGO.service.NewsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/news")
@RequiredArgsConstructor
@Tag(name = "뉴스", description = "투자 뉴스 조회 API")
public class NewsController {
    
    private final NewsService newsService;
    
    @GetMapping("/general")
    @Operation(
        summary = "일반 투자 뉴스 조회", 
        description = "주식, 투자, 증권 등 일반적인 투자 관련 뉴스를 최신순으로 조회합니다."
    )
    public ResponseEntity<Page<NewsResponse>> getGeneralNews(
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            
            @Parameter(description = "페이지 크기", example = "20") 
            @RequestParam(defaultValue = "20") int size
    ) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<NewsResponse> newsPage = newsService.getGeneralInvestmentNews(pageable)
                    .map(NewsResponse::from);
            
            log.info("일반 투자 뉴스 조회 - 페이지: {}, 크기: {}, 총 {}개", page, size, newsPage.getTotalElements());
            
            return ResponseEntity.ok(newsPage);
            
        } catch (Exception e) {
            log.error("일반 투자 뉴스 조회 실패: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/stocks")
    @Operation(
        summary = "관심종목 뉴스 조회",
        description = "사용자가 지정한 관심종목들의 뉴스를 최신순으로 조회합니다."
    )
    public ResponseEntity<Page<NewsResponse>> getStockNews(
            @Parameter(description = "종목 코드 목록 (쉼표 구분)", example = "005930,000660,035420")
            @RequestParam String stockCodes,
            
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            
            @Parameter(description = "페이지 크기", example = "20")
            @RequestParam(defaultValue = "20") int size
    ) {
        try {
            List<String> codeList = Arrays.asList(stockCodes.split(","));
            Pageable pageable = PageRequest.of(page, size);
            
            Page<NewsResponse> newsPage = newsService.getStockNews(codeList, pageable)
                    .map(NewsResponse::from);
            
            log.info("관심종목 뉴스 조회 - 종목: {}, 페이지: {}, 크기: {}, 총 {}개", 
                    stockCodes, page, size, newsPage.getTotalElements());
            
            return ResponseEntity.ok(newsPage);
            
        } catch (Exception e) {
            log.error("관심종목 뉴스 조회 실패: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/search")
    @Operation(
        summary = "뉴스 검색",
        description = "제목, 내용, 키워드에서 특정 단어를 검색하여 관련 뉴스를 조회합니다."
    )
    public ResponseEntity<Page<NewsResponse>> searchNews(
            @Parameter(description = "검색 키워드", example = "삼성전자")
            @RequestParam String keyword,
            
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            
            @Parameter(description = "페이지 크기", example = "20")
            @RequestParam(defaultValue = "20") int size
    ) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<NewsResponse> newsPage = newsService.searchNews(keyword, pageable)
                    .map(NewsResponse::from);
            
            log.info("뉴스 검색 - 키워드: {}, 페이지: {}, 크기: {}, 총 {}개", 
                    keyword, page, size, newsPage.getTotalElements());
            
            return ResponseEntity.ok(newsPage);
            
        } catch (Exception e) {
            log.error("뉴스 검색 실패: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @PostMapping("/collect/manual")
    @Operation(
        summary = "수동 뉴스 수집",
        description = "관리자용 - 뉴스를 수동으로 수집합니다. (개발/테스트 용도)"
    )
    public ResponseEntity<String> collectNewsManually() {
        try {
            log.info("===== 수동 뉴스 수집 시작 =====");
            
            // 일반 투자 뉴스 수집
            log.info("일반 투자 뉴스 수집 호출...");
            newsService.collectAndSaveGeneralNews();
            
            // 관심종목 뉴스 수집  
            newsService.collectAndSaveStockNews();
            
            log.info("수동 뉴스 수집 완료");
            
            return ResponseEntity.ok("뉴스 수집이 완료되었습니다.");
            
        } catch (Exception e) {
            log.error("수동 뉴스 수집 실패: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                    .body("뉴스 수집 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
}