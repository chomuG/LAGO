package com.lago.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lago.app.domain.entity.DailyQuizResult
import com.lago.app.domain.entity.DailyQuizStatus
import com.lago.app.domain.entity.Quiz
import com.lago.app.domain.usecase.GetDailyQuizUseCase
import com.lago.app.domain.usecase.SolveDailyQuizUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class DailyQuizUiState {
    object Loading : DailyQuizUiState()
    data class Success(val status: DailyQuizStatus) : DailyQuizUiState()
    data class Error(val message: String) : DailyQuizUiState()
}

sealed class DailyQuizSolveUiState {
    object Idle : DailyQuizSolveUiState()
    object Loading : DailyQuizSolveUiState()
    data class Success(val result: DailyQuizResult) : DailyQuizSolveUiState()
    data class Error(val message: String) : DailyQuizSolveUiState()
}

@HiltViewModel
class DailyQuizViewModel @Inject constructor(
    private val getDailyQuizUseCase: GetDailyQuizUseCase,
    private val solveDailyQuizUseCase: SolveDailyQuizUseCase,
    private val userPreferences: com.lago.app.data.local.prefs.UserPreferences
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<DailyQuizUiState>(DailyQuizUiState.Loading)
    val uiState: StateFlow<DailyQuizUiState> = _uiState.asStateFlow()
    
    private val _solveState = MutableStateFlow<DailyQuizSolveUiState>(DailyQuizSolveUiState.Idle)
    val solveState: StateFlow<DailyQuizSolveUiState> = _solveState.asStateFlow()
    
    private var currentQuizId: Int = 0
    private var startTime: Long = 0
    
    fun loadDailyQuiz(userId: Int = userPreferences.getUserIdLong().toInt()) {
        viewModelScope.launch {
            _uiState.value = DailyQuizUiState.Loading
            android.util.Log.d("DailyQuizViewModel", "로드 - 사용할 userId: $userId")
            startTime = System.currentTimeMillis()
            
            getDailyQuizUseCase(userId)
                .onSuccess { status ->
                    _uiState.value = DailyQuizUiState.Success(status)
                    currentQuizId = status.quiz?.quizId ?: 0
                }
                .onFailure { error ->
                    _uiState.value = DailyQuizUiState.Error(error.message ?: "Unknown error")
                }
        }
    }
    
    fun solveQuiz(userId: Int = userPreferences.getUserIdLong().toInt(), userAnswer: Boolean) {
        if (currentQuizId == 0) return
        
        android.util.Log.d("DailyQuizViewModel", "퀴즈 풀이 - 사용할 userId: $userId")
        val solvedTimeSeconds = ((System.currentTimeMillis() - startTime) / 1000).toInt()
        
        viewModelScope.launch {
            _solveState.value = DailyQuizSolveUiState.Loading
            
            solveDailyQuizUseCase(userId, currentQuizId, userAnswer, solvedTimeSeconds)
                .onSuccess { result ->
                    _solveState.value = DailyQuizSolveUiState.Success(result)
                }
                .onFailure { error ->
                    _solveState.value = DailyQuizSolveUiState.Error(error.message ?: "Unknown error")
                }
        }
    }
    
    fun resetSolveState() {
        _solveState.value = DailyQuizSolveUiState.Idle
    }
    
    fun retryLoadQuiz(userId: Int = userPreferences.getUserIdLong().toInt()) {
        loadDailyQuiz(userId)
    }
}