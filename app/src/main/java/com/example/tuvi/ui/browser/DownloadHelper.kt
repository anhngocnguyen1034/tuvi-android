package com.example.tuvi.ui.browser

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.webkit.MimeTypeMap
import android.webkit.URLUtil

fun enqueueDownload(
    context: Context,
    url: String,
    userAgent: String = "Mozilla/5.0",
    contentDisposition: String = "",
    mimeType: String = ""
): String? {
    return try {
        val fileName = resolveFileName(url, contentDisposition, mimeType)

        val request = DownloadManager.Request(Uri.parse(url)).apply {
            setTitle(fileName)
            setDescription(context.getString(com.example.tuvi.R.string.browser_download_desc))
            addRequestHeader("User-Agent", userAgent)
            setNotificationVisibility(
                DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED
            )
            setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
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

private fun resolveFileName(url: String, contentDisposition: String, mimeType: String): String {
    // 1. Extract filename from URL path (most reliable for direct image URLs)
    val pathSegment = try {
        Uri.parse(url).lastPathSegment?.takeIf { '.' in it && !it.startsWith('.') }
    } catch (_: Exception) { null }
    if (pathSegment != null) return pathSegment

    // 2. URLUtil with actual (non-wildcard) MIME type
    val concreteMime = mimeType.takeIf { it.isNotBlank() && '*' !in it } ?: ""
    val guessed = URLUtil.guessFileName(url, contentDisposition, concreteMime)
    if (!guessed.endsWith(".bin") && guessed.isNotBlank()) return guessed

    // 3. Derive extension from MIME type, default to jpg for images
    val ext = when {
        concreteMime.isNotBlank() ->
            MimeTypeMap.getSingleton().getExtensionFromMimeType(concreteMime) ?: "jpg"
        mimeType.startsWith("image/") -> "jpg"
        else -> "bin"
    }
    return "download_${System.currentTimeMillis()}.$ext"
}

fun guessMimeFromUrl(url: String): String {
    val ext = MimeTypeMap.getFileExtensionFromUrl(url)
    return MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext) ?: ""
}
