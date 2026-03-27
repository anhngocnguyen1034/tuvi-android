package com.example.tuvi.ui.browser

/**
 * Cấu hình đầu vào của BrowserModule.
 * Truyền vào khi navigate đến BrowserScreen từ bất kỳ màn hình nào.
 */
data class BrowserConfig(
    val initialUrl: String,
    val title: String = "Trình duyệt",
    val showAddressBar: Boolean = true,
    val allowUserNavigation: Boolean = true,
    val javaScriptEnabled: Boolean = true,
    val userAgent: String? = null
)
