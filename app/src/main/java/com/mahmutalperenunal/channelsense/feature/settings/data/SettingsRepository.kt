package com.mahmutalperenunal.channelsense.feature.settings.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.mahmutalperenunal.channelsense.feature.settings.model.AppSettings
import com.mahmutalperenunal.channelsense.wifi.model.WifiBand
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first

private val Context.settingsDataStore by preferencesDataStore("channel_sense_settings")

object SettingsRepository {

    private const val BAND_2_4 = "2_4"
    private const val BAND_5 = "5"
    private const val BAND_6 = "6"

    private lateinit var appContext: Context

    private object Keys {
        val DEFAULT_BAND = stringPreferencesKey("default_band")
        val AUTO_REFRESH = booleanPreferencesKey("auto_refresh_enabled")
    }

    fun ensureInitialized(context: Context) {
        if (!::appContext.isInitialized) {
            appContext = context.applicationContext
        }
    }

    val settingsFlow: Flow<AppSettings>
        get() = appContext.settingsDataStore.data.map { prefs ->
            val bandString = prefs[Keys.DEFAULT_BAND]
            val band = when (bandString) {
                BAND_2_4 -> WifiBand.TWO_GHZ
                BAND_5 -> WifiBand.FIVE_GHZ
                BAND_6 -> WifiBand.SIX_GHZ
                else -> WifiBand.TWO_GHZ
            }

            val autoRefresh = prefs[Keys.AUTO_REFRESH] ?: false

            AppSettings(
                defaultBand = band,
                autoRefreshEnabled = autoRefresh
            )
        }

    suspend fun getCurrentSettings(): AppSettings {
        return settingsFlow.first()
    }

    suspend fun updateSettings(transform: (AppSettings) -> AppSettings) {
        appContext.settingsDataStore.edit { prefs ->
            val current = AppSettings(
                defaultBand = when (prefs[Keys.DEFAULT_BAND]) {
                    BAND_2_4 -> WifiBand.TWO_GHZ
                    BAND_5 -> WifiBand.FIVE_GHZ
                    BAND_6 -> WifiBand.SIX_GHZ
                    else -> WifiBand.TWO_GHZ
                },
                autoRefreshEnabled = prefs[Keys.AUTO_REFRESH] ?: false
            )

            val newSettings = transform(current)

            prefs[Keys.DEFAULT_BAND] = when (newSettings.defaultBand) {
                WifiBand.TWO_GHZ -> BAND_2_4
                WifiBand.FIVE_GHZ -> BAND_5
                WifiBand.SIX_GHZ -> BAND_6
            }

            prefs[Keys.AUTO_REFRESH] = newSettings.autoRefreshEnabled
        }
    }
}