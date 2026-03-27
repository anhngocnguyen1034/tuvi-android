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

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebViewContainer(
    modifier: Modifier = Modifier,
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
                    onPageFinished(
                        url ?: "",
                        view.title ?: "",
                        view.canGoBack(),
                        view.canGoForward()
                    )
                }

                override fun onReceivedError(
                    view: WebView,
                    request: WebResourceRequest?,
                    error: WebResourceError?
                ) {
                    if (request?.isForMainFrame == true) {
                        onError(error?.description?.toString() ?: "Lỗi không xác định")
                    }
                }
            }

            webChromeClient = object : WebChromeClient() {
                override fun onProgressChanged(view: WebView, newProgress: Int) {
                    onProgressChanged(newProgress)
                }
            }

            loadUrl(config.initialUrl)
        }
    }

    // Nhận lệnh từ ViewModel và thực thi trên WebView
    LaunchedEffect(commands) {
        commands.collect { cmd ->
            when (cmd) {
                is BrowserCommand.LoadUrl  -> webView.loadUrl(cmd.url)
                is BrowserCommand.GoBack   -> if (webView.canGoBack()) webView.goBack()
                is BrowserCommand.GoForward -> if (webView.canGoForward()) webView.goForward()
                is BrowserCommand.Reload   -> webView.reload()
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose { webView.destroy() }
    }

    AndroidView(factory = { webView }, modifier = modifier)
}
