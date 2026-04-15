package com.example.tuvi.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.tuvi.data.remote.dto.NgayInfoDto
import com.example.tuvi.data.remote.dto.ThangLichDto
import com.example.tuvi.di.AppContainer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar

sealed interface LichUiState {
    data object Loading : LichUiState
    data class Success(val data: ThangLichDto) : LichUiState
    data class Error(val message: String) : LichUiState
}

class LichViewModel(
    private val apiService: com.example.tuvi.data.remote.TuViApiService,
) : ViewModel() {

    private val _uiState = MutableStateFlow<LichUiState>(LichUiState.Loading)
    val uiState: StateFlow<LichUiState> = _uiState.asStateFlow()

    private val _selectedDay = MutableStateFlow<NgayInfoDto?>(null)
    val selectedDay: StateFlow<NgayInfoDto?> = _selectedDay.asStateFlow()

    /** Tháng/năm đang hiển thị. */
    private val _currentMonth = MutableStateFlow(Calendar.getInstance().get(Calendar.MONTH) + 1)
    val currentMonth: StateFlow<Int> = _currentMonth.asStateFlow()

    private val _currentYear = MutableStateFlow(Calendar.getInstance().get(Calendar.YEAR))
    val currentYear: StateFlow<Int> = _currentYear.asStateFlow()

    init {
        loadMonth(_currentMonth.value, _currentYear.value)
    }

    fun loadMonth(thang: Int, nam: Int) {
        _currentMonth.value = thang
        _currentYear.value = nam
        _selectedDay.value = null
        _uiState.value = LichUiState.Loading
        viewModelScope.launch {
            try {
                val result = apiService.getLichThang(nam, thang)
                _uiState.value = LichUiState.Success(result)
                // Tự chọn ngày hôm nay nếu đang ở tháng hiện tại
                val now = Calendar.getInstance()
                if (thang == now.get(Calendar.MONTH) + 1 && nam == now.get(Calendar.YEAR)) {
                    _selectedDay.value = result.ngay.getOrNull(now.get(Calendar.DAY_OF_MONTH) - 1)
                }
            } catch (e: Exception) {
                _uiState.value = LichUiState.Error(e.message ?: "Lỗi không xác định")
            }
        }
    }

    fun prevMonth() {
        val m = _currentMonth.value
        val y = _currentYear.value
        if (m == 1) loadMonth(12, y - 1) else loadMonth(m - 1, y)
    }

    fun nextMonth() {
        val m = _currentMonth.value
        val y = _currentYear.value
        if (m == 12) loadMonth(1, y + 1) else loadMonth(m + 1, y)
    }

    fun selectDay(day: NgayInfoDto) {
        _selectedDay.value = day
    }

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return LichViewModel(AppContainer.apiService) as T
            }
        }
    }
}
