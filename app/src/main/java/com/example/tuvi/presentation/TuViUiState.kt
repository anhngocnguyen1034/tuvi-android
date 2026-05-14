package com.example.tuvi.presentation

import androidx.compose.runtime.Immutable
import com.example.tuvi.domain.model.TuViChart

sealed interface TuViUiState {
    object Idle : TuViUiState

    object Loading : TuViUiState

    @Immutable
    data class Success(
        val data: TuViChart,
        /** Filled after user requests `/api/interpret` from the chart screen. */
        val aiReading: String? = null,
    ) : TuViUiState

    @Immutable
    data class Error(val message: String) : TuViUiState
}
