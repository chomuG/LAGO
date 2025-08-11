package com.lago.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lago.app.domain.entity.ChartPattern
import com.lago.app.domain.usecase.GetChartPatternsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class ChartPatternsUiState {
    object Loading : ChartPatternsUiState()
    data class Success(val patterns: List<ChartPattern>) : ChartPatternsUiState()
    data class Error(val message: String) : ChartPatternsUiState()
}

@HiltViewModel
class PatternStudyViewModel @Inject constructor(
    private val getChartPatternsUseCase: GetChartPatternsUseCase
) : ViewModel() {
    
    private val _patternsState = MutableStateFlow<ChartPatternsUiState>(ChartPatternsUiState.Loading)
    val patternsState: StateFlow<ChartPatternsUiState> = _patternsState.asStateFlow()
    
    fun loadChartPatterns() {
        viewModelScope.launch {
            _patternsState.value = ChartPatternsUiState.Loading
            
            getChartPatternsUseCase().fold(
                onSuccess = { patterns ->
                    _patternsState.value = ChartPatternsUiState.Success(patterns)
                },
                onFailure = { exception ->
                    _patternsState.value = ChartPatternsUiState.Error(
                        exception.message ?: "알 수 없는 오류가 발생했습니다."
                    )
                }
            )
        }
    }
}