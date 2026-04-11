package com.example.tuvi.ui.browser

data class BrowserUiState(
    val url: String = "",
    val displayTitle: String = "",
    val progress: Int = 0,
    val isLoading: Boolean = false,
    val canGoBack: Boolean = false,
    val canGoForward: Boolean = false,
    val error: String? = null
)

sealed interface BrowserCommand {
    data class LoadUrl(val url: String) : BrowserCommand
    /**
     * [fallbackUrl]: URL tại mục trước trong navHistory — dùng khi WebView không còn stack
     * (vd. vừa khôi phục tab sau khi thoát app), vì `webView.goBack()` không hoạt động.
     */
    data class GoBack(val fallbackUrl: String? = null) : BrowserCommand
    /** Tương tự [GoBack], cho bước tiếp trong lịch sử đã lưu. */
    data class GoForward(val fallbackUrl: String? = null) : BrowserCommand
    object Reload    : BrowserCommand
    object Stop      : BrowserCommand
}
