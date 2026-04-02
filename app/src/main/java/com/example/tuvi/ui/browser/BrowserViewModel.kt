package com.example.tuvi.ui.browser

import android.webkit.CookieManager
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.tuvi.data.local.BookmarkDao
import com.example.tuvi.data.local.BookmarkItemEntity
import com.example.tuvi.data.local.HistoryDao
import com.example.tuvi.di.AppContainer
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class BrowserViewModel(
    private val historyDao: HistoryDao,
    private val bookmarkDao: BookmarkDao,
    private val incognito: Boolean = false
) : ViewModel() {

    // ── Tab state ─────────────────────────────────────────────────────────────
    val tabs = mutableStateListOf<TabState>().also { it.add(TabState()) }

    var activeTabId by mutableStateOf(tabs.first().id)
        private set

    var showTabSwitcher by mutableStateOf(false)
        private set

    val activeTab by derivedStateOf { tabs.firstOrNull { it.id == activeTabId } }

    /** true khi tab đang active là incognito */
    val isActiveIncognito by derivedStateOf { activeTab?.isIncognito == true }

    /** Tab switcher đang hiển thị danh sách incognito hay thường */
    var showIncognitoList by mutableStateOf(false)
        private set

    // ── Commands ──────────────────────────────────────────────────────────────
    private val _commands = MutableSharedFlow<BrowserCommand>(extraBufferCapacity = 8)
    val commands: SharedFlow<BrowserCommand> = _commands.asSharedFlow()

    // ── Bookmark: isBookmarked tự cập nhật theo URL của tab active ────────────
    /**
     * Flow<Boolean> — emit mỗi khi URL active thay đổi hoặc bảng bookmark thay đổi.
     * Dùng snapshotFlow + flatMapLatest để bridge giữa Compose state và Flow.
     */
    val isCurrentUrlBookmarked = snapshotFlow { activeTab?.url ?: "" }
        .flatMapLatest { url ->
            if (url.isBlank()) flowOf(false) else bookmarkDao.isBookmarked(url)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    // ── Tab management ────────────────────────────────────────────────────────

    fun addNewTab(url: String = "https://www.google.com") {
        val newTab = TabState(url = url, title = "Tab mới", isIncognito = false)
        tabs.add(newTab)
        activeTabId = newTab.id
        showTabSwitcher = false
    }

    fun addNewIncognitoTab(url: String = "https://www.google.com") {
        val newTab = TabState(url = url, title = "Ẩn danh", isIncognito = true)
        tabs.add(newTab)
        activeTabId = newTab.id
        showTabSwitcher = false
    }

    fun closeTab(id: String): Boolean {
        val idx = tabs.indexOfFirst { it.id == id }
        if (idx < 0) return false
        val wasIncognito = tabs[idx].isIncognito
        tabs.removeAt(idx)
        // Xoá cookie nếu đóng tab ẩn danh và không còn tab ẩn danh nào khác
        if (wasIncognito && tabs.none { it.isIncognito }) {
            CookieManager.getInstance().removeAllCookies(null)
        }
        if (tabs.isEmpty()) return true
        if (activeTabId == id) {
            activeTabId = tabs.getOrNull(idx)?.id ?: tabs.last().id
        }
        return false
    }

    fun closeAllIncognitoTabs() {
        tabs.removeAll { it.isIncognito }
        CookieManager.getInstance().removeAllCookies(null)
        if (tabs.isEmpty()) {
            addNewTab()  // addNewTab sẽ set showTabSwitcher = false
            return
        }
        if (activeTab?.isIncognito == true || activeTab == null) {
            activeTabId = tabs.last().id
        }
        showIncognitoList = false  // reset về panel thường
        showTabSwitcher = false
    }

    fun switchTab(id: String) {
        if (tabs.any { it.id == id }) {
            activeTabId = id
            showTabSwitcher = false
            // Đồng bộ panel switcher theo tab được chọn
            showIncognitoList = tabs.firstOrNull { it.id == id }?.isIncognito == true
        }
    }

    fun setTabSwitcherPanel(incognito: Boolean) { showIncognitoList = incognito }

    fun openTabSwitcher() {
        showIncognitoList = activeTab?.isIncognito == true
        showTabSwitcher = true
    }
    fun closeTabSwitcher() { showTabSwitcher = false }

    // ── Bookmark actions ──────────────────────────────────────────────────────

    /**
     * Toggle: nếu đã bookmark → xoá; nếu chưa → thêm.
     * @return true nếu vừa thêm, false nếu vừa xoá
     */
    fun toggleBookmark(): Boolean {
        val url   = activeTab?.url   ?: return false
        val title = activeTab?.title ?: url
        val added = !isCurrentUrlBookmarked.value
        viewModelScope.launch {
            if (added) {
                bookmarkDao.insertBookmark(
                    BookmarkItemEntity(url = url, title = title, createdTime = System.currentTimeMillis())
                )
            } else {
                bookmarkDao.deleteBookmarkByUrl(url)
            }
        }
        return added
    }

    // ── WebViewClient callbacks ───────────────────────────────────────────────

    fun onPageStarted(tabId: String, url: String) = updateTab(tabId) {
        it.copy(url = url, isLoading = true, error = null)
    }

    fun onPageFinished(tabId: String, url: String, title: String, canGoBack: Boolean, canGoForward: Boolean) {
        updateTab(tabId) {
            it.copy(url = url, title = title.ifBlank { url }, isLoading = false,
                progress = 100, canGoBack = canGoBack, canGoForward = canGoForward)
        }
        val tabIsIncognito = tabs.firstOrNull { it.id == tabId }?.isIncognito == true
        if (!incognito && !tabIsIncognito && url.isNotBlank() && url != "about:blank") {
            viewModelScope.launch {
                historyDao.upsert(url = url, title = title.ifBlank { url }, timestamp = System.currentTimeMillis())
            }
        }
    }

    fun onProgressChanged(tabId: String, progress: Int) = updateTab(tabId) { it.copy(progress = progress) }

    fun onReceivedError(tabId: String, description: String) = updateTab(tabId) {
        it.copy(isLoading = false, error = description)
    }

    // ── Navigation commands ───────────────────────────────────────────────────

    fun navigateTo(rawInput: String) {
        val url = rawInput.trim().toFullUrl()
        updateTab(activeTabId) { it.copy(url = url, error = null) }
        viewModelScope.launch { _commands.emit(BrowserCommand.LoadUrl(url)) }
    }

    fun goBack()    { viewModelScope.launch { _commands.emit(BrowserCommand.GoBack) } }
    fun goForward() { viewModelScope.launch { _commands.emit(BrowserCommand.GoForward) } }
    fun reload()    { viewModelScope.launch { _commands.emit(BrowserCommand.Reload) } }
    fun stop()      { viewModelScope.launch { _commands.emit(BrowserCommand.Stop) } }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun updateTab(id: String, transform: (TabState) -> TabState) {
        val idx = tabs.indexOfFirst { it.id == id }
        if (idx >= 0) tabs[idx] = transform(tabs[idx])
    }

    private fun String.toFullUrl(): String {
        if (startsWith("http://") || startsWith("https://")) return this
        // IP address (vd: 192.168.1.1 hoặc 192.168.1.1:8080) → dùng http
        val ipRegex = Regex("""^\d{1,3}(\.\d{1,3}){3}(:\d+)?(/.*)?$""")
        if (matches(ipRegex)) return "http://$this"
        // Domain có dấu chấm, không có khoảng trắng → https
        return if (contains(".") && !contains(" ")) "https://$this"
        else "https://www.google.com/search?q=${android.net.Uri.encode(this)}"
    }

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                BrowserViewModel(AppContainer.historyDao, AppContainer.bookmarkDao) as T
        }
    }
}
