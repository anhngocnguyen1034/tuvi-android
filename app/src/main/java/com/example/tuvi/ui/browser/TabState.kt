package com.example.tuvi.ui.browser

import java.util.UUID

/**
 * Trạng thái của một tab riêng lẻ.
 * WebView thực tế được tạo & giữ bên ngoài ViewModel (trong Composable) để tránh
 * memory leak — ViewModel chỉ giữ metadata và không có reference đến Context/View.
 */
data class TabState(
    val id: String = UUID.randomUUID().toString(),
    val url: String = "https://www.google.com",
    val title: String = "Tab mới",
    val progress: Int = 0,
    val isLoading: Boolean = false,
    val canGoBack: Boolean = false,
    val canGoForward: Boolean = false,
    val error: String? = null,
    val isIncognito: Boolean = false
)
