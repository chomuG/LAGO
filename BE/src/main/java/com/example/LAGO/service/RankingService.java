package com.example.LAGO.service;

import com.example.LAGO.domain.StockHolding;
import com.example.LAGO.dto.response.RankingResponse;
import com.example.LAGO.repository.*;
import com.example.LAGO.realtime.RealtimeDataService;
import org.springframework.data.domain.PageRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.redis.core.RedisTemplate;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * 랭킹 서비스
 * 총자산 기준 사용자 랭킹 조회
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RankingService {

    private final DataSource dataSource;
    private final StockHoldingRepository stockHoldingRepository;
    private final StockInfoRepository stockInfoRepository;
    private final Ticks1dRepository ticks1dRepository;
    private final RealtimeDataService realtimeDataService;
    private final RedisTemplate<String, Object> redisTemplate;
    
    // Java 21 Virtual Threads Executor
    private final Executor virtualThreadExecutor = Executors.newVirtualThreadPerTaskExecutor();
    
    private static final String RANKING_CACHE_KEY = "ranking:total_asset";
    private static final Duration CACHE_DURATION = Duration.ofMinutes(1);

    /**
     * 총자산 기준 사용자 랭킹 조회 (실시간 계산)
     * 타입 0(모의투자) 계좌만 대상으로 함
     * 
     * @param limit 조회할 랭킹 수 (기본값: 100)
     * @return 랭킹 목록
     */
    @SuppressWarnings("unchecked")
    public List<RankingResponse> getTotalAssetRanking(Integer limit) {
        if (limit == null || limit <= 0) {
            limit = 100;
        }
        
        // 1. 캐시에서 조회 시도
        String cacheKey = RANKING_CACHE_KEY + ":" + limit;
        try {
            List<RankingResponse> cachedRanking = (List<RankingResponse>) redisTemplate.opsForValue().get(cacheKey);
            if (cachedRanking != null && !cachedRanking.isEmpty()) {
                log.info("⚡ 캐시에서 랭킹 조회 완료: {} 건 (즉시 반환)", cachedRanking.size());
                return cachedRanking;
            }
        } catch (Exception e) {
            log.warn("캐시 조회 실패, 실시간 계산으로 진행: {}", e.getMessage());
        }
        
        // 2. 캐시 미스 - 실시간 계산
        log.info("🚀 실시간 랭킹 계산 시작: limit={}", limit);
        List<RankingResponse> rankings = calculateRankingRealtime(limit);
        
        // 3. 계산 결과를 캐시에 저장
        try {
            redisTemplate.opsForValue().set(cacheKey, rankings, CACHE_DURATION);
            log.info("💾 랭킹 결과 캐시 저장 완료 (TTL: {}분)", CACHE_DURATION.toMinutes());
        } catch (Exception e) {
            log.warn("캐시 저장 실패: {}", e.getMessage());
        }
        
        return rankings;
    }
    
    private List<RankingResponse> calculateRankingRealtime(Integer limit) {
        List<UserInfo> users = getUsersWithBalance();
        log.info("📊 총 {} 명의 사용자 조회 완료", users.size());
        
        // Virtual Threads로 병렬 처리
        List<CompletableFuture<UserAssetInfo>> futures = users.stream()
                .map(user -> CompletableFuture.supplyAsync(() -> 
                    calculateUserTotalAsset(user), virtualThreadExecutor))
                .toList();
        
        // 모든 비동기 작업 완료 대기
        List<UserAssetInfo> userAssets = futures.stream()
                .map(CompletableFuture::join)
                .filter(asset -> asset != null)
                .sorted((a, b) -> {
                    int assetCompare = Integer.compare(b.totalAsset, a.totalAsset);
                    return assetCompare != 0 ? assetCompare : Long.compare(a.userId, b.userId);
                })
                .toList();
        
        // 랭킹 응답 생성 (순위 부여)
        List<RankingResponse> rankings = new ArrayList<>();
        for (int i = 0; i < Math.min(userAssets.size(), limit); i++) {
            UserAssetInfo userAsset = userAssets.get(i);
            RankingResponse ranking = RankingResponse.builder()
                    .rank(i + 1)
                    .userId(userAsset.userId)
                    .username(userAsset.nickname)
                    .personality(userAsset.personality)
                    .isAi(userAsset.isAi)
                    .totalAsset(userAsset.totalAsset)
                    .profitRate(0.0)
                    .totalProfit(0)
                    .build();
            rankings.add(ranking);
        }
        
        log.info("🏆 실시간 랭킹 계산 완료: {} 건", rankings.size());
        return rankings;
    }
    
    private List<UserInfo> getUsersWithBalance() {
        String sql = """
            SELECT 
                u.user_id,
                u.nickname,
                u.personality,
                u.is_ai,
                SUM(a.balance) AS total_balance
            FROM accounts a
            INNER JOIN users u ON a.user_id = u.user_id
            WHERE (
                (u.is_ai = false AND a.type = 0) OR 
                (u.is_ai = true AND a.type = 2)
            )
            AND u.deleted_at IS NULL
            GROUP BY u.user_id, u.nickname, u.personality, u.is_ai
            """;
        
        List<UserInfo> users = new ArrayList<>();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                users.add(new UserInfo(
                    rs.getLong("user_id"),
                    rs.getString("nickname"),
                    rs.getString("personality"),
                    rs.getBoolean("is_ai"),
                    rs.getInt("total_balance")
                ));
            }
        } catch (Exception e) {
            log.error("사용자 목록 조회 실패", e);
            throw new RuntimeException("사용자 목록 조회에 실패했습니다: " + e.getMessage());
        }
        return users;
    }
    
    private UserAssetInfo calculateUserTotalAsset(UserInfo user) {
        try {
            Integer stockValue = calculateStockValueFast(user.userId, user.isAi);
            Integer totalAsset = user.balance + stockValue;
            
            log.debug("💰 계산! 사용자 {} ({}) 총자산 = 현금 {}원 + 주식평가 {}원 = {}원", 
                     user.nickname, user.userId, user.balance, stockValue, totalAsset);
            
            return new UserAssetInfo(
                user.userId, user.nickname, user.personality, 
                user.isAi, user.balance, stockValue, totalAsset
            );
        } catch (Exception e) {
            log.warn("사용자 {} 자산 계산 실패: {}", user.userId, e.getMessage());
            return new UserAssetInfo(
                user.userId, user.nickname, user.personality, 
                user.isAi, user.balance, 0, user.balance
            );
        }
    }

    /**
     * 특정 사용자의 랭킹 조회 (실시간 계산)
     * 
     * @param userId 사용자 ID
     * @return 해당 사용자의 랭킹 정보
     */
    public RankingResponse getUserRanking(Long userId) {
        log.info("사용자 랭킹 조회 (실시간 계산): userId={}", userId);
        
        // 1. 전체 랭킹 계산 (성능 최적화 필요시 별도 구현)
        List<RankingResponse> allRankings = getTotalAssetRanking(1000); // 최대 1000명
        
        // 2. 해당 사용자 찾기
        RankingResponse userRanking = allRankings.stream()
                .filter(ranking -> ranking.getUserId().equals(userId))
                .findFirst()
                .orElse(null);
        
        if (userRanking == null) {
            // 3. 랭킹 1000위 밖인 경우 개별 계산
            userRanking = calculateUserRankingIndividually(userId);
        }
        
        if (userRanking == null) {
            throw new RuntimeException("해당 사용자의 랭킹 정보를 찾을 수 없습니다: " + userId);
        }
        
        return userRanking;
    }

    /**
     * 특정 사용자의 랭킹을 개별적으로 계산
     */
    private RankingResponse calculateUserRankingIndividually(Long userId) {
        try {
            // 1. 해당 사용자의 계좌 정보 조회 (유저 단위로 합산)
            String userSql = """
                SELECT 
                    u.user_id,
                    u.nickname,
                    u.personality,
                    u.is_ai,
                    SUM(a.balance) AS total_balance
                FROM accounts a
                INNER JOIN users u ON a.user_id = u.user_id
                WHERE (
                    (u.is_ai = false AND a.type = 0) OR 
                    (u.is_ai = true AND a.type = 2)
                ) 
                AND a.user_id = ? AND u.deleted_at IS NULL
                GROUP BY u.user_id, u.nickname, u.personality, u.is_ai
                """;
            
            UserAssetInfo userAsset = null;
            
            try (Connection connection = dataSource.getConnection();
                 PreparedStatement stmt = connection.prepareStatement(userSql)) {
                
                stmt.setLong(1, userId);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        Integer balance = rs.getInt("total_balance");
                        String nickname = rs.getString("nickname");
                        String personality = rs.getString("personality");
                        boolean isAi = rs.getBoolean("is_ai");
                        
                        Integer stockValue = 0;
                        try {
//                            stockValue = calculateRealtimeStockValueByUserAndType(userId, isAi);
                        } catch (Exception e) {
                            log.warn("사용자 {} 주식 평가금액 계산 실패, 0으로 설정: {}", userId, e.getMessage());
                            stockValue = 0;
                        }
                        Integer totalAsset = balance + stockValue;
                        
                        log.info("💰 계산! 개별랭킹 사용자 {} ({}) 총자산 = 현금 {}원 + 주식평가 {}원 = {}원", 
                                 nickname, userId, balance, stockValue, totalAsset);
                        
                        userAsset = new UserAssetInfo(
                            userId, nickname, personality, isAi, balance, stockValue, totalAsset
                        );
                    }
                }
            }
            
            if (userAsset == null) {
                return null;
            }
            
            // 2. 해당 사용자보다 자산이 많은 사용자 수 계산하여 랭킹 결정
            String rankSql = """
                SELECT COUNT(*) + 1 as user_rank
                FROM accounts a
                INNER JOIN users u ON a.user_id = u.user_id
                WHERE (
                    (u.is_ai = false AND a.type = 0) OR 
                    (u.is_ai = true AND a.type = 2)
                ) 
                AND u.deleted_at IS NULL
                AND a.user_id != ?
                """;
            
            // 실제로는 다른 모든 사용자의 실시간 자산을 계산해야 하지만 
            // 성능상 이슈가 있으므로 기존 total_asset 기반으로 대략적인 랭킹 계산
            int approximateRank = 1;
            try (Connection connection = dataSource.getConnection();
                 PreparedStatement stmt = connection.prepareStatement(
                     "SELECT COUNT(*) + 1 as user_rank FROM accounts a " +
                     "INNER JOIN users u ON a.user_id = u.user_id " +
                     "WHERE ((u.is_ai = false AND a.type = 0) OR (u.is_ai = true AND a.type = 2)) " +
                     "AND u.deleted_at IS NULL AND a.total_asset > ?")) {
                
                stmt.setInt(1, userAsset.totalAsset);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        approximateRank = rs.getInt("user_rank");
                    }
                }
            }
            
            return RankingResponse.builder()
                    .rank(approximateRank)
                    .userId(userAsset.userId)
                    .username(userAsset.nickname)
                    .personality(userAsset.personality)
                    .isAi(userAsset.isAi)
                    .totalAsset(userAsset.totalAsset)
                    .profitRate(0.0) // 실시간 계산이므로 0으로 설정
                    .totalProfit(0)  // 실시간 계산이므로 0으로 설정
                    .build();
            
        } catch (Exception e) {
            log.warn("사용자 개별 랭킹 계산 실패: userId={}, error={}", userId, e.getMessage());
            return null;
        }
    }

    /**
     * 빠른 주식 평가금액 계산 (배치 최적화)
     */
    private Integer calculateStockValueFast(Long userId, boolean isAi) {
        int accountType = isAi ? 2 : 0;
        
        List<StockHolding> holdings = stockHoldingRepository.findByUserIdAndAccountType(userId, accountType);
        
        if (holdings.isEmpty()) {
            return 0;
        }
        
        // 한 번에 모든 종목 코드 조회 (배치 최적화)
        List<Integer> stockInfoIds = holdings.stream()
                .map(StockHolding::getStockInfoId)
                .toList();
        
        return stockInfoRepository.findAllById(stockInfoIds)
                .parallelStream()
                .mapToInt(stockInfo -> {
                    StockHolding holding = holdings.stream()
                            .filter(h -> h.getStockInfoId().equals(stockInfo.getStockInfoId()))
                            .findFirst()
                            .orElse(null);
                    
                    if (holding == null || holding.getQuantity() <= 0) {
                        return 0;
                    }
                    
                    // Redis 먼저, 없으면 ticks_1d
                    Integer price = realtimeDataService.getLatestPrice(stockInfo.getCode());
                    if (price == null || price <= 0) {
                        price = ticks1dRepository.findLatestByCode(stockInfo.getCode(), PageRequest.of(0, 1))
                                .stream()
                                .findFirst()
                                .map(ticks1d -> ticks1d.getClosePrice())
                                .orElse(0);
                    }
                    
                    if (price > 0) {
                        long value = ((long) price) * holding.getQuantity();
                        return value > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) value;
                    }
                    
                    return 0;
                })
                .sum();
    }
    
    private Integer calculateSingleStockValue(StockHolding holding) {
        try {
            String stockCode = getStockCodeById(holding.getStockInfoId());
            if (stockCode == null || holding.getQuantity() <= 0) {
                return 0;
            }
            
            // Redis 먼저, 없으면 ticks_1d
            Integer price = realtimeDataService.getLatestPrice(stockCode);
            if (price == null || price <= 0) {
                price = ticks1dRepository.findLatestByCode(stockCode, PageRequest.of(0, 1))
                        .stream()
                        .findFirst()
                        .map(ticks1d -> ticks1d.getClosePrice())
                        .orElse(0);
            }
            
            if (price > 0) {
                long value = ((long) price) * holding.getQuantity();
                return value > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) value;
            }
            
            return 0;
        } catch (Exception e) {
            log.warn("단일 종목 계산 실패: stockInfoId={}, error={}", holding.getStockInfoId(), e.getMessage());
            return 0;
        }
    }

    /**
     * 종목 ID로 종목 코드 조회
     */
    private String getStockCodeById(Integer stockId) {
        try {
            return stockInfoRepository.findById(stockId)
                    .map(stockInfo -> stockInfo.getCode())
                    .orElse(null);
        } catch (Exception e) {
            log.warn("종목 ID {} 코드 조회 실패: {}", stockId, e.getMessage());
            return null;
        }
    }

    /**
     * 현재가 조회 (Redis → ticks_1d 최근 종가 → 폴백 순)
     */
    private Integer getCurrentPrice(String stockCode, Integer stockId) {
        try {
            // 1. Redis 실시간 가격 조회 시도
            Integer realtimePrice = realtimeDataService.getLatestPrice(stockCode);
            if (realtimePrice != null && realtimePrice > 0) {
                log.debug("Redis에서 종목 {} 실시간 가격 조회: {}", stockCode, realtimePrice);
                return realtimePrice;
            }
            
            // 2. ticks_1d 테이블에서 최근 종가 조회 (폴백)
            Integer latestClosePrice = ticks1dRepository.findLatestByCode(stockCode, PageRequest.of(0, 1))
                    .stream()
                    .findFirst()
                    .map(ticks1d -> ticks1d.getClosePrice())
                    .orElse(null);
            
            if (latestClosePrice != null && latestClosePrice > 0) {
                log.debug("ticks_1d에서 종목 {} 최근 종가 조회: {}", stockCode, latestClosePrice);
                return latestClosePrice;
            }
            
            log.warn("종목 {}의 현재가를 찾을 수 없음 (Redis: {}, ticks_1d: {})", 
                    stockCode, realtimePrice, latestClosePrice);
            return null;
            
        } catch (Exception e) {
            log.warn("종목 {} 현재가 조회 실패: {}", stockCode, e.getMessage());
            return null;
        }
    }

    /**
     * 사용자 기본 정보
     */
    private record UserInfo(
        Long userId,
        String nickname, 
        String personality,
        boolean isAi,
        Integer balance
    ) {}
    
    /**
     * 사용자 자산 정보
     */
    private record UserAssetInfo(
        Long userId,
        String nickname,
        String personality,
        boolean isAi,
        Integer balance,
        Integer stockValue,
        Integer totalAsset
    ) {}
}