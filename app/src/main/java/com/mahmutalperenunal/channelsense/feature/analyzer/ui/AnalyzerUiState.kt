package com.mahmutalperenunal.channelsense.feature.analyzer.ui

import com.mahmutalperenunal.channelsense.feature.analyzer.logic.ChannelRecommendation
import com.mahmutalperenunal.channelsense.wifi.connection.ConnectedWifiInfo
import com.mahmutalperenunal.channelsense.wifi.model.ChannelUsage
import com.mahmutalperenunal.channelsense.wifi.model.WifiBand

enum class ScanMode { QUICK, DETAILED }
enum class ScanProblem { PERMISSION, WIFI_DISABLED, LOCATION_DISABLED, THROTTLED, UNKNOWN }

data class AnalyzerUiState(
    val selectedBand: WifiBand = WifiBand.TWO_GHZ,
    val channels: List<ChannelUsage> = emptyList(),
    val recommendation: ChannelRecommendation? = null,
    val connectedWifi: ConnectedWifiInfo? = null,
    val isScanning: Boolean = false,
    val scanMode: ScanMode = ScanMode.QUICK,
    val completedSamples: Int = 0,
    val requestedSamples: Int = 1,
    val usesCachedResults: Boolean = false,
    val lastScanEpochMillis: Long? = null,
    val problem: ScanProblem? = null
)
