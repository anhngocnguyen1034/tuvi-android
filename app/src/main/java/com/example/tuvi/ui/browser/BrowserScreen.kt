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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tuvi.ui.theme.TuViDivider
import com.example.tuvi.ui.theme.TuViGold
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
    val uiState by viewModel.uiState.collectAsState()
    val keyboard = LocalSoftwareKeyboardController.current
    var showMore by remember { mutableStateOf(false) }

    BackHandler(enabled = uiState.canGoBack) { viewModel.goBack() }

    Scaffold(
        containerColor = TuViNavy,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = uiState.displayTitle.ifBlank { config.title },
                            color = TuViIvory,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (uiState.url.isNotBlank()) {
                            Text(
                                text = uiState.url,
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
                    if (uiState.isLoading) {
                        IconButton(onClick = {}) {
                            Icon(Icons.Default.Close, contentDescription = "Dừng", tint = TuViIvoryDim)
                        }
                    } else {
                        IconButton(onClick = { viewModel.reload() }) {
                            Icon(Icons.Default.Refresh, contentDescription = "Tải lại", tint = TuViGold)
                        }
                    }

                    // ⋮ More
                    Box {
                        IconButton(onClick = { showMore = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Thêm", tint = TuViGold)
                        }
                        MoreDropdownMenu(
                            expanded = showMore,
                            onDismiss = { showMore = false },
                            onOpenHistory = { showMore = false; onOpenHistory() }
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = TuViNavy)
            )
        },
        bottomBar = {
            BrowserBottomBar(
                canGoBack = uiState.canGoBack,
                canGoForward = uiState.canGoForward,
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
                    url = uiState.url,
                    enabled = config.allowUserNavigation,
                    onNavigate = { input ->
                        keyboard?.hide()
                        viewModel.navigateTo(input)
                    }
                )
            }

            if (uiState.isLoading) {
                LinearProgressIndicator(
                    progress = { uiState.progress / 100f },
                    modifier = Modifier.fillMaxWidth().height(2.dp),
                    color = TuViGold,
                    trackColor = TuViNavyLight,
                    strokeCap = StrokeCap.Round
                )
            } else {
                Spacer(Modifier.height(2.dp))
            }

            if (uiState.error != null) {
                ErrorBanner(message = uiState.error!!)
            }

            WebViewContainer(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                config = config,
                commands = viewModel.commands,
                onPageStarted = { url -> viewModel.onPageStarted(url) },
                onPageFinished = { url, title, canBack, canFwd ->
                    viewModel.onPageFinished(url, title, canBack, canFwd)
                },
                onProgressChanged = { viewModel.onProgressChanged(it) },
                onError = { viewModel.onReceivedError(it) }
            )
        }
    }
}

// ── More dropdown ─────────────────────────────────────────────────────────────

@Composable
private fun MoreDropdownMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    onOpenHistory: () -> Unit
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
                    Text("⏱", fontSize = 16.sp)
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
        IconButton(
            onClick = onBack,
            enabled = canGoBack,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                Icons.Default.ArrowBack,
                contentDescription = "Lùi",
                tint = if (canGoBack) TuViGold else TuViIvoryDim.copy(alpha = 0.3f)
            )
        }
        IconButton(
            onClick = onForward,
            enabled = canGoForward,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                Icons.Default.ArrowForward,
                contentDescription = "Tiến",
                tint = if (canGoForward) TuViGold else TuViIvoryDim.copy(alpha = 0.3f)
            )
        }
        IconButton(
            onClick = onReload,
            modifier = Modifier.weight(1f)
        ) {
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
