package com.example.tuvi.ui.browser

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.tuvi.data.local.HistoryDao
import com.example.tuvi.di.AppContainer
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

/**
 * Quản lý toàn bộ logic multi-tab.
 *
 * Vòng đời WebView:
 * - ViewModel chỉ giữ [TabState] (metadata thuần Kotlin, không có Context/View).
 * - WebView thực tế được tạo trong Composable [TabWebViewHolder] bằng `remember { WebView(context) }`.
 * - Compose tự động gắn lifecycle: khi một tab bị xoá khỏi [tabs], Compose dispose
 *   composable tương ứng → DisposableEffect gọi webView.destroy().
 * - Khi chuyển tab, WebView của tab cũ bị ẩn (size = 0.dp) nhưng không bị destroy,
 *   giữ nguyên trạng thái cuộn và dữ liệu.
 */
class BrowserViewModel(
    private val historyDao: HistoryDao,
    private val incognito: Boolean = false
) : ViewModel() {

    // ── Tab state (Compose-observable SnapshotStateList) ──────────────────────
    val tabs = mutableStateListOf<TabState>().also {
        it.add(TabState())
    }

    var activeTabId by mutableStateOf(tabs.first().id)
        private set

    var showTabSwitcher by mutableStateOf(false)
        private set

    /** Tab đang active — derived, tự recompose khi tabs/activeTabId thay đổi */
    val activeTab by derivedStateOf { tabs.firstOrNull { it.id == activeTabId } }

    // ── Commands gửi tới WebView của tab active ───────────────────────────────
    private val _commands = MutableSharedFlow<BrowserCommand>(extraBufferCapacity = 8)
    val commands: SharedFlow<BrowserCommand> = _commands.asSharedFlow()

    // ── Tab management ────────────────────────────────────────────────────────

    /** Thêm tab mới và chuyển sang tab đó. */
    fun addNewTab(url: String = "https://www.google.com") {
        val newTab = TabState(url = url, title = "Tab mới")
        tabs.add(newTab)
        activeTabId = newTab.id
        showTabSwitcher = false
    }

    /**
     * Đóng một tab.
     * - Nếu là tab active, chuyển sang tab liền kề.
     * - Nếu đóng tab cuối cùng → trả về true để caller navigate về Home.
     */
    fun closeTab(id: String): Boolean {
        val idx = tabs.indexOfFirst { it.id == id }
        if (idx < 0) return false
        tabs.removeAt(idx)
        if (tabs.isEmpty()) return true   // caller phải navigate về Home
        if (activeTabId == id) {
            activeTabId = tabs.getOrNull(idx)?.id ?: tabs.last().id
        }
        return false
    }

    fun switchTab(id: String) {
        if (tabs.any { it.id == id }) {
            activeTabId = id
            showTabSwitcher = false
        }
    }

    fun openTabSwitcher()  { showTabSwitcher = true }
    fun closeTabSwitcher() { showTabSwitcher = false }

    // ── Callbacks từ WebViewClient/WebChromeClient (per tab) ─────────────────

    fun onPageStarted(tabId: String, url: String) = updateTab(tabId) {
        it.copy(url = url, isLoading = true, error = null)
    }

    fun onPageFinished(tabId: String, url: String, title: String, canGoBack: Boolean, canGoForward: Boolean) {
        updateTab(tabId) {
            it.copy(
                url = url,
                title = title.ifBlank { url },
                isLoading = false,
                progress = 100,
                canGoBack = canGoBack,
                canGoForward = canGoForward
            )
        }
        if (!incognito && url.isNotBlank() && url != "about:blank") {
            viewModelScope.launch {
                historyDao.upsert(url = url, title = title.ifBlank { url }, timestamp = System.currentTimeMillis())
            }
        }
    }

    fun onProgressChanged(tabId: String, progress: Int) = updateTab(tabId) {
        it.copy(progress = progress)
    }

    fun onReceivedError(tabId: String, description: String) = updateTab(tabId) {
        it.copy(isLoading = false, error = description)
    }

    // ── Lệnh điều hướng (chỉ active tab nhận) ────────────────────────────────

    fun navigateTo(rawInput: String) {
        val url = rawInput.trim().toFullUrl()
        updateTab(activeTabId) { it.copy(url = url, error = null) }
        viewModelScope.launch { _commands.emit(BrowserCommand.LoadUrl(url)) }
    }

    fun goBack()    { viewModelScope.launch { _commands.emit(BrowserCommand.GoBack) } }
    fun goForward() { viewModelScope.launch { _commands.emit(BrowserCommand.GoForward) } }
    fun reload()    { viewModelScope.launch { _commands.emit(BrowserCommand.Reload) } }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun updateTab(id: String, transform: (TabState) -> TabState) {
        val idx = tabs.indexOfFirst { it.id == id }
        if (idx >= 0) tabs[idx] = transform(tabs[idx])
    }

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
