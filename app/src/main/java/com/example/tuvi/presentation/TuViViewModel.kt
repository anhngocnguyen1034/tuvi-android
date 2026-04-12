package com.example.tuvi.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.tuvi.di.AppContainer
import com.example.tuvi.domain.model.TuViChart
import com.example.tuvi.domain.model.TuViChartInput
import com.example.tuvi.domain.usecase.GetTuViChartUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TuViViewModel(
    private val getTuViChart: GetTuViChartUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<TuViUiState>(TuViUiState.Idle)
    val uiState: StateFlow<TuViUiState> = _uiState

    private val _lastInput = MutableStateFlow<TuViChartInput?>(null)
    val lastInput: StateFlow<TuViChartInput?> = _lastInput

    /** Lá số mở từ mục đã lưu → coi như đã có trong DB (icon ic_saved). */
    private val _openedFromSavedLibrary = MutableStateFlow(false)
    val openedFromSavedLibrary: StateFlow<Boolean> = _openedFromSavedLibrary.asStateFlow()

    /** Id bản ghi trong `saved_charts`; null = chưa lưu DB (icon ic_save). */
    private val _savedChartId = MutableStateFlow<Long?>(null)
    val savedChartId: StateFlow<Long?> = _savedChartId.asStateFlow()

    fun getTuVi(
        ten: String,
        ngay: Int,
        thang: Int,
        nam: Int,
        namXem: Int,
        gio: Int,
        phut: Int,
        gioiTinh: Int,
        duongLich: Boolean = true
    ) {
        val input = TuViChartInput(ten, ngay, thang, nam, namXem, gio, phut, gioiTinh, duongLich)
        _lastInput.value = input
        _openedFromSavedLibrary.value = false
        _savedChartId.value = null
        viewModelScope.launch {
            _uiState.value = TuViUiState.Loading
            getTuViChart(input)
                .onSuccess { _uiState.value = TuViUiState.Success(it) }
                .onFailure { _uiState.value = TuViUiState.Error(it.message ?: "Unknown error") }
        }
    }

    fun resetState() {
        _uiState.value = TuViUiState.Idle
        _openedFromSavedLibrary.value = false
        _savedChartId.value = null
    }

    fun loadSavedChart(input: TuViChartInput, chart: TuViChart, savedChartId: Long) {
        _lastInput.value = input
        _openedFromSavedLibrary.value = true
        _savedChartId.value = savedChartId
        _uiState.value = TuViUiState.Success(chart)
    }

    fun setSavedChartId(id: Long?) {
        _savedChartId.value = id
    }

    /** Sau khi xóa khỏi thư viện: bỏ cờ “mở từ đã lưu”. */
    fun markChartRemovedFromLibrary() {
        _savedChartId.value = null
        _openedFromSavedLibrary.value = false
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer { TuViViewModel(AppContainer.getTuViChartUseCase) }
        }
    }
}
