package com.example.tuvi.ui.browser

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.example.tuvi.R
import com.example.tuvi.data.local.HistoryDao
import com.example.tuvi.data.local.HistoryItemEntity
import com.example.tuvi.di.AppContainer
import com.example.tuvi.ui.theme.LoraFontFamily
import com.example.tuvi.ui.theme.TuViGold
import com.example.tuvi.ui.theme.TuViGoldDark
import com.example.tuvi.ui.theme.TuViGoldLight
import com.example.tuvi.ui.theme.TuViIvory
import com.example.tuvi.ui.theme.TuViIvoryDim
import com.example.tuvi.ui.theme.TuViNavy
import com.example.tuvi.ui.theme.TuViNavyCard
import com.example.tuvi.ui.theme.TuViNavyLight
import com.example.tuvi.ui.theme.TuViRed
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ── ViewModel ────────────────────────────────────────────────────────────────

class HistoryViewModel(private val dao: HistoryDao) : ViewModel() {

    val history: StateFlow<List<HistoryItemEntity>> = dao.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun delete(id: Long) = viewModelScope.launch { dao.deleteById(id) }

    fun clearAll() = viewModelScope.launch { dao.clearAll() }

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                HistoryViewModel(AppContainer.historyDao) as T
        }
    }
}

// ── Screen ───────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    onBack: () -> Unit,
    onOpenUrl: (String) -> Unit,
    vm: HistoryViewModel = viewModel(factory = HistoryViewModel.Factory)
) {
    val history by vm.history.collectAsStateWithLifecycle()
    var showClearDialog by remember { mutableStateOf(false) }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text(stringResource(R.string.history_clear_all_title), color = TuViIvory) },
            text = {
                Text(
                    stringResource(R.string.history_clear_all_message),
                    color = TuViIvoryDim
                )
            },
            confirmButton = {
                TextButton(onClick = { showClearDialog = false; vm.clearAll() }) {
                    Text(
                        stringResource(R.string.history_btn_clear_all),
                        color = TuViRed,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text(stringResource(R.string.btn_cancel), color = TuViIvoryDim)
                }
            },
            containerColor = TuViNavyCard
        )
    }

    Scaffold(
        containerColor = TuViNavy,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.history_screen_title),
                        color = TuViGold,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = stringResource(R.string.settings_back),
                            tint = TuViGold
                        )
                    }
                },
                actions = {
                    if (history.isNotEmpty()) {
                        IconButton(onClick = { showClearDialog = true }) {
                            Icon(
                                painter = painterResource(R.drawable.ic_trash),
                                contentDescription = stringResource(R.string.history_cd_clear_all),
                                tint = TuViRed
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = TuViNavy)
            )
        }
    ) { padding ->
        if (history.isEmpty()) {
            HistoryEmptyState(modifier = Modifier
                .fillMaxSize()
                .padding(padding))
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(history, key = { it.id }) { item ->
                    HistoryItem(
                        item = item,
                        onTap = { onOpenUrl(item.url) },
                        onDelete = { vm.delete(item.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun HistoryItem(
    item: HistoryItemEntity,
    onTap: () -> Unit,
    onDelete: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(TuViNavyLight)
            .border(1.dp, TuViNavyCard, RoundedCornerShape(10.dp))
            .clickable(onClick = onTap)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Favicon của website
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(TuViGoldDark.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            SubcomposeAsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(faviconUrl(item.url))
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                modifier = Modifier.size(22.dp),
                contentScale = ContentScale.Fit,
                error = {
                    Icon(
                        painter = painterResource(R.drawable.ic_clock_light),
                        contentDescription = null,
                        tint = TuViGoldDark,
                        modifier = Modifier.size(18.dp)
                    )
                },
                loading = {
                    Icon(
                        painter = painterResource(R.drawable.ic_clock_light),
                        contentDescription = null,
                        tint = TuViGoldDark,
                        modifier = Modifier.size(18.dp)
                    )
                }
            )
        }

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.title,
                color = TuViIvory,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = item.url,
                color = TuViIvoryDim,
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(Modifier.width(8.dp))

        Text(
            text = formatHistoryTime(item.timestamp),
            color = TuViGoldLight.copy(alpha = 0.6f),
            fontSize = 11.sp,
            fontStyle = FontStyle.Italic
        )

        // Menu 3 chấm
        Box {
            IconButton(
                onClick = { menuExpanded = true },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    Icons.Default.MoreVert,
                    contentDescription = null,
                    tint = TuViIvoryDim,
                    modifier = Modifier.size(18.dp)
                )
            }
            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.history_delete_item), color = TuViRed) },
                    onClick = {
                        menuExpanded = false
                        onDelete()
                    }
                )
            }
        }
    }
}

@Composable
private fun HistoryEmptyState(modifier: Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painter = painterResource(R.drawable.empty),
                contentDescription = null,
                modifier = Modifier.size(160.dp)
            )
            Spacer(Modifier.height(12.dp))
            Text(
                stringResource(R.string.history_empty_title),
                color = TuViIvoryDim,
                fontSize = 16.sp,
                fontFamily = LoraFontFamily
            )
        }
    }
}

private fun faviconUrl(url: String): String = try {
    val host = URL(url).host
    "https://www.google.com/s2/favicons?domain=$host&sz=64"
} catch (_: Exception) { "" }

private fun formatHistoryTime(millis: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - millis
    val oneDayMs = 24 * 60 * 60 * 1000L
    return if (diff < oneDayMs) {
        SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(millis))
    } else {
        SimpleDateFormat("HH:mm - dd/MM", Locale.getDefault()).format(Date(millis))
    }
}
