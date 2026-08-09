package com.mahmutalperenunal.channelsense.feature.settings.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.core.content.ContextCompat
import com.mahmutalperenunal.channelsense.R
import com.mahmutalperenunal.channelsense.feature.settings.model.CongestionAlertInterval
import com.mahmutalperenunal.channelsense.feature.settings.model.AppLanguage
import com.mahmutalperenunal.channelsense.feature.settings.model.AppThemeMode
import com.mahmutalperenunal.channelsense.monitoring.CongestionAlert
import com.mahmutalperenunal.channelsense.monitoring.WifiCongestionNotifier
import com.mahmutalperenunal.channelsense.ui.components.ChannelSenseTopBar
import com.mahmutalperenunal.channelsense.wifi.connection.ConnectedWifiProvider
import com.mahmutalperenunal.channelsense.wifi.model.WifiBand
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import androidx.core.net.toUri
import kotlin.time.Duration.Companion.milliseconds

private const val STUDIO_WEBSITE_URL = "https://www.alpwarestudio.com/"
private const val SOURCE_CODE_URL = "https://github.com/mahmutaunal/ChannelSense"
private const val PRIVACY_POLICY_URL = "https://mahmutaunal.github.io/ChannelSense/"
private const val MORE_APPS_URL = "https://play.google.com/store/apps/dev?id=5245599652065968716"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    onCheckForUpdate: () -> Unit,
    onRateApp: () -> Unit,
    viewModel: SettingsViewModel = viewModel()
) {
    val state = viewModel.uiState
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val linkErrorMessage = stringResource(R.string.settings_link_open_error)
    val monitoringPermissionError = stringResource(R.string.settings_congestion_permission_error)
    val versionName = remember(context) { context.appVersionName() }
    var pendingMonitoringEnable by remember { mutableStateOf(false) }
    var showBackgroundLocationExplanation by remember { mutableStateOf(false) }

    LaunchedEffect(state.congestionAlertsEnabled) {
        if (state.congestionAlertsEnabled && context.isDebuggable()) {
            delay(5_000.milliseconds)
            context.sendDebugCongestionAlert()
        }
    }

    val backgroundSettingsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (!context.needsBackgroundLocationPermission() && pendingMonitoringEnable) {
            viewModel.onCongestionAlertsChanged(true)
        } else {
            scope.launch { snackbarHostState.showSnackbar(monitoringPermissionError) }
        }
        pendingMonitoringEnable = false
    }

    val backgroundLocationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted && pendingMonitoringEnable) {
            viewModel.onCongestionAlertsChanged(true)
        } else if (!granted) {
            scope.launch { snackbarHostState.showSnackbar(monitoringPermissionError) }
        }
        pendingMonitoringEnable = false
    }
    val notificationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (!granted) {
            pendingMonitoringEnable = false
            scope.launch { snackbarHostState.showSnackbar(monitoringPermissionError) }
        } else if (context.needsBackgroundLocationPermission()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                showBackgroundLocationExplanation = true
            } else {
                backgroundLocationLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            }
        } else if (pendingMonitoringEnable) {
            viewModel.onCongestionAlertsChanged(true)
            pendingMonitoringEnable = false
        }
    }

    fun requestMonitoringEnable() {
        pendingMonitoringEnable = true
        when {
            context.needsNotificationPermission() ->
                notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            context.needsBackgroundLocationPermission() -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    showBackgroundLocationExplanation = true
                } else {
                    backgroundLocationLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                }
            }
            else -> {
                viewModel.onCongestionAlertsChanged(true)
                pendingMonitoringEnable = false
            }
        }
    }

    fun openLink(url: String) {
        if (!context.openExternalLink(url)) {
            scope.launch { snackbarHostState.showSnackbar(linkErrorMessage) }
        }
    }

    Scaffold(
        topBar = {
            ChannelSenseTopBar(
                title = stringResource(R.string.settings_title),
                subtitle = stringResource(R.string.settings_subtitle),
                onBackClick = onBackClick
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                SettingsSection(
                    title = stringResource(R.string.settings_section_personalization_title),
                    subtitle = stringResource(R.string.settings_section_personalization_subtitle)
                ) {
                    PersonalizationSettings(
                        themeMode = state.themeMode,
                        language = state.language,
                        onThemeModeSelected = viewModel::onThemeModeChanged,
                        onLanguageSelected = viewModel::onLanguageChanged
                    )
                }
            }

            item {
                SettingsSection(title = stringResource(R.string.settings_section_wifi_analyzer_title)) {
                    DefaultBandSettingRow(
                        selectedBand = state.defaultBand,
                        onBandSelected = viewModel::onDefaultBandChanged
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    AutoRefreshSettingRow(
                        enabled = state.autoRefreshEnabled,
                        onToggle = viewModel::onAutoRefreshChanged
                    )
                }
            }

            item {
                SettingsSection(
                    title = stringResource(R.string.settings_section_monitoring_title),
                    subtitle = stringResource(R.string.settings_section_monitoring_subtitle)
                ) {
                    CongestionAlertsSettingRow(
                        enabled = state.congestionAlertsEnabled,
                        interval = state.congestionAlertInterval,
                        onToggle = { enabled ->
                            if (enabled) requestMonitoringEnable()
                            else viewModel.onCongestionAlertsChanged(false)
                        },
                        onIntervalSelected = viewModel::onCongestionAlertIntervalChanged
                    )
                }
            }

            item {
                SettingsSection(title = stringResource(R.string.settings_section_alpware_title)) {
                    ExternalLinkRow(
                        icon = Icons.Default.Language,
                        title = stringResource(R.string.settings_studio_website_title),
                        subtitle = stringResource(R.string.settings_studio_website_description),
                        onClick = { openLink(STUDIO_WEBSITE_URL) }
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    ExternalLinkRow(
                        icon = Icons.Default.Apps,
                        title = stringResource(R.string.settings_more_apps_title),
                        subtitle = stringResource(R.string.settings_more_apps_description),
                        onClick = { openLink(MORE_APPS_URL) }
                    )
                }
            }

            item {
                SettingsSection(title = stringResource(R.string.settings_section_support_title)) {
                    ActionRow(
                        icon = Icons.Default.RateReview,
                        title = stringResource(R.string.settings_rate_app_title),
                        subtitle = stringResource(R.string.settings_rate_app_description),
                        onClick = onRateApp
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    ActionRow(
                        icon = Icons.Default.SystemUpdate,
                        title = stringResource(R.string.settings_check_update_title),
                        subtitle = stringResource(R.string.settings_check_update_description),
                        onClick = onCheckForUpdate
                    )
                }
            }

            item {
                SettingsSection(title = stringResource(R.string.settings_section_about_title)) {
                    ExternalLinkRow(
                        icon = Icons.Default.Code,
                        title = stringResource(R.string.settings_source_code_title),
                        subtitle = stringResource(R.string.settings_source_code_description),
                        onClick = { openLink(SOURCE_CODE_URL) }
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    ExternalLinkRow(
                        icon = Icons.Default.PrivacyTip,
                        title = stringResource(R.string.settings_privacy_policy_title),
                        subtitle = stringResource(R.string.settings_privacy_policy_description),
                        onClick = { openLink(PRIVACY_POLICY_URL) }
                    )
                }
            }

            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = stringResource(R.string.app_name),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = stringResource(R.string.settings_app_version, versionName),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = stringResource(R.string.settings_made_by_alpware),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }

    if (showBackgroundLocationExplanation) {
        AlertDialog(
            onDismissRequest = {
                showBackgroundLocationExplanation = false
                pendingMonitoringEnable = false
            },
            title = { Text(stringResource(R.string.settings_background_location_title)) },
            text = { Text(stringResource(R.string.settings_background_location_explanation)) },
            confirmButton = {
                Button(onClick = {
                    showBackgroundLocationExplanation = false
                    backgroundSettingsLauncher.launch(
                        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = "package:${context.packageName}".toUri()
                        }
                    )
                }) {
                    Text(stringResource(R.string.settings_open_app_permissions))
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showBackgroundLocationExplanation = false
                    pendingMonitoringEnable = false
                }) {
                    Text(stringResource(R.string.settings_not_now))
                }
            }
        )
    }
}

@Composable
private fun SettingsSection(
    title: String,
    subtitle: String? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Column(
            modifier = Modifier.padding(horizontal = 4.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            tonalElevation = 0.dp
        ) {
            Column(content = content)
        }
    }
}

@Composable
private fun PersonalizationSettings(
    themeMode: AppThemeMode,
    language: AppLanguage,
    onThemeModeSelected: (AppThemeMode) -> Unit,
    onLanguageSelected: (AppLanguage) -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 6.dp)) {
        SettingChoiceBlock(
            icon = Icons.Default.Palette,
            title = stringResource(R.string.settings_theme_title),
            description = stringResource(R.string.settings_theme_description)
        ) {
            AppThemeMode.entries.forEach { option ->
                FilterChip(
                    selected = themeMode == option,
                    onClick = { onThemeModeSelected(option) },
                    label = { Text(stringResource(option.labelResource())) }
                )
            }
        }
        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
        SettingChoiceBlock(
            icon = Icons.Default.Translate,
            title = stringResource(R.string.settings_language_title),
            description = stringResource(R.string.settings_language_description)
        ) {
            AppLanguage.entries.forEach { option ->
                FilterChip(
                    selected = language == option,
                    onClick = { onLanguageSelected(option) },
                    label = { Text(stringResource(option.labelResource())) }
                )
            }
        }
    }
}

@Composable
private fun SettingChoiceBlock(
    icon: ImageVector,
    title: String,
    description: String,
    choices: @Composable RowScope.() -> Unit
) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            SettingIcon(icon)
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(text = title, style = MaterialTheme.typography.titleSmall)
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            content = choices
        )
    }
}

private fun AppThemeMode.labelResource(): Int = when (this) {
    AppThemeMode.SYSTEM -> R.string.settings_theme_system
    AppThemeMode.LIGHT -> R.string.settings_theme_light
    AppThemeMode.DARK -> R.string.settings_theme_dark
}

private fun AppLanguage.labelResource(): Int = when (this) {
    AppLanguage.SYSTEM -> R.string.settings_language_system
    AppLanguage.TURKISH -> R.string.settings_language_turkish
    AppLanguage.ENGLISH -> R.string.settings_language_english
}

@Composable
private fun DefaultBandSettingRow(
    selectedBand: WifiBand,
    onBandSelected: (WifiBand) -> Unit
) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            SettingIcon(Icons.Default.Wifi)
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = stringResource(R.string.settings_default_band_title),
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = stringResource(R.string.settings_default_band_description),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            BandChip(WifiBand.TWO_GHZ, selectedBand, R.string.wifi_band_24ghz, onBandSelected)
            BandChip(WifiBand.FIVE_GHZ, selectedBand, R.string.wifi_band_5ghz, onBandSelected)
            BandChip(WifiBand.SIX_GHZ, selectedBand, R.string.wifi_band_6ghz, onBandSelected)
        }
    }
}

@Composable
private fun BandChip(
    band: WifiBand,
    selectedBand: WifiBand,
    labelRes: Int,
    onBandSelected: (WifiBand) -> Unit
) {
    FilterChip(
        selected = selectedBand == band,
        onClick = { onBandSelected(band) },
        label = { Text(stringResource(labelRes)) }
    )
}

@Composable
private fun AutoRefreshSettingRow(
    enabled: Boolean,
    onToggle: (Boolean) -> Unit
) {
    ListItem(
        headlineContent = { Text(stringResource(R.string.settings_auto_refresh_title)) },
        supportingContent = {
            Text(
                text = stringResource(R.string.settings_auto_refresh_description),
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        },
        leadingContent = { SettingIcon(Icons.Default.Autorenew) },
        trailingContent = {
            Switch(
                checked = enabled,
                onCheckedChange = onToggle
            )
        },
        colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    )
}

@Composable
private fun CongestionAlertsSettingRow(
    enabled: Boolean,
    interval: CongestionAlertInterval,
    onToggle: (Boolean) -> Unit,
    onIntervalSelected: (CongestionAlertInterval) -> Unit
) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SettingIcon(Icons.Default.NotificationsActive)
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = stringResource(R.string.settings_congestion_alerts_title),
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = stringResource(R.string.settings_congestion_alerts_description),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(checked = enabled, onCheckedChange = onToggle)
        }

        if (enabled) {
            Text(
                text = stringResource(R.string.settings_congestion_interval_title),
                style = MaterialTheme.typography.labelLarge
            )
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CongestionAlertInterval.entries.forEach { option ->
                    FilterChip(
                        selected = interval == option,
                        onClick = { onIntervalSelected(option) },
                        label = { Text(stringResource(option.labelResource())) }
                    )
                }
            }
            Text(
                text = stringResource(R.string.settings_congestion_battery_note),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun CongestionAlertInterval.labelResource(): Int = when (this) {
    CongestionAlertInterval.THIRTY_MINUTES -> R.string.interval_30_minutes
    CongestionAlertInterval.ONE_HOUR -> R.string.interval_1_hour
    CongestionAlertInterval.THREE_HOURS -> R.string.interval_3_hours
    CongestionAlertInterval.SIX_HOURS -> R.string.interval_6_hours
}


@Composable
private fun ActionRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    ListItem(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        headlineContent = { Text(title) },
        supportingContent = {
            Text(
                text = subtitle,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        },
        leadingContent = { SettingIcon(icon) },
        colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    )
}

@Composable
private fun ExternalLinkRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    ListItem(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        headlineContent = { Text(title) },
        supportingContent = {
            Text(
                text = subtitle,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        },
        leadingContent = { SettingIcon(icon) },
        trailingContent = {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        },
        colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    )

}

@Composable
private fun SettingIcon(icon: ImageVector) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.padding(9.dp).size(20.dp)
        )
    }
}

private fun Context.appVersionName(): String = runCatching {
    packageManager.getPackageInfo(packageName, 0).versionName
}.getOrNull().orEmpty().ifBlank { "—" }

private fun Context.openExternalLink(url: String): Boolean = runCatching {
    val intent = Intent(Intent.ACTION_VIEW, url.toUri()).apply {
        addCategory(Intent.CATEGORY_BROWSABLE)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    startActivity(intent)
    true
}.getOrDefault(false)

private fun Context.needsNotificationPermission(): Boolean =
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
        ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
        PackageManager.PERMISSION_GRANTED

private fun Context.needsBackgroundLocationPermission(): Boolean =
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
        ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) !=
        PackageManager.PERMISSION_GRANTED

private fun Context.isDebuggable(): Boolean =
    applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0

private fun Context.sendDebugCongestionAlert() {
    val provider = ConnectedWifiProvider(this)
    val connected = try {
        provider.current()
    } finally {
        provider.close()
    }
    val currentChannel = connected?.channel ?: 6
    WifiCongestionNotifier.notify(
        context = this,
        alert = CongestionAlert(
            networkName = connected?.ssid ?: "Demo Wi-Fi",
            currentChannel = currentChannel,
            occupancyPercent = 92,
            recommendedChannel = debugRecommendedChannel(currentChannel)
        )
    )
}

private fun debugRecommendedChannel(currentChannel: Int): Int = when {
    currentChannel <= 14 -> listOf(1, 6, 11).first { it != currentChannel }
    currentChannel < 180 -> if (currentChannel == 36) 44 else 36
    else -> if (currentChannel == 5) 21 else 5
}
