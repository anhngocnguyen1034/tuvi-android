package com.example.tuvi.ui.browser

import android.view.WindowManager
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
import androidx.compose.material.icons.filled.Star
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
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
import kotlinx.coroutines.launch

// ── Incognito color palette (mirror từ TabSwitcherScreen) ─────────────────────
private val IncogBg     = Color(0xFF0D0D0D)
private val IncogCard   = Color(0xFF1C1C1C)
private val IncogAccent = Color(0xFFE0E0E0)
private val IncogDim    = Color(0xFF9E9E9E)
private val IncogDiv    = Color(0xFF2C2C2C)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrowserScreen(
    config: BrowserConfig,
    onBack: () -> Unit,
    onOpenHistory: () -> Unit = {},
    onOpenBookmarks: () -> Unit = {},
    viewModel: BrowserViewModel = viewModel(factory = BrowserViewModel.Factory)
) {
    val tabs              = viewModel.tabs
    val activeTabId       = viewModel.activeTabId
    val activeTab         = viewModel.activeTab
    val isIncognito       = viewModel.isActiveIncognito
    val isBookmarked      by viewModel.isCurrentUrlBookmarked.collectAsState()
    val keyboard          = LocalSoftwareKeyboardController.current
    var showMore          by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope             = rememberCoroutineScope()
    val density           = LocalDensity.current

    // ── Download / long-press media ───────────────────────────────────────────
    var pendingDownloadUrl by remember { mutableStateOf<String?>(null) }

    // ── Màu động theo chế độ ──────────────────────────────────────────────────
    val bgColor       = if (isIncognito) IncogBg    else TuViNavy
    val cardColor     = if (isIncognito) IncogCard  else TuViNavyCard
    val accentColor   = if (isIncognito) IncogAccent else TuViGold
    val dimColor      = if (isIncognito) IncogDim   else TuViIvoryDim
    val progressTrack = if (isIncognito) IncogCard  else TuViNavyLight

    // ── FLAG_SECURE: bật khi incognito, tắt khi thường ───────────────────────
    val context  = LocalContext.current
    val activity = context as? android.app.Activity
    DisposableEffect(isIncognito) {
        if (isIncognito) activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        else             activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        onDispose { activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_SECURE) }
    }

    // ── Toolbar cố định (không thu/ẩn theo cuộn WebView) ─────────────────────
    var toolbarHeightPx by remember { mutableStateOf(0) }

    // BackHandler
    BackHandler(enabled = true) {
        when {
            viewModel.showTabSwitcher    -> viewModel.closeTabSwitcher()
            activeTab?.canGoBack == true -> viewModel.goBack()
            else                         -> viewModel.openTabSwitcher()
        }
    }

    val toolbarHeightDp = with(density) { toolbarHeightPx.toDp() }
    val contentTopPadding = toolbarHeightDp

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            containerColor = bgColor,
            // topBar: toolbar cố định phía trên WebView
            bottomBar = {
                BrowserBottomBar(
                    canGoBack = activeTab?.canGoBack ?: false,
                    canGoForward = activeTab?.canGoForward ?: false,
                    isIncognito = isIncognito,
                    onBack = { viewModel.goBack() },
                    onForward = { viewModel.goForward() },
                    onReload = { viewModel.reload() }
                )
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = padding.calculateBottomPadding())
            ) {
                // ── Vùng nội dung (đẩy xuống dưới toolbar) ───────────────────
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = contentTopPadding)
                ) {
                    if (activeTab?.isLoading == true) {
                        LinearProgressIndicator(
                            progress = { (activeTab.progress / 100f).coerceIn(0f, 1f) },
                            modifier = Modifier.fillMaxWidth().height(2.dp),
                            color = accentColor,
                            trackColor = progressTrack,
                            strokeCap = StrokeCap.Round
                        )
                    } else {
                        Spacer(Modifier.height(2.dp))
                    }

                    if (activeTab?.error != null) {
                        ErrorBanner(message = activeTab.error!!)
                    }

                    // ── Multi-WebView box với Pull-to-Refresh ─────────────────
                    val pullState = rememberPullToRefreshState()
                    PullToRefreshBox(
                        isRefreshing = activeTab?.isLoading == true,
                        onRefresh = { viewModel.reload() },
                        state = pullState,
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        indicator = {
                            PullToRefreshDefaults.Indicator(
                                state = pullState,
                                isRefreshing = activeTab?.isLoading == true,
                                color = accentColor,
                                containerColor = cardColor
                            )
                        }
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            tabs.forEach { tab ->
                                key(tab.id) {
                                    val isActive = tab.id == activeTabId
                                    TabWebViewHolder(
                                        modifier = if (isActive) Modifier.fillMaxSize()
                                                   else Modifier.size(0.dp),
                                        tabId = tab.id,
                                        initialUrl = tab.url,
                                        isActive = isActive,
                                        isIncognito = tab.isIncognito,
                                        config = config,
                                        commands = viewModel.commands,
                                        onPageStarted = { url -> viewModel.onPageStarted(tab.id, url) },
                                        onPageFinished = { url, title, canBack, canFwd ->
                                            viewModel.onPageFinished(tab.id, url, title, canBack, canFwd)
                                        },
                                        onProgressChanged = { viewModel.onProgressChanged(tab.id, it) },
                                        onError = { viewModel.onReceivedError(tab.id, it) },
                                        onLongPressMedia = { url -> pendingDownloadUrl = url }
                                    )
                                }
                            }
                        }
                    }
                }

                // ── Toolbar + address bar — cố định phía trên ──
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .align(Alignment.TopStart)
                        .background(bgColor)
                        .onGloballyPositioned { coords ->
                            if (coords.size.height > 0) toolbarHeightPx = coords.size.height
                        }
                ) {
                    // TopAppBar
                    TopAppBar(
                        title = {
                            Column {
                                Text(
                                    text = activeTab?.title?.ifBlank { config.title } ?: config.title,
                                    color = if (isIncognito) IncogAccent else TuViIvory,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                if (activeTab?.url?.isNotBlank() == true) {
                                    Text(
                                        text = activeTab.url,
                                        color = dimColor,
                                        fontSize = 11.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        },
                        navigationIcon = {
                            IconButton(onClick = onBack) {
                                Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại", tint = accentColor)
                            }
                        },
                        actions = {
                            if (activeTab?.isLoading == true) {
                                IconButton(onClick = { viewModel.stop() }) {
                                    Icon(Icons.Default.Close, contentDescription = "Dừng", tint = dimColor)
                                }
                            } else {
                                IconButton(onClick = { viewModel.reload() }) {
                                    Icon(Icons.Default.Refresh, contentDescription = "Tải lại", tint = accentColor)
                                }
                            }
                            if (!isIncognito) {
                                IconButton(onClick = {
                                    val added = viewModel.toggleBookmark()
                                    scope.launch {
                                        snackbarHostState.showSnackbar(
                                            if (added) "Đã thêm vào Dấu trang" else "Đã xoá khỏi Dấu trang"
                                        )
                                    }
                                }) {
                                    Icon(
                                        Icons.Default.Star,
                                        contentDescription = "Dấu trang",
                                        tint = if (isBookmarked) TuViGold else TuViIvoryDim.copy(alpha = 0.4f)
                                    )
                                }
                            }
                            TabCountButton(count = tabs.size, isIncognito = isIncognito,
                                onClick = { viewModel.openTabSwitcher() })
                            Box {
                                IconButton(onClick = { showMore = true }) {
                                    Icon(Icons.Default.MoreVert, contentDescription = "Thêm", tint = accentColor)
                                }
                                MoreDropdownMenu(
                                    expanded = showMore,
                                    isIncognito = isIncognito,
                                    onDismiss = { showMore = false },
                                    onOpenHistory = { showMore = false; onOpenHistory() },
                                    onOpenTabs = { showMore = false; viewModel.openTabSwitcher() },
                                    onOpenBookmarks = { showMore = false; onOpenBookmarks() }
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = bgColor)
                    )

                    // AddressBar
                    if (config.showAddressBar) {
                        AddressBar(
                            url = activeTab?.url ?: "",
                            enabled = config.allowUserNavigation,
                            isIncognito = isIncognito,
                            onNavigate = { input ->
                                keyboard?.hide()
                                viewModel.navigateTo(input)
                            }
                        )
                    }
                }
            }
        }

        // ── Tab Switcher overlay ──
        TabSwitcherOverlay(
            visible = viewModel.showTabSwitcher,
            tabs = tabs,
            activeTabId = activeTabId,
            showIncognitoList = viewModel.showIncognitoList,
            onSelectTab = { viewModel.switchTab(it) },
            onCloseTab = { id ->
                val shouldExit = viewModel.closeTab(id)
                if (shouldExit) onBack()
            },
            onNewTab = { viewModel.addNewTab() },
            onNewIncognitoTab = { viewModel.addNewIncognitoTab() },
            onCloseAllIncognito = { viewModel.closeAllIncognitoTabs() },
            onSwitchPanel = { viewModel.setTabSwitcherPanel(it) },
            onDismiss = { viewModel.closeTabSwitcher() },
            onOpenBookmarks = { viewModel.closeTabSwitcher(); onOpenBookmarks() }
        )

        // ── Image / file long-press bottom sheet ──
        pendingDownloadUrl?.let { url ->
            ImageActionSheet(
                imageUrl = url,
                onDownload = {
                    val fileName = enqueueDownload(
                        context = context,
                        url = url,
                        mimeType = guessMimeFromUrl(url)
                    )
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            if (fileName != null) "Đang tải: $fileName"
                            else "Không thể tải file này"
                        )
                    }
                },
                onDismiss = { pendingDownloadUrl = null }
            )
        }
    }
}

// ── Tab count button ──────────────────────────────────────────────────────────

@Composable
private fun TabCountButton(count: Int, isIncognito: Boolean, onClick: () -> Unit) {
    val border = if (isIncognito) IncogAccent else TuViGold
    val bg     = if (isIncognito) IncogCard   else TuViNavyCard
    IconButton(onClick = onClick) {
        Box(
            modifier = Modifier
                .size(26.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(bg)
                .border(1.5.dp, border, RoundedCornerShape(6.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (count > 99) "99+" else count.toString(),
                color = border,
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
    isIncognito: Boolean,
    onDismiss: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenTabs: () -> Unit,
    onOpenBookmarks: () -> Unit
) {
    val bg   = if (isIncognito) IncogCard else TuViNavyCard
    val text = if (isIncognito) IncogAccent else TuViIvory

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
        modifier = Modifier.background(bg)
    ) {
        DropdownMenuItem(
            text = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("⬜", fontSize = 15.sp)
                    Spacer(Modifier.size(10.dp))
                    Text("Quản lý tab", color = text, fontSize = 14.sp)
                }
            },
            onClick = onOpenTabs,
            colors = MenuDefaults.itemColors(textColor = text)
        )
        if (!isIncognito) {
            DropdownMenuItem(
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("⏱", fontSize = 15.sp)
                        Spacer(Modifier.size(10.dp))
                        Text("Lịch sử duyệt web", color = text, fontSize = 14.sp)
                    }
                },
                onClick = onOpenHistory,
                colors = MenuDefaults.itemColors(textColor = text)
            )
            DropdownMenuItem(
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("★", fontSize = 15.sp)
                        Spacer(Modifier.size(10.dp))
                        Text("Dấu trang", color = text, fontSize = 14.sp)
                    }
                },
                onClick = onOpenBookmarks,
                colors = MenuDefaults.itemColors(textColor = text)
            )
        }
    }
}

// ── Address bar ───────────────────────────────────────────────────────────────

@Composable
private fun AddressBar(
    url: String,
    enabled: Boolean,
    isIncognito: Boolean,
    onNavigate: (String) -> Unit
) {
    var text by remember(url) { mutableStateOf(url) }

    val bg       = if (isIncognito) IncogBg   else TuViNavy
    val cardBg   = if (isIncognito) IncogCard else TuViNavyCard
    val focused  = if (isIncognito) IncogAccent else TuViGold
    val unfocus  = if (isIncognito) IncogDiv  else TuViDivider
    val textCol  = if (isIncognito) IncogAccent else TuViIvory
    val hintCol  = if (isIncognito) IncogDim  else TuViIvoryDim

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(bg)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Incognito indicator icon
        if (isIncognito) {
            Text(
                text = "\uD83D\uDD75\uFE0F",
                fontSize = 16.sp,
                modifier = Modifier.padding(end = 8.dp)
            )
        }
        OutlinedTextField(
            value = text,
            onValueChange = { if (enabled) text = it },
            readOnly = !enabled,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(10.dp),
            textStyle = TextStyle(fontSize = 13.sp, color = textCol),
            placeholder = { Text("Nhập URL hoặc tìm kiếm...", color = hintCol, fontSize = 13.sp) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
            keyboardActions = KeyboardActions(onGo = { onNavigate(text) }),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = focused,
                unfocusedBorderColor = unfocus,
                focusedContainerColor = cardBg,
                unfocusedContainerColor = cardBg,
                cursorColor = focused
            )
        )
    }
}

// ── Bottom bar ────────────────────────────────────────────────────────────────

@Composable
private fun BrowserBottomBar(
    canGoBack: Boolean,
    canGoForward: Boolean,
    isIncognito: Boolean,
    onBack: () -> Unit,
    onForward: () -> Unit,
    onReload: () -> Unit
) {
    val bg      = if (isIncognito) IncogBg   else TuViNavy
    val divider = if (isIncognito) IncogDiv  else TuViDivider
    val accent  = if (isIncognito) IncogAccent else TuViGold
    val dim     = if (isIncognito) IncogDim  else TuViIvoryDim

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(bg)
            .border(width = 1.dp, color = divider.copy(alpha = 0.5f))
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack, enabled = canGoBack, modifier = Modifier.weight(1f)) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Lùi",
                tint = if (canGoBack) accent else dim.copy(alpha = 0.3f))
        }
        IconButton(onClick = onForward, enabled = canGoForward, modifier = Modifier.weight(1f)) {
            Icon(Icons.Default.ArrowForward, contentDescription = "Tiến",
                tint = if (canGoForward) accent else dim.copy(alpha = 0.3f))
        }
        IconButton(onClick = onReload, modifier = Modifier.weight(1f)) {
            Icon(Icons.Default.Refresh, contentDescription = "Tải lại", tint = accent)
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
