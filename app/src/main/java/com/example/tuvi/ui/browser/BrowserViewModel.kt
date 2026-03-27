package com.example.tuvi.ui.browser

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.tuvi.data.local.HistoryDao
import com.example.tuvi.di.AppContainer
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class BrowserViewModel(
    private val historyDao: HistoryDao,
    /** Khi true, không lưu lịch sử — chuẩn bị cho chế độ ẩn danh sau này */
    private val incognito: Boolean = false
) : ViewModel() {

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
        if (!incognito && url.isNotBlank() && url != "about:blank") {
            viewModelScope.launch {
                historyDao.upsert(
                    url = url,
                    title = title.ifBlank { url },
                    timestamp = System.currentTimeMillis()
                )
            }
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

    private fun String.toFullUrl(): String {
        if (startsWith("http://") || startsWith("https://")) return this
        return if (contains(".") && !contains(" ")) "https://$this"
        else "https://www.google.com/search?q=${android.net.Uri.encode(this)}"
    }

    // ── Factory ───────────────────────────────────────────────────────────────

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                BrowserViewModel(AppContainer.historyDao) as T
        }
    }
}
