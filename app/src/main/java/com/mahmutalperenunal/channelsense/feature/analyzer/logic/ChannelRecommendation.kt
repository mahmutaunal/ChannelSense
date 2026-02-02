package com.mahmutalperenunal.channelsense.feature.analyzer.logic

import com.mahmutalperenunal.channelsense.wifi.model.WifiBand

data class ChannelRecommendation(
    val band: WifiBand,
    val recommendedChannel: Int,
    val reason: String
)