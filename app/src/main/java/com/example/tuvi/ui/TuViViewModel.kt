package com.example.tuvi.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tuvi.model.TuViRequest
import com.example.tuvi.model.TuViResponse
import com.example.tuvi.network.NetworkModule
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed interface TuViUiState {
    object Idle : TuViUiState
    object Loading : TuViUiState
    data class Success(val data: TuViResponse) : TuViUiState
    data class Error(val message: String) : TuViUiState
}

class TuViViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<TuViUiState>(TuViUiState.Idle)
    val uiState: StateFlow<TuViUiState> = _uiState

    fun getTuVi(
        ten: String,
        ngay: Int,
        thang: Int,
        nam: Int,
        gio: Int,
        phut: Int,
        gioiTinh: Int
    ) {
        viewModelScope.launch {
            _uiState.value = TuViUiState.Loading
            try {
                val request = TuViRequest(
                    ten = ten,
                    ngay = ngay,
                    thang = thang,
                    nam = nam,
                    gio = gio,
                    phut = phut,
                    gio_sinh = ((gio + 1) / 2) % 12 + 1,
                    gioi_tinh = gioiTinh
                )
                val response = NetworkModule.apiService.getTuVi(request)
                _uiState.value = TuViUiState.Success(response)
            } catch (e: Exception) {
                _uiState.value = TuViUiState.Error(e.message ?: "Unknown Error occurred")
            }
        }
    }

    fun resetState() {
        _uiState.value = TuViUiState.Idle
    }
}
