package com.example.tuvi.ui.browser

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tuvi.data.local.BookmarkDao
import com.example.tuvi.data.local.BookmarkItemEntity
import com.example.tuvi.di.AppContainer
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

class BookmarkViewModel(private val dao: BookmarkDao) : ViewModel() {

    val bookmarks: StateFlow<List<BookmarkItemEntity>> = dao.getAllBookmarks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun delete(id: Long) = viewModelScope.launch { dao.deleteBookmarkById(id) }

    fun updateTitle(id: Long, newTitle: String) {
        if (newTitle.isNotBlank()) viewModelScope.launch { dao.updateTitle(id, newTitle.trim()) }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                BookmarkViewModel(AppContainer.bookmarkDao) as T
        }
    }
}

// ── Screen ───────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookmarkScreen(
    onBack: () -> Unit,
    onOpenUrl: (String) -> Unit,
    vm: BookmarkViewModel = viewModel(factory = BookmarkViewModel.Factory)
) {
    val bookmarks by vm.bookmarks.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = TuViNavy,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Star, contentDescription = null,
                            tint = TuViGold, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Dấu trang", color = TuViGold, fontWeight = FontWeight.Bold)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại", tint = TuViGold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = TuViNavy)
            )
        }
    ) { padding ->
        if (bookmarks.isEmpty()) {
            BookmarkEmptyState(Modifier.fillMaxSize().padding(padding))
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(bookmarks, key = { it.id }) { item ->
                    SwipeableBookmarkItem(
                        item = item,
                        onTap = { onOpenUrl(item.url) },
                        onDelete = { vm.delete(item.id) },
                        onEditTitle = { vm.updateTitle(item.id, it) }
                    )
                }
            }
        }
    }
}

// ── Swipe-to-delete wrapper ───────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeableBookmarkItem(
    item: BookmarkItemEntity,
    onTap: () -> Unit,
    onDelete: () -> Unit,
    onEditTitle: (String) -> Unit
) {
    var showEdit by remember { mutableStateOf(false) }

    if (showEdit) {
        EditTitleDialog(
            currentTitle = item.title,
            onConfirm = { showEdit = false; onEditTitle(it) },
            onDismiss = { showEdit = false }
        )
    }

    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) { onDelete(); true }
            else false
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            Box(
                modifier = Modifier.fillMaxSize()
                    .clip(RoundedCornerShape(12.dp))
                    .background(TuViRed.copy(alpha = 0.85f)),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(Icons.Default.Delete, contentDescription = "Xoá",
                    tint = Color.White, modifier = Modifier.padding(end = 24.dp))
            }
        }
    ) {
        BookmarkItem(item = item, onTap = onTap, onLongPress = { showEdit = true })
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun BookmarkItem(
    item: BookmarkItemEntity,
    onTap: () -> Unit,
    onLongPress: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .clip(RoundedCornerShape(12.dp))
            .background(TuViNavyLight)
            .border(1.dp, TuViNavyCard, RoundedCornerShape(12.dp))
            .combinedClickable(onClick = onTap, onLongClick = onLongPress)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Favicon badge
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(TuViGoldDark.copy(alpha = 0.18f))
                .border(1.dp, TuViGoldDark.copy(alpha = 0.4f), RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(item.url.toFaviconEmoji(), fontSize = 20.sp)
        }

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.title,
                color = TuViIvory,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = item.url.toDomainDisplay(),
                color = TuViIvoryDim,
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Icon(
            Icons.Default.Star,
            contentDescription = null,
            tint = TuViGold.copy(alpha = 0.5f),
            modifier = Modifier.size(16.dp)
        )
    }
}

// ── Edit title dialog ─────────────────────────────────────────────────────────

@Composable
private fun EditTitleDialog(
    currentTitle: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var text by remember { mutableStateOf(currentTitle) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Chỉnh sửa tiêu đề", color = TuViGold, fontWeight = FontWeight.Bold) },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = TuViGold,
                    unfocusedBorderColor = TuViNavyLight,
                    focusedTextColor = TuViIvory,
                    unfocusedTextColor = TuViIvory,
                    cursorColor = TuViGold,
                    focusedContainerColor = TuViNavyCard,
                    unfocusedContainerColor = TuViNavyCard
                )
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(text) }) {
                Text("Lưu", color = TuViGold, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Huỷ", color = TuViIvoryDim) }
        },
        containerColor = TuViNavyCard
    )
}

@Composable
private fun BookmarkEmptyState(modifier: Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.Star, contentDescription = null,
                tint = TuViGoldDark, modifier = Modifier.size(52.dp))
            Spacer(Modifier.height(12.dp))
            Text("Chưa có dấu trang nào", color = TuViIvoryDim, fontSize = 16.sp)
            Spacer(Modifier.height(4.dp))
            Text("Nhấn * trên thanh địa chỉ để lưu trang",
                color = TuViIvoryDim.copy(alpha = 0.5f), fontSize = 12.sp)
        }
    }
}

private fun String.toFaviconEmoji(): String {
    val domain = toDomainDisplay().lowercase()
    return when {
        domain.contains("google")    -> "G"
        domain.contains("youtube")   -> "YT"
        domain.contains("facebook")  -> "FB"
        domain.contains("wikipedia") -> "W"
        domain.contains("github")    -> "GH"
        domain.contains("reddit")    -> "R"
        domain.contains("twitter") || domain.contains("x.com") -> "X"
        else -> "Web"
    }
}

private fun String.toDomainDisplay(): String = try {
    android.net.Uri.parse(this).host?.removePrefix("www.") ?: this
} catch (_: Exception) { this }
