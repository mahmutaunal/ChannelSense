package com.mahmutalperenunal.channelsense.feature.settings.model

import com.mahmutalperenunal.channelsense.wifi.model.WifiBand

data class AppSettings(
    val defaultBand: WifiBand = WifiBand.TWO_GHZ,
    val autoRefreshEnabled: Boolean = false
)