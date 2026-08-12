package com.anhnn.tuvi.ui.specs

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anhnn.tuvi.domain.model.TuViChartInput
import com.anhnn.tuvi.domain.model.TuViVanHanResult
import com.anhnn.tuvi.domain.repository.TuViRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SpecsViewModel @Inject constructor(
    val repository: TuViRepository
): ViewModel() {
    var uiState by mutableStateOf<TuViVanHanResult?>(null)
        private set
    var isLoading by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set

    /** Lấy biểu đồ vận hạn 12 tháng (timeline + summary). Không tốn quota AI.
     *  Luận giải văn bản AI (getTuViVanHanAiReading) sẽ được thêm sau khi cần. */
    fun fetchVanHanTimeline(input: TuViChartInput){
        viewModelScope.launch {
            isLoading = true
            try {
                uiState = repository.getTuViVanHan(input)
            } catch (e: Exception) {
                uiState = null
                errorMessage = e.localizedMessage
            } finally {
                isLoading = false
            }
        }
    }
}