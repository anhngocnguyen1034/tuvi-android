package com.example.tuvi.ui.browser

import android.net.Uri
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Star
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
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tuvi.ui.theme.IncognitoBg
import com.example.tuvi.ui.theme.IncognitoCard
import com.example.tuvi.ui.theme.IncognitoDivider
import com.example.tuvi.ui.theme.IncognitoEmphasis
import com.example.tuvi.ui.theme.IncognitoMuted
import com.example.tuvi.ui.theme.TuViDivider
import com.example.tuvi.ui.browser.BookmarkScreen
import com.example.tuvi.ui.browser.HistoryScreen
import com.example.tuvi.ui.theme.TuViGold
import com.example.tuvi.ui.theme.TuViGoldDark
import com.example.tuvi.ui.theme.TuViIvory
import com.example.tuvi.ui.theme.TuViIvoryDim
import com.example.tuvi.ui.theme.TuViNavy
import com.example.tuvi.ui.theme.TuViNavyCard
import com.example.tuvi.ui.theme.TuViNavyLight
import com.example.tuvi.ui.theme.TuViRed
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrowserScreen(
    config: BrowserConfig,
    onBack: () -> Unit,
    viewModel: BrowserViewModel = viewModel(factory = BrowserViewModel.Factory)
) {
    val tabs              = viewModel.tabs
    val activeTabId       = viewModel.activeTabId
    val activeTab         = viewModel.activeTab
    val isIncognito       = viewModel.isActiveIncognito
    val isBookmarked      by viewModel.isCurrentUrlBookmarked.collectAsStateWithLifecycle()
    val keyboard          = LocalSoftwareKeyboardController.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope             = rememberCoroutineScope()
    val density           = LocalDensity.current
    val keyboardOpen      = WindowInsets.ime.getBottom(density) > 0

    // ── Download / long-press media ───────────────────────────────────────────
    var pendingDownloadUrl by remember { mutableStateOf<String?>(null) }

    // ── Màu động theo chế độ ──────────────────────────────────────────────────
    val bgColor       = if (isIncognito) IncognitoBg    else TuViNavy
    val cardColor     = if (isIncognito) IncognitoCard  else TuViNavyCard
    val accentColor   = if (isIncognito) IncognitoEmphasis else TuViGold
    val dimColor      = if (isIncognito) IncognitoMuted   else TuViIvoryDim
    val progressTrack = if (isIncognito) IncognitoCard  else TuViNavyLight

    // ── FLAG_SECURE: bật khi incognito, tắt khi thường ───────────────────────
    val context  = LocalContext.current
    val activity = context as? android.app.Activity
    DisposableEffect(isIncognito) {
        if (isIncognito) activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        else             activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        onDispose { activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_SECURE) }
    }

    // BackHandler
    BackHandler(enabled = true) {
        when {
            viewModel.showHistoryOverlay  -> viewModel.closeHistory()
            viewModel.showBookmarkOverlay -> viewModel.closeBookmarks()
            viewModel.showTabSwitcher     -> viewModel.closeTabSwitcher()
            activeTab?.canGoBack == true  -> viewModel.goBack()
            else                          -> viewModel.openTabSwitcher()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            snackbarHost = { SnackbarHost(snackbarHostState) },
            containerColor = bgColor,
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(bgColor)
                ) {
                    if (config.showAddressBar) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (!keyboardOpen) {
                                IconButton(onClick = onBack) {
                                    Icon(
                                        Icons.Default.Home,
                                        contentDescription = "Về màn hình chính",
                                        tint = accentColor
                                    )
                                }
                            }
                            AddressBar(
                                modifier = Modifier.weight(1f),
                                url = activeTab?.url ?: "",
                                enabled = config.allowUserNavigation,
                                isIncognito = isIncognito,
                                onNavigate = { input ->
                                    keyboard?.hide()
                                    viewModel.navigateTo(input)
                                }
                            )
                            if (!keyboardOpen) {
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
                            }
                        }
                    } else {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = onBack) {
                                Icon(
                                    Icons.Default.Home,
                                    contentDescription = "Về màn hình chính",
                                    tint = accentColor
                                )
                            }
                            Text(
                                text = activeTab?.title?.ifBlank { config.title } ?: config.title,
                                color = if (isIncognito) IncognitoEmphasis else TuViIvory,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
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
                        }
                    }
                }

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
                    ErrorBanner(message = activeTab.error)
                }

                Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    tabs.forEach { tab ->
                        key(tab.id) {
                            val isActive = tab.id == activeTabId
                            TabWebViewHolder(
                                modifier = Modifier.fillMaxSize(),
                                tabId = tab.id,
                                initialUrl = tab.url,
                                isActive = isActive,
                                isIncognito = tab.isIncognito,
                                config = config,
                                commands = viewModel.commands,
                                pendingLoadUrl = tab.pendingLoadUrl,
                                onPendingLoadConsumed = { viewModel.consumePendingLoad(tab.id) },
                                onPageStarted = { url -> viewModel.onPageStarted(tab.id, url) },
                                onPageFinished = { url, title, canBack, canFwd ->
                                    viewModel.onPageFinished(tab.id, url, title, canBack, canFwd)
                                },
                                onProgressChanged = { viewModel.onProgressChanged(tab.id, it) },
                                onError = { viewModel.onReceivedError(tab.id, it) },
                                onLongPressMedia = { url -> pendingDownloadUrl = url },
                                onNavigationStateSync = { canBack, canFwd ->
                                    viewModel.syncTabNavState(tab.id, canBack, canFwd)
                                },
                                onCaptureThumbnail = { bmp ->
                                    viewModel.updateThumbnail(tab.id, bmp)
                                }
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
            thumbnails = viewModel.thumbnails,
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
            onOpenBookmarks = { viewModel.closeTabSwitcher(); viewModel.openBookmarks() }
        )

        // ── History overlay (không navigate ra ngoài, giữ WebView sống) ──
        if (viewModel.showHistoryOverlay) {
            Box(modifier = Modifier.fillMaxSize()) {
                HistoryScreen(
                    onBack = { viewModel.closeHistory() },
                    onOpenUrl = { url ->
                        viewModel.closeHistory()
                        viewModel.navigateTo(url)
                    }
                )
            }
        }

        // ── Bookmarks overlay ──
        if (viewModel.showBookmarkOverlay) {
            Box(modifier = Modifier.fillMaxSize()) {
                BookmarkScreen(
                    onBack = { viewModel.closeBookmarks() },
                    onOpenUrl = { url ->
                        viewModel.closeBookmarks()
                        viewModel.navigateTo(url)
                    }
                )
            }
        }

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

        if (!viewModel.showHistoryOverlay && !viewModel.showBookmarkOverlay && !viewModel.showTabSwitcher) {
            BrowserBottomBar(
                canGoBack = activeTab?.canGoBack ?: false,
                canGoForward = activeTab?.canGoForward ?: false,
                isIncognito = isIncognito,
                tabCount = tabs.size,
                onBack = { viewModel.goBack() },
                onForward = { viewModel.goForward() },
                onNewIncognitoTab = { viewModel.addNewIncognitoTab() },
                onOpenTabs = { viewModel.openTabSwitcher() },
                onOpenHistory = { viewModel.openHistory() },
                onOpenBookmarks = { viewModel.openBookmarks() },
                onReload = { viewModel.reload() },
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}

// ── Tab count button ──────────────────────────────────────────────────────────

@Composable
private fun TabCountButton(
    count: Int,
    isIncognito: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val border = if (isIncognito) IncognitoEmphasis else TuViGold
    val bg     = if (isIncognito) IncognitoCard   else TuViNavyCard
    IconButton(onClick = onClick, modifier = modifier) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(RoundedCornerShape(5.dp))
                .background(bg)
                .border(1.5.dp, border, RoundedCornerShape(5.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (count > 99) "99+" else count.toString(),
                color = border,
                fontSize = if (count > 9) 8.sp else 11.sp,
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
    onOpenBookmarks: () -> Unit,
    onReload: () -> Unit = {}
) {
    val bg   = if (isIncognito) IncognitoCard else TuViNavyCard
    val text = if (isIncognito) IncognitoEmphasis else TuViIvory

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
        modifier = Modifier.background(bg)
    ) {
        DropdownMenuItem(
            text = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("↺", fontSize = 15.sp)
                    Spacer(Modifier.size(10.dp))
                    Text("Tải lại trang", color = text, fontSize = 14.sp)
                }
            },
            onClick = onReload,
            colors = MenuDefaults.itemColors(textColor = text)
        )
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
                        Text("H", fontSize = 15.sp)
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
                        Icon(Icons.Default.Star, contentDescription = null, tint = text, modifier = Modifier.size(15.dp))
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

/**
 * Trang kết quả tìm Google: chỉ hiện từ khóa `q`, không hiện google.com/search…
 */
private fun googleSearchQueryForDisplay(url: String): String? {
    return try {
        val uri = Uri.parse(url.trim())
        val host = uri.host?.lowercase() ?: return null
        // Chỉ google.com / www.google.com / google.com.vn… — không khớp apis.google.com
        if (!host.matches(Regex("^(www\\.)?google\\.[a-z.]{2,}$", RegexOption.IGNORE_CASE))) return null
        val path = uri.path ?: return null
        if (!path.startsWith("/search") && !path.startsWith("/m/search")) return null
        val q = uri.getQueryParameter("q")?.trim() ?: return null
        if (q.isEmpty()) null else q
    } catch (_: Exception) {
        null
    }
}

/**
 * Hiển thị thanh địa chỉ không có `http://` / `https://` (giống Chrome).
 * Trang tìm Google → chỉ hiện nội dung ô tìm (tham số `q`).
 * Các trang khác: host + path + query (đã bỏ scheme); bỏ `www.` ở đầu tên miền.
 */
private fun displayUrl(url: String): String {
    if (url.isBlank() || url == "about:blank") return ""
    googleSearchQueryForDisplay(url)?.let { return it }
    val t = url.trim()
    val withoutScheme = when {
        t.startsWith("https://", ignoreCase = true) -> t.substring(8)
        t.startsWith("http://", ignoreCase = true) -> t.substring(7)
        else -> t
    }
    val firstSlash = withoutScheme.indexOf('/')
    val hostPort = if (firstSlash == -1) withoutScheme else withoutScheme.substring(0, firstSlash)
    val rest = if (firstSlash == -1) "" else withoutScheme.substring(firstSlash)
    val hostNoWww = if (hostPort.startsWith("www.", ignoreCase = true)) hostPort.substring(4) else hostPort
    return hostNoWww + rest
}

@Composable
private fun AddressBar(
    modifier: Modifier = Modifier,
    url: String,
    enabled: Boolean,
    isIncognito: Boolean,
    onNavigate: (String) -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }
    var editText  by remember { mutableStateOf(displayUrl(url)) }

    val bg       = if (isIncognito) IncognitoBg   else TuViNavy
    val cardBg   = if (isIncognito) IncognitoCard else TuViNavyCard
    val focused  = if (isIncognito) IncognitoEmphasis else TuViGold
    val unfocus  = if (isIncognito) IncognitoDivider  else TuViDivider
    val textCol  = if (isIncognito) IncognitoEmphasis else TuViIvory
    val hintCol  = if (isIncognito) IncognitoMuted  else TuViIvoryDim

    // Mỗi khi URL tab đổi (tải trang, Back/Forward, chọn từ lịch sử…) → đồng bộ thanh tìm.
    // Phải cập nhật cả khi đang focus: lúc focus field đang hiện editText, không phải displayUrl(url).
    androidx.compose.runtime.LaunchedEffect(url) {
        editText = displayUrl(url)
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(bg)
            .padding(horizontal = 4.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Incognito indicator icon
        if (isIncognito) {
            Text(
                text = "Ẩn",
                fontSize = 16.sp,
                modifier = Modifier.padding(end = 8.dp)
            )
        }
        OutlinedTextField(
            value = if (isFocused) editText else displayUrl(url),
            onValueChange = { if (enabled) editText = it },
            readOnly = !enabled,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(10.dp),
            textStyle = TextStyle(
                fontSize = 13.sp,
                color = textCol,
                // Unfocused: căn giữa giống Chrome
                textAlign = if (isFocused) TextAlign.Start else TextAlign.Center
            ),
            placeholder = { Text("Tìm kiếm hoặc nhập địa chỉ...", color = hintCol, fontSize = 13.sp,
                modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
            keyboardActions = KeyboardActions(onGo = { onNavigate(editText) }),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = focused,
                unfocusedBorderColor = unfocus.copy(alpha = 0.4f),
                focusedContainerColor = cardBg,
                unfocusedContainerColor = cardBg,
                cursorColor = focused
            ),
            // Khi focus: hiện full URL, select all để dễ gõ đè
            interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }.also { src ->
                androidx.compose.runtime.LaunchedEffect(src) {
                    src.interactions.collect { interaction ->
                        when (interaction) {
                            is androidx.compose.foundation.interaction.FocusInteraction.Focus -> {
                                editText = displayUrl(url)
                                isFocused = true
                            }
                            is androidx.compose.foundation.interaction.FocusInteraction.Unfocus -> {
                                isFocused = false
                            }
                        }
                    }
                }
            }
        )
    }
}

// ── Bottom bar (Chrome-style) ─────────────────────────────────────────────────

@Composable
private fun BrowserBottomBar(
    canGoBack: Boolean,
    canGoForward: Boolean,
    isIncognito: Boolean,
    tabCount: Int,
    onBack: () -> Unit,
    onForward: () -> Unit,
    onNewIncognitoTab: () -> Unit,
    onOpenTabs: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenBookmarks: () -> Unit,
    onReload: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val bgBase  = if (isIncognito) IncognitoBg   else TuViNavy
    val bgLight = if (isIncognito) IncognitoCard  else TuViNavyLight
    val divider = if (isIncognito) IncognitoDivider  else TuViDivider
    val accent  = if (isIncognito) IncognitoEmphasis else TuViGold
    val dim     = if (isIncognito) IncognitoMuted else TuViIvoryDim
    var showMenu by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .background(bgBase)
    ) {
        // Top hairline border
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(0.5.dp)
                .background(divider.copy(alpha = 0.5f))
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // ← Back
            IconButton(onClick = onBack, enabled = canGoBack, modifier = Modifier.weight(1f)) {
                Icon(
                    Icons.Default.ArrowBack,
                    contentDescription = "Lùi",
                    tint = if (canGoBack) accent else dim.copy(alpha = 0.3f),
                    modifier = Modifier.size(22.dp)
                )
            }
            // → Forward
            IconButton(onClick = onForward, enabled = canGoForward, modifier = Modifier.weight(1f)) {
                Icon(
                    Icons.Default.ArrowForward,
                    contentDescription = "Tiến",
                    tint = if (canGoForward) accent else dim.copy(alpha = 0.3f),
                    modifier = Modifier.size(22.dp)
                )
            }
            // Incognito — tạo tab ẩn danh mới
            IconButton(onClick = onNewIncognitoTab, modifier = Modifier.weight(1f)) {
                Text(
                    text = "Ẩn",
                    fontSize = 18.sp,
                    modifier = Modifier.padding(2.dp)
                )
            }
            // ⬜ Tab count
            TabCountButton(
                count = tabCount,
                isIncognito = isIncognito,
                onClick = onOpenTabs,
                modifier = Modifier.weight(1f)
            )
            // ⋮ Menu
            Box(modifier = Modifier.weight(1f)) {
                IconButton(
                    onClick = { showMenu = true },
                    modifier = Modifier.align(Alignment.Center)
                ) {
                    Icon(
                        Icons.Default.MoreVert,
                        contentDescription = "Thêm",
                        tint = accent,
                        modifier = Modifier.size(22.dp)
                    )
                }
                MoreDropdownMenu(
                    expanded = showMenu,
                    isIncognito = isIncognito,
                    onDismiss = { showMenu = false },
                    onOpenHistory = { showMenu = false; onOpenHistory() },
                    onOpenTabs = { showMenu = false; onOpenTabs() },
                    onOpenBookmarks = { showMenu = false; onOpenBookmarks() },
                    onReload = { showMenu = false; onReload() }
                )
            }
        }
    }  // end outer Box
}

@Composable
private fun ErrorBanner(message: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(TuViRed.copy(alpha = 0.15f))
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("[!] $message", color = TuViRed, fontSize = 12.sp, modifier = Modifier.weight(1f))
    }
}
