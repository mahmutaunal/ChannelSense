package com.mahmutalperenunal.channelsense.feature.analyzer.logic

import com.mahmutalperenunal.channelsense.wifi.model.WifiBand

enum class RecommendationConfidence { LOW, MEDIUM, HIGH }

data class ChannelRecommendation(
    val band: WifiBand,
    val recommendedChannel: Int,
    val score: Double,
    val confidence: RecommendationConfidence,
    val sampleCount: Int,
    val detectedAccessPoints: Int,
    val alternativeChannels: List<Int>,
    val isCurrentChannelAcceptable: Boolean = false
)
