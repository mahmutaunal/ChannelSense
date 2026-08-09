package com.mahmutalperenunal.channelsense.feature.analyzer

import android.annotation.SuppressLint
import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mahmutalperenunal.channelsense.feature.analyzer.logic.ChannelAnalyzer
import com.mahmutalperenunal.channelsense.feature.analyzer.ui.AnalyzerUiState
import com.mahmutalperenunal.channelsense.feature.analyzer.ui.ScanMode
import com.mahmutalperenunal.channelsense.feature.analyzer.ui.ScanProblem
import com.mahmutalperenunal.channelsense.feature.settings.data.SettingsRepository
import com.mahmutalperenunal.channelsense.feature.settings.model.AppSettings
import com.mahmutalperenunal.channelsense.wifi.connection.ConnectedWifiProvider
import com.mahmutalperenunal.channelsense.wifi.model.WifiBand
import com.mahmutalperenunal.channelsense.wifi.model.WifiNetworkInfo
import com.mahmutalperenunal.channelsense.wifi.scanner.WifiScanOutcome
import com.mahmutalperenunal.channelsense.wifi.scanner.WifiScanner
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

class AnalyzerViewModel(application: Application) : AndroidViewModel(application) {
    private val scanner = WifiScanner(application)
    private val connectedWifiProvider = ConnectedWifiProvider(application)
    private val settingsRepo = SettingsRepository
    private var autoRefreshJob: Job? = null
    private var scanJob: Job? = null
    private var lastNetworks: List<WifiNetworkInfo> = emptyList()

    var uiState by mutableStateOf(AnalyzerUiState())
        private set

    init {
        viewModelScope.launch {
            settingsRepo.ensureInitialized(getApplication())
            val settings =
                runCatching { settingsRepo.getCurrentSettings() }.getOrDefault(AppFallbackSettings)
            uiState = uiState.copy(selectedBand = settings.defaultBand)
            settingsRepo.settingsFlow.collectLatest { handleAutoRefreshSetting(it.autoRefreshEnabled) }
        }
    }

    fun onBandSelected(band: WifiBand) {
        if (uiState.selectedBand == band) return
        uiState = uiState.copy(selectedBand = band)
        recalculate(lastNetworks, uiState.completedSamples.coerceAtLeast(1))
    }

    fun onRefreshRequested(mode: ScanMode = ScanMode.QUICK) {
        scanJob?.cancel()
        scanJob = viewModelScope.launch {
            val requested = if (mode == ScanMode.DETAILED) 3 else 1
            val collected = mutableListOf<WifiNetworkInfo>()
            var cached = false
            uiState = uiState.copy(
                isScanning = true,
                scanMode = mode,
                requestedSamples = requested,
                completedSamples = 0,
                problem = null,
                usesCachedResults = false,
                connectedWifi = connectedWifiProvider.current()
            )

            repeat(requested) { index ->
                when (val result = scanner.scan()) {
                    is WifiScanOutcome.Success -> {
                        collected += result.networks
                        cached = cached || !result.fresh
                        uiState =
                            uiState.copy(completedSamples = index + 1, usesCachedResults = cached)
                    }

                    WifiScanOutcome.PermissionRequired -> return@launch finishProblem(ScanProblem.PERMISSION)
                    WifiScanOutcome.WifiDisabled -> return@launch finishProblem(ScanProblem.WIFI_DISABLED)
                    WifiScanOutcome.LocationDisabled -> return@launch finishProblem(ScanProblem.LOCATION_DISABLED)
                    WifiScanOutcome.ThrottledOrUnavailable -> {
                        if (collected.isEmpty()) return@launch finishProblem(ScanProblem.THROTTLED)
                        cached = true
                    }

                    is WifiScanOutcome.Failure -> if (collected.isEmpty()) return@launch finishProblem(
                        ScanProblem.UNKNOWN
                    )
                }
                if (index < requested - 1) delay(5_000.milliseconds)
            }
            recalculate(collected, uiState.completedSamples.coerceAtLeast(1))
            uiState = uiState.copy(
                isScanning = false,
                usesCachedResults = cached,
                lastScanEpochMillis = System.currentTimeMillis(),
                connectedWifi = connectedWifiProvider.current()
            )
        }
    }

    private fun recalculate(networks: List<WifiNetworkInfo>, samples: Int) {
        val aggregated =
            networks.groupBy { it.bssid ?: "${it.ssid}-${it.frequency}" }.map { (_, values) ->
                val latest = values.maxByOrNull { it.timestampMicros } ?: values.first()
                latest.copy(rssi = values.map { it.rssi }.average().toInt())
            }
        if (aggregated.isNotEmpty()) lastNetworks = aggregated
        val connected = connectedWifiProvider.current()
        val analysis = ChannelAnalyzer.analyze(
            networks = if (aggregated.isNotEmpty()) aggregated else lastNetworks,
            band = uiState.selectedBand,
            sampleCount = samples,
            currentChannel = connected?.takeIf { it.band == uiState.selectedBand }?.channel
        )
        uiState = uiState.copy(
            channels = analysis.usages,
            recommendation = analysis.recommendation,
            connectedWifi = connected
        )
    }

    private fun finishProblem(problem: ScanProblem) {
        uiState = uiState.copy(isScanning = false, problem = problem)
    }

    private fun handleAutoRefreshSetting(enabled: Boolean) {
        autoRefreshJob?.cancel()
        if (!enabled) return
        autoRefreshJob = viewModelScope.launch {
            while (true) {
                delay(60_000.milliseconds); onRefreshRequested(ScanMode.QUICK)
            }
        }
    }

    @SuppressLint("EmptySuperCall")
    override fun onCleared() {
        autoRefreshJob?.cancel()
        scanJob?.cancel()
        connectedWifiProvider.close()
        super.onCleared()
    }

    companion object {
        private val AppFallbackSettings = AppSettings(WifiBand.TWO_GHZ, false)
    }
}
