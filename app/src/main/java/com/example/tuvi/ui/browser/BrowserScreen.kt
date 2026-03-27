package com.example.tuvi.ui.browser

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tuvi.ui.theme.TuViDivider
import com.example.tuvi.ui.theme.TuViGold
import com.example.tuvi.ui.theme.TuViGoldDark
import com.example.tuvi.ui.theme.TuViIvory
import com.example.tuvi.ui.theme.TuViIvoryDim
import com.example.tuvi.ui.theme.TuViNavy
import com.example.tuvi.ui.theme.TuViNavyCard
import com.example.tuvi.ui.theme.TuViNavyLight
import com.example.tuvi.ui.theme.TuViRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrowserScreen(
    config: BrowserConfig,
    onBack: () -> Unit,
    onOpenHistory: () -> Unit = {},
    viewModel: BrowserViewModel = viewModel(factory = BrowserViewModel.Factory)
) {
    val tabs       = viewModel.tabs
    val activeTabId = viewModel.activeTabId
    val activeTab  = viewModel.activeTab
    val keyboard   = LocalSoftwareKeyboardController.current
    var showMore   by remember { mutableStateOf(false) }

    // BackHandler: goBack trong tab → Tab Switcher → pop stack
    BackHandler(enabled = true) {
        when {
            viewModel.showTabSwitcher          -> viewModel.closeTabSwitcher()
            activeTab?.canGoBack == true       -> viewModel.goBack()
            else                               -> viewModel.openTabSwitcher()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = TuViNavy,
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = activeTab?.title?.ifBlank { config.title } ?: config.title,
                                color = TuViIvory,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (activeTab?.url?.isNotBlank() == true) {
                                Text(
                                    text = activeTab.url,
                                    color = TuViIvoryDim,
                                    fontSize = 11.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại", tint = TuViGold)
                        }
                    },
                    actions = {
                        // Reload / Stop
                        if (activeTab?.isLoading == true) {
                            IconButton(onClick = {}) {
                                Icon(Icons.Default.Close, contentDescription = "Dừng", tint = TuViIvoryDim)
                            }
                        } else {
                            IconButton(onClick = { viewModel.reload() }) {
                                Icon(Icons.Default.Refresh, contentDescription = "Tải lại", tint = TuViGold)
                            }
                        }

                        // Tab count button
                        TabCountButton(
                            count = tabs.size,
                            onClick = { viewModel.openTabSwitcher() }
                        )

                        // ⋮ More
                        Box {
                            IconButton(onClick = { showMore = true }) {
                                Icon(Icons.Default.MoreVert, contentDescription = "Thêm", tint = TuViGold)
                            }
                            MoreDropdownMenu(
                                expanded = showMore,
                                onDismiss = { showMore = false },
                                onOpenHistory = { showMore = false; onOpenHistory() },
                                onOpenTabs = { showMore = false; viewModel.openTabSwitcher() }
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = TuViNavy)
                )
            },
            bottomBar = {
                BrowserBottomBar(
                    canGoBack = activeTab?.canGoBack ?: false,
                    canGoForward = activeTab?.canGoForward ?: false,
                    onBack = { viewModel.goBack() },
                    onForward = { viewModel.goForward() },
                    onReload = { viewModel.reload() }
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                if (config.showAddressBar) {
                    AddressBar(
                        url = activeTab?.url ?: "",
                        enabled = config.allowUserNavigation,
                        onNavigate = { input ->
                            keyboard?.hide()
                            viewModel.navigateTo(input)
                        }
                    )
                }

                if (activeTab?.isLoading == true) {
                    LinearProgressIndicator(
                        progress = { (activeTab.progress) / 100f },
                        modifier = Modifier.fillMaxWidth().height(2.dp),
                        color = TuViGold,
                        trackColor = TuViNavyLight,
                        strokeCap = StrokeCap.Round
                    )
                } else {
                    Spacer(Modifier.height(2.dp))
                }

                if (activeTab?.error != null) {
                    ErrorBanner(message = activeTab.error!!)
                }

                // ── Multi-WebView box ──
                // Mỗi tab có WebView riêng; tab active: fillMaxSize, tab khác: size(0.dp)
                // key(tab.id) giúp Compose giữ WebView instance qua recompose
                Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    tabs.forEach { tab ->
                        key(tab.id) {
                            val isActive = tab.id == activeTabId
                            TabWebViewHolder(
                                modifier = if (isActive) Modifier.fillMaxSize()
                                           else Modifier.size(0.dp),
                                tabId = tab.id,
                                initialUrl = tab.url,
                                isActive = isActive,
                                config = config,
                                commands = viewModel.commands,
                                onPageStarted = { url -> viewModel.onPageStarted(tab.id, url) },
                                onPageFinished = { url, title, canBack, canFwd ->
                                    viewModel.onPageFinished(tab.id, url, title, canBack, canFwd)
                                },
                                onProgressChanged = { viewModel.onProgressChanged(tab.id, it) },
                                onError = { viewModel.onReceivedError(tab.id, it) }
                            )
                        }
                    }
                }
            }
        }

        // ── Tab Switcher overlay ──
        TabSwitcherOverlay(
            visible = viewModel.showTabSwitcher,
            tabs = tabs,
            activeTabId = activeTabId,
            onSelectTab = { viewModel.switchTab(it) },
            onCloseTab = { id ->
                val shouldExit = viewModel.closeTab(id)
                if (shouldExit) onBack()
            },
            onNewTab = { viewModel.addNewTab() },
            onDismiss = { viewModel.closeTabSwitcher() }
        )
    }
}

// ── Tab count button ──────────────────────────────────────────────────────────

@Composable
private fun TabCountButton(count: Int, onClick: () -> Unit) {
    IconButton(onClick = onClick) {
        Box(
            modifier = Modifier
                .size(26.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(TuViNavyCard)
                .border(1.5.dp, TuViGold, RoundedCornerShape(6.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (count > 99) "99+" else count.toString(),
                color = TuViGold,
                fontSize = if (count > 9) 9.sp else 12.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
    }
}

// ── More dropdown ─────────────────────────────────────────────────────────────

@Composable
private fun MoreDropdownMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenTabs: () -> Unit
    // Thêm callback mới tại đây khi cần tính năng khác trong More
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
        modifier = Modifier.background(TuViNavyCard)
    ) {
        DropdownMenuItem(
            text = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("⬜", fontSize = 15.sp)
                    Spacer(Modifier.size(10.dp))
                    Text("Quản lý tab", color = TuViIvory, fontSize = 14.sp)
                }
            },
            onClick = onOpenTabs,
            colors = MenuDefaults.itemColors(textColor = TuViIvory)
        )
        DropdownMenuItem(
            text = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("⏱", fontSize = 15.sp)
                    Spacer(Modifier.size(10.dp))
                    Text("Lịch sử duyệt web", color = TuViIvory, fontSize = 14.sp)
                }
            },
            onClick = onOpenHistory,
            colors = MenuDefaults.itemColors(textColor = TuViIvory)
        )
        // ── Slot cho tính năng tương lai ──────────────────────────────────────
        // DropdownMenuItem( text = { Text("...") }, onClick = { ... } )
    }
}

// ── Address bar ───────────────────────────────────────────────────────────────

@Composable
private fun AddressBar(
    url: String,
    enabled: Boolean,
    onNavigate: (String) -> Unit
) {
    var text by remember(url) { mutableStateOf(url) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(TuViNavy)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = text,
            onValueChange = { if (enabled) text = it },
            readOnly = !enabled,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(10.dp),
            textStyle = TextStyle(fontSize = 13.sp, color = TuViIvory),
            placeholder = { Text("Nhập URL hoặc tìm kiếm...", color = TuViIvoryDim, fontSize = 13.sp) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
            keyboardActions = KeyboardActions(onGo = { onNavigate(text) }),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = TuViGold,
                unfocusedBorderColor = TuViDivider,
                focusedContainerColor = TuViNavyCard,
                unfocusedContainerColor = TuViNavyCard,
                cursorColor = TuViGold
            )
        )
    }
}

// ── Bottom bar ────────────────────────────────────────────────────────────────

@Composable
private fun BrowserBottomBar(
    canGoBack: Boolean,
    canGoForward: Boolean,
    onBack: () -> Unit,
    onForward: () -> Unit,
    onReload: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(TuViNavy)
            .border(width = 1.dp, color = TuViDivider.copy(alpha = 0.5f))
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack, enabled = canGoBack, modifier = Modifier.weight(1f)) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Lùi",
                tint = if (canGoBack) TuViGold else TuViIvoryDim.copy(alpha = 0.3f))
        }
        IconButton(onClick = onForward, enabled = canGoForward, modifier = Modifier.weight(1f)) {
            Icon(Icons.Default.ArrowForward, contentDescription = "Tiến",
                tint = if (canGoForward) TuViGold else TuViIvoryDim.copy(alpha = 0.3f))
        }
        IconButton(onClick = onReload, modifier = Modifier.weight(1f)) {
            Icon(Icons.Default.Refresh, contentDescription = "Tải lại", tint = TuViGold)
        }
    }
}

// ── Error banner ──────────────────────────────────────────────────────────────

@Composable
private fun ErrorBanner(message: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(TuViRed.copy(alpha = 0.15f))
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("⚠  $message", color = TuViRed, fontSize = 12.sp, modifier = Modifier.weight(1f))
    }
}
