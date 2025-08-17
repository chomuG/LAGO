package com.example.LAGO.service;

import com.example.LAGO.dto.response.RankingResponse;
import com.example.LAGO.repository.AccountRepository;
import com.example.LAGO.repository.MockTradeRepository;
import com.example.LAGO.repository.StockInfoRepository;
import com.example.LAGO.repository.Ticks1dRepository;
import com.example.LAGO.realtime.RealtimeDataService;
import org.springframework.data.domain.PageRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 랭킹 서비스
 * 총자산 기준 사용자 랭킹 조회
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RankingService {

    private final DataSource dataSource;
    private final AccountRepository accountRepository;
    private final MockTradeRepository mockTradeRepository;
    private final StockInfoRepository stockInfoRepository;
    private final Ticks1dRepository ticks1dRepository;
    private final RealtimeDataService realtimeDataService;

    /**
     * 총자산 기준 사용자 랭킹 조회 (실시간 계산)
     * 타입 0(모의투자) 계좌만 대상으로 함
     * 
     * @param limit 조회할 랭킹 수 (기본값: 100)
     * @return 랭킹 목록
     */
    public List<RankingResponse> getTotalAssetRanking(Integer limit) {
        log.info("총자산 기준 랭킹 조회 (실시간 계산): limit={}", limit);
        
        if (limit == null || limit <= 0) {
            limit = 100; // 기본값
        }
        
        List<RankingResponse> rankings = new ArrayList<>();
        
        // 1. 유저 단위로 집계된 계좌 정보 조회 (중복 방지)
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
            ORDER BY u.user_id ASC
            """;
        
        try (Connection connection = dataSource.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            
            List<UserAssetInfo> userAssets = new ArrayList<>();
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Long userId = rs.getLong("user_id");
                    String nickname = rs.getString("nickname");
                    String personality = rs.getString("personality");
                    boolean isAi = rs.getBoolean("is_ai");
                    Integer balance = rs.getInt("total_balance");
                    
                    // 2. 유저별 보유 주식 평가금액 실시간 계산 (해당 타입의 모든 계좌 합산)
                    Integer stockValue = 0;
                    try {
                        stockValue = calculateRealtimeStockValueByUserAndType(userId, isAi);
                    } catch (Exception e) {
                        log.warn("사용자 {} 주식 평가금액 계산 실패, 0으로 설정: {}", userId, e.getMessage());
                        stockValue = 0;
                    }
                    Integer totalAsset = balance + stockValue;
                    
                    log.info("💰 계산! 사용자 {} ({}) 총자산 = 현금 {}원 + 주식평가 {}원 = {}원", 
                             nickname, userId, balance, stockValue, totalAsset);
                    
                    userAssets.add(new UserAssetInfo(
                        userId, nickname, personality, isAi, balance, stockValue, totalAsset
                    ));
                }
            }
            
            // 3. 총자산 기준 정렬 및 랭킹 부여
            userAssets.sort((a, b) -> {
                int assetCompare = Integer.compare(b.totalAsset, a.totalAsset);
                return assetCompare != 0 ? assetCompare : Long.compare(a.userId, b.userId);
            });
            
            // 4. 상위 limit개만 응답 생성
            for (int i = 0; i < Math.min(userAssets.size(), limit); i++) {
                UserAssetInfo userAsset = userAssets.get(i);
                
                RankingResponse ranking = RankingResponse.builder()
                        .rank(i + 1)
                        .userId(userAsset.userId)
                        .username(userAsset.nickname)
                        .personality(userAsset.personality)
                        .isAi(userAsset.isAi)
                        .totalAsset(userAsset.totalAsset)
                        .profitRate(0.0) // 실시간 계산이므로 0으로 설정
                        .totalProfit(0)  // 실시간 계산이므로 0으로 설정
                        .build();
                
                rankings.add(ranking);
            }
            
        } catch (Exception e) {
            log.error("실시간 랭킹 조회 중 오류 발생", e);
            throw new RuntimeException("실시간 랭킹 조회에 실패했습니다: " + e.getMessage());
        }
        
        log.info("실시간 랭킹 조회 완료: {} 건", rankings.size());
        return rankings;
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
                            stockValue = calculateRealtimeStockValueByUserAndType(userId, isAi);
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
     * 유저+계좌타입 기준으로 보유 주식 실시간 평가금액 계산 (유저의 해당타입 모든 계좌 합산)
     * 
     * @param userId 사용자 ID
     * @param isAi AI 여부
     * @return 총 주식 평가금액
     */
    private Integer calculateRealtimeStockValueByUserAndType(Long userId, boolean isAi) {
        try {
            int accountType = isAi ? 2 : 0;
            log.info("🧮 계산! 사용자 {} (AI: {}) 계좌타입 {} 보유주식 평가금액 계산 시작", userId, isAi, accountType);
            
            List<Object[]> holdings = mockTradeRepository.findCurrentHoldingsByUserIdAndType(userId, accountType);
            
            if (holdings.isEmpty()) {
                log.info("🧮 계산! 사용자 {} 보유주식 없음, 평가금액 0원", userId);
                return 0;
            }
            
            Integer totalStockValue = 0;
            log.info("🧮 계산! 사용자 {} 보유종목 {}개 발견", userId, holdings.size());
            
            for (Object[] holding : holdings) {
                Integer stockId = (Integer) holding[0];
                Long currentQuantity = ((Number) holding[1]).longValue();
                
                if (currentQuantity <= 0) {
                    log.warn("🧮 계산! 종목 ID {} 보유량이 0 이하: {}", stockId, currentQuantity);
                    continue;
                }
                
                // 종목 코드 조회
                String stockCode = getStockCodeById(stockId);
                if (stockCode == null) {
                    log.warn("🧮 계산! 종목 ID {}에 대한 코드를 찾을 수 없음", stockId);
                    continue;
                }
                
                // Redis 실시간 가격 우선 조회
                Integer currentPrice = realtimeDataService.getLatestPrice(stockCode);
                String priceSource = "Redis";
                
                // Redis에 없으면 ticks_1d에서 최근 종가 조회 (fallback)
                if (currentPrice == null || currentPrice <= 0) {
                    currentPrice = ticks1dRepository.findLatestByCode(stockCode, PageRequest.of(0, 1))
                            .stream()
                            .findFirst()
                            .map(ticks1d -> ticks1d.getClosePrice())
                            .orElse(0);
                    priceSource = "ticks_1d";
                }
                
                if (currentPrice > 0) {
                    // 오버플로우 방지를 위해 long으로 계산 후 Integer로 변환
                    long stockValue = ((long) currentPrice) * currentQuantity;
                    
                    log.info("🧮 계산! 종목 {} (ID: {}) = {}원({}) × {}주 = {}원", 
                             stockCode, stockId, currentPrice, priceSource, currentQuantity, stockValue);
                    
                    if (stockValue > Integer.MAX_VALUE) {
                        log.warn("🧮 계산! 주식 평가금액이 Integer 범위를 초과함: userId={}, stockId={}, value={}", userId, stockId, stockValue);
                        stockValue = Integer.MAX_VALUE;
                    }
                    totalStockValue += (int) stockValue;
                    
                    log.info("🧮 계산! 누적 평가금액: {}원", totalStockValue);
                } else {
                    log.warn("🧮 계산! 종목 {} 가격을 찾을 수 없음 (Redis: {}, ticks_1d: {})", stockCode, 
                             realtimeDataService.getLatestPrice(stockCode), currentPrice);
                }
            }
            
            log.info("🧮 계산! 사용자 {} 최종 주식 평가금액: {}원", userId, totalStockValue);
            return totalStockValue;
            
        } catch (Exception e) {
            log.error("🧮 계산! 사용자 {} 주식 평가금액 계산 실패: {}", userId, e.getMessage(), e);
            return 0;
        }
    }

    /**
     * 특정 계좌의 보유 주식 실시간 평가금액 계산 (최적화 버전)
     * 사용자 타입에 따라 올바른 계좌 타입에서 보유주식 조회
     * 
     * @param accountId 계좌 ID
     * @param userId 사용자 ID
     * @param isAi AI 여부
     * @return 총 주식 평가금액
     */
    private Integer calculateRealtimeStockValueOptimized(Long accountId, Long userId, boolean isAi) {
        try {
            List<Object[]> holdings;
            
            if (isAi && (userId == 1L || userId == 2L || userId == 3L || userId == 4L)) {
                // AI 봇들은 타입 2 계좌에서 조회
                holdings = mockTradeRepository.findCurrentHoldingsByAccountIdAndType(accountId, 2);
            } else {
                // 일반 사용자는 타입 0 계좌에서 조회
                holdings = mockTradeRepository.findCurrentHoldingsByAccountIdAndType(accountId, 0);
            }
            
            if (holdings.isEmpty()) {
                return 0;
            }
            
            Integer totalStockValue = 0;
            
            for (Object[] holding : holdings) {
                Integer stockId = (Integer) holding[0];
                Long currentQuantity = (Long) holding[1];
                
                if (currentQuantity <= 0) continue;
                
                // 종목 코드 조회
                String stockCode = getStockCodeById(stockId);
                if (stockCode == null) continue;
                
                // Redis 실시간 가격 우선 조회
                Integer currentPrice = realtimeDataService.getLatestPrice(stockCode);
                
                // Redis에 없으면 ticks_1d에서 최근 종가 조회 (fallback)
                if (currentPrice == null || currentPrice <= 0) {
                    currentPrice = ticks1dRepository.findLatestByCode(stockCode, PageRequest.of(0, 1))
                            .stream()
                            .findFirst()
                            .map(ticks1d -> ticks1d.getClosePrice())
                            .orElse(0);
                }
                
                if (currentPrice > 0) {
                    Integer stockValue = currentPrice * currentQuantity.intValue();
                    totalStockValue += stockValue;
                }
            }
            
            return totalStockValue;
            
        } catch (Exception e) {
            log.warn("계좌 {} 주식 평가금액 최적화 계산 실패: {}", accountId, e.getMessage());
            return 0;
        }
    }

    /**
     * 특정 계좌의 보유 주식 실시간 평가금액 계산 (기존 버전)
     * Redis 실시간 가격 → DB 종가 → 거래 시점 가격 순으로 폴백
     * 
     * @param accountId 계좌 ID
     * @return 총 주식 평가금액
     */
    private Integer calculateRealtimeStockValue(Long accountId) {
        try {
            // MockTrade에서 현재 보유 종목 조회
            List<Object[]> holdings = mockTradeRepository.findCurrentHoldingsByAccountId(accountId);
            
            Integer totalStockValue = 0;
            
            log.debug("📊 계좌 {} 보유 종목 수: {}", accountId, holdings.size());
            
            for (Object[] holding : holdings) {
                Integer stockId = (Integer) holding[0];
                Long currentQuantity = (Long) holding[1]; // HAVING 조건으로 0보다 큰 것만 조회됨
                
                if (currentQuantity <= 0) continue;
                
                // 종목 정보 조회
                String stockCode = getStockCodeById(stockId);
                if (stockCode == null) {
                    log.warn("종목 ID {}에 대한 코드를 찾을 수 없음", stockId);
                    continue;
                }
                
                // 현재가 조회 (Redis → DB → 거래가격 순으로 폴백)
                Integer currentPrice = getCurrentPrice(stockCode, stockId);
                if (currentPrice == null || currentPrice <= 0) {
                    log.warn("종목 {} (ID: {})의 현재가를 찾을 수 없음", stockCode, stockId);
                    continue;
                }
                
                // 평가금액 계산
                Integer stockValue = currentPrice * currentQuantity.intValue();
                totalStockValue += stockValue;
                
                log.debug("📈 종목: {} (ID: {}), 보유량: {}, 현재가: {}, 평가금액: {}", 
                         stockCode, stockId, currentQuantity, currentPrice, stockValue);
            }
            
            return totalStockValue;
            
        } catch (Exception e) {
            log.warn("계좌 {} 주식 평가금액 계산 실패: {}", accountId, e.getMessage());
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
     * 사용자 자산 정보를 담는 내부 클래스
     */
    private static class UserAssetInfo {
        final Long userId;
        final String nickname;
        final String personality;
        final boolean isAi;
        final Integer balance;
        final Integer stockValue;
        final Integer totalAsset;

        UserAssetInfo(Long userId, String nickname, String personality, boolean isAi, 
                     Integer balance, Integer stockValue, Integer totalAsset) {
            this.userId = userId;
            this.nickname = nickname;
            this.personality = personality;
            this.isAi = isAi;
            this.balance = balance;
            this.stockValue = stockValue;
            this.totalAsset = totalAsset;
        }
    }
}