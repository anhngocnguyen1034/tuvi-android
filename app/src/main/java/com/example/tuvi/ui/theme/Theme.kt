package com.example.tuvi.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

@Composable
fun TuViTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    TuViComposeColors.initIfNeeded(LocalContext.current.applicationContext)

    val colorScheme = remember(darkTheme) {
        if (darkTheme) {
            darkColorScheme(
                primary = TuViGold,
                onPrimary = TuViNavy,
                primaryContainer = TuViNavyCard,
                secondary = TuViRed,
                onSecondary = TuViIvory,
                background = TuViNavy,
                onBackground = TuViIvory,
                surface = TuViNavyLight,
                onSurface = TuViIvory,
                secondaryContainer = TuViNavyCard,
                onSecondaryContainer = TuViGoldLight,
                outline = TuViDivider
            )
        } else {
            lightColorScheme(
                primary = TuViGoldDark,
                onPrimary = TuViIvory,
                primaryContainer = TuViGoldLight.copy(alpha = 0.35f),
                secondary = TuViRed,
                onSecondary = TuViIvory,
                background = TuViNavy,
                onBackground = TuViIvory,
                surface = TuViNavyLight,
                onSurface = TuViIvory,
                secondaryContainer = TuViNavyCard,
                onSecondaryContainer = TuViGoldDark,
                outline = TuViDivider
            )
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
