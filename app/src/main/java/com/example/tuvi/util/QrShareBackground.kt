package com.example.tuvi.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader

/**
 * Một "mẫu" ảnh nền cho QR chia sẻ. QR luôn được đặt chính giữa ảnh nền.
 *
 * - [Gradient]: nền gradient dựng sẵn trong code (luôn có, không cần file).
 * - [Asset]: ảnh nền do người dùng bỏ vào `assets/qr_backgrounds/`.
 */
sealed interface QrShareBackground {
    val id: String

    data class Gradient(
        override val id: String,
        val colors: IntArray,
    ) : QrShareBackground

    data class Asset(
        override val id: String,
        /** Đường dẫn tương đối trong thư mục assets, vd `qr_backgrounds/bg1.png`. */
        val assetPath: String,
    ) : QrShareBackground
}

private const val QR_BG_DIR = "qr_backgrounds"
private val IMAGE_EXTS = listOf(".png", ".jpg", ".jpeg", ".webp")

/**
 * Danh sách mẫu nền: ảnh trong assets (nếu có) đứng trước, rồi tới các nền gradient dựng sẵn
 * để tính năng luôn hoạt động kể cả khi chưa thêm ảnh nào.
 */
fun loadQrShareBackgrounds(context: Context): List<QrShareBackground> {
    val assets = runCatching {
        context.assets.list(QR_BG_DIR)
            ?.filter { name -> IMAGE_EXTS.any { name.lowercase().endsWith(it) } }
            ?.sorted()
            ?.map { QrShareBackground.Asset(id = it, assetPath = "$QR_BG_DIR/$it") }
            ?: emptyList()
    }.getOrDefault(emptyList())

    val gradients = listOf(
        QrShareBackground.Gradient("navy_gold", intArrayOf(0xFF1B1F3B.toInt(), 0xFF3A2F5B.toInt())),
        QrShareBackground.Gradient("royal", intArrayOf(0xFF4B4EEE.toInt(), 0xFFA1A2FF.toInt())),
        QrShareBackground.Gradient("sunset", intArrayOf(0xFFB56576.toInt(), 0xFF6D597A.toInt())),
        QrShareBackground.Gradient("emerald", intArrayOf(0xFF0F3443.toInt(), 0xFF34E89E.toInt())),
    )

    return assets + gradients
}

/**
 * Ghép QR (nội dung [qrContent]) vào giữa mẫu nền [background], trả về [Bitmap] vuông [sizePx].
 * QR được đặt trên một thẻ trắng bo góc để đảm bảo luôn quét được.
 */
fun composeQrShareImage(
    context: Context,
    background: QrShareBackground,
    qrContent: String,
    sizePx: Int = 1080,
): Bitmap {
    val result = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(result)

    // 1) Vẽ nền
    when (background) {
        is QrShareBackground.Gradient -> {
            val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                shader = LinearGradient(
                    0f, 0f, sizePx.toFloat(), sizePx.toFloat(),
                    background.colors, null, Shader.TileMode.CLAMP
                )
            }
            canvas.drawRect(0f, 0f, sizePx.toFloat(), sizePx.toFloat(), paint)
        }

        is QrShareBackground.Asset -> {
            val bg = runCatching {
                context.assets.open(background.assetPath).use { BitmapFactory.decodeStream(it) }
            }.getOrNull()
            if (bg != null) {
                drawCenterCrop(canvas, bg, sizePx)
                bg.recycle()
            } else {
                canvas.drawColor(Color.DKGRAY)
            }
        }
    }

    // 2) Thẻ trắng bo góc ở giữa
    val cardSize = sizePx * 0.52f
    val cardLeft = (sizePx - cardSize) / 2f
    val cardTop = (sizePx - cardSize) / 2f
    val cardRect = RectF(cardLeft, cardTop, cardLeft + cardSize, cardTop + cardSize)
    val radius = cardSize * 0.10f
    // đổ bóng nhẹ cho thẻ
    val cardPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        setShadowLayer(sizePx * 0.02f, 0f, sizePx * 0.008f, 0x55000000)
    }
    canvas.drawRoundRect(cardRect, radius, radius, cardPaint)

    // 3) Vẽ QR trong thẻ
    val qrPadding = cardSize * 0.08f
    val qrSize = (cardSize - qrPadding * 2).toInt().coerceAtLeast(1)
    val qr = generateQrBitmap(qrContent, sizePx = qrSize)
    if (qr != null) {
        val scaled = if (qr.width != qrSize) {
            Bitmap.createScaledBitmap(qr, qrSize, qrSize, false)
        } else qr
        canvas.drawBitmap(scaled, cardLeft + qrPadding, cardTop + qrPadding, null)
        if (scaled !== qr) scaled.recycle()
        qr.recycle()
    }

    return result
}

/** Vẽ [src] lấp đầy khung vuông [size] theo kiểu center-crop. */
private fun drawCenterCrop(canvas: Canvas, src: Bitmap, size: Int) {
    val scale = maxOf(size.toFloat() / src.width, size.toFloat() / src.height)
    val drawW = src.width * scale
    val drawH = src.height * scale
    val left = (size - drawW) / 2f
    val top = (size - drawH) / 2f
    val dst = RectF(left, top, left + drawW, top + drawH)
    canvas.drawBitmap(src, Rect(0, 0, src.width, src.height), dst, Paint(Paint.FILTER_BITMAP_FLAG))
}
