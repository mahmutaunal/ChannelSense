package com.mahmutalperenunal.channelsense.feature.settings.ui

import com.mahmutalperenunal.channelsense.wifi.model.WifiBand
import com.mahmutalperenunal.channelsense.feature.settings.model.CongestionAlertInterval
import com.mahmutalperenunal.channelsense.feature.settings.model.AppLanguage
import com.mahmutalperenunal.channelsense.feature.settings.model.AppThemeMode

data class SettingsUiState(
    val defaultBand: WifiBand = WifiBand.TWO_GHZ,
    val autoRefreshEnabled: Boolean = false,
    val congestionAlertsEnabled: Boolean = false,
    val congestionAlertInterval: CongestionAlertInterval = CongestionAlertInterval.SIX_HOURS,
    val themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    val materialYouEnabled: Boolean = true,
    val language: AppLanguage = AppLanguage.SYSTEM
)
