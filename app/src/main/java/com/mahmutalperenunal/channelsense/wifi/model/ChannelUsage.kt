package com.mahmutalperenunal.channelsense.wifi.model

data class ChannelUsage(
    val channel: Int,
    val band: WifiBand,
    val detectedAccessPoints: Int,
    val averageRssi: Int,
    val interferenceScore: Double,
    val occupancyPercent: Int
)
