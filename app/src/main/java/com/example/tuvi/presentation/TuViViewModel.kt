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
import kotlinx.coroutines.launch

class TuViViewModel(
    private val getTuViChart: GetTuViChartUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<TuViUiState>(TuViUiState.Idle)
    val uiState: StateFlow<TuViUiState> = _uiState

    private val _lastInput = MutableStateFlow<TuViChartInput?>(null)
    val lastInput: StateFlow<TuViChartInput?> = _lastInput

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
        viewModelScope.launch {
            _uiState.value = TuViUiState.Loading
            getTuViChart(input)
                .onSuccess { _uiState.value = TuViUiState.Success(it) }
                .onFailure { _uiState.value = TuViUiState.Error(it.message ?: "Unknown error") }
        }
    }

    fun resetState() {
        _uiState.value = TuViUiState.Idle
    }

    fun loadSavedChart(input: TuViChartInput, chart: TuViChart) {
        _lastInput.value = input
        _uiState.value = TuViUiState.Success(chart)
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer { TuViViewModel(AppContainer.getTuViChartUseCase) }
        }
    }
}
