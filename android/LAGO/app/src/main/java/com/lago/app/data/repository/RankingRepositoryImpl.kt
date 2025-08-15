package com.lago.app.data.repository

import com.lago.app.data.remote.ApiService
import com.lago.app.data.remote.dto.CalculatedRankingUser
import com.lago.app.data.remote.dto.RankingDto
import com.lago.app.domain.repository.RankingRepository
import com.lago.app.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RankingRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : RankingRepository {
    
    override suspend fun getRanking(): Flow<Resource<List<CalculatedRankingUser>>> = flow {
        try {
            emit(Resource.Loading())
            
            val response = apiService.getRanking()
            android.util.Log.d("RankingRepository", "🏆 랭킹 API 응답: ${response.size}명")
            
            // API 데이터를 계산된 데이터로 변환
            val calculatedRankings = response.map { dto ->
                calculateRankingMetrics(dto)
            }
            
            emit(Resource.Success(calculatedRankings))
            
        } catch (e: Exception) {
            android.util.Log.e("RankingRepository", "🏆 랭킹 로드 실패: ${e.localizedMessage}", e)
            emit(Resource.Error(e.localizedMessage ?: "랭킹 데이터를 불러오는데 실패했습니다."))
        }
    }
    
    /**
     * 랭킹 계산 로직: (총자산 - 1,000,000) / 1,000,000 * 100
     */
    private fun calculateRankingMetrics(dto: RankingDto): CalculatedRankingUser {
        val initialAsset = 1_000_000L // 초기 자산 100만원
        val profit = dto.totalAsset - initialAsset
        val profitRate = if (initialAsset > 0) {
            (profit.toDouble() / initialAsset) * 100
        } else 0.0
        
        android.util.Log.d("RankingRepository", "🏆 ${dto.username}: 총자산 ${dto.totalAsset}원 → 수익 ${profit}원 (${String.format("%.1f", profitRate)}%)")
        
        return CalculatedRankingUser(
            rank = dto.rank,
            userId = dto.userId,
            username = dto.username,
            totalAsset = dto.totalAsset,
            calculatedProfitRate = profitRate,
            calculatedProfit = profit,
            isCurrentUser = dto.userId == 5, // 임시 테스트용 사용자 ID
            isAi = dto.username.contains("AI") || dto.username.contains("봇"), // AI 봇 판별
            personality = dto.personality
        )
    }
}