package com.mahmutalperenunal.channelsense.feature.settings.ui

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mahmutalperenunal.channelsense.feature.settings.data.SettingsRepository
import com.mahmutalperenunal.channelsense.wifi.model.WifiBand
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SettingsViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repo = SettingsRepository

    var uiState by mutableStateOf(SettingsUiState())
        private set

    init {
        viewModelScope.launch {
            repo.ensureInitialized(getApplication())

            repo.settingsFlow.collectLatest { settings ->
                uiState = SettingsUiState(
                    defaultBand = settings.defaultBand,
                    autoRefreshEnabled = settings.autoRefreshEnabled
                )
            }
        }
    }

    fun onDefaultBandChanged(band: WifiBand) {
        viewModelScope.launch {
            repo.updateSettings { current ->
                current.copy(defaultBand = band)
            }
        }
    }

    fun onAutoRefreshChanged(enabled: Boolean) {
        viewModelScope.launch {
            repo.updateSettings { current ->
                current.copy(autoRefreshEnabled = enabled)
            }
        }
    }
}