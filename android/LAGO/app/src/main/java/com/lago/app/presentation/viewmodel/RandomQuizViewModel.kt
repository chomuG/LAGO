package com.lago.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lago.app.domain.entity.Quiz
import com.lago.app.domain.usecase.GetRandomQuizUseCase
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

@HiltViewModel
class RandomQuizViewModel @Inject constructor(
    private val getRandomQuizUseCase: GetRandomQuizUseCase
) : ViewModel() {
    
    private val _quizState = MutableStateFlow<RandomQuizUiState>(RandomQuizUiState.Loading)
    val quizState: StateFlow<RandomQuizUiState> = _quizState.asStateFlow()
    
    fun loadRandomQuiz() {
        viewModelScope.launch {
            _quizState.value = RandomQuizUiState.Loading
            
            getRandomQuizUseCase().fold(
                onSuccess = { quiz ->
                    _quizState.value = RandomQuizUiState.Success(quiz)
                },
                onFailure = { exception ->
                    _quizState.value = RandomQuizUiState.Error(
                        exception.message ?: "퀴즈를 불러오는 중 오류가 발생했습니다."
                    )
                }
            )
        }
    }
    
    fun getNewQuiz() {
        loadRandomQuiz()
    }
}