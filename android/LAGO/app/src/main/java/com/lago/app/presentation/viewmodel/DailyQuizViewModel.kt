package com.lago.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lago.app.domain.entity.Quiz
import com.lago.app.domain.usecase.GetDailyQuizUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class DailyQuizUiState {
    object Loading : DailyQuizUiState()
    data class Success(val quiz: Quiz) : DailyQuizUiState()
    data class Error(val message: String) : DailyQuizUiState()
}

@HiltViewModel
class DailyQuizViewModel @Inject constructor(
    private val getDailyQuizUseCase: GetDailyQuizUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<DailyQuizUiState>(DailyQuizUiState.Loading)
    val uiState: StateFlow<DailyQuizUiState> = _uiState.asStateFlow()
    
    init {
        loadDailyQuiz()
    }
    
    private fun loadDailyQuiz() {
        viewModelScope.launch {
            _uiState.value = DailyQuizUiState.Loading
            getDailyQuizUseCase()
                .onSuccess { quiz ->
                    _uiState.value = DailyQuizUiState.Success(quiz)
                }
                .onFailure { error ->
                    _uiState.value = DailyQuizUiState.Error(error.message ?: "Unknown error")
                }
        }
    }
    
    fun retryLoadQuiz() {
        loadDailyQuiz()
    }
}