package com.example.tuvi.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

// Tử Vi always-dark scheme
private val TuViColorScheme = darkColorScheme(
    primary          = TuViGold,
    onPrimary        = TuViNavy,
    primaryContainer = TuViNavyCard,
    secondary        = TuViRed,
    onSecondary      = TuViIvory,
    background       = TuViNavy,
    onBackground     = TuViIvory,
    surface          = TuViNavyLight,
    onSurface        = TuViIvory,
    secondaryContainer = TuViNavyCard,
    onSecondaryContainer = TuViGoldLight,
    outline          = TuViDivider
)

@Composable
fun TuViTheme(
    darkTheme: Boolean = true,          // tử vi is always dark
    dynamicColor: Boolean = false,      // disable dynamic color – use our palette
    content: @Composable () -> Unit
) {
    val colorScheme = TuViColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}