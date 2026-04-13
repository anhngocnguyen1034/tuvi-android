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
    val localeTag: String
)

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = (application as TuViApplication).userPreferencesRepository

    val uiState: StateFlow<SettingsUiState> = combine(
        repo.themeDarkFlow,
        repo.localeTagFlow
    ) { dark, loc -> SettingsUiState(themeDark = dark, localeTag = loc) }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            SettingsUiState(themeDark = true, localeTag = UserPreferencesRepository.LOCALE_VI)
        )

    fun setThemeDark(dark: Boolean) {
        viewModelScope.launch {
            repo.setThemeDark(dark)
            AppCompatDelegate.setDefaultNightMode(
                if (dark) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
            )
        }
    }

    fun setLocaleTag(tag: String) {
        viewModelScope.launch {
            repo.setLocaleTag(tag)
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(tag))
        }
    }
}
