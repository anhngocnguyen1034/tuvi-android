package com.example.tuvi.ui.browser

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.net.Uri
import android.webkit.DownloadListener
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.flow.SharedFlow

/**
 * Tạo WebView với cấu hình phù hợp theo chế độ thường / ẩn danh.
 * Incognito: tắt cache, tắt form data, tắt domStorage để không rò rỉ dữ liệu.
 */
@SuppressLint("SetJavaScriptEnabled")
private fun createWebView(
    context: android.content.Context,
    config: BrowserConfig,
    isIncognito: Boolean
): WebView = WebView(context).apply {
    settings.apply {
        javaScriptEnabled = config.javaScriptEnabled
        loadWithOverviewMode = true
        useWideViewPort = true
        setSupportZoom(true)
        builtInZoomControls = true
        displayZoomControls = false
        mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
        config.userAgent?.let { userAgentString = it }

        if (isIncognito) {
            domStorageEnabled = false
            databaseEnabled = false
            cacheMode = WebSettings.LOAD_NO_CACHE
            saveFormData = false
            savePassword = false
        } else {
            domStorageEnabled = true
            cacheMode = WebSettings.LOAD_DEFAULT
        }
    }
    if (isIncognito) {
        clearCache(true)
        android.webkit.WebStorage.getInstance().deleteAllData()
    }
}

/**
 * Giữ và hiển thị một WebView cho một tab cụ thể.
 *
 * Hỗ trợ:
 * - Upload file/ảnh qua `<input type="file">` (onShowFileChooser)
 * - Download qua giữ lâu ảnh/link và qua web-triggered download
 *
 * @param isActive          chỉ tab active mới nhận và thực thi [commands].
 * @param isIncognito       tab ẩn danh: không cache, không lưu history, xóa cookie khi đóng.
 * @param onLongPressMedia  callback khi user giữ lâu trên ảnh/link — trả về URL để download.
 */
@SuppressLint("SetJavaScriptEnabled", "ClickableViewAccessibility")
@Composable
fun TabWebViewHolder(
    modifier: Modifier = Modifier,
    tabId: String,
    initialUrl: String,
    isActive: Boolean,
    isIncognito: Boolean = false,
    config: BrowserConfig,
    commands: SharedFlow<BrowserCommand>,
    onPageStarted: (url: String) -> Unit,
    onPageFinished: (url: String, title: String, canGoBack: Boolean, canGoForward: Boolean) -> Unit,
    onProgressChanged: (Int) -> Unit,
    onError: (String) -> Unit,
    onLongPressMedia: (url: String) -> Unit = {},
) {
    val context = LocalContext.current

    // ── File upload: giữ callback từ WebChromeClient để trả kết quả sau khi picker đóng ──
    // Dùng mutable ref vì WebChromeClient được tạo trong remember{} (non-Composable scope)
    val fileChooserCallbackRef = remember {
        object {
            var callback: android.webkit.ValueCallback<Array<Uri>>? = null
        }
    }

    // Launcher chọn nhiều file (ảnh + tài liệu đều được)
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        fileChooserCallbackRef.callback?.onReceiveValue(
            if (uris.isEmpty()) null else uris.toTypedArray()
        )
        fileChooserCallbackRef.callback = null
    }

    val webView = remember {
        createWebView(context, config, isIncognito).apply {
            webViewClient = object : WebViewClient() {
                override fun onPageStarted(view: WebView, url: String?, favicon: Bitmap?) {
                    onPageStarted(url ?: "")
                }
                override fun onPageFinished(view: WebView, url: String?) {
                    onPageFinished(url ?: "", view.title ?: "", view.canGoBack(), view.canGoForward())
                }
                override fun onReceivedError(
                    view: WebView,
                    request: WebResourceRequest?,
                    error: WebResourceError?
                ) {
                    if (request?.isForMainFrame == true)
                        onError(error?.description?.toString() ?: "Lỗi không xác định")
                }
            }

            webChromeClient = object : WebChromeClient() {
                override fun onProgressChanged(view: WebView, newProgress: Int) {
                    onProgressChanged(newProgress)
                }

                // ── Upload file/ảnh từ web ────────────────────────────────────
                override fun onShowFileChooser(
                    webView: WebView,
                    filePathCallback: android.webkit.ValueCallback<Array<Uri>>,
                    fileChooserParams: FileChooserParams
                ): Boolean {
                    // Nếu có callback cũ chưa xử lý → huỷ để tránh treo
                    fileChooserCallbackRef.callback?.onReceiveValue(null)
                    fileChooserCallbackRef.callback = filePathCallback

                    // Lấy MIME type mà web yêu cầu (vd: "image/*", "application/pdf")
                    val acceptTypes = fileChooserParams.acceptTypes
                    val mimeType = when {
                        acceptTypes.isNullOrEmpty() || acceptTypes.all { it.isBlank() } -> "*/*"
                        acceptTypes.size == 1 -> acceptTypes[0].ifBlank { "*/*" }
                        else -> acceptTypes.filter { it.isNotBlank() }.joinToString(",")
                    }
                    filePickerLauncher.launch(mimeType)
                    return true
                }
            }

            // ── Download: web trigger (Content-Disposition attachment, blob…) ──
            setDownloadListener(DownloadListener { url, _, _, _, _ ->
                onLongPressMedia(url)
            })

            // ── Long-press trên ảnh / link ────────────────────────────────────
            setOnLongClickListener {
                val result = hitTestResult
                val mediaUrl = when (result.type) {
                    WebView.HitTestResult.IMAGE_TYPE,
                    WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE -> result.extra
                    WebView.HitTestResult.SRC_ANCHOR_TYPE        -> result.extra
                    else -> null
                }
                if (!mediaUrl.isNullOrBlank()) {
                    onLongPressMedia(mediaUrl)
                    true
                } else {
                    false
                }
            }

            loadUrl(initialUrl)
        }
    }

    // Chỉ tab active mới collect và thực thi commands
    LaunchedEffect(isActive) {
        if (isActive) {
            try {
                commands.collect { cmd ->
                    when (cmd) {
                        is BrowserCommand.LoadUrl   -> webView.loadUrl(cmd.url)
                        is BrowserCommand.GoBack    -> if (webView.canGoBack()) webView.goBack()
                        is BrowserCommand.GoForward -> if (webView.canGoForward()) webView.goForward()
                        is BrowserCommand.Reload    -> webView.reload()
                        is BrowserCommand.Stop      -> webView.stopLoading()
                    }
                }
            } catch (_: Exception) {
                // Flow bị huỷ hoặc WebView đã destroy — bỏ qua
            }
        }
    }

    // Giải phóng bộ nhớ khi tab bị đóng (composable rời khỏi tree)
    DisposableEffect(tabId) {
        onDispose {
            // Huỷ file chooser callback nếu còn treo
            fileChooserCallbackRef.callback?.onReceiveValue(null)
            fileChooserCallbackRef.callback = null

            if (isIncognito) {
                webView.clearCache(true)
                webView.clearHistory()
                webView.clearFormData()
                android.webkit.CookieManager.getInstance().removeSessionCookies(null)
            }
            webView.destroy()
        }
    }

    AndroidView(factory = { webView }, modifier = modifier)
}
