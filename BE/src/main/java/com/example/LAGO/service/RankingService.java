package com.example.LAGO.service;

import com.example.LAGO.dto.response.RankingResponse;
import com.example.LAGO.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

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

    /**
     * 총자산 기준 사용자 랭킹 조회
     * 타입 0(모의투자) 계좌만 대상으로 함
     * 
     * @param limit 조회할 랭킹 수 (기본값: 100)
     * @return 랭킹 목록
     */
    @Transactional(readOnly = true)
    public List<RankingResponse> getTotalAssetRanking(Integer limit) {
        log.info("총자산 기준 랭킹 조회: limit={}", limit);
        
        if (limit == null || limit <= 0) {
            limit = 100; // 기본값
        }
        
        List<RankingResponse> rankings = new ArrayList<>();
        
        String sql = """
            WITH ranked_accounts AS (
                SELECT 
                    a.user_id,
                    u.nickname,
                    a.total_asset,
                    a.profit_rate,
                    a.profit,
                    ROW_NUMBER() OVER (ORDER BY a.total_asset DESC, a.user_id ASC) as rank
                FROM accounts a
                INNER JOIN users u ON a.user_id = u.user_id
                WHERE a.type = 0
                AND u.deleted_at IS NULL
            )
            SELECT 
                rank,
                user_id,
                nickname,
                total_asset,
                profit_rate,
                profit
            FROM ranked_accounts
            WHERE rank <= ?
            ORDER BY rank
            """;
        
        try (Connection connection = dataSource.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            
            stmt.setInt(1, limit);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    RankingResponse ranking = RankingResponse.builder()
                            .rank(rs.getInt("rank"))
                            .userId(rs.getLong("user_id"))
                            .username(rs.getString("nickname"))
                            .totalAsset(rs.getInt("total_asset"))
                            .profitRate(rs.getDouble("profit_rate"))
                            .totalProfit(rs.getInt("profit"))
                            .build();
                    
                    rankings.add(ranking);
                }
            }
            
        } catch (Exception e) {
            log.error("랭킹 조회 중 오류 발생", e);
            throw new RuntimeException("랭킹 조회에 실패했습니다: " + e.getMessage());
        }
        
        log.info("랭킹 조회 완료: {} 건", rankings.size());
        return rankings;
    }

    /**
     * 특정 사용자의 랭킹 조회
     * 
     * @param userId 사용자 ID
     * @return 해당 사용자의 랭킹 정보
     */
    @Transactional(readOnly = true)
    public RankingResponse getUserRanking(Long userId) {
        log.info("사용자 랭킹 조회: userId={}", userId);
        
        String sql = """
            WITH ranked_accounts AS (
                SELECT 
                    a.user_id,
                    u.nickname,
                    a.total_asset,
                    a.profit_rate,
                    a.profit,
                    ROW_NUMBER() OVER (ORDER BY a.total_asset DESC, a.user_id ASC) as rank
                FROM accounts a
                INNER JOIN users u ON a.user_id = u.user_id
                WHERE a.type = 0
                AND u.deleted_at IS NULL
            )
            SELECT 
                rank,
                user_id,
                nickname,
                total_asset,
                profit_rate,
                profit
            FROM ranked_accounts
            WHERE user_id = ?
            """;
        
        try (Connection connection = dataSource.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            
            stmt.setLong(1, userId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return RankingResponse.builder()
                            .rank(rs.getInt("rank"))
                            .userId(rs.getLong("user_id"))
                            .username(rs.getString("nickname"))
                            .totalAsset(rs.getInt("total_asset"))
                            .profitRate(rs.getDouble("profit_rate"))
                            .totalProfit(rs.getInt("profit"))
                            .build();
                }
            }
            
        } catch (Exception e) {
            log.error("사용자 랭킹 조회 중 오류 발생: userId={}", userId, e);
            throw new RuntimeException("사용자 랭킹 조회에 실패했습니다: " + e.getMessage());
        }
        
        throw new RuntimeException("해당 사용자의 랭킹 정보를 찾을 수 없습니다: " + userId);
    }
}