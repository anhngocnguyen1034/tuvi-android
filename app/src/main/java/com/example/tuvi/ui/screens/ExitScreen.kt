package com.example.tuvi.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.anhnn.exit.ExitAppHandler as LibExitAppHandler
import com.example.tuvi.R
import com.example.tuvi.ads.AdNames
import com.example.tuvi.ads.BannerAdView
import com.example.tuvi.ads.NativeAdCard

/**
 * Wrapper mỏng quanh [com.anhnn.exit.ExitAppHandler] (thư viện `anhnn-components-exit`):
 * điền sẵn icon + tên app + text đã dịch + quảng cáo của app (banner trên cùng, native ở giữa).
 * Nhờ vậy call ở màn Home vẫn gọn 1 dòng: `ExitAppHandler { activity.finish() }`.
 */
@Composable
fun ExitAppHandler(onExit: () -> Unit) {
    LibExitAppHandler(
        onExit = onExit,
        appIcon = painterResource(R.mipmap.ic_launcher),
        appName = stringResource(R.string.app_name),
        title = stringResource(R.string.exit_dialog_title),
        message = stringResource(R.string.exit_dialog_message),
        confirmText = stringResource(R.string.exit_dialog_confirm),
        dismissText = stringResource(R.string.exit_dialog_dismiss),
        topContent = { BannerAdView(adName = AdNames.EXIT_BANNER) },
        adContent = { NativeAdCard(adName = AdNames.EXIT_NATIVE) },
    )
}
