package com.example.tuvi.data.preferences

import android.content.Context
import android.provider.Settings
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.userPrefsDataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

class UserPreferencesRepository(context: Context) {

    private val appContext = context.applicationContext
    private val dataStore = appContext.userPrefsDataStore

    /**
     * ANDROID_ID — định danh thiết bị (scoped theo signing key từ API 26), sống qua gỡ-cài-lại.
     * Lưu ý: client thuần vẫn reset được bằng factory reset / clear-data và giả mạo được trên máy root.
     */
    private val currentDeviceId: String =
        Settings.Secure.getString(appContext.contentResolver, Settings.Secure.ANDROID_ID).orEmpty()

    companion object {
        private val KEY_THEME_DARK = booleanPreferencesKey("theme_dark")
        private val KEY_LOCALE = stringPreferencesKey("app_locale")
        private val KEY_NOTIF_HOLIDAY = booleanPreferencesKey("notif_holiday")
        private val KEY_NOTIF_LUNAR = booleanPreferencesKey("notif_lunar")
        private val KEY_AI_USED = booleanPreferencesKey("ai_used")
        private val KEY_AI_USED_DEVICE = stringPreferencesKey("ai_used_device_id")

        const val LOCALE_VI = "vi"
        const val LOCALE_EN = "en"
    }

    val themeDarkFlow: Flow<Boolean> = dataStore.data.map { prefs -> prefs[KEY_THEME_DARK] ?: true }

    val localeTagFlow: Flow<String> = dataStore.data.map { prefs ->
        prefs[KEY_LOCALE] ?: LOCALE_VI
    }

    val notifHolidayFlow: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[KEY_NOTIF_HOLIDAY] ?: true
    }

    val notifLunarFlow: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[KEY_NOTIF_LUNAR] ?: true
    }

    suspend fun initialSnapshot(): Pair<Boolean, String> {
        val prefs = dataStore.data.first()
        return Pair(
            prefs[KEY_THEME_DARK] ?: true,
            prefs[KEY_LOCALE] ?: LOCALE_VI
        )
    }

    suspend fun setThemeDark(isDark: Boolean) {
        dataStore.edit { it[KEY_THEME_DARK] = isDark }
    }

    suspend fun setLocaleTag(tag: String) {
        dataStore.edit { it[KEY_LOCALE] = tag }
    }

    suspend fun setNotifHoliday(enabled: Boolean) {
        dataStore.edit { it[KEY_NOTIF_HOLIDAY] = enabled }
    }

    suspend fun setNotifLunar(enabled: Boolean) {
        dataStore.edit { it[KEY_NOTIF_LUNAR] = enabled }
    }

    /**
     * "Đã dùng AI" = đã có device id lưu và khớp với thiết bị hiện tại.
     * Fallback: cờ boolean cũ (`ai_used`) từ bản trước → coi như đã dùng trên thiết bị này.
     */
    val aiUsedFlow: Flow<Boolean> = dataStore.data.map { prefs ->
        val usedDevice = prefs[KEY_AI_USED_DEVICE]
        when {
            !usedDevice.isNullOrEmpty() -> usedDevice == currentDeviceId
            prefs[KEY_AI_USED] == true -> true   // legacy
            else -> false
        }
    }

    suspend fun isAiUsed(): Boolean = aiUsedFlow.first()

    suspend fun markAiUsed() {
        dataStore.edit {
            it[KEY_AI_USED_DEVICE] = currentDeviceId
            it[KEY_AI_USED] = true   // giữ tương thích ngược
        }
    }
}
