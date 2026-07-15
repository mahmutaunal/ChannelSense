package com.mahmutalperenunal.channelsense.wifi.model

data class WifiNetworkInfo(
    val ssid: String?,
    val bssid: String?,
    val rssi: Int,
    val frequency: Int,
    val band: WifiBand,
    val channel: Int,
    val channelWidthMhz: Int = 20,
    val centerFrequencyMhz: Int = frequency,
    val timestampMicros: Long = 0L
)
