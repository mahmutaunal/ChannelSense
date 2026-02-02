package com.mahmutalperenunal.channelsense.feature.settings.ui

import com.mahmutalperenunal.channelsense.wifi.model.WifiBand

data class SettingsUiState(
    val defaultBand: WifiBand = WifiBand.TWO_GHZ,
    val autoRefreshEnabled: Boolean = false
)