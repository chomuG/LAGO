package com.lago.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lago.app.domain.entity.News
import com.lago.app.domain.usecase.GetInterestNewsUseCase
import com.lago.app.domain.usecase.GetNewsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NewsViewModel @Inject constructor(
    private val getNewsUseCase: GetNewsUseCase,
    private val getInterestNewsUseCase: GetInterestNewsUseCase
) : ViewModel() {

    private val _newsState = MutableStateFlow<NewsUiState>(NewsUiState.Loading)
    val newsState: StateFlow<NewsUiState> = _newsState.asStateFlow()

    private val _interestNewsState = MutableStateFlow<NewsUiState>(NewsUiState.Loading)
    val interestNewsState: StateFlow<NewsUiState> = _interestNewsState.asStateFlow()

    fun loadNews() {
        viewModelScope.launch {
            _newsState.value = NewsUiState.Loading
            getNewsUseCase().fold(
                onSuccess = { newsList ->
                    _newsState.value = NewsUiState.Success(newsList)
                },
                onFailure = { exception ->
                    _newsState.value = NewsUiState.Error(exception.message ?: "Unknown error")
                }
            )
        }
    }

    fun loadInterestNews() {
        viewModelScope.launch {
            _interestNewsState.value = NewsUiState.Loading
            getInterestNewsUseCase().fold(
                onSuccess = { newsList ->
                    _interestNewsState.value = NewsUiState.Success(newsList)
                },
                onFailure = { exception ->
                    _interestNewsState.value = NewsUiState.Error(exception.message ?: "Unknown error")
                }
            )
        }
    }
}

sealed class NewsUiState {
    object Loading : NewsUiState()
    data class Success(val newsList: List<News>) : NewsUiState()
    data class Error(val message: String) : NewsUiState()
}