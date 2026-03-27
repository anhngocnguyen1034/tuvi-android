package com.example.tuvi.ui.browser

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class BrowserViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(BrowserUiState())
    val uiState: StateFlow<BrowserUiState> = _uiState.asStateFlow()

    private val _commands = MutableSharedFlow<BrowserCommand>(extraBufferCapacity = 8)
    val commands: SharedFlow<BrowserCommand> = _commands.asSharedFlow()

    // ── Callbacks từ WebViewClient / WebChromeClient ──────────────────────────

    fun onPageStarted(url: String) {
        _uiState.update { it.copy(url = url, isLoading = true, error = null) }
    }

    fun onPageFinished(url: String, title: String, canGoBack: Boolean, canGoForward: Boolean) {
        _uiState.update {
            it.copy(
                url = url,
                displayTitle = title.ifBlank { url },
                isLoading = false,
                progress = 100,
                canGoBack = canGoBack,
                canGoForward = canGoForward
            )
        }
    }

    fun onProgressChanged(progress: Int) {
        _uiState.update { it.copy(progress = progress) }
    }

    fun onReceivedError(description: String) {
        _uiState.update { it.copy(isLoading = false, error = description) }
    }

    fun onNavigationStateChanged(canGoBack: Boolean, canGoForward: Boolean) {
        _uiState.update { it.copy(canGoBack = canGoBack, canGoForward = canGoForward) }
    }

    // ── Lệnh ra cho WebViewContainer ─────────────────────────────────────────

    fun navigateTo(rawInput: String) {
        val url = rawInput.trim().toFullUrl()
        viewModelScope.launch { _commands.emit(BrowserCommand.LoadUrl(url)) }
        _uiState.update { it.copy(url = url, error = null) }
    }

    fun goBack()    { viewModelScope.launch { _commands.emit(BrowserCommand.GoBack) } }
    fun goForward() { viewModelScope.launch { _commands.emit(BrowserCommand.GoForward) } }
    fun reload()    { viewModelScope.launch { _commands.emit(BrowserCommand.Reload) } }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /** Chuyển input của người dùng thành URL đầy đủ. */
    private fun String.toFullUrl(): String {
        if (startsWith("http://") || startsWith("https://")) return this
        // Trông như domain? → thêm https
        return if (contains(".") && !contains(" ")) "https://$this"
        // Ngược lại → Google search
        else "https://www.google.com/search?q=${android.net.Uri.encode(this)}"
    }
}
