package com.example.tuvi.ui.browser

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
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
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.example.tuvi.presentation.TabState
import com.example.tuvi.R
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tuvi.ui.theme.IncognitoBg
import com.example.tuvi.ui.theme.IncognitoCard
import com.example.tuvi.ui.theme.IncognitoDimDark
import com.example.tuvi.ui.theme.IncognitoEmphasis
import com.example.tuvi.ui.theme.IncognitoMuted
import com.example.tuvi.ui.theme.TuViGold
import com.example.tuvi.ui.theme.TuViGoldDark
import com.example.tuvi.ui.theme.TuViGoldLight
import com.example.tuvi.ui.theme.TuViIvory
import com.example.tuvi.ui.theme.TuViIvoryDim
import com.example.tuvi.ui.theme.TuViNavy
import com.example.tuvi.ui.theme.TuViNavyCard
import com.example.tuvi.ui.theme.TuViNavyLight
import com.example.tuvi.ui.theme.TuViRed

/**
 * Màn hình quản lý tab dạng lưới 2 cột (Chrome-style).
 * Hỗ trợ 2 panel: "Thường" và "Ẩn danh".
 */
@Composable
fun TabSwitcherOverlay(
    visible: Boolean,
    tabs: List<TabState>,
    thumbnails: Map<String, ImageBitmap> = emptyMap(),
    activeTabId: String,
    showIncognitoList: Boolean,
    onSelectTab: (String) -> Unit,
    onCloseTab: (String) -> Unit,
    onNewTab: () -> Unit,
    onNewIncognitoTab: () -> Unit,
    onCloseAllIncognito: () -> Unit,
    onSwitchPanel: (Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    val normalTabs    = tabs.filter { !it.isIncognito }
    val incognitoTabs = tabs.filter { it.isIncognito }

    val bg       = if (showIncognitoList) IncognitoBg    else TuViNavy
    val cardBg   = if (showIncognitoList) IncognitoCard  else TuViNavyCard
    val accent   = if (showIncognitoList) IncognitoEmphasis else TuViGold

    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit  = slideOutVertically(targetOffsetY = { it }) + fadeOut()
    ) {
        Box(modifier = Modifier.fillMaxSize().background(bg).statusBarsPadding().navigationBarsPadding()) {
            Column(modifier = Modifier.fillMaxSize()) {
                // ── Header ──
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (showIncognitoList) stringResource(R.string.tab_switcher_title_incognito) else stringResource(R.string.tab_switcher_title_open),
                        color = accent,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    if (showIncognitoList && incognitoTabs.isNotEmpty()) {
                        TextButton(onClick = onCloseAllIncognito) {
                            Text(stringResource(R.string.tab_switcher_close_all), color = TuViRed, fontSize = 13.sp)
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(painter=painterResource(R.drawable.ic_close), contentDescription = stringResource(R.string.btn_close), tint = TuViIvoryDim)
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
                            if (showIncognitoList) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_incognito),
                                    contentDescription = null,
                                    tint = IncognitoEmphasis,
                                    modifier = Modifier.size(48.dp)
                                )
                            } else {
                                Text(
                                    text = stringResource(R.string.tab_switcher_web_icon),
                                    fontSize = 48.sp
                                )
                            }
                            Spacer(Modifier.height(12.dp))
                            Text(
                                text = if (showIncognitoList) stringResource(R.string.tab_switcher_empty_incognito)
                                       else stringResource(R.string.tab_switcher_empty),
                                color = if (showIncognitoList) IncognitoDimDark else TuViIvoryDim,
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
                                thumbnail = thumbnails[tab.id],
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
                        contentColor = if (showIncognitoList) IncognitoBg else TuViNavy,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.weight(1f).height(50.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(painter = painterResource(R.drawable.ic_add), contentDescription = null, modifier = Modifier.size(20.dp))
                            Text(
                                if (showIncognitoList) stringResource(R.string.tab_switcher_new_incognito) else stringResource(R.string.tab_switcher_new_tab),
                                fontWeight = FontWeight.Bold, fontSize = 14.sp
                            )
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
    thumbnail: ImageBitmap?,
    isActive: Boolean,
    isIncognito: Boolean,
    onSelect: () -> Unit,
    onClose: () -> Unit
) {
    val accent      = if (isIncognito) IncognitoEmphasis else TuViGold
    val headerBg    = if (isIncognito) IncognitoCard     else TuViNavyCard
    val contentBg   = if (isIncognito) IncognitoBg       else TuViNavy
    val borderColor = if (isActive) accent else Color.Transparent
    val titleColor  = if (isIncognito) IncognitoEmphasis else TuViIvory

    Box(
        modifier = Modifier
            .aspectRatio(0.65f)
            .shadow(if (isActive) 10.dp else 4.dp, RoundedCornerShape(14.dp))
            .clip(RoundedCornerShape(14.dp))
            .border(2.dp, borderColor, RoundedCornerShape(14.dp))
            .clickable { onSelect() }
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // ── Header (Chrome-style): favicon + title + close ──
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(headerBg)
                    .padding(start = 10.dp, end = 4.dp, top = 6.dp, bottom = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Favicon / incognito icon
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(accent.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    if (isIncognito) {
                        Icon(
                            painter = painterResource(R.drawable.ic_incognito),
                            contentDescription = null,
                            tint = accent,
                            modifier = Modifier.size(10.dp)
                        )
                    } else {
                        Text(
                            text = tab.url.toFaviconChar(),
                            fontSize = 8.sp,
                            color = accent,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(Modifier.size(6.dp))
                Text(
                    text = tab.title.ifBlank { tab.url.toDomain().ifBlank { stringResource(R.string.browser_tab_new_title) } },
                    color = titleColor,
                    fontSize = 11.sp,
                    fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                // Close button
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .clickable { onClose() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter=painterResource(R.drawable.ic_close),
                        contentDescription = stringResource(R.string.tab_cd_close),
                        tint = if (isIncognito) IncognitoMuted else TuViIvoryDim,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            // ── Thumbnail ──
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(contentBg),
                contentAlignment = Alignment.Center
            ) {
                if (thumbnail != null && !isIncognito) {
                    Image(
                        bitmap = thumbnail,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else if (isIncognito) {
                    Icon(
                        painter = painterResource(R.drawable.ic_incognito),
                        contentDescription = null,
                        tint = IncognitoEmphasis.copy(alpha = 0.35f),
                        modifier = Modifier.size(48.dp)
                    )
                } else {
                    Text(
                        text = tab.url.toDomain().ifBlank { "New tab" },
                        color = TuViIvoryDim.copy(alpha = 0.4f),
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                }
                // Loading bar ở dưới cùng thumbnail
                if (tab.isLoading) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .fillMaxWidth()
                            .height(2.dp)
                            .background(accent.copy(alpha = 0.7f))
                    )
                }
            }
        }
    }
}

private fun String.toFaviconChar(): String {
    val domain = toDomain().lowercase()
    return when {
        domain.contains("google")    -> "G"
        domain.contains("youtube")   -> "Y"
        domain.contains("facebook")  -> "F"
        domain.contains("wikipedia") -> "W"
        domain.contains("github")    -> "G"
        domain.contains("reddit")    -> "R"
        domain.contains("twitter") || domain.contains("x.com") -> "X"
        else -> domain.firstOrNull()?.uppercaseChar()?.toString() ?: "W"
    }
}

private fun String.toDomain(): String {
    return try {
        android.net.Uri.parse(this).host?.removePrefix("www.") ?: this
    } catch (_: Exception) { this }
}
