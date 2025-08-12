package com.example.LAGO.repository;

import com.example.LAGO.domain.News;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface NewsRepository extends JpaRepository<News, Long> {
    
    // 일반 투자 뉴스 (종목 특정되지 않은 일반 뉴스) - PostgreSQL 최적화
    @Query("SELECT n FROM News n WHERE n.stockCode IS NULL " +
           "AND (LOWER(n.keywords) LIKE LOWER(:keyword1) OR LOWER(n.keywords) LIKE LOWER(:keyword2) OR " +
           "     LOWER(n.keywords) LIKE LOWER(:keyword3) OR LOWER(n.keywords) LIKE LOWER(:keyword4) OR " +
           "     LOWER(n.keywords) LIKE LOWER(:keyword5) OR LOWER(n.keywords) LIKE LOWER(:keyword6)) " +
           "ORDER BY n.publishedDate DESC")
    Page<News> findGeneralInvestmentNews(
        @Param("keyword1") String keyword1,
        @Param("keyword2") String keyword2, 
        @Param("keyword3") String keyword3,
        @Param("keyword4") String keyword4,
        @Param("keyword5") String keyword5,
        @Param("keyword6") String keyword6,
        Pageable pageable
    );
    
    // 관심종목별 뉴스
    @Query("SELECT n FROM News n WHERE n.stockCode IN :stockCodes " +
           "ORDER BY n.publishedDate DESC")
    Page<News> findByStockCodesOrderByPublishedDateDesc(
        @Param("stockCodes") List<String> stockCodes, 
        Pageable pageable
    );
    
    // 특정 종목 뉴스
    Page<News> findByStockCodeOrderByPublishedDateDesc(String stockCode, Pageable pageable);
    
    // 최신 뉴스 (전체)
    Page<News> findAllByOrderByPublishedDateDesc(Pageable pageable);
    
    // 중복 뉴스 체크용 (URL 기준)
    boolean existsByUrl(String url);
    
    // URL로 뉴스 조회 (업서트용)
    Optional<News> findByUrl(String url);
    
    // 특정 기간 뉴스
    @Query("SELECT n FROM News n WHERE n.publishedDate BETWEEN :startDate AND :endDate " +
           "ORDER BY n.publishedDate DESC")
    List<News> findByPublishedDateBetween(
        @Param("startDate") LocalDateTime startDate, 
        @Param("endDate") LocalDateTime endDate
    );
    
    // 키워드 검색 - PostgreSQL ILIKE 사용 (대소문자 무시)
    @Query("SELECT n FROM News n WHERE " +
           "(LOWER(n.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           " LOWER(n.content) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           " LOWER(n.keywords) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "ORDER BY n.publishedDate DESC")
    Page<News> findByKeywordSearch(@Param("keyword") String keyword, Pageable pageable);
    
    // 생성일 기준 최신순 조회 (Google RSS용)
    Page<News> findAllByOrderByCreatedAtDesc(Pageable pageable);
    
    // 오래된 뉴스 조회 (정리용)
    List<News> findByCreatedAtBefore(LocalDateTime cutoffDate);
    
    // 종목별 뉴스 조회
    List<News> findByStockCode(String stockCode);
    
    // 종목 코드 리스트로 뉴스 조회 (관심종목)
    Page<News> findByStockCodeInOrderByPublishedDateDesc(List<String> stockCodes, Pageable pageable);
    
    // PostgreSQL 전용: 감정 분석 통계 (전체)
    @Query(value = "SELECT sentiment, COUNT(*) as count FROM \"NEWS\" " +
                   "WHERE sentiment IS NOT NULL " +
                   "GROUP BY sentiment", nativeQuery = true)
    List<Object[]> getSentimentStatistics();
    
    // PostgreSQL 전용: 종목별 감정 분석 통계  
    @Query(value = "SELECT sentiment, COUNT(*) as count FROM \"NEWS\" " +
                   "WHERE stock_code = :stockCode AND sentiment IS NOT NULL " +
                   "GROUP BY sentiment", nativeQuery = true)
    List<Object[]> getSentimentStatisticsByStockCode(@Param("stockCode") String stockCode);
    
    // PostgreSQL 전용: 최근 7일 뉴스 수 집계
    @Query(value = "SELECT DATE(created_at) as date, COUNT(*) as count FROM \"NEWS\" " +
                   "WHERE created_at >= NOW() - INTERVAL '7 days' " +
                   "GROUP BY DATE(created_at) " +
                   "ORDER BY date DESC", nativeQuery = true)
    List<Object[]> getRecentNewsCountByDate();
}