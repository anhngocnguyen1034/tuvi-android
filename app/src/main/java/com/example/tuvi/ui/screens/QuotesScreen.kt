package com.example.tuvi.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tuvi.R
import com.example.tuvi.domain.model.Quote
import com.example.tuvi.presentation.QuotesUiState
import com.example.tuvi.presentation.QuotesViewModel
import com.example.tuvi.ui.theme.LoraFontFamily
import com.example.tuvi.ui.theme.TuViGold
import com.example.tuvi.ui.theme.TuViGoldDark
import com.example.tuvi.ui.theme.TuViIvory
import com.example.tuvi.ui.theme.TuViIvoryDim
import com.example.tuvi.ui.theme.TuViNavy
import com.example.tuvi.ui.theme.TuViNavyCard
import com.example.tuvi.ui.theme.TuViNavyLight
import com.example.tuvi.ui.theme.TuViTheme
import com.example.tuvi.widget.QuoteWidgetController
import com.example.tuvi.widget.QuoteWidgetPinner
import com.example.tuvi.widget.QuoteWidgetSize
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun QuotesScreen(
    onBack: () -> Unit,
    viewModel: QuotesViewModel = viewModel(factory = QuotesViewModel.Factory),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var spotlightQuote by remember { mutableStateOf<Quote?>(null) }
    val appContext = LocalContext.current.applicationContext
    val scope = rememberCoroutineScope()
    var showSizeDialog by remember { mutableStateOf(false) }

    val onSetWidget: (Quote) -> Unit = { quote ->
        // Cập nhật luôn thẻ "Quote of the day" trên đầu màn cho khớp với widget.
        spotlightQuote = quote
        scope.launch {
            QuoteWidgetController.setWidgetQuote(appContext, quote.id)
            Toast.makeText(
                appContext,
                appContext.getString(R.string.quotes_set_widget_done),
                Toast.LENGTH_SHORT,
            ).show()
        }
    }

    if (showSizeDialog) {
        WidgetSizeDialog(
            onDismiss = { showSizeDialog = false },
            onPick = { size ->
                showSizeDialog = false
                val ok = QuoteWidgetPinner.pin(appContext, size)
                if (!ok) {
                    Toast.makeText(
                        appContext,
                        appContext.getString(R.string.quote_widget_unsupported),
                        Toast.LENGTH_LONG,
                    ).show()
                }
            },
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(TuViNavy)
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        QuotesTopBar(onBack = onBack, onAddWidget = { showSizeDialog = true })

        when (val state = uiState) {
            QuotesUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = TuViGold)
                }
            }

            is QuotesUiState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(state.message, color = TuViIvoryDim, textAlign = TextAlign.Center)
                        Spacer(Modifier.height(12.dp))
                        TextButton(onClick = viewModel::load) {
                            Text(stringResource(R.string.quotes_retry), color = TuViGold)
                        }
                    }
                }
            }

            is QuotesUiState.Success -> {
                val hero = spotlightQuote ?: state.dailyQuote
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 24.dp),
                ) {
                    item(key = "hero") {
                        DailyQuoteCard(
                            quote = hero,
                            isDaily = spotlightQuote == null,
                            onSetWidget = onSetWidget,
                            onRandom = {
                                viewModel.randomQuote()?.let { spotlightQuote = it }
                            },
                        )
                    }

                    item(key = "search") {
                        QuotesSearchBar(
                            query = state.searchQuery,
                            onQueryChange = viewModel::setSearchQuery,
                        )
                    }

                    if (state.categories.isNotEmpty()) {
                        item(key = "categories") {
                            LazyRow(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                contentPadding = PaddingValues(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                item(key = "all") {
                                    CategoryChip(
                                        label = stringResource(R.string.quotes_category_all),
                                        selected = state.selectedCategory == null,
                                        onClick = { viewModel.setSelectedCategory(null) },
                                    )
                                }
                                items(state.categories, key = { it }) { category ->
                                    CategoryChip(
                                        label = category,
                                        selected = state.selectedCategory == category,
                                        onClick = {
                                            val next = if (state.selectedCategory == category) null else category
                                            viewModel.setSelectedCategory(next)
                                        },
                                    )
                                }
                            }
                        }
                    }

                    if (state.displayed.isEmpty()) {
                        item(key = "empty") {
                            Text(
                                text = stringResource(R.string.quotes_empty),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 24.dp, vertical = 32.dp),
                                color = TuViIvoryDim,
                                textAlign = TextAlign.Center,
                            )
                        }
                    } else {
                        items(state.displayed, key = { it.id }) { quote ->
                            QuoteListItem(
                                quote = quote,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                                onSetWidget = onSetWidget,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuotesTopBar(onBack: () -> Unit, onAddWidget: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.settings_back),
                tint = TuViGold,
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.quotes_screen_title),
                color = TuViGold,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = LoraFontFamily,
            )
            Text(
                text = stringResource(R.string.quotes_screen_subtitle),
                color = TuViIvoryDim,
                fontSize = 12.sp,
            )
        }
        IconButton(onClick = onAddWidget) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = stringResource(R.string.quote_widget_add),
                tint = TuViGold,
            )
        }
    }
}

@Composable
private fun WidgetSizeDialog(
    onDismiss: () -> Unit,
    onPick: (QuoteWidgetSize) -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = TuViNavyCard,
        title = {
            Text(
                text = stringResource(R.string.quote_widget_choose_size),
                color = TuViGold,
                fontWeight = FontWeight.Bold,
            )
        },
        text = {
            Column {
                WidgetSizeOption(R.string.quote_widget_size_small) { onPick(QuoteWidgetSize.SMALL) }
                WidgetSizeOption(R.string.quote_widget_size_medium) { onPick(QuoteWidgetSize.MEDIUM) }
                WidgetSizeOption(R.string.quote_widget_size_large) { onPick(QuoteWidgetSize.LARGE) }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.btn_cancel), color = TuViIvoryDim)
            }
        },
    )
}

@Composable
private fun WidgetSizeOption(labelRes: Int, onClick: () -> Unit) {
    Text(
        text = stringResource(labelRes),
        color = TuViIvory,
        fontSize = 16.sp,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp, horizontal = 4.dp),
    )
}

@Composable
private fun DailyQuoteCard(
    quote: Quote,
    isDaily: Boolean,
    onSetWidget: (Quote) -> Unit,
    onRandom: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.linearGradient(
                    listOf(
                        TuViNavyLight.copy(alpha = 0.95f),
                        TuViNavyCard.copy(alpha = 0.9f),
                    ),
                ),
            )
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    listOf(TuViGold.copy(alpha = 0.7f), TuViGoldDark.copy(alpha = 0.3f)),
                ),
                shape = RoundedCornerShape(20.dp),
            )
            .padding(20.dp),
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(
                        if (isDaily) R.string.quotes_daily_label else R.string.quotes_random_label,
                    ),
                    color = TuViGold,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                Row {
                    IconButton(onClick = onRandom, modifier = Modifier.size(36.dp)) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = stringResource(R.string.quotes_random),
                            tint = TuViGold,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                    IconButton(onClick = { onSetWidget(quote) }, modifier = Modifier.size(36.dp)) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = stringResource(R.string.quotes_set_widget),
                            tint = TuViGold,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
            }

            Text(
                text = "\u201C",
                color = TuViGold.copy(alpha = 0.35f),
                fontSize = 40.sp,
                lineHeight = 32.sp,
            )

            Text(
                text = quote.noiDung,
                color = TuViIvory,
                fontSize = 17.sp,
                lineHeight = 26.sp,
                fontFamily = LoraFontFamily,
            )

            quote.tiengAnh?.let { english ->
                Spacer(Modifier.height(10.dp))
                Text(
                    text = english,
                    color = TuViIvoryDim.copy(alpha = 0.85f),
                    fontSize = 13.sp,
                    lineHeight = 20.sp,
                    fontStyle = FontStyle.Italic,
                )
            }

            Spacer(Modifier.height(14.dp))

            Text(
                text = formatAuthor(quote.tacGia),
                color = TuViGold.copy(alpha = 0.9f),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.End,
            )
        }
    }
}

@Composable
private fun QuotesSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        placeholder = {
            Text(
                stringResource(R.string.quotes_search_hint),
                color = TuViIvoryDim.copy(alpha = 0.6f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        leadingIcon = {
            Icon(Icons.Default.Search, contentDescription = null, tint = TuViGold)
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Default.Clear, contentDescription = stringResource(R.string.btn_clear), tint = TuViIvoryDim)
                }
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = TuViIvory,
            unfocusedTextColor = TuViIvory,
            focusedBorderColor = TuViGold,
            unfocusedBorderColor = TuViIvoryDim.copy(alpha = 0.35f),
            cursorColor = TuViGold,
        ),
    )
}

@Composable
private fun CategoryChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            Text(
                text = label,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = TuViGold.copy(alpha = 0.22f),
            selectedLabelColor = TuViGold,
            containerColor = TuViNavyLight.copy(alpha = 0.7f),
            labelColor = TuViIvoryDim,
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            borderColor = TuViIvoryDim.copy(alpha = 0.25f),
            selectedBorderColor = TuViGold.copy(alpha = 0.6f),
        ),
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun QuoteListItem(
    quote: Quote,
    modifier: Modifier = Modifier,
    onSetWidget: (Quote) -> Unit = {},
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(TuViNavyLight.copy(alpha = 0.55f))
            .border(1.dp, TuViIvoryDim.copy(alpha = 0.12f), RoundedCornerShape(14.dp))
            .padding(16.dp),
    ) {
        Column {
            Text(
                text = quote.noiDung,
                color = TuViIvory,
                fontSize = 15.sp,
                lineHeight = 22.sp,
            )

            Spacer(Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = formatAuthor(quote.tacGia),
                    color = TuViGold.copy(alpha = 0.85f),
                    fontSize = 13.sp,
                    modifier = Modifier.weight(1f),
                )
                IconButton(
                    onClick = { onSetWidget(quote) },
                    modifier = Modifier.size(32.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = stringResource(R.string.quotes_set_widget),
                        tint = TuViGold.copy(alpha = 0.85f),
                        modifier = Modifier.size(18.dp),
                    )
                }
            }

            if (quote.tuKhoa.isNotEmpty()) {
                Spacer(Modifier.height(4.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    quote.tuKhoa.take(3).forEach { keyword ->
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(TuViGold.copy(alpha = 0.12f))
                                .padding(horizontal = 10.dp, vertical = 4.dp),
                        ) {
                            Text(keyword, color = TuViGold.copy(alpha = 0.85f), fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun formatAuthor(author: String?): String {
    val fallback = stringResource(R.string.quotes_unknown_author)
    val name = author?.takeIf { it.isNotBlank() }
    return if (name != null) "— $name" else "— $fallback"
}

@Preview(name = "Quote card – Dark", showBackground = true)
@Composable
private fun QuoteListItemDarkPreview() {
    TuViTheme(darkTheme = true) {
        QuoteListItem(
            quote = Quote(
                id = 1,
                noiDung = "Để hạnh phúc rất đơn giản, nhưng để đơn giản rất khó khăn.",
                tiengAnh = null,
                tacGia = "Rabindranath Tagore",
                tuKhoa = listOf("Đơn giản", "Hạnh phúc"),
            ),
        )
    }
}

@Preview(name = "Quote card – Light", showBackground = true)
@Composable
private fun QuoteListItemLightPreview() {
    TuViTheme(darkTheme = false) {
        DailyQuoteCard(
            quote = Quote(
                id = 1,
                noiDung = "Để hạnh phúc rất đơn giản, nhưng để đơn giản rất khó khăn.",
                tiengAnh = "It is very simple to be happy, but it is very difficult to be simple.",
                tacGia = "Rabindranath Tagore",
                tuKhoa = listOf("Đơn giản", "Hạnh phúc"),
            ),
            isDaily = true,
            onSetWidget = {},
            onRandom = {},
        )
    }
}
