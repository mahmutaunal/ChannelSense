package com.mahmutalperenunal.channelsense.feature.analyzer

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mahmutalperenunal.channelsense.feature.analyzer.logic.ChannelAnalyzer
import com.mahmutalperenunal.channelsense.feature.analyzer.ui.AnalyzerUiState
import com.mahmutalperenunal.channelsense.feature.settings.data.SettingsRepository
import com.mahmutalperenunal.channelsense.feature.settings.model.AppSettings
import com.mahmutalperenunal.channelsense.wifi.model.WifiBand
import com.mahmutalperenunal.channelsense.wifi.scanner.WifiScanner
import com.mahmutalperenunal.channelsense.wifi.scanner.WifiScanResultMapper
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class AnalyzerViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val scanner = WifiScanner(application.applicationContext)
    private val settingsRepo = SettingsRepository
    private var autoRefreshJob: kotlinx.coroutines.Job? = null

    var uiState by mutableStateOf(AnalyzerUiState())
        private set

    init {
        viewModelScope.launch {
            settingsRepo.ensureInitialized(getApplication())

            val settings = try {
                settingsRepo.getCurrentSettings()
            } catch (_: Exception) {
                AppFallbackSettings
            }

            uiState = uiState.copy(selectedBand = settings.defaultBand)
            onRefreshRequested()

            settingsRepo.settingsFlow.collectLatest { appSettings ->
                handleAutoRefreshSetting(appSettings.autoRefreshEnabled)
            }
        }
    }

    fun onBandSelected(band: WifiBand) {
        if (uiState.selectedBand == band) return
        uiState = uiState.copy(selectedBand = band)
        onRefreshRequested()
    }

    fun onRefreshRequested() {
        viewModelScope.launch {
            uiState = uiState.copy(isScanning = true, errorMessage = null)

            try {
                val networks = scanner.scan()
                val usage = WifiScanResultMapper.toChannelUsage(networks)
                val selected = WifiScanResultMapper.filterByBand(usage, uiState.selectedBand)

                val recommendation = ChannelAnalyzer.recommendChannel(
                    band = uiState.selectedBand,
                    channels = selected
                )

                uiState = uiState.copy(
                    isScanning = false,
                    channels = selected,
                    recommendation = recommendation
                )
            } catch (e: Exception) {
                uiState = uiState.copy(
                    isScanning = false,
                    errorMessage = e.message ?: "error_wifi_scan_failed"
                )
            }
        }
    }

    private fun handleAutoRefreshSetting(enabled: Boolean) {
        autoRefreshJob?.cancel()
        autoRefreshJob = null

        if (!enabled) return

        autoRefreshJob = viewModelScope.launch {
            while (true) {
                delay(30_000)
                onRefreshRequested()
            }
        }
    }

    override fun onCleared() {
        autoRefreshJob?.cancel()
        super.onCleared()
    }

    companion object {
        private val AppFallbackSettings = AppSettings(
            defaultBand = WifiBand.TWO_GHZ,
            autoRefreshEnabled = false
        )
    }
}