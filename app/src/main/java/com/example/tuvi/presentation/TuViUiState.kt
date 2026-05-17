package com.example.tuvi.presentation

import androidx.compose.runtime.Immutable
import androidx.annotation.StringRes
import com.example.tuvi.R
import com.example.tuvi.domain.model.CungSlug
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
        val AiNoCung = Res(R.string.error_ai_no_cung)
        val AiInsufficientTokens = Res(R.string.error_ai_insufficient_tokens)
    }
}

sealed interface TuViUiState {
    object Idle : TuViUiState

    object Loading : TuViUiState

    @Immutable
    data class Success(
        val data: TuViChart,
        /** Cache luận giải theo từng cung sau khi gọi `/api/interpret`. */
        val aiReadings: Map<CungSlug, String> = emptyMap(),
    ) : TuViUiState

    @Immutable
    data class Error(val error: TuViError) : TuViUiState
}
