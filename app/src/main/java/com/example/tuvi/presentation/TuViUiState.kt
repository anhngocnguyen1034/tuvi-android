package com.example.tuvi.presentation

import com.example.tuvi.domain.model.TuViChart

sealed interface TuViUiState {
    object Idle    : TuViUiState
    object Loading : TuViUiState
    data class Success(val data: TuViChart)   : TuViUiState
    data class Error(val message: String)     : TuViUiState
}
