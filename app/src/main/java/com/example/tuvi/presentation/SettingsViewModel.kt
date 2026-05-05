package com.example.tuvi.presentation

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.tuvi.TuViApplication
import com.example.tuvi.data.preferences.UserPreferencesRepository
import androidx.compose.runtime.Immutable
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@Immutable
data class SettingsUiState(
    val themeDark: Boolean,
    val localeTag: String,
    val notifHoliday: Boolean = true,
    val notifLunar: Boolean = true,
)

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = (application as TuViApplication).userPreferencesRepository

    val uiState: StateFlow<SettingsUiState> = combine(
        repo.themeDarkFlow,
        repo.localeTagFlow,
        repo.notifHolidayFlow,
        repo.notifLunarFlow,
    ) { dark, loc, holiday, lunar ->
        SettingsUiState(themeDark = dark, localeTag = loc, notifHoliday = holiday, notifLunar = lunar)
    }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            SettingsUiState(themeDark = true, localeTag = UserPreferencesRepository.LOCALE_VI)
        )

    fun setThemeDark(dark: Boolean) {
        viewModelScope.launch {
            repo.setThemeDark(dark)
        }
    }

    fun setLocaleTag(tag: String) {
        viewModelScope.launch {
            repo.setLocaleTag(tag)
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(tag))
        }
    }

    fun setNotifHoliday(enabled: Boolean) {
        viewModelScope.launch { repo.setNotifHoliday(enabled) }
    }

    fun setNotifLunar(enabled: Boolean) {
        viewModelScope.launch { repo.setNotifLunar(enabled) }
    }
}
