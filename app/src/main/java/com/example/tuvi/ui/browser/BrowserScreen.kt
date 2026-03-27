package com.example.tuvi.ui.browser

import android.content.Intent
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
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
    viewModel: BrowserViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val keyboard = LocalSoftwareKeyboardController.current

    // Khi WebView canGoBack, ấn Back điều hướng trong trang thay vì pop stack
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
                    if (uiState.isLoading) {
                        IconButton(onClick = { /* stop not exposed by WebView easily */ }) {
                            Icon(Icons.Default.Close, contentDescription = "Dừng", tint = TuViIvoryDim)
                        }
                    } else {
                        IconButton(onClick = { viewModel.reload() }) {
                            Icon(Icons.Default.Refresh, contentDescription = "Tải lại", tint = TuViGold)
                        }
                    }
                    IconButton(onClick = {
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, uiState.url)
                        }
                        context.startActivity(Intent.createChooser(intent, "Chia sẻ link"))
                    }) {
                        Icon(Icons.Default.Share, contentDescription = "Chia sẻ", tint = TuViGold)
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
            // Address bar
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

            // Progress bar
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

            // Error banner
            if (uiState.error != null) {
                ErrorBanner(message = uiState.error!!)
            }

            // WebView
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
            .border(
                width = 1.dp,
                color = TuViDivider.copy(alpha = 0.5f)
            )
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BottomNavButton(
            modifier = Modifier.weight(1f),
            icon = { Icon(Icons.Default.ArrowBack, contentDescription = "Lùi", tint = if (canGoBack) TuViGold else TuViIvoryDim.copy(alpha = 0.3f)) },
            enabled = canGoBack,
            onClick = onBack
        )
        BottomNavButton(
            modifier = Modifier.weight(1f),
            icon = { Icon(Icons.Default.ArrowForward, contentDescription = "Tiến", tint = if (canGoForward) TuViGold else TuViIvoryDim.copy(alpha = 0.3f)) },
            enabled = canGoForward,
            onClick = onForward
        )
        BottomNavButton(
            modifier = Modifier.weight(1f),
            icon = { Icon(Icons.Default.Refresh, contentDescription = "Tải lại", tint = TuViGold) },
            enabled = true,
            onClick = onReload
        )
    }
}

@Composable
private fun BottomNavButton(
    modifier: Modifier,
    icon: @Composable () -> Unit,
    enabled: Boolean,
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
    ) {
        icon()
    }
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
        Text(
            text = "⚠  $message",
            color = TuViRed,
            fontSize = 12.sp,
            modifier = Modifier.weight(1f)
        )
    }
}
