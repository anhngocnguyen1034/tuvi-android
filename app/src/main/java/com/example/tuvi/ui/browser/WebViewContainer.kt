package com.example.tuvi.ui.browser

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.flow.SharedFlow

/**
 * Giữ và hiển thị một WebView cho một tab cụ thể.
 *
 * Vòng đời:
 * - `remember { WebView(context) }` giữ instance qua recompose miễn composable còn trong tree.
 * - Caller dùng `key(tabId)` → Compose preserve instance khi tab không thay đổi id.
 * - Khi tab bị xoá khỏi danh sách → composable bị dispose → `DisposableEffect` gọi `webView.destroy()`.
 * - Tab không active: modifier `Modifier.size(0.dp)` ẩn về mặt layout nhưng WebView vẫn tồn tại
 *   trong bộ nhớ, giữ nguyên scroll position và DOM state — không tải lại trang.
 *
 * @param isActive chỉ tab active mới nhận và thực thi [commands].
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun TabWebViewHolder(
    modifier: Modifier = Modifier,
    tabId: String,
    initialUrl: String,
    isActive: Boolean,
    config: BrowserConfig,
    commands: SharedFlow<BrowserCommand>,
    onPageStarted: (url: String) -> Unit,
    onPageFinished: (url: String, title: String, canGoBack: Boolean, canGoForward: Boolean) -> Unit,
    onProgressChanged: (Int) -> Unit,
    onError: (String) -> Unit
) {
    val context = LocalContext.current
    val webView = remember {
        WebView(context).apply {
            settings.apply {
                javaScriptEnabled = config.javaScriptEnabled
                domStorageEnabled = true
                loadWithOverviewMode = true
                useWideViewPort = true
                setSupportZoom(true)
                builtInZoomControls = true
                displayZoomControls = false
                mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
                config.userAgent?.let { userAgentString = it }
            }
            webViewClient = object : WebViewClient() {
                override fun onPageStarted(view: WebView, url: String?, favicon: Bitmap?) {
                    onPageStarted(url ?: "")
                }
                override fun onPageFinished(view: WebView, url: String?) {
                    onPageFinished(url ?: "", view.title ?: "", view.canGoBack(), view.canGoForward())
                }
                override fun onReceivedError(view: WebView, request: WebResourceRequest?, error: WebResourceError?) {
                    if (request?.isForMainFrame == true)
                        onError(error?.description?.toString() ?: "Lỗi không xác định")
                }
            }
            webChromeClient = object : WebChromeClient() {
                override fun onProgressChanged(view: WebView, newProgress: Int) {
                    onProgressChanged(newProgress)
                }
            }
            loadUrl(initialUrl)
        }
    }

    // Chỉ tab active mới collect và thực thi commands
    LaunchedEffect(isActive) {
        if (isActive) {
            commands.collect { cmd ->
                when (cmd) {
                    is BrowserCommand.LoadUrl   -> webView.loadUrl(cmd.url)
                    is BrowserCommand.GoBack    -> if (webView.canGoBack()) webView.goBack()
                    is BrowserCommand.GoForward -> if (webView.canGoForward()) webView.goForward()
                    is BrowserCommand.Reload    -> webView.reload()
                }
            }
        }
    }

    // Giải phóng bộ nhớ khi tab bị đóng (composable rời khỏi tree)
    DisposableEffect(tabId) {
        onDispose { webView.destroy() }
    }

    AndroidView(factory = { webView }, modifier = modifier)
}
