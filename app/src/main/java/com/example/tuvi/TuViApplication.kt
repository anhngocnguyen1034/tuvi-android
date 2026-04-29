package com.example.tuvi

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.example.tuvi.data.preferences.UserPreferencesRepository
import com.example.tuvi.di.AppContainer
import com.example.tuvi.ui.theme.TuViComposeColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking

class TuViApplication : Application() {

    lateinit var userPreferencesRepository: UserPreferencesRepository
        private set

    var initialDark: Boolean = true
        private set

    override fun onCreate() {
        super.onCreate()
        userPreferencesRepository = UserPreferencesRepository(this)
        val savedDark = runBlocking(Dispatchers.IO) {
            val (dark, localeTag) = userPreferencesRepository.initialSnapshot()
            AppCompatDelegate.setDefaultNightMode(
                if (dark) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
            )
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(localeTag))
            dark
        }
        initialDark = savedDark
        TuViComposeColors.initIfNeeded(this, forceNight = savedDark)
        AppContainer.init(this)
    }
}
