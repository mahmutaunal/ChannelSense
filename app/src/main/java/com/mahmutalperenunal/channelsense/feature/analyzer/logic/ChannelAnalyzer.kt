package com.mahmutalperenunal.channelsense.feature.analyzer.logic

import com.mahmutalperenunal.channelsense.wifi.model.ChannelUsage
import com.mahmutalperenunal.channelsense.wifi.model.WifiBand

object ChannelAnalyzer {

    fun recommendChannel(
        band: WifiBand,
        channels: List<ChannelUsage>
    ): ChannelRecommendation? {
        val bandChannels = channels.filter { it.band == band }
        if (bandChannels.isEmpty()) return null

        val minDeviceCount = bandChannels.minOf { it.deviceCount }
        val candidates = bandChannels.filter { it.deviceCount == minDeviceCount }

        val best = candidates.minByOrNull { it.averageRssi } ?: candidates.first()

        val reason = "reason_least_neighbors|${best.deviceCount}|${best.averageRssi}"

        return ChannelRecommendation(
            band = band,
            recommendedChannel = best.channel,
            reason = reason
        )
    }
}