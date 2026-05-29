package com.example.tuvi.ads

import android.content.Context
import android.util.Log
import com.example.tuvi.R
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings

object RemoteConfigManager {

    private const val TAG = "RemoteConfig"

    // Keys
    const val KEY_ADS_ENABLED = "ads_enabled"
    const val KEY_INTER_MIN_INTERVAL_MS = "inter_min_interval_ms"

    private val config by lazy { Firebase.remoteConfig }

    fun init(context: Context) {
        val settings = remoteConfigSettings {
            // Debug: fetch ngay khi gọi. Release: cache ~12h theo default của Firebase.
            minimumFetchIntervalInSeconds = if (isDebuggable(context)) 0 else 3600
        }
        config.setConfigSettingsAsync(settings)
        config.setDefaultsAsync(R.xml.remote_config_defaults)
        fetch()
    }

    fun fetch() {
        config.fetchAndActivate()
            .addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w(TAG, "fetch failed", task.exception)
                }
            }
    }

    fun adsEnabled(): Boolean = config.getBoolean(KEY_ADS_ENABLED)

    fun interMinIntervalMs(): Long = config.getLong(KEY_INTER_MIN_INTERVAL_MS)

    private fun isDebuggable(context: Context): Boolean =
        (context.applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE) != 0
}
