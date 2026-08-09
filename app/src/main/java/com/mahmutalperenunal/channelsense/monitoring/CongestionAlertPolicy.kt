package com.mahmutalperenunal.channelsense.monitoring

import com.mahmutalperenunal.channelsense.domain.analyzer.ChannelRecommendationEngine
import com.mahmutalperenunal.channelsense.wifi.connection.ConnectedWifiInfo
import com.mahmutalperenunal.channelsense.domain.quality.ConnectionQualityEvaluator

data class CongestionAlert(
    val networkName: String,
    val currentChannel: Int,
    val occupancyPercent: Int,
    val recommendedChannel: Int
)

object CongestionAlertPolicy {
    const val HIGH_CONGESTION_PERCENT = 75

    fun evaluate(
        connectedWifi: ConnectedWifiInfo?,
        analysis: ChannelRecommendationEngine.Analysis
    ): CongestionAlert? {
        val networkName = connectedWifi?.ssid ?: return null
        val currentChannel = connectedWifi.channel ?: return null
        val recommendation = analysis.recommendation ?: return null
        val currentUsage = analysis.usages.firstOrNull { it.channel == currentChannel } ?: return null
        val congestionPercent = ConnectionQualityEvaluator.estimatedCongestionPercent(
            currentUsage.interferenceScore
        )

        if (congestionPercent < HIGH_CONGESTION_PERCENT) return null
        if (recommendation.isCurrentChannelAcceptable) return null
        if (recommendation.recommendedChannel == currentChannel) return null

        return CongestionAlert(
            networkName = networkName,
            currentChannel = currentChannel,
            occupancyPercent = congestionPercent,
            recommendedChannel = recommendation.recommendedChannel
        )
    }
}
