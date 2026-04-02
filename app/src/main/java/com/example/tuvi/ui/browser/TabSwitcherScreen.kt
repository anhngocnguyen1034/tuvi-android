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
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
import com.example.tuvi.ui.theme.TuViRed

// ── Incognito palette ──────────────────────────────────────────────────────────
private val IncogBg       = Color(0xFF0D0D0D)
private val IncogCard     = Color(0xFF1C1C1C)
private val IncogAccent   = Color(0xFF9E9E9E)
private val IncogSelected = Color(0xFFE0E0E0)
private val IncogDim      = Color(0xFF757575)

/**
 * Màn hình quản lý tab dạng lưới 2 cột (Chrome-style).
 * Hỗ trợ 2 panel: "Thường" và "Ẩn danh".
 */
@Composable
fun TabSwitcherOverlay(
    visible: Boolean,
    tabs: List<TabState>,
    activeTabId: String,
    showIncognitoList: Boolean,
    onSelectTab: (String) -> Unit,
    onCloseTab: (String) -> Unit,
    onNewTab: () -> Unit,
    onNewIncognitoTab: () -> Unit,
    onCloseAllIncognito: () -> Unit,
    onSwitchPanel: (Boolean) -> Unit,
    onDismiss: () -> Unit,
    onOpenBookmarks: () -> Unit = {}
) {
    val normalTabs    = tabs.filter { !it.isIncognito }
    val incognitoTabs = tabs.filter { it.isIncognito }

    val bg       = if (showIncognitoList) IncogBg    else TuViNavy
    val cardBg   = if (showIncognitoList) IncogCard  else TuViNavyCard
    val accent   = if (showIncognitoList) IncogSelected else TuViGold

    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit  = slideOutVertically(targetOffsetY = { it }) + fadeOut()
    ) {
        Box(modifier = Modifier.fillMaxSize().background(bg)) {
            Column(modifier = Modifier.fillMaxSize()) {

                // ── Panel selector ──
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(cardBg)
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    PanelTab(
                        label = "${normalTabs.size} Thường",
                        selected = !showIncognitoList,
                        accent = TuViGold,
                        onClick = { onSwitchPanel(false) }
                    )
                    PanelTab(
                        label = "${incognitoTabs.size} Ẩn danh \uD83D\uDD75\uFE0F",
                        selected = showIncognitoList,
                        accent = IncogSelected,
                        onClick = { onSwitchPanel(true) }
                    )
                }

                // ── Header ──
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (showIncognitoList) "Chế độ ẩn danh" else "Tab đang mở",
                        color = accent,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    if (showIncognitoList && incognitoTabs.isNotEmpty()) {
                        TextButton(onClick = onCloseAllIncognito) {
                            Text("Đóng tất cả", color = TuViRed, fontSize = 13.sp)
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Đóng", tint = TuViIvoryDim)
                    }
                }

                // ── Grid tabs ──
                val displayedTabs = if (showIncognitoList) incognitoTabs else normalTabs
                if (displayedTabs.isEmpty()) {
                    Box(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = if (showIncognitoList) "\uD83D\uDD75\uFE0F" else "🌐",
                                fontSize = 48.sp
                            )
                            Spacer(Modifier.height(12.dp))
                            Text(
                                text = if (showIncognitoList) "Không có tab ẩn danh nào"
                                       else "Không có tab nào",
                                color = if (showIncognitoList) IncogDim else TuViIvoryDim,
                                fontSize = 14.sp
                            )
                        }
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(displayedTabs, key = { it.id }) { tab ->
                            TabCard(
                                tab = tab,
                                isActive = tab.id == activeTabId,
                                isIncognito = showIncognitoList,
                                onSelect = { onSelectTab(tab.id) },
                                onClose  = { onCloseTab(tab.id) }
                            )
                        }
                    }
                }

                // ── Bottom bar ──
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(cardBg)
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FloatingActionButton(
                        onClick = if (showIncognitoList) onNewIncognitoTab else onNewTab,
                        containerColor = accent,
                        contentColor = if (showIncognitoList) IncogBg else TuViNavy,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.weight(1f).height(50.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(20.dp))
                            Text(
                                if (showIncognitoList) "Tab ẩn danh mới" else "Thẻ mới",
                                fontWeight = FontWeight.Bold, fontSize = 14.sp
                            )
                        }
                    }
                    if (!showIncognitoList) {
                        IconButton(onClick = onOpenBookmarks) {
                            Icon(Icons.Default.Star, contentDescription = "Dấu trang", tint = TuViGold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PanelTab(label: String, selected: Boolean, accent: Color, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            color = if (selected) accent else TuViIvoryDim,
            fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
        )
        Spacer(Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(2.dp)
                .background(if (selected) accent else Color.Transparent)
        )
    }
}

@Composable
private fun TabCard(
    tab: TabState,
    isActive: Boolean,
    isIncognito: Boolean,
    onSelect: () -> Unit,
    onClose: () -> Unit
) {
    val borderColor = if (isActive) {
        if (isIncognito) IncogSelected else TuViGold
    } else {
        if (isIncognito) IncogCard else TuViNavyCard
    }
    val borderWidth = if (isActive) 2.dp else 1.dp
    val bgColor     = if (isIncognito) IncogCard else TuViNavyLight
    val gradFrom    = if (isIncognito) IncogCard else TuViNavyCard
    val gradTo      = if (isIncognito) IncogBg   else TuViNavy

    Box(
        modifier = Modifier
            .aspectRatio(0.75f)
            .shadow(if (isActive) 8.dp else 3.dp, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .border(borderWidth, borderColor, RoundedCornerShape(12.dp))
            .clickable { onSelect() }
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // ── Thumbnail area ──
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Brush.verticalGradient(listOf(gradFrom, gradTo))),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (isIncognito) "\uD83D\uDD75\uFE0F" else tab.url.toFavicon(),
                        fontSize = 32.sp
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = tab.url.toDomain(),
                        color = if (isIncognito) IncogAccent else TuViIvoryDim,
                        fontSize = 10.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }
                if (tab.isLoading) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .fillMaxWidth()
                            .height(2.dp)
                            .background(
                                (if (isIncognito) IncogSelected else TuViGold).copy(alpha = 0.6f)
                            )
                    )
                }
            }

            // ── Tab title bar ──
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(if (isIncognito) IncogCard else TuViNavyCard)
                    .padding(start = 8.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = tab.title.ifBlank { tab.url.toDomain() },
                    color = if (isActive) {
                        if (isIncognito) IncogSelected else TuViGoldLight
                    } else {
                        if (isIncognito) IncogAccent else TuViIvory
                    },
                    fontSize = 11.sp,
                    fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(if (isIncognito) IncogBg else TuViNavyLight)
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
