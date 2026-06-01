package com.example.tuvi.ads

import android.content.Context
import android.util.Log
import com.example.tuvi.R
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import org.json.JSONObject

object RemoteConfigManager {

    private const val TAG = "RemoteConfig"

    // Keys
    const val KEY_ADS_ENABLED = "ads_enabled"
    const val KEY_INTER_MIN_INTERVAL_MS = "inter_min_interval_ms"

    // JSON map { "ad_name": "ca-app-pub-xxx/yyy", ... } — mỗi vị trí 1 ad unit riêng.
    const val KEY_AD_UNITS = "ad_units"

    // Google sample test units — fallback theo định dạng khi ad_name chưa có trong config.
    private const val TEST_INTER_UNIT_ID = "ca-app-pub-3940256099942544/1033173712"
    private const val TEST_NATIVE_UNIT_ID = "ca-app-pub-3940256099942544/2247696110"
    private const val TEST_BANNER_UNIT_ID = "ca-app-pub-3940256099942544/6300978111"

    private val config by lazy { Firebase.remoteConfig }

    // Cache JSON đã parse để không parse lại mỗi lần tra ad unit.
    @Volatile private var adUnitsJson: String? = null
    @Volatile private var adUnitsMap: Map<String, String> = emptyMap()

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

    /** Ad unit cho interstitial theo [adName]; fallback về test unit nếu chưa cấu hình. */
    fun interAdUnitId(adName: String): String = adUnitId(adName) ?: TEST_INTER_UNIT_ID

    /** Ad unit cho native theo [adName]; fallback về test unit nếu chưa cấu hình. */
    fun nativeAdUnitId(adName: String): String = adUnitId(adName) ?: TEST_NATIVE_UNIT_ID

    /** Ad unit cho banner theo [adName]; fallback về test unit nếu chưa cấu hình. */
    fun bannerAdUnitId(adName: String): String = adUnitId(adName) ?: TEST_BANNER_UNIT_ID

    /** Tra ad unit id theo tên từ JSON [KEY_AD_UNITS]; null nếu không có/để rỗng. */
    private fun adUnitId(adName: String): String? {
        val json = config.getString(KEY_AD_UNITS)
        if (json != adUnitsJson) {
            adUnitsJson = json
            adUnitsMap = parseAdUnits(json)
        }
        return adUnitsMap[adName]?.takeIf { it.isNotBlank() }
    }

    private fun parseAdUnits(json: String): Map<String, String> {
        if (json.isBlank()) return emptyMap()
        return runCatching {
            val obj = JSONObject(json)
            buildMap {
                obj.keys().forEach { key -> put(key, obj.optString(key)) }
            }
        }.getOrElse {
            Log.w(TAG, "ad_units parse failed: ${it.message}")
            emptyMap()
        }
    }

    private fun isDebuggable(context: Context): Boolean =
        (context.applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE) != 0
}
