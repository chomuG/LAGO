package com.lago.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lago.app.domain.entity.Quiz
import com.lago.app.domain.entity.QuizResult
import com.lago.app.domain.usecase.GetRandomQuizUseCase
import com.lago.app.domain.usecase.SolveRandomQuizUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class RandomQuizUiState {
    object Loading : RandomQuizUiState()
    data class Success(val quiz: Quiz) : RandomQuizUiState()
    data class Error(val message: String) : RandomQuizUiState()
}

sealed class QuizSolveUiState {
    object Idle : QuizSolveUiState()
    object Loading : QuizSolveUiState()
    data class Success(val result: QuizResult) : QuizSolveUiState()
    data class Error(val message: String) : QuizSolveUiState()
}

@HiltViewModel
class RandomQuizViewModel @Inject constructor(
    private val getRandomQuizUseCase: GetRandomQuizUseCase,
    private val solveRandomQuizUseCase: SolveRandomQuizUseCase
) : ViewModel() {
    
    private val _quizState = MutableStateFlow<RandomQuizUiState>(RandomQuizUiState.Loading)
    val quizState: StateFlow<RandomQuizUiState> = _quizState.asStateFlow()
    
    private val _solveState = MutableStateFlow<QuizSolveUiState>(QuizSolveUiState.Idle)
    val solveState: StateFlow<QuizSolveUiState> = _solveState.asStateFlow()
    
    private var currentQuizId: Int = 0
    
    fun loadRandomQuiz(excludeQuizId: Int = 0) {
        viewModelScope.launch {
            _quizState.value = RandomQuizUiState.Loading
            
            getRandomQuizUseCase(excludeQuizId).fold(
                onSuccess = { quiz ->
                    _quizState.value = RandomQuizUiState.Success(quiz)
                    currentQuizId = quiz.quizId
                },
                onFailure = { exception ->
                    _quizState.value = RandomQuizUiState.Error(
                        exception.message ?: "퀴즈를 불러오는 중 오류가 발생했습니다."
                    )
                }
            )
        }
    }
    
    fun solveQuiz(userId: Int, userAnswer: Boolean) {
        val quizId = currentQuizId
        if (quizId == 0) return
        
        viewModelScope.launch {
            _solveState.value = QuizSolveUiState.Loading
            
            solveRandomQuizUseCase(userId, quizId, userAnswer).fold(
                onSuccess = { result ->
                    _solveState.value = QuizSolveUiState.Success(result)
                },
                onFailure = { exception ->
                    _solveState.value = QuizSolveUiState.Error(
                        exception.message ?: "퀴즈 풀이 중 오류가 발생했습니다."
                    )
                }
            )
        }
    }
    
    fun getNewQuiz() {
        // 현재 퀴즈 ID를 제외하고 새로운 퀴즈 요청
        loadRandomQuiz(currentQuizId)
        // 풀이 상태 초기화
        _solveState.value = QuizSolveUiState.Idle
    }
    
    fun resetSolveState() {
        _solveState.value = QuizSolveUiState.Idle
    }
}