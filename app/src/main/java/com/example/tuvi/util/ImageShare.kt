package com.example.tuvi.util

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

/**
 * Lưu [bitmap] ra file PNG trong cache rồi mở trình chia sẻ hệ thống (ACTION_SEND, image/png).
 * [text] (nếu có) được đính kèm làm nội dung văn bản.
 */
fun shareBitmap(context: Context, bitmap: Bitmap, text: String? = null) {
    val dir = File(context.cacheDir, "shared_qr").apply { mkdirs() }
    val file = File(dir, "tuvi_qr.png")
    FileOutputStream(file).use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }

    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "image/png"
        putExtra(Intent.EXTRA_STREAM, uri)
        if (!text.isNullOrBlank()) putExtra(Intent.EXTRA_TEXT, text)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, null).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    })
}
