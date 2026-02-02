package com.mahmutalperenunal.channelsense.wifi.model

data class ChannelUsage(
    val channel: Int,
    val band: WifiBand,
    val deviceCount: Int,
    val averageRssi: Int
)