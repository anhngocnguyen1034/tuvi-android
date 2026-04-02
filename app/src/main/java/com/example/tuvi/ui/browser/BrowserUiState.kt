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
    object GoBack    : BrowserCommand
    object GoForward : BrowserCommand
    object Reload    : BrowserCommand
    object Stop      : BrowserCommand
}
