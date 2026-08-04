package com.anhnn.tuvi.presentation

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.anhnn.tuvi.TuViApplication
import androidx.compose.runtime.Immutable
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@Immutable
data class SettingsUiState(
    val themeDark: Boolean,
    val notifHoliday: Boolean = true,
    val notifLunar: Boolean = true,
)

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = (application as TuViApplication).userPreferencesRepository

    val uiState: StateFlow<SettingsUiState> = combine(
        repo.themeDarkFlow,
        repo.notifHolidayFlow,
        repo.notifLunarFlow,
    ) { dark, holiday, lunar ->
        SettingsUiState(themeDark = dark, notifHoliday = holiday, notifLunar = lunar)
    }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            SettingsUiState(themeDark = false)
        )

    fun setThemeDark(dark: Boolean) {
        viewModelScope.launch {
            repo.setThemeDark(dark)
        }
    }

    fun setNotifHoliday(enabled: Boolean) {
        viewModelScope.launch { repo.setNotifHoliday(enabled) }
    }

    fun setNotifLunar(enabled: Boolean) {
        viewModelScope.launch { repo.setNotifLunar(enabled) }
    }
}
