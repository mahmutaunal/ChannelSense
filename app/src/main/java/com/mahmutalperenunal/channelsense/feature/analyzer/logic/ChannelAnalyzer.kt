package com.mahmutalperenunal.channelsense.feature.analyzer.logic

import com.mahmutalperenunal.channelsense.domain.analyzer.ChannelRecommendationEngine
import com.mahmutalperenunal.channelsense.wifi.model.WifiBand
import com.mahmutalperenunal.channelsense.wifi.model.WifiNetworkInfo

object ChannelAnalyzer {
    fun analyze(
        networks: List<WifiNetworkInfo>,
        band: WifiBand,
        sampleCount: Int,
        currentChannel: Int?
    ) = ChannelRecommendationEngine.analyze(networks, band, sampleCount, currentChannel)
}
