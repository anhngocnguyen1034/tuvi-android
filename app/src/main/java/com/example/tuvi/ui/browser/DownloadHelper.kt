package com.example.tuvi.ui.browser

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.webkit.MimeTypeMap
import android.webkit.URLUtil

/**
 * Wrapper tiện lợi cho Android DownloadManager.
 * Hỗ trợ tải file thông thường (từ setDownloadListener) và tải ảnh (từ long-press).
 *
 * @return tên file đã được lên lịch tải (để hiển thị toast), hoặc null nếu thất bại.
 */
fun enqueueDownload(
    context: Context,
    url: String,
    userAgent: String = "Mozilla/5.0",
    contentDisposition: String = "",
    mimeType: String = ""
): String? {
    return try {
        val fileName = URLUtil.guessFileName(url, contentDisposition, mimeType)
            .ifBlank { "download_${System.currentTimeMillis()}" }

        val request = DownloadManager.Request(Uri.parse(url)).apply {
            setTitle(fileName)
            setDescription(context.getString(com.example.tuvi.R.string.browser_download_desc))
            addRequestHeader("User-Agent", userAgent)
            setNotificationVisibility(
                DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED
            )
            setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
            // Cho phép tải qua cả WiFi và Mobile data
            setAllowedNetworkTypes(
                DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE
            )
        }

        val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        dm.enqueue(request)
        fileName
    } catch (_: Exception) {
        null
    }
}

/** Lấy MIME type từ đuôi file URL (dùng khi tải ảnh qua long-press) */
fun guessMimeFromUrl(url: String): String {
    val ext = MimeTypeMap.getFileExtensionFromUrl(url)
    return MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext) ?: "image/*"
}
