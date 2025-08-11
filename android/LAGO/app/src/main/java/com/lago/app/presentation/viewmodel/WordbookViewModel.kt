package com.lago.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lago.app.domain.entity.Term
import com.lago.app.domain.usecase.GetTermsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class TermsUiState {
    object Loading : TermsUiState()
    data class Success(val terms: List<Term>) : TermsUiState()
    data class Error(val message: String) : TermsUiState()
}

@HiltViewModel
class WordbookViewModel @Inject constructor(
    private val getTermsUseCase: GetTermsUseCase
) : ViewModel() {
    
    private val _termsState = MutableStateFlow<TermsUiState>(TermsUiState.Loading)
    val termsState: StateFlow<TermsUiState> = _termsState.asStateFlow()
    
    fun loadTerms(userId: Int? = null) {
        viewModelScope.launch {
            _termsState.value = TermsUiState.Loading
            
            getTermsUseCase(userId).fold(
                onSuccess = { terms ->
                    _termsState.value = TermsUiState.Success(terms)
                },
                onFailure = { exception ->
                    _termsState.value = TermsUiState.Error(
                        exception.message ?: "알 수 없는 오류가 발생했습니다."
                    )
                }
            )
        }
    }
}