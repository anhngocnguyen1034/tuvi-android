package com.example.tuvi.ui.screens

import androidx.compose.animation.animateContentSize
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.anhnn.ads.BannerAd
import com.example.tuvi.R
import com.example.tuvi.ads.AdNames
import com.example.tuvi.domain.model.SavedChart
import com.example.tuvi.presentation.SavedChartsViewModel
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedChartsScreen(
    onBack: () -> Unit,
    onOpenChart: (SavedChart) -> Unit,
    viewModel: SavedChartsViewModel = viewModel(factory = SavedChartsViewModel.Factory)
) {
    val charts by viewModel.charts.collectAsStateWithLifecycle()
    val groups by viewModel.groups.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedGroup by viewModel.selectedGroup.collectAsStateWithLifecycle()

    val allGroupLabel = stringResource(R.string.saved_group_all)
    val allGroups = listOf(allGroupLabel) + groups

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.saved_screen_title),
                        color = TuViGold,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            painter = painterResource(R.drawable.ic_back),
                            contentDescription = stringResource(R.string.settings_back),
                            tint = TuViGold
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = Color.Transparent,
                )
            )
        },
        containerColor = TuViNavy
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                placeholder = { Text(stringResource(R.string.saved_search_placeholder), color = TuViIvoryDim) },
                leadingIcon = {
                    Icon(
                        painter = painterResource(R.drawable.ic_search),
                        contentDescription = null,
                        tint = TuViGold
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setSearchQuery("") }) {
                            Icon(
                                painter = painterResource(R.drawable.ic_close),
                                contentDescription = stringResource(R.string.saved_cd_clear_search),
                                tint = TuViIvoryDim
                            )
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = TuViGold,
                    unfocusedBorderColor = TuViNavyCard,
                    focusedTextColor = TuViIvory,
                    unfocusedTextColor = TuViIvory,
                    cursorColor = TuViGold,
                    focusedContainerColor = TuViNavyLight,
                    unfocusedContainerColor = TuViNavyLight
                ),
                singleLine = true
            )

            // Group filter chips
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                items(allGroups, key = { it }) { group ->
                    FilterChip(
                        selected = selectedGroup == group,
                        onClick = { viewModel.setSelectedGroup(group) },
                        label = { Text(group, fontSize = 13.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = TuViGold,
                            selectedLabelColor = TuViNavy,
                            containerColor = TuViNavyCard,
                            labelColor = TuViIvoryDim
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = selectedGroup == group,
                            selectedBorderColor = TuViGoldDark,
                            borderColor = TuViNavyCard
                        )
                    )
                }
            }

            // Count
            Text(
                text = stringResource(R.string.saved_chart_count, charts.size),
                color = TuViIvoryDim,
                fontSize = 12.sp,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )

            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                if (charts.isEmpty()) {
                    EmptyState(
                        hasSearch = searchQuery.isNotBlank() || selectedGroup != allGroupLabel
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(charts, key = { it.id }) { chart ->
                            SwipeToDismissChartItem(
                                chart = chart,
                                onDelete = { viewModel.delete(chart.id) },
                                onClick = { onOpenChart(chart) }
                            )
                        }
                    }
                }
            }

            // Banner đáy nằm TRONG content, cùng nền TuViNavy với cả màn → không tạo
            // đường lệch màu như Scaffold.bottomBar (vốn là surface riêng).
            BannerAd(
                adName = AdNames.SAVED_BANNER,
                modifier = Modifier.navigationBarsPadding()
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeToDismissChartItem(
    chart: SavedChart,
    onDelete: () -> Unit,
    onClick: () -> Unit
) {
    var showConfirm by remember { mutableStateOf(false) }
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                showConfirm = true
            }
            false // không dismiss tự động, chờ confirm
        }
    )

    if (showConfirm) {
        AlertDialog(
            onDismissRequest = { showConfirm = false },
            title = { Text(stringResource(R.string.saved_delete_dialog_title), color = TuViIvory) },
            text = { Text(stringResource(R.string.saved_delete_dialog_message, chart.ten), color = TuViIvoryDim) },
            confirmButton = {
                TextButton(onClick = { showConfirm = false; onDelete() }) {
                    Text(stringResource(R.string.btn_delete), color = TuViRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirm = false }) {
                    Text(stringResource(R.string.btn_cancel), color = TuViIvoryDim)
                }
            },
            containerColor = TuViNavyCard
        )
    }

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(12.dp))
                    .background(TuViRed.copy(alpha = 0.8f)),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_trash),
                    contentDescription = stringResource(R.string.saved_cd_delete),
                    tint = Color.White,
                    modifier = Modifier.padding(end = 24.dp)
                )
            }
        }
    ) {
        SavedChartCard(chart = chart, onClick = onClick)
    }
}

@Composable
private fun SavedChartCard(chart: SavedChart, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = TuViNavyLight),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    brush = Brush.horizontalGradient(listOf(TuViGoldDark, TuViNavyCard)),
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar circle
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(listOf(TuViNavyCard, TuViNavy))
                    )
                    .border(1.5.dp, TuViGold, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                val initial = chart.ten.firstOrNull()?.uppercaseChar()?.toString() ?: "?"
                Text(
                    text = initial,
                    color = TuViGold,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            }

            Spacer(Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = chart.ten,
                    color = TuViIvory,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(3.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    InfoTag(chart.ngaySinh)
                    InfoTag(chart.gioiTinh)
                }
                Spacer(Modifier.height(3.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    GroupBadge(chart.nhom)
                    Text(
                        text = formatDate(chart.ngayLuu),
                        color = TuViIvoryDim,
                        fontSize = 11.sp
                    )
                }
            }

            Icon(
                painter = painterResource(R.drawable.ic_person),
                contentDescription = null,
                tint = TuViGold.copy(alpha = 0.4f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun InfoTag(text: String) {
    Text(
        text = text,
        color = TuViIvoryDim,
        fontSize = 12.sp
    )
}

@Composable
private fun GroupBadge(nhom: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(TuViGoldDark.copy(alpha = 0.25f))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = nhom,
            color = TuViGoldLight,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun EmptyState(hasSearch: Boolean) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {

            Image(
                painterResource(R.drawable.empty),
                contentDescription = null,
                modifier = Modifier.size(200.dp)
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = if (hasSearch) stringResource(R.string.saved_empty_search_message) else stringResource(R.string.saved_empty_state_message),
                color = TuViIvoryDim,
                fontSize = 16.sp,
                fontFamily = LoraFontFamily
            )
            if (!hasSearch) {
                Spacer(Modifier.height(6.dp))
                Text(
                    text = stringResource(R.string.saved_empty_state_hint),
                    color = TuViIvoryDim.copy(alpha = 0.6f),
                    fontSize = 13.sp
                )
            }
        }
    }
}

private fun formatDate(millis: Long): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    return sdf.format(Date(millis))
}
