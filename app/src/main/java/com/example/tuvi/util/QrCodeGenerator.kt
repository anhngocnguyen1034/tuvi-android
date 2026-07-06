package com.example.tuvi.util

import android.graphics.Bitmap
import android.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel

/**
 * Tạo QR code (offline, không cần mạng) từ [content] — dùng để giới thiệu app cho
 * người dùng khác quét tải về. Trả về [ImageBitmap] để hiển thị trực tiếp trong Compose,
 * hoặc null nếu mã hoá thất bại.
 */
fun generateQrCode(
    content: String,
    sizePx: Int = 640,
    foreground: Int = Color.BLACK,
    background: Int = Color.WHITE,
): ImageBitmap? {
    if (content.isBlank()) return null
    return try {
        val hints = mapOf(
            EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.M,
            EncodeHintType.MARGIN to 1,
            EncodeHintType.CHARACTER_SET to "UTF-8",
        )
        val matrix = QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, sizePx, sizePx, hints)
        val width = matrix.width
        val height = matrix.height
        val pixels = IntArray(width * height)
        for (y in 0 until height) {
            val offset = y * width
            for (x in 0 until width) {
                pixels[offset + x] = if (matrix.get(x, y)) foreground else background
            }
        }
        Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888).apply {
            setPixels(pixels, 0, width, 0, 0, width, height)
        }.asImageBitmap()
    } catch (e: Exception) {
        null
    }
}
