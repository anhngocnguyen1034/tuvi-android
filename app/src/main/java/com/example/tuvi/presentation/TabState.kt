package com.example.tuvi.presentation

import androidx.compose.runtime.Immutable
import java.util.UUID

/**
 * Trạng thái của một tab riêng lẻ.
 * WebView thực tế được tạo & giữ bên ngoài ViewModel (trong Composable) để tránh
 * memory leak — ViewModel chỉ giữ metadata và không có reference đến Context/View.
 */
@Immutable
data class TabState(
    val id: String = UUID.randomUUID().toString(),
    val url: String = "https://www.google.com",
    val title: String = "",
    val progress: Int = 0,
    val isLoading: Boolean = false,
    val canGoBack: Boolean = false,
    val canGoForward: Boolean = false,
    val error: String? = null,
    val isIncognito: Boolean = false,
    /** URL cần load ngay (set bởi navigateTo/lịch sử/bookmark). Null = không có lệnh load mới. */
    val pendingLoadUrl: String? = null,
    /** Stack URL đã visit trong tab này — dùng để khôi phục back/forward sau khi app restart. */
    val navHistory: List<String> = emptyList(),
    /** Index hiện tại trong navHistory. -1 = chưa load trang nào. */
    val navHistoryIndex: Int = -1,
    /**
     * true khi navigation đang xảy ra do goBack()/goForward().
     * onPageFinished sẽ không push vào navHistory khi flag này bật.
     */
    val isHistoryNav: Boolean = false,
)
