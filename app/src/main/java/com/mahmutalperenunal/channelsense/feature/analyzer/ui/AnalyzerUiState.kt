package com.mahmutalperenunal.channelsense.feature.analyzer.ui

import com.mahmutalperenunal.channelsense.feature.analyzer.logic.ChannelRecommendation
import com.mahmutalperenunal.channelsense.wifi.model.ChannelUsage
import com.mahmutalperenunal.channelsense.wifi.model.WifiBand

data class AnalyzerUiState(
    val selectedBand: WifiBand = WifiBand.TWO_GHZ,
    val channels: List<ChannelUsage> = emptyList(),
    val recommendation: ChannelRecommendation? = null,
    val isScanning: Boolean = false,
    val errorMessage: String? = null
)