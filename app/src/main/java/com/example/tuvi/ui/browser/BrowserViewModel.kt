package com.example.tuvi.ui.browser

import android.webkit.CookieManager
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.graphics.ImageBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.tuvi.data.local.BookmarkDao
import com.example.tuvi.data.local.BookmarkItemEntity
import com.example.tuvi.data.local.HistoryDao
import com.example.tuvi.data.local.TabSessionDao
import com.example.tuvi.data.local.TabSessionEntity
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
    private val tabSessionDao: TabSessionDao,
    private val incognito: Boolean = false
) : ViewModel() {

    // ── Tab state ─────────────────────────────────────────────────────────────
    val tabs = mutableStateListOf<TabState>().also { it.add(TabState()) }


    init {
        if (!incognito) {
            viewModelScope.launch {
                // Khôi phục toàn bộ tab session từ DB
                val saved = tabSessionDao.getAll()
                if (saved.isNotEmpty()) {
                    tabs.clear()
                    saved.forEach { entity ->
                        val history = if (entity.navHistoryJson.isBlank()) emptyList()
                                      else entity.navHistoryJson.split("\n").filter { it.isNotBlank() }
                        val histIdx = entity.navHistoryIndex.coerceIn(-1, history.size - 1)
                        tabs.add(
                            TabState(
                                id = entity.id,
                                url = entity.url,
                                title = entity.title,
                                pendingLoadUrl = entity.url,
                                navHistory = history,
                                navHistoryIndex = histIdx,
                                canGoBack = histIdx > 0,
                                canGoForward = histIdx < history.size - 1
                            )
                        )
                        if (entity.isActive) activeTabId = entity.id
                    }
                    // Đảm bảo activeTabId hợp lệ
                    if (tabs.none { it.id == activeTabId }) {
                        activeTabId = tabs.last().id
                    }
                } else {
                    // Lần đầu mở app: khôi phục URL gần nhất từ history
                    val latest = historyDao.getLatestUrl().orEmpty().trim()
                    if (latest.isNotEmpty() && tabs.isNotEmpty()) {
                        val first = tabs.first()
                        tabs[0] = first.copy(url = latest, pendingLoadUrl = latest)
                        tabLastSavedUrl[first.id] = latest
                    }
                }
            }
        }
    }

    /** Lưu toàn bộ tab thường (không lưu tab ẩn danh) vào DB. */
    private fun saveTabs() {
        viewModelScope.launch {
            val entities = tabs
                .filter { !it.isIncognito }
                .mapIndexed { idx, tab ->
                    TabSessionEntity(
                        id = tab.id,
                        url = tab.url,
                        title = tab.title,
                        sortOrder = idx,
                        isActive = tab.id == activeTabId,
                        navHistoryJson = tab.navHistory.joinToString("\n"),
                        navHistoryIndex = tab.navHistoryIndex
                    )
                }
            tabSessionDao.clearAll()
            if (entities.isNotEmpty()) tabSessionDao.insertAll(entities)
        }
    }

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

    // Thumbnail (screenshot) của từng tab — Map<tabId, ImageBitmap>
    val thumbnails = mutableStateMapOf<String, ImageBitmap>()

    fun updateThumbnail(tabId: String, bitmap: ImageBitmap) {
        thumbnails[tabId] = bitmap
    }

    // URL đã lưu vào history gần nhất của mỗi tab (key = tabId)
    private val tabLastSavedUrl = mutableMapOf<String, String>()

    // ── Tab management ────────────────────────────────────────────────────────

    fun addNewTab(url: String = "https://www.google.com") {
        val newTab = TabState(url = url, title = "Tab mới", isIncognito = false)
        tabs.add(newTab)
        activeTabId = newTab.id
        showTabSwitcher = false
        saveTabs()
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
        if (tabs.isEmpty()) {
            viewModelScope.launch { tabSessionDao.clearAll() }
            return true
        }
        if (activeTabId == id) {
            activeTabId = tabs.getOrNull(idx)?.id ?: tabs.last().id
        }
        saveTabs()
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
            showIncognitoList = tabs.firstOrNull { it.id == id }?.isIncognito == true
            saveTabs()
        }
    }

    fun setTabSwitcherPanel(incognito: Boolean) { showIncognitoList = incognito }

    fun openTabSwitcher() {
        showIncognitoList = activeTab?.isIncognito == true
        showTabSwitcher = true
    }
    fun closeTabSwitcher() { showTabSwitcher = false }

    // ── Overlay screens (History / Bookmarks) ─────────────────────────────────
    var showHistoryOverlay  by mutableStateOf(false); private set
    var showBookmarkOverlay by mutableStateOf(false); private set

    fun openHistory()        { showHistoryOverlay  = true  }
    fun closeHistory()       { showHistoryOverlay  = false }
    fun openBookmarks()      { showBookmarkOverlay = true  }
    fun closeBookmarks()     { showBookmarkOverlay = false }

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

    fun onPageStarted(tabId: String, url: String) {
        updateTab(tabId) { it.copy(url = url, isLoading = true, error = null) }
        // Reset để cho phép lưu URL mới khi navigation bắt đầu
        tabLastSavedUrl.remove(tabId)
    }

    @Suppress("UNUSED_PARAMETER")
    fun onPageFinished(tabId: String, url: String, title: String, canGoBack: Boolean, canGoForward: Boolean) {
        // Đọc flag isHistoryNav từ state trước khi updateTab (để dùng trong điều kiện ghi log/history)
        val isHistNav = tabs.firstOrNull { it.id == tabId }?.isHistoryNav == true
        updateTab(tabId) { tab ->
            val newHistory: List<String>
            val newIndex: Int
            if (tab.isHistoryNav || url.isBlank() || url == "about:blank") {
                // Đang thực hiện back/forward từ stack của chúng ta → không push vào navHistory
                newHistory = tab.navHistory
                newIndex = tab.navHistoryIndex
            } else if (
                tab.navHistoryIndex >= 0 &&
                sameDocumentUrl(tab.navHistory.getOrNull(tab.navHistoryIndex).orEmpty(), url)
            ) {
                // WebView thường gọi onPageFinished 2+ lần (redirect, SPA, subframe…).
                // Lần sau isHistoryNav đã false → nếu vẫn là đúng trang đang đứng, KHÔNG được
                // take(index+1) (sẽ cắt mất phần forward trong navHistory).
                newHistory = tab.navHistory
                newIndex = tab.navHistoryIndex
            } else {
                // Navigation thực sự → cắt bỏ phần forward rồi push URL mới
                val truncated = tab.navHistory.take((tab.navHistoryIndex + 1).coerceAtLeast(0))
                // Tránh push URL trùng liên tiếp (ví dụ: redirect cùng page)
                newHistory = if (truncated.lastOrNull()?.let { sameDocumentUrl(it, url) } == true) {
                    truncated
                } else {
                    truncated + url
                }
                newIndex = newHistory.size - 1
            }
            // Back/forward do lệnh của app: stack đúng theo navHistory (đã sync mỗi lần load).
            // Navigate thường: tính từ navHistory sau khi push/truncate (hoặc giữ nguyên nếu trùng finish).
            val newCanGoBack    = if (tab.isHistoryNav) tab.navHistoryIndex > 0 else newIndex > 0
            val newCanGoForward = if (tab.isHistoryNav) {
                tab.navHistoryIndex < tab.navHistory.size - 1
            } else {
                newIndex < newHistory.size - 1
            }
        tab.copy(
                url = url,
                title = title.ifBlank { url },
                isLoading = false,
                progress = 100,
                canGoBack = newCanGoBack,
                canGoForward = newCanGoForward,
                navHistory = newHistory,
                navHistoryIndex = newIndex,
                isHistoryNav = false  // reset sau khi page load xong
            )
        }
        val tabIsIncognito = tabs.firstOrNull { it.id == tabId }?.isIncognito == true
        if (!incognito && !tabIsIncognito && !isHistNav && url.isNotBlank() && url != "about:blank"
            && tabLastSavedUrl[tabId] != url) {
            tabLastSavedUrl[tabId] = url
            viewModelScope.launch {
                historyDao.upsert(url = url, title = title.ifBlank { url }, timestamp = System.currentTimeMillis())
            }
            saveTabs()
        }
    }

    fun onProgressChanged(tabId: String, progress: Int) = updateTab(tabId) { it.copy(progress = progress) }

    /** Sync lại trạng thái nav khi tab được focus trở lại — không dùng nữa, stack tự quản lý. */
    fun syncTabNavState(tabId: String, canGoBack: Boolean, canGoForward: Boolean) {
        // no-op: canGoBack/canGoForward được quản lý qua navHistory stack, không qua WebView native
    }

    fun onReceivedError(tabId: String, description: String) = updateTab(tabId) {
        it.copy(isLoading = false, error = description)
    }

    // ── Navigation commands ───────────────────────────────────────────────────

    fun navigateTo(rawInput: String) {
        val url = rawInput.trim().toFullUrl()
        // Dùng pendingLoadUrl (state-based) thay vì SharedFlow để tránh mất lệnh
        // khi WebView chưa kịp resume collecting (ví dụ: vừa quay từ HistoryScreen)
        updateTab(activeTabId) { it.copy(url = url, pendingLoadUrl = url, error = null) }
    }

    fun consumePendingLoad(tabId: String) =
        updateTab(tabId) { it.copy(pendingLoadUrl = null) }

    fun goBack() {
        val tab = activeTab ?: return
        if (!tab.canGoBack) return
        val prevIndex = (tab.navHistoryIndex - 1).coerceAtLeast(0)
        updateTab(tab.id) {
            it.copy(
                navHistoryIndex = prevIndex,
                isHistoryNav = true,
                error = null
            )
        }
        val fallbackUrl = tab.navHistory.getOrNull(prevIndex)
        viewModelScope.launch { _commands.emit(BrowserCommand.GoBack(fallbackUrl)) }
    }

    fun goForward() {
        val tab = activeTab ?: return
        if (!tab.canGoForward) return
        val nextIndex = (tab.navHistoryIndex + 1).coerceAtMost(tab.navHistory.size - 1)
        val fallbackUrl = tab.navHistory.getOrNull(nextIndex)
        updateTab(tab.id) {
            it.copy(
                navHistoryIndex = nextIndex,
                isHistoryNav = true,
                error = null
            )
        }
        viewModelScope.launch { _commands.emit(BrowserCommand.GoForward(fallbackUrl)) }
    }
    fun reload()    { viewModelScope.launch { _commands.emit(BrowserCommand.Reload) } }
    fun stop()      { viewModelScope.launch { _commands.emit(BrowserCommand.Stop) } }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun updateTab(id: String, transform: (TabState) -> TabState) {
        val idx = tabs.indexOfFirst { it.id == id }
        if (idx >= 0) tabs[idx] = transform(tabs[idx])
    }

    /** So khớp URL đã lưu với URL từ WebView (tránh cắt forward do lệch /, #, …). */
    private fun sameDocumentUrl(a: String, b: String): Boolean {
        if (a.isBlank() || b.isBlank()) return false
        if (a == b) return true
        fun norm(u: String) = u.trimEnd('/').substringBefore("#")
        return norm(a) == norm(b)
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
                BrowserViewModel(AppContainer.historyDao, AppContainer.bookmarkDao, AppContainer.tabSessionDao) as T
        }
    }
}
