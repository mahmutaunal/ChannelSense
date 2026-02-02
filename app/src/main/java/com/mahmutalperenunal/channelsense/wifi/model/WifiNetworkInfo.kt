package com.mahmutalperenunal.channelsense.wifi.model

data class WifiNetworkInfo(
    val ssid: String?,
    val bssid: String?,
    val rssi: Int,
    val frequency: Int,
    val band: WifiBand,
    val channel: Int
)