package com.example.tuvi.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader
import androidx.annotation.DrawableRes
import com.example.tuvi.R

/**
 * Một "mẫu" ảnh nền cho QR chia sẻ. QR luôn được đặt chính giữa ảnh nền.
 *
 * - [Gradient]: nền gradient dựng sẵn trong code (luôn có, không cần file).
 * - [Asset]: ảnh nền do người dùng bỏ vào `assets/qr_backgrounds/`.
 * - [Drawable]: ảnh nền đóng gói sẵn trong `res/drawable` (vd qr1..qr5).
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

    data class Drawable(
        override val id: String,
        @param:DrawableRes val resId: Int,
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

    val drawables = listOf(
        QrShareBackground.Drawable("qr1", R.drawable.qr1),
        QrShareBackground.Drawable("qr2", R.drawable.qr2),
        QrShareBackground.Drawable("qr3", R.drawable.qr3),
        QrShareBackground.Drawable("qr4", R.drawable.qr4),
        QrShareBackground.Drawable("qr5", R.drawable.qr5),
    )

    val gradients = listOf(
        QrShareBackground.Gradient("navy_gold", intArrayOf(0xFF1B1F3B.toInt(), 0xFF3A2F5B.toInt())),
        QrShareBackground.Gradient("royal", intArrayOf(0xFF4B4EEE.toInt(), 0xFFA1A2FF.toInt())),
        QrShareBackground.Gradient("sunset", intArrayOf(0xFFB56576.toInt(), 0xFF6D597A.toInt())),
        QrShareBackground.Gradient("emerald", intArrayOf(0xFF0F3443.toInt(), 0xFF34E89E.toInt())),
    )

    return drawables + assets + gradients
}

/** Độ bão hoà còn giữ của ảnh nền (0 = xám hoàn toàn, 1 = giữ nguyên). */
private const val BG_SATURATION = 0.35f

/** Lớp trắng phủ lên nền cho ảnh "chìm" hẳn xuống (~0.68 → ảnh còn hiện ~32%). */
private const val BG_FADE_OVERLAY = 0xADFFFFFF.toInt()

/** Bề rộng QR so với cạnh ảnh — QR trải gần kín, chỉ chừa lề (quiet zone). */
private const val QR_COVERAGE = 0.82f

/**
 * Ghép QR (nội dung [qrContent]) lên mẫu nền [background], trả về [Bitmap] vuông [sizePx].
 *
 * Kiểu "nền chìm": ảnh nền được giảm bão hoà + phủ lớp trắng cho nhạt hẳn đi, rồi các điểm mã
 * QR màu tối (nền QR trong suốt) xếp chồng phủ gần kín ảnh — ảnh chìm vẫn hiện qua các ô sáng,
 * tạo cảm giác hoà trộn. Hợp với ảnh phong cảnh / sản phẩm / gradient.
 */
fun composeQrShareImage(
    context: Context,
    background: QrShareBackground,
    qrContent: String,
    sizePx: Int = 1080,
): Bitmap {
    val result = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(result)

    // 1) Vẽ nền (đã giảm bão hoà cho ảnh)
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
                drawCenterCrop(canvas, bg, sizePx, desaturate = true)
                bg.recycle()
            } else {
                canvas.drawColor(Color.DKGRAY)
            }
        }

        is QrShareBackground.Drawable -> {
            val bg = runCatching {
                BitmapFactory.decodeResource(context.resources, background.resId)
            }.getOrNull()
            if (bg != null) {
                drawCenterCrop(canvas, bg, sizePx, desaturate = true)
                bg.recycle()
            } else {
                canvas.drawColor(Color.DKGRAY)
            }
        }
    }

    // 2) Phủ lớp trắng cho nền "chìm" hẳn — QR tối phía trên sẽ nổi rõ, đủ tương phản để quét
    canvas.drawColor(BG_FADE_OVERLAY)

    // 3) QR màu tối, nền QR trong suốt, phủ gần kín ảnh → nền chìm hiện qua các ô sáng
    val qrSize = (sizePx * QR_COVERAGE).toInt().coerceAtLeast(1)
    val qr = generateQrBitmap(
        qrContent,
        sizePx = qrSize,
        foreground = Color.BLACK,
        background = Color.TRANSPARENT,
    )
    if (qr != null) {
        val scaled = if (qr.width != qrSize) {
            Bitmap.createScaledBitmap(qr, qrSize, qrSize, false)
        } else qr
        val offset = (sizePx - qrSize) / 2f
        canvas.drawBitmap(scaled, offset, offset, null)
        if (scaled !== qr) scaled.recycle()
        qr.recycle()
    }

    return result
}

/**
 * Vẽ [src] lấp đầy khung vuông [size] theo kiểu center-crop.
 * [desaturate] = true thì giảm bão hoà ảnh (dùng cho nền chìm).
 */
private fun drawCenterCrop(canvas: Canvas, src: Bitmap, size: Int, desaturate: Boolean = false) {
    val scale = maxOf(size.toFloat() / src.width, size.toFloat() / src.height)
    val drawW = src.width * scale
    val drawH = src.height * scale
    val left = (size - drawW) / 2f
    val top = (size - drawH) / 2f
    val dst = RectF(left, top, left + drawW, top + drawH)
    val paint = Paint(Paint.FILTER_BITMAP_FLAG).apply {
        if (desaturate) {
            colorFilter = ColorMatrixColorFilter(ColorMatrix().apply { setSaturation(BG_SATURATION) })
        }
    }
    canvas.drawBitmap(src, Rect(0, 0, src.width, src.height), dst, paint)
}
