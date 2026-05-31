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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MenuDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tuvi.presentation.BrowserViewModel
import com.example.tuvi.presentation.LongPressTarget
import com.example.tuvi.presentation.TabState
import com.example.tuvi.R
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
    val bookmarkAdded     = stringResource(R.string.browser_bookmark_added)
    val bookmarkRemoved   = stringResource(R.string.browser_bookmark_removed)
    val keyboardOpen      = WindowInsets.ime.getBottom(density) > 0

    // ── Download / long-press media ───────────────────────────────────────────
    var pendingDownloadUrl by remember { mutableStateOf<String?>(null) }
    var longPressTarget by remember { mutableStateOf<LongPressTarget?>(null) }

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
            else                          -> onBack()   // hết lịch sử ⇒ về Home
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
                                .padding(horizontal = 4.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (!keyboardOpen) {
                                IconButton(onClick = onBack, modifier = Modifier.size(40.dp)) {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_home),
                                        contentDescription = stringResource(R.string.browser_cd_home),
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
                                    IconButton(
                                        onClick = {
                                            val added = viewModel.toggleBookmark()
                                            scope.launch {
                                                snackbarHostState.showSnackbar(
                                                    if (added) bookmarkAdded else bookmarkRemoved
                                                )
                                            }
                                        },
                                        modifier = Modifier.size(40.dp)
                                    ) {
                                        Icon(
                                            painter = painterResource(R.drawable.ic_favorite),
                                            contentDescription = stringResource(R.string.browser_cd_bookmark),
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
                                    painter=painterResource(R.drawable.ic_home),
                                    contentDescription = stringResource(R.string.browser_cd_home),
                                    tint = accentColor
                                )
                            }
                            Text(
                                text = activeTab?.title?.ifBlank { config.title.ifBlank { stringResource(R.string.browser_default_title) } } ?: config.title.ifBlank { stringResource(R.string.browser_default_title) },
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
                                            if (added) bookmarkAdded else bookmarkRemoved
                                        )
                                    }
                                }) {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_favorite),
                                        contentDescription = stringResource(R.string.browser_cd_bookmark),
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
                                onLongPress = { target -> longPressTarget = target },
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
            onDismiss = { viewModel.closeTabSwitcher() }
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

        // ── Web-triggered download sheet ──
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
                            if (fileName != null) context.getString(R.string.browser_downloading, fileName)
                            else context.getString(R.string.browser_download_error)
                        )
                    }
                },
                onDismiss = { pendingDownloadUrl = null }
            )
        }

        // ── Long-press context menu (link / image) ──
        longPressTarget?.let { target ->
            LongPressMenu(
                target = target,
                onOpenInCurrentTab = { url -> viewModel.navigateTo(url) },
                onOpenInNewTab = { url ->
                    viewModel.addNewTab(url)
                },
                onDownload = { url ->
                    val fileName = enqueueDownload(
                        context = context,
                        url = url,
                        mimeType = guessMimeFromUrl(url)
                    )
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            if (fileName != null) context.getString(R.string.browser_downloading, fileName)
                            else context.getString(R.string.browser_download_error)
                        )
                    }
                },
                onDismiss = { longPressTarget = null }
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
    val bg      = if (isIncognito) IncognitoCard  else TuViNavyCard
    val border  = if (isIncognito) IncognitoEmphasis.copy(alpha = 0.25f) else TuViGold.copy(alpha = 0.25f)
    val divider = if (isIncognito) IncognitoDivider else TuViDivider
    val text    = if (isIncognito) IncognitoEmphasis else TuViIvory
    val subText = if (isIncognito) IncognitoMuted else TuViIvoryDim

    @Composable
    fun MenuItem(label: String, onClick: () -> Unit) {
        DropdownMenuItem(
            text = {
                Text(
                    text = label,
                    color = text,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.1.sp
                )
            },
            onClick = onClick,
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 13.dp),
            colors = MenuDefaults.itemColors(textColor = text))
        
    }

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
        modifier = Modifier
            .background(bg)
            .border(0.5.dp, border, RoundedCornerShape(12.dp))
            .widthIn(min = 190.dp)
    ) {
        if (!isIncognito) {
            HorizontalDivider(color = divider.copy(alpha = 0.5f), thickness = 0.5.dp)
            MenuItem(stringResource(R.string.browser_menu_history), onOpenHistory)
            HorizontalDivider(color = divider.copy(alpha = 0.5f), thickness = 0.5.dp)
            MenuItem(stringResource(R.string.browser_menu_bookmarks), onOpenBookmarks)
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
    val textCol  = if (isIncognito) IncognitoEmphasis else TuViIvory
    val hintCol  = if (isIncognito) IncognitoMuted  else TuViIvoryDim

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
        val shown = if (isFocused) editText else displayUrl(url)
        BasicTextField(
            value = shown,
            onValueChange = { if (enabled) editText = it },
            readOnly = !enabled,
            singleLine = true,
            textStyle = TextStyle(
                fontSize = 13.sp,
                color = textCol,
                // Unfocused: căn giữa giống Chrome
                textAlign = if (isFocused) TextAlign.Start else TextAlign.Center
            ),
            cursorBrush = SolidColor(focused),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
            keyboardActions = KeyboardActions(onGo = { onNavigate(editText) }),
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(10.dp))
                .background(cardBg)
                .padding(horizontal = 12.dp, vertical = 7.dp),
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
            },
            decorationBox = { inner ->
                Box(contentAlignment = Alignment.CenterStart) {
                    if (shown.isEmpty()) {
                        Text(
                            stringResource(R.string.browser_search_placeholder),
                            color = hintCol,
                            fontSize = 13.sp,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    }
                    inner()
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
    val divider = if (isIncognito) IncognitoDivider  else TuViDivider
    val accent  = if (isIncognito) IncognitoEmphasis else TuViGold
    val dim     = if (isIncognito) IncognitoMuted else TuViIvoryDim
    var showMenu by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(bgBase.copy(alpha = 0.96f))
            .navigationBarsPadding()
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
                    painter=painterResource(R.drawable.ic_back),
                    contentDescription = stringResource(R.string.browser_cd_back),
                    tint = if (canGoBack) accent else dim.copy(alpha = 0.3f),
                    modifier = Modifier.size(22.dp)
                )
            }
            // → Forward
            IconButton(onClick = onForward, enabled = canGoForward, modifier = Modifier.weight(1f)) {
                Icon(
                    painter = painterResource(R.drawable.ic_forward),
                    contentDescription = stringResource(R.string.browser_cd_forward),
                    tint = if (canGoForward) accent else dim.copy(alpha = 0.3f),
                    modifier = Modifier.size(22.dp)
                )
            }
            // Incognito — tạo tab ẩn danh mới
            IconButton(onClick = onNewIncognitoTab, modifier = Modifier.weight(1f)) {
                Icon(
                    painter = painterResource(R.drawable.ic_incognito),
                    contentDescription = stringResource(R.string.browser_incognito_icon),
                    tint = accent,
                    modifier = Modifier.size(22.dp)
                )
            }
            // Tab count
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
                        painter = painterResource(R.drawable.ic_more),
                        contentDescription = stringResource(R.string.browser_cd_more),
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
