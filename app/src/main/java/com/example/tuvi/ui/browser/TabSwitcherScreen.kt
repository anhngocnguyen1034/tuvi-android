package com.example.tuvi.ui.browser

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tuvi.ui.theme.TuViGold
import com.example.tuvi.ui.theme.TuViGoldDark
import com.example.tuvi.ui.theme.TuViGoldLight
import com.example.tuvi.ui.theme.TuViIvory
import com.example.tuvi.ui.theme.TuViIvoryDim
import com.example.tuvi.ui.theme.TuViNavy
import com.example.tuvi.ui.theme.TuViNavyCard
import com.example.tuvi.ui.theme.TuViNavyLight

/**
 * Màn hình quản lý tab dạng lưới 2 cột (Chrome-style).
 * Render như một overlay fullscreen với AnimatedVisibility.
 */
@Composable
fun TabSwitcherOverlay(
    visible: Boolean,
    tabs: List<TabState>,
    activeTabId: String,
    onSelectTab: (String) -> Unit,
    onCloseTab: (String) -> Unit,
    onNewTab: () -> Unit,
    onDismiss: () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit  = slideOutVertically(targetOffsetY = { it }) + fadeOut()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(TuViNavy)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // ── Header ──
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${tabs.size} tab",
                        color = TuViGold,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Đóng", tint = TuViIvoryDim)
                    }
                }

                // ── Grid tabs ──
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(tabs, key = { it.id }) { tab ->
                        TabCard(
                            tab = tab,
                            isActive = tab.id == activeTabId,
                            onSelect = { onSelectTab(tab.id) },
                            onClose  = { onCloseTab(tab.id) }
                        )
                    }
                }

                // ── Bottom bar: nút + Thẻ mới ──
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(TuViNavyCard)
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FloatingActionButton(
                        onClick = onNewTab,
                        containerColor = TuViGold,
                        contentColor = TuViNavy,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth(0.7f).height(50.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(20.dp))
                            Text("Thẻ mới", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TabCard(
    tab: TabState,
    isActive: Boolean,
    onSelect: () -> Unit,
    onClose: () -> Unit
) {
    val borderColor = if (isActive) TuViGold else TuViNavyCard
    val borderWidth = if (isActive) 2.dp else 1.dp

    Box(
        modifier = Modifier
            .aspectRatio(0.75f)
            .shadow(if (isActive) 8.dp else 3.dp, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .background(TuViNavyLight)
            .border(borderWidth, borderColor, RoundedCornerShape(12.dp))
            .clickable { onSelect() }
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // ── Thumbnail area ──
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(
                        Brush.verticalGradient(
                            listOf(TuViNavyCard, TuViNavy)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Favicon placeholder + domain
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = tab.url.toFavicon(),
                        fontSize = 32.sp
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = tab.url.toDomain(),
                        color = TuViIvoryDim,
                        fontSize = 10.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }

                // Loading indicator
                if (tab.isLoading) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .fillMaxWidth()
                            .height(2.dp)
                            .background(TuViGold.copy(alpha = 0.6f))
                    )
                }
            }

            // ── Tab title bar ──
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(TuViNavyCard)
                    .padding(start = 8.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = tab.title.ifBlank { tab.url.toDomain() },
                    color = if (isActive) TuViGoldLight else TuViIvory,
                    fontSize = 11.sp,
                    fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                // [X] close
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(TuViNavyLight)
                        .clickable { onClose() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Đóng tab",
                        tint = TuViIvoryDim,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }
    }
}

/** Lấy emoji favicon đại diện từ domain */
private fun String.toFavicon(): String {
    val domain = toDomain().lowercase()
    return when {
        domain.contains("google")    -> "🔍"
        domain.contains("youtube")   -> "▶"
        domain.contains("facebook")  -> "📘"
        domain.contains("wikipedia") -> "📖"
        domain.contains("github")    -> "🐙"
        domain.contains("reddit")    -> "🔴"
        domain.contains("twitter") || domain.contains("x.com") -> "𝕏"
        else -> "🌐"
    }
}

private fun String.toDomain(): String {
    return try {
        android.net.Uri.parse(this).host?.removePrefix("www.") ?: this
    } catch (_: Exception) { this }
}
