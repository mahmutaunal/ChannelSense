package com.mahmutalperenunal.channelsense.feature.settings.model

import com.mahmutalperenunal.channelsense.wifi.model.WifiBand

data class AppSettings(
    val defaultBand: WifiBand = WifiBand.TWO_GHZ,
    val autoRefreshEnabled: Boolean = false,
    val congestionAlertsEnabled: Boolean = false,
    val congestionAlertInterval: CongestionAlertInterval = CongestionAlertInterval.SIX_HOURS,
    val themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    val materialYouEnabled: Boolean = true
)

enum class AppThemeMode { SYSTEM, LIGHT, DARK }

enum class AppLanguage(val languageTag: String) {
    SYSTEM(""),
    TURKISH("tr"),
    ENGLISH("en")
}

enum class CongestionAlertInterval(val minutes: Long) {
    THIRTY_MINUTES(30),
    ONE_HOUR(60),
    THREE_HOURS(180),
    SIX_HOURS(360)
}
