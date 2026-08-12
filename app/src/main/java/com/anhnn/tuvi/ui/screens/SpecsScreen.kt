package com.anhnn.tuvi.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.anhnn.tuvi.domain.model.TuViChartInput
import com.anhnn.tuvi.ui.specs.SpecsViewModel
@Composable
fun SpecsScreen(
    viewModel: SpecsViewModel,
    input: TuViChartInput,
    onBack: () -> Boolean
) {
    val uiState = viewModel.uiState
    val isLoading = viewModel.isLoading
    val errorMessage = viewModel.errorMessage

    // Lưu trữ index của tháng đang được chọn (Mặc định chọn tháng đầu tiên index = 0)
    var selectedMonthIndex by remember { mutableStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Vận Hạn Chi Tiết Năm ${input.namXem}",
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(modifier = Modifier.height(16.dp))

        when {
            isLoading -> {
                Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            errorMessage != null -> {
                Text(text = "Lỗi: $errorMessage", color = MaterialTheme.colorScheme.error)
            }
            uiState != null && !uiState.timeline.isNullOrEmpty() -> {
                val timeline = uiState.timeline!!
                val currentSelectedData = timeline[selectedMonthIndex]

                // 1. Thông tin tóm tắt nhanh
                uiState.summary?.let { summary ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(text = "🌟 Tháng rực rỡ nhất: Tháng ${summary.bestMonth}", style = MaterialTheme.typography.bodyMedium)
                            Text(text = "⚠️ Tháng cần cẩn trọng: Tháng ${summary.worstMonth}", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 2. Biểu đồ tương tác
                Text(text = "Biểu đồ biến động 12 tháng (Chạm vào để xem chi tiết)", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))

                VanHanInteractiveLineChart(
                    timeline = timeline,
                    selectedIndex = selectedMonthIndex,
                    onMonthSelected = { index ->
                        selectedMonthIndex = index
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // 3. Card chi tiết bóc tách điểm (Breakdown) của tháng đang chọn
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Chi tiết ${currentSelectedData.thangTen} (${currentSelectedData.cungTen} - ${currentSelectedData.cungChu})",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "⭐ Điểm tổng kết: ${currentSelectedData.normalizedScore} / 10", style = MaterialTheme.typography.bodyLarge)

                        Spacer(modifier = Modifier.height(8.dp))
                        Divider()
                        Spacer(modifier = Modifier.height(8.dp))

                        // Bóc tách chi tiết điểm từ object breakdown
                        val bd = currentSelectedData.breakdown
                        Text(text = "• Chính tinh: ${bd.chinhTinh}")
                        Text(text = "• Cát tinh: ${bd.catTinh}")
                        Text(text = "• Sát tinh: ${bd.satTinh}")
                        Text(text = "• Sao lưu: ${bd.saoLuu}")
                    }
                }
            }
        }
    }
}