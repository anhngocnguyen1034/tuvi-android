package com.example.tuvi.ui.theme

import androidx.compose.ui.graphics.Color

// --- Tử Vi Palette ---
// Background / surfaces
val TuViNavy       = Color(0xFF0D1B3E)   // deep night sky
val TuViNavyLight  = Color(0xFF162450)   // card surface
val TuViNavyCard   = Color(0xFF1C2D5E)   // elevated card

// Primary accent – warm gold
val TuViGold       = Color(0xFFD4A843)
val TuViGoldLight  = Color(0xFFF5CB6A)
val TuViGoldDark   = Color(0xFFAA8020)

// Secondary accent – silk crimson
val TuViRed        = Color(0xFFC0392B)
val TuViRedLight   = Color(0xFFE57369)

// Text / neutral
val TuViIvory      = Color(0xFFF5EED8)   // primary text
val TuViIvoryDim   = Color(0xFFC5BAA0)   // secondary / hint text
val TuViDivider    = Color(0xFF2E4080)

// Legacy references kept so Theme.kt still compiles
val Purple80       = TuViGoldLight
val PurpleGrey80   = TuViIvoryDim
val Pink80         = TuViRedLight
val Purple40       = TuViGold
val PurpleGrey40   = TuViNavyLight
val Pink40         = TuViRed