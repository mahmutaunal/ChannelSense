package com.mahmutalperenunal.channelsense.feature.analyzer.ui

import android.content.Intent
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mahmutalperenunal.channelsense.R
import com.mahmutalperenunal.channelsense.feature.analyzer.AnalyzerViewModel
import com.mahmutalperenunal.channelsense.feature.analyzer.logic.RecommendationConfidence
import com.mahmutalperenunal.channelsense.domain.quality.ConnectionLimitingFactor
import com.mahmutalperenunal.channelsense.domain.quality.ConnectionQualityEvaluator
import com.mahmutalperenunal.channelsense.domain.quality.ConnectionQualityGrade
import com.mahmutalperenunal.channelsense.ui.components.ChannelBarChart
import com.mahmutalperenunal.channelsense.ui.components.ChannelSenseTopBar
import com.mahmutalperenunal.channelsense.ui.components.SectionHeading
import com.mahmutalperenunal.channelsense.ui.components.StatusBadge
import com.mahmutalperenunal.channelsense.wifi.connection.WifiStandard
import com.mahmutalperenunal.channelsense.wifi.model.ChannelUsage
import com.mahmutalperenunal.channelsense.wifi.model.WifiBand
import com.mahmutalperenunal.channelsense.wifi.permissions.WifiPermissionHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyzerScreen(
    onChannelSelected: (Int) -> Unit,
    onOpenSettings: () -> Unit,
    onSuccessfulScan: (Boolean) -> Unit,
    viewModel: AnalyzerViewModel = viewModel()
) {
    val context = LocalContext.current
    val state = viewModel.uiState
    var showInfo by remember { mutableStateOf(false) }
    var lastTrackedScanEpochMillis by rememberSaveable { mutableStateOf<Long?>(null) }
    var hasPermission by remember {
        mutableStateOf(
            WifiPermissionHelper.hasRequiredPermissions(
                context
            )
        )
    }
    val permissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            hasPermission = WifiPermissionHelper.hasRequiredPermissions(context)
            if (hasPermission) viewModel.onRefreshRequested()
        }

    LaunchedEffect(hasPermission) {
        if (hasPermission && state.lastScanEpochMillis == null && !state.isScanning) {
            viewModel.onRefreshRequested(ScanMode.QUICK)
        }
    }

    // A changed completion timestamp means a scan finished successfully. This survives
    // recomposition without counting the same result more than once.
    LaunchedEffect(state.lastScanEpochMillis) {
        val completedAt = state.lastScanEpochMillis
        if (
            completedAt != null &&
            completedAt != lastTrackedScanEpochMillis &&
            state.recommendation != null
        ) {
            lastTrackedScanEpochMillis = completedAt
            onSuccessfulScan(state.scanMode == ScanMode.DETAILED)
        }
    }

    Scaffold(
        topBar = {
            ChannelSenseTopBar(
                title = stringResource(R.string.app_name),
                subtitle = stringResource(R.string.home_subtitle),
                actions = {
                    IconButton(onClick = { showInfo = true }) {
                        Icon(
                            Icons.Default.Info,
                            stringResource(R.string.cd_info)
                        )
                    }
                    IconButton(onClick = onOpenSettings) {
                        Icon(
                            Icons.Default.Settings,
                            stringResource(R.string.cd_settings)
                        )
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        if (!hasPermission) {
            PermissionContent(
                modifier = Modifier
                    .padding(padding)
                    .padding(20.dp),
                onGrant = { permissionLauncher.launch(WifiPermissionHelper.requiredPermissions()) }
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 40.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                item { ConnectedNetworkCard(state) }
                item {
                    AnalysisControls(
                        state = state,
                        onBandSelected = viewModel::onBandSelected,
                        onQuick = { viewModel.onRefreshRequested(ScanMode.QUICK) },
                        onDetailed = { viewModel.onRefreshRequested(ScanMode.DETAILED) }
                    )
                }
                state.problem?.let { problem ->
                    item {
                        ProblemCard(problem) {
                            when (problem) {
                                ScanProblem.LOCATION_DISABLED -> context.startActivity(
                                    Intent(
                                        Settings.ACTION_LOCATION_SOURCE_SETTINGS
                                    )
                                )

                                ScanProblem.WIFI_DISABLED -> context.startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))
                                ScanProblem.PERMISSION -> permissionLauncher.launch(
                                    WifiPermissionHelper.requiredPermissions()
                                )

                                else -> viewModel.onRefreshRequested()
                            }
                        }
                    }
                }
                if (state.usesCachedResults) item {
                    AssistChip(
                        onClick = {},
                        label = { Text(stringResource(R.string.scan_cached_warning)) })
                }
                state.recommendation?.let { recommendation ->
                    item {
                        RecommendationCard(
                            state = state,
                            onOpenGuide = { onChannelSelected(recommendation.recommendedChannel) }
                        )
                    }
                }
                item {
                    SectionHeading(
                        title = stringResource(R.string.channels_and_details),
                        subtitle = stringResource(R.string.channels_section_subtitle)
                    )
                }
                item {
                    ChannelBarChart(
                        channels = state.channels,
                        recommendedChannel = state.recommendation?.recommendedChannel,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp),
                        onChannelClick = { onChannelSelected(it.channel) }
                    )
                }
                if (state.channels.isEmpty() && !state.isScanning) {
                    item {
                        Text(
                            stringResource(R.string.no_channel_info_try_refresh),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                } else {
                    items(state.channels, key = { it.channel }) {
                        ChannelRow(
                            it,
                            state.recommendation?.recommendedChannel == it.channel
                        )
                    }
                }
                item {
                    Text(
                        stringResource(R.string.analysis_disclaimer),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }

    if (showInfo) ModalBottomSheet(onDismissRequest = { showInfo = false }) { InfoContent() }
}

@Composable
private fun ConnectedNetworkCard(state: AnalyzerUiState) {
    val wifi = state.connectedWifi
    val currentUsage = wifi?.let { connected ->
        state.channels.firstOrNull {
            it.channel == connected.channel && it.band == connected.band
        }
    }
    val quality = wifi?.let { ConnectionQualityEvaluator.evaluate(it, currentUsage) }
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 0.dp)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    stringResource(R.string.connected_network),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                if (quality != null) {
                    StatusBadge(
                        text = stringResource(quality.grade.labelResource()),
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.72f),
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            Text(
                wifi?.ssid ?: stringResource(R.string.not_connected_or_hidden),
                style = MaterialTheme.typography.titleLarge
            )

            if (wifi != null && quality != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = stringResource(R.string.connection_quality_score),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f)
                        )
                        Text(
                            text = quality.score.toString(),
                            style = MaterialTheme.typography.displaySmall
                        )
                    }
                    Text(
                        text = stringResource(R.string.connection_quality_score_out_of),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                }
                LinearProgressIndicator(
                    progress = { quality.score / 100f },
                    modifier = Modifier.fillMaxWidth().height(8.dp),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    QualityMetric(
                        label = stringResource(R.string.quality_signal),
                        value = wifi.signalDbm?.let {
                            stringResource(R.string.quality_dbm_value, it)
                        } ?: stringResource(R.string.quality_unknown),
                        modifier = Modifier.weight(1f)
                    )
                    QualityMetric(
                        label = stringResource(R.string.quality_receive_link),
                        value = wifi.receiveLinkSpeedMbps?.let {
                            stringResource(R.string.quality_mbps_value, it)
                        } ?: stringResource(R.string.quality_unknown),
                        modifier = Modifier.weight(1f)
                    )
                    QualityMetric(
                        label = stringResource(R.string.quality_transmit_link),
                        value = wifi.transmitLinkSpeedMbps?.let {
                            stringResource(R.string.quality_mbps_value, it)
                        } ?: stringResource(R.string.quality_unknown),
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    QualityMetric(
                        label = stringResource(R.string.quality_wifi_standard),
                        value = stringResource(wifi.wifiStandard.labelResource()),
                        modifier = Modifier.weight(1f)
                    )
                    QualityMetric(
                        label = stringResource(R.string.quality_channel),
                        value = wifi.channel?.toString() ?: stringResource(R.string.quality_unknown),
                        modifier = Modifier.weight(1f)
                    )
                    QualityMetric(
                        label = stringResource(R.string.quality_congestion),
                        value = quality.congestionPercent?.let {
                            stringResource(R.string.quality_percent_value, it)
                        } ?: stringResource(R.string.quality_pending),
                        modifier = Modifier.weight(1f)
                    )
                }

                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.65f)
                ) {
                    Text(
                        text = stringResource(quality.limitingFactor.explanationResource()),
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Text(
                    text = stringResource(R.string.connection_quality_disclaimer),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun QualityMetric(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.56f)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 9.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = value,
                style = MaterialTheme.typography.labelLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

private fun ConnectionQualityGrade.labelResource(): Int = when (this) {
    ConnectionQualityGrade.EXCELLENT -> R.string.quality_grade_excellent
    ConnectionQualityGrade.GOOD -> R.string.quality_grade_good
    ConnectionQualityGrade.FAIR -> R.string.quality_grade_fair
    ConnectionQualityGrade.POOR -> R.string.quality_grade_poor
}

private fun ConnectionLimitingFactor.explanationResource(): Int = when (this) {
    ConnectionLimitingFactor.NONE -> R.string.quality_factor_none
    ConnectionLimitingFactor.SIGNAL -> R.string.quality_factor_signal
    ConnectionLimitingFactor.CONGESTION -> R.string.quality_factor_congestion
    ConnectionLimitingFactor.LINK_SPEED -> R.string.quality_factor_link_speed
}

private fun WifiStandard.labelResource(): Int = when (this) {
    WifiStandard.LEGACY -> R.string.wifi_standard_legacy
    WifiStandard.WIFI_4 -> R.string.wifi_standard_4
    WifiStandard.WIFI_5 -> R.string.wifi_standard_5
    WifiStandard.WIFI_6 -> R.string.wifi_standard_6
    WifiStandard.WIFI_6_AD -> R.string.wifi_standard_6_ad
    WifiStandard.WIFI_7 -> R.string.wifi_standard_7
    WifiStandard.UNKNOWN -> R.string.quality_unknown
}

@Composable
private fun AnalysisControls(
    state: AnalyzerUiState,
    onBandSelected: (WifiBand) -> Unit,
    onQuick: () -> Unit,
    onDetailed: () -> Unit
) {
    ElevatedCard(
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SectionHeading(
                title = stringResource(R.string.analysis_controls_title),
                subtitle = stringResource(R.string.analysis_controls_subtitle)
            )
            BandSelector(state.selectedBand, onBandSelected)
            ScanActions(
                scanning = state.isScanning,
                completed = state.completedSamples,
                requested = state.requestedSamples,
                scanMode = state.scanMode,
                onQuick = onQuick,
                onDetailed = onDetailed
            )
        }
    }
}

@Composable
private fun BandSelector(selected: WifiBand, onSelect: (WifiBand) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(
            space = 8.dp,
            alignment = Alignment.CenterHorizontally
        ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        WifiBand.entries.forEach { band ->
            FilterChip(
                modifier = Modifier.weight(1f),
                selected = selected == band,
                onClick = { onSelect(band) },
                label = { Text(bandLabel(band)) })
        }
    }
}

@Composable
private fun ScanActions(
    scanning: Boolean,
    completed: Int,
    requested: Int,
    scanMode: ScanMode = ScanMode.QUICK,
    onQuick: () -> Unit,
    onDetailed: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = onQuick,
                enabled = !scanning,
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 14.dp)
            ) {
                if (scanning && scanMode == ScanMode.QUICK) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Icon(Icons.Default.Refresh, contentDescription = null)
                }
                Spacer(Modifier.width(6.dp))
                Text(
                    text = stringResource(R.string.quick_scan),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            FilledTonalButton(
                onClick = onDetailed,
                enabled = !scanning,
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 14.dp)
            ) {
                if (scanning && scanMode == ScanMode.DETAILED) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(Modifier.width(6.dp))
                } else {
                    Icon(Icons.Default.Tune, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                }
                Text(
                    text = stringResource(R.string.detailed_analysis),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        if (scanning) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.secondaryContainer
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.5.dp
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (scanMode == ScanMode.DETAILED) {
                                    stringResource(R.string.detailed_scan_in_progress_title)
                                } else {
                                    stringResource(R.string.quick_scan_in_progress_title)
                                },
                                style = MaterialTheme.typography.titleSmall
                            )
                            Text(
                                text = stringResource(R.string.scan_in_progress_description),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    LinearProgressIndicator(
                        progress = { completed.toFloat() / requested.coerceAtLeast(1) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = stringResource(R.string.scan_progress, completed, requested),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun RecommendationCard(state: AnalyzerUiState, onOpenGuide: () -> Unit) {
    val recommendation = state.recommendation ?: return
    val isCurrentChannelGood = recommendation.isCurrentChannelAcceptable

    ElevatedCard(
        onClick = onOpenGuide,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = if (isCurrentChannelGood) {
                MaterialTheme.colorScheme.secondaryContainer
            } else {
                MaterialTheme.colorScheme.tertiaryContainer
            }
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = if (isCurrentChannelGood) Icons.Default.CheckCircle else Icons.Default.WarningAmber,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = if (isCurrentChannelGood) {
                                stringResource(R.string.current_channel_is_good)
                            } else {
                                stringResource(R.string.recommended_channel_label)
                            },
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.channel_number_only, recommendation.recommendedChannel),
                        style = MaterialTheme.typography.headlineLarge
                    )
                    Text(
                        text = bandLabel(recommendation.band),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Surface(
                    shape = MaterialTheme.shapes.extraLarge,
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.65f)
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = stringResource(R.string.tap_for_detailed_guide),
                        modifier = Modifier.padding(14.dp).size(22.dp)
                    )
                }
            }
            Text(
                text = if (isCurrentChannelGood) {
                    stringResource(R.string.current_channel_good_explanation)
                } else {
                    stringResource(R.string.recommended_channel_simple_explanation)
                },
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = stringResource(
                    R.string.recommendation_summary,
                    recommendation.detectedAccessPoints,
                    confidenceLabel(recommendation.confidence),
                    recommendation.sampleCount
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (recommendation.alternativeChannels.isNotEmpty()) {
                Text(
                    text = stringResource(
                        R.string.alternative_channels_simple,
                        recommendation.alternativeChannels.joinToString()
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = stringResource(R.string.tap_for_detailed_guide),
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}

@Composable
private fun ChannelRow(usage: ChannelUsage, recommended: Boolean) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = if (recommended) MaterialTheme.colorScheme.primaryContainer
        else MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Row(
            Modifier
                .padding(14.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.surface
            ) {
                Text(
                    text = usage.channel.toString(),
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    style = MaterialTheme.typography.titleLarge
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(stringResource(R.string.channel_item_title, usage.channel), style = MaterialTheme.typography.titleSmall)
                Text(
                    stringResource(
                        R.string.channel_item_access_point_count,
                        usage.detectedAccessPoints
                    ), style = MaterialTheme.typography.bodySmall
                )
                Text(
                    stringResource(
                        R.string.channel_item_average_neighbor_signal,
                        usage.averageRssi
                    ), style = MaterialTheme.typography.bodySmall
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                StatusBadge(
                    text = stringResource(R.string.occupancy_percent, usage.occupancyPercent),
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
                if (recommended) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        stringResource(R.string.best_candidate),
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }
}

@Composable
private fun ProblemCard(problem: ScanProblem, onAction: () -> Unit) {
    val text = when (problem) {
        ScanProblem.PERMISSION -> stringResource(R.string.problem_permission)
        ScanProblem.WIFI_DISABLED -> stringResource(R.string.problem_wifi_disabled)
        ScanProblem.LOCATION_DISABLED -> stringResource(R.string.problem_location_disabled)
        ScanProblem.THROTTLED -> stringResource(R.string.problem_throttled)
        ScanProblem.UNKNOWN -> stringResource(R.string.error_wifi_scan_failed)
    }
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
        Row(
            Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(Icons.Default.WarningAmber, contentDescription = null)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(text, color = MaterialTheme.colorScheme.onErrorContainer)
                TextButton(onClick = onAction, contentPadding = PaddingValues(0.dp)) {
                    Text(stringResource(R.string.action_fix_or_retry))
                }
            }
        }
    }
}

@Composable
private fun PermissionContent(modifier: Modifier, onGrant: () -> Unit) {
    Column(
        modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Icon(
                Icons.Default.Refresh,
                contentDescription = null,
                modifier = Modifier.padding(20.dp).size(34.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(Modifier.height(24.dp))
        Text(
            stringResource(R.string.permission_title_need_permission),
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(Modifier.height(10.dp))
        Text(
            stringResource(R.string.permission_body_location_required),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(8.dp))
        Text(
            stringResource(R.string.permission_body_privacy_note),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(24.dp))
        Button(onClick = onGrant, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.permission_button_grant_and_scan))
        }
    }
}

@Composable
private fun InfoContent() {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(stringResource(R.string.about_title), style = MaterialTheme.typography.headlineSmall)
        Text(stringResource(R.string.about_body_what_it_does))
        HorizontalDivider()
        Text(stringResource(R.string.about_body_why_permission))
        Text(stringResource(R.string.about_body_privacy), color = MaterialTheme.colorScheme.primary)
        Text(stringResource(R.string.about_body_router_note))
        Spacer(Modifier.height(12.dp))
    }
}

@Composable
private fun bandLabel(band: WifiBand?): String = when (band) {
    WifiBand.TWO_GHZ -> stringResource(R.string.wifi_band_24ghz)
    WifiBand.FIVE_GHZ -> stringResource(R.string.wifi_band_5ghz)
    WifiBand.SIX_GHZ -> stringResource(R.string.wifi_band_6ghz)
    null -> "—"
}

@Composable
private fun confidenceLabel(confidence: RecommendationConfidence): String = when (confidence) {
    RecommendationConfidence.LOW -> stringResource(R.string.confidence_low)
    RecommendationConfidence.MEDIUM -> stringResource(R.string.confidence_medium)
    RecommendationConfidence.HIGH -> stringResource(R.string.confidence_high)
}
