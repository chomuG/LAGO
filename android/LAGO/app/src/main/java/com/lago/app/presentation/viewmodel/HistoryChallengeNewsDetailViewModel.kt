package com.lago.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lago.app.domain.entity.HistoryChallengeNews
import com.lago.app.domain.usecase.GetHistoryChallengeNewsDetailUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class HistoryChallengeNewsDetailUiState {
    object Loading : HistoryChallengeNewsDetailUiState()
    data class Success(val news: HistoryChallengeNews) : HistoryChallengeNewsDetailUiState()
    data class Error(val message: String) : HistoryChallengeNewsDetailUiState()
}

@HiltViewModel
class HistoryChallengeNewsDetailViewModel @Inject constructor(
    private val getHistoryChallengeNewsDetailUseCase: GetHistoryChallengeNewsDetailUseCase
) : ViewModel() {

    private val _newsDetailState = MutableStateFlow<HistoryChallengeNewsDetailUiState>(HistoryChallengeNewsDetailUiState.Loading)
    val newsDetailState: StateFlow<HistoryChallengeNewsDetailUiState> = _newsDetailState

    fun loadNewsDetail(challengeNewsId: Int) {
        viewModelScope.launch {
            _newsDetailState.value = HistoryChallengeNewsDetailUiState.Loading
            
            try {
                android.util.Log.d("HistoryChallengeNewsDetailViewModel", "📰 역사적 챌린지 뉴스 상세 로드 시작 - challengeNewsId: $challengeNewsId")
                
                val result = getHistoryChallengeNewsDetailUseCase(challengeId = 1, challengeNewsId = challengeNewsId)
                
                result.fold(
                    onSuccess = { news ->
                        android.util.Log.d("HistoryChallengeNewsDetailViewModel", "📰 역사적 챌린지 뉴스 상세 로드 성공: ${news.title}")
                        _newsDetailState.value = HistoryChallengeNewsDetailUiState.Success(news)
                    },
                    onFailure = { error ->
                        android.util.Log.e("HistoryChallengeNewsDetailViewModel", "📰 역사적 챌린지 뉴스 상세 로드 실패: ${error.message}")
                        _newsDetailState.value = HistoryChallengeNewsDetailUiState.Error("뉴스를 불러오는데 실패했습니다")
                    }
                )
            } catch (e: Exception) {
                android.util.Log.e("HistoryChallengeNewsDetailViewModel", "📰 역사적 챌린지 뉴스 상세 로드 예외", e)
                _newsDetailState.value = HistoryChallengeNewsDetailUiState.Error("뉴스를 불러오는데 실패했습니다: ${e.message}")
            }
        }
    }
}