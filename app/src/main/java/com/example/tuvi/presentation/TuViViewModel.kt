package com.example.tuvi.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.tuvi.di.AppContainer
import com.example.tuvi.domain.AiInterpretationUnavailableException
import com.example.tuvi.domain.model.CungSlug
import com.example.tuvi.domain.model.SavedChart
import com.example.tuvi.domain.model.TuViChart
import com.example.tuvi.domain.model.TuViChartInput
import com.example.tuvi.domain.usecase.DeleteSavedChartUseCase
import com.example.tuvi.domain.usecase.GetTuViChartUseCase
import com.example.tuvi.domain.usecase.GetTuViInterpretUseCase
import com.example.tuvi.domain.usecase.SaveChartUseCase
import kotlinx.serialization.json.Json
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TuViViewModel(
    private val getTuViChart: GetTuViChartUseCase,
    private val getTuViInterpret: GetTuViInterpretUseCase,
    private val saveChartUseCase: SaveChartUseCase,
    private val deleteChartUseCase: DeleteSavedChartUseCase,
    private val json: Json
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

    private val _aiInterpretLoading = MutableStateFlow(false)
    val aiInterpretLoading: StateFlow<Boolean> = _aiInterpretLoading.asStateFlow()

    /** Cung user đang chọn trên màn AI; null = chưa chọn. */
    private val _selectedCung = MutableStateFlow<CungSlug?>(null)
    val selectedCung: StateFlow<CungSlug?> = _selectedCung.asStateFlow()

    fun selectCung(cung: CungSlug) {
        _selectedCung.value = cung
    }

    fun getTuVi(
        ten: String,
        ngay: Int,
        thang: Int,
        nam: Int,
        namXem: Int,
        gio: Int,
        phut: Int,
        gioiTinh: Int,
        duongLich: Boolean = true,
    ) {
        val input = TuViChartInput(ten, ngay, thang, nam, namXem, gio, phut, gioiTinh, duongLich)
        _lastInput.value = input
        _openedFromSavedLibrary.value = false
        _savedChartId.value = null
        _aiInterpretLoading.value = false
        _selectedCung.value = null
        viewModelScope.launch {
            _uiState.value = TuViUiState.Loading
            getTuViChart(input)
                .onSuccess { _uiState.value = TuViUiState.Success(it) }
                .onFailure { mapFailureToUi(it) }
        }
    }

    /**
     * POST /api/interpret cho [cung] cụ thể; cache kết quả vào [TuViUiState.Success.aiReadings].
     * Lỗi trả qua [onError] (toast/snackbar).
     */
    fun fetchAiInterpretation(cung: CungSlug?, onError: (TuViError) -> Unit) {
        val input = _lastInput.value
        if (input == null) {
            onError(TuViError.AiNoInput)
            return
        }
        val success = _uiState.value as? TuViUiState.Success
        if (success == null) {
            onError(TuViError.AiNoChart)
            return
        }
        if (cung == null) {
            onError(TuViError.AiNoCung)
            return
        }
        viewModelScope.launch {
            _aiInterpretLoading.value = true
            try {
                getTuViInterpret(input, cung)
                    .onSuccess { interpretation ->
                        val base = _uiState.value as? TuViUiState.Success ?: return@onSuccess
                        _uiState.value = base.copy(
                            data = interpretation.chart,
                            aiReadings = base.aiReadings + (cung to interpretation.aiReading),
                        )
                    }
                    .onFailure { onError(mapThrowable(it)) }
            } finally {
                _aiInterpretLoading.value = false
            }
        }
    }

    private fun mapThrowable(t: Throwable): TuViError = when (t) {
        is AiInterpretationUnavailableException -> TuViError.AiUnavailable
        else -> t.message?.let { TuViError.Raw(it) } ?: TuViError.Unknown
    }

    private fun mapFailureToUi(t: Throwable) {
        _uiState.value = TuViUiState.Error(mapThrowable(t))
    }

    fun resetState() {
        _uiState.value = TuViUiState.Idle
        _openedFromSavedLibrary.value = false
        _savedChartId.value = null
        _aiInterpretLoading.value = false
        _selectedCung.value = null
    }

    fun loadSavedChart(input: TuViChartInput, chart: TuViChart, savedChartId: Long) {
        _lastInput.value = input
        _openedFromSavedLibrary.value = true
        _savedChartId.value = savedChartId
        _aiInterpretLoading.value = false
        _selectedCung.value = null
        _uiState.value = TuViUiState.Success(chart)
    }

    fun setSavedChartId(id: Long?) {
        _savedChartId.value = id
    }

    /** Sau khi xóa khỏi thư viện: bỏ cờ "mở từ đã lưu". */
    fun markChartRemovedFromLibrary() {
        _savedChartId.value = null
        _openedFromSavedLibrary.value = false
    }

    fun saveChart(nhom: String, onResult: (Result<Long>) -> Unit) {
        val input = _lastInput.value
        val chart = (_uiState.value as? TuViUiState.Success)?.data
        if (input == null || chart == null) {
            onResult(Result.failure(Exception("No data")))
            return
        }
        viewModelScope.launch {
            val saved = SavedChart(
                ten = input.ten,
                ngaySinh = "${input.ngay}/${input.thang}/${input.nam}",
                gioiTinh = if (input.gioiTinh == 1) "Nam" else "Nu",
                nhom = nhom,
                ngayLuu = System.currentTimeMillis(),
                inputJson = json.encodeToString(TuViChartInput.serializer(), input),
                chartJson = json.encodeToString(TuViChart.serializer(), chart)
            )
            saveChartUseCase(saved)
                .onSuccess { id -> setSavedChartId(id); onResult(Result.success(id)) }
                .onFailure { onResult(Result.failure(it)) }
        }
    }

    fun deleteChart(id: Long, onResult: (Result<Unit>) -> Unit) {
        viewModelScope.launch {
            runCatching { deleteChartUseCase(id) }
                .onSuccess { markChartRemovedFromLibrary(); onResult(Result.success(Unit)) }
                .onFailure { onResult(Result.failure(it)) }
        }
    }

    fun openSavedChart(saved: SavedChart, onResult: (Boolean) -> Unit) {
        runCatching {
            val chart = json.decodeFromString(TuViChart.serializer(), saved.chartJson)
            val input = json.decodeFromString(TuViChartInput.serializer(), saved.inputJson)
            loadSavedChart(input, chart, saved.id)
        }.onSuccess { onResult(true) }.onFailure { onResult(false) }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                TuViViewModel(
                    AppContainer.getTuViChartUseCase,
                    AppContainer.getTuViInterpretUseCase,
                    AppContainer.saveChartUseCase,
                    AppContainer.deleteSavedChartUseCase,
                    AppContainer.appJson
                )
            }
        }
    }
}
