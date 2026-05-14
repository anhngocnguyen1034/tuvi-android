package com.example.tuvi.presentation

import androidx.compose.runtime.Immutable
import androidx.annotation.StringRes
import com.example.tuvi.R
import com.example.tuvi.domain.model.TuViChart

/**
 * Lỗi user-facing dạng typed thay vì String đã resolve.
 * ViewModel không phụ thuộc Context/Resource; UI dùng [resolve] để lấy text theo locale hiện hành.
 */
sealed interface TuViError {
    data class Res(@StringRes val resId: Int) : TuViError
    data class Raw(val message: String) : TuViError

    companion object {
        val Unknown = Res(R.string.error_unknown)
        val AiUnavailable = Res(R.string.error_ai_interpret_503)
        val AiNoInput = Res(R.string.error_ai_no_input)
        val AiNoChart = Res(R.string.error_ai_no_chart)
    }
}

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
    data class Error(val error: TuViError) : TuViUiState
}
