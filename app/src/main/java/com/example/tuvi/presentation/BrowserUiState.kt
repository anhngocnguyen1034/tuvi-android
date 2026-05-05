package com.example.tuvi.presentation

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable

@Immutable
data class BrowserUiState(
    val url: String = "",
    val displayTitle: String = "",
    val progress: Int = 0,
    val isLoading: Boolean = false,
    val canGoBack: Boolean = false,
    val canGoForward: Boolean = false,
    val error: String? = null
)

@Stable
sealed class LongPressTarget(open val url: String, open val x: Float, open val y: Float) {
    @Immutable data class Link(override val url: String, override val x: Float, override val y: Float) : LongPressTarget(url, x, y)
    @Immutable data class Image(override val url: String, override val x: Float, override val y: Float) : LongPressTarget(url, x, y)
}

@Stable
sealed interface BrowserCommand {
    @Immutable data class LoadUrl(val url: String) : BrowserCommand
    /**
     * [fallbackUrl]: URL tại mục trước trong navHistory — dùng khi WebView không còn stack
     * (vd. vừa khôi phục tab sau khi thoát app), vì `webView.goBack()` không hoạt động.
     */
    @Immutable data class GoBack(val fallbackUrl: String? = null) : BrowserCommand
    /** Tương tự [GoBack], cho bước tiếp trong lịch sử đã lưu. */
    @Immutable data class GoForward(val fallbackUrl: String? = null) : BrowserCommand
    object Reload    : BrowserCommand
    object Stop      : BrowserCommand
}
