package com.example.tuvi.presentation

import androidx.compose.runtime.Immutable
import com.example.tuvi.domain.model.TuViChart

sealed interface TuViUiState {
    object Idle : TuViUiState

    @Immutable
    data class Loading(val requestingAiReading: Boolean = false) : TuViUiState

    @Immutable
    data class Success(
        val data: TuViChart,
        /** Non-null after `/api/interpret`; plain chart flow keeps this null. */
        val aiReading: String? = null,
    ) : TuViUiState

    @Immutable
    data class Error(val message: String) : TuViUiState
}
