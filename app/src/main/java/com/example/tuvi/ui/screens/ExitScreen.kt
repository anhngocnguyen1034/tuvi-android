package com.example.tuvi.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.graphics.drawable.toBitmap
import com.anhnn.exit.ExitAppHandler as LibExitAppHandler
import com.example.tuvi.R
import com.anhnn.ads.BannerAd
import com.anhnn.ads.NativeAd
import com.example.tuvi.ads.AdNames

/**
 * Wrapper mỏng quanh [com.anhnn.exit.ExitAppHandler] (thư viện `anhnn-components-exit`):
 * điền sẵn icon + tên app + text đã dịch + quảng cáo của app (banner trên cùng, native ở giữa).
 * Nhờ vậy call ở màn Home vẫn gọn 1 dòng: `ExitAppHandler { activity.finish() }`.
 */
@Composable
fun ExitAppHandler(onExit: () -> Unit) {
    val context = LocalContext.current
    // Icon launcher là adaptive-icon (XML) -> painterResource KHÔNG load được. Lấy drawable đã
    // render qua PackageManager rồi rasterize sang bitmap để hiển thị an toàn.
    val appIcon = remember(context) {
        runCatching {
            BitmapPainter(
                context.packageManager
                    .getApplicationIcon(context.packageName)
                    .toBitmap(width = 192, height = 192)
                    .asImageBitmap()
            )
        }.getOrNull()
    }

    LibExitAppHandler(
        onExit = onExit,
        appIcon = appIcon,
        appName = stringResource(R.string.app_name),
        title = stringResource(R.string.exit_dialog_title),
        message = stringResource(R.string.exit_dialog_message),
        confirmText = stringResource(R.string.exit_dialog_confirm),
        dismissText = stringResource(R.string.exit_dialog_dismiss),
        topContent = { BannerAd(adName = AdNames.EXIT_BANNER) },
        adContent = { NativeAd(adName = AdNames.EXIT_NATIVE) },
    )
}
