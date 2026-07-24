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
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
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
import com.mahmutalperenunal.channelsense.ui.components.ChannelBarChart
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
            TopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
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
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.background
                )
            )
        }
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
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item { ConnectedNetworkCard(state) }
                item { BandSelector(state.selectedBand, viewModel::onBandSelected) }
                item {
                    ScanActions(
                        scanning = state.isScanning,
                        completed = state.completedSamples,
                        requested = state.requestedSamples,
                        scanMode = state.scanMode,
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
                    ChannelBarChart(
                        channels = state.channels,
                        recommendedChannel = state.recommendation?.recommendedChannel,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp),
                        onChannelClick = { onChannelSelected(it.channel) }
                    )
                }
                item {
                    Text(
                        stringResource(R.string.channels_and_details),
                        style = MaterialTheme.typography.titleMedium
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
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                stringResource(R.string.connected_network),
                style = MaterialTheme.typography.labelLarge
            )
            Text(
                wifi?.ssid ?: stringResource(R.string.not_connected_or_hidden),
                style = MaterialTheme.typography.titleMedium
            )
            if (wifi?.channel != null) Text(
                stringResource(
                    R.string.connected_network_details,
                    wifi.channel,
                    bandLabel(wifi.band)
                )
            )
        }
    }
}

@Composable
private fun BandSelector(selected: WifiBand, onSelect: (WifiBand) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(
            space = 20.dp,
            alignment = Alignment.CenterHorizontally
        ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        WifiBand.entries.forEach { band ->
            FilterChip(
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
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp)
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
            OutlinedButton(
                onClick = onDetailed,
                enabled = !scanning,
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp)
            ) {
                if (scanning && scanMode == ScanMode.DETAILED) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp
                    )
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
            OutlinedCard(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.outlinedCardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
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

    OutlinedCard(
        onClick = onOpenGuide,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = if (isCurrentChannelGood) {
                    stringResource(R.string.current_channel_is_good)
                } else {
                    stringResource(R.string.recommended_channel_label)
                },
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = stringResource(
                    R.string.recommended_channel_title,
                    recommendation.recommendedChannel,
                    bandLabel(recommendation.band)
                ),
                style = MaterialTheme.typography.headlineSmall
            )
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
            HorizontalDivider()
            Text(
                text = stringResource(R.string.tap_for_detailed_guide),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun ChannelRow(usage: ChannelUsage, recommended: Boolean) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            Modifier
                .padding(14.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    stringResource(R.string.channel_item_title, usage.channel),
                    style = MaterialTheme.typography.titleSmall
                )
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
                Text(
                    stringResource(R.string.occupancy_percent, usage.occupancyPercent),
                    style = MaterialTheme.typography.labelLarge
                )
                if (recommended) Text(
                    stringResource(R.string.best_candidate),
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelSmall
                )
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
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(text, color = MaterialTheme.colorScheme.onErrorContainer)
            TextButton(onClick = onAction) { Text(stringResource(R.string.action_fix_or_retry)) }
        }
    }
}

@Composable
private fun PermissionContent(modifier: Modifier, onGrant: () -> Unit) {
    Column(modifier.fillMaxSize(), verticalArrangement = Arrangement.Center) {
        Text(
            stringResource(R.string.permission_title_need_permission),
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(Modifier.height(12.dp)); Text(stringResource(R.string.permission_body_location_required))
        Spacer(Modifier.height(8.dp)); Text(
        stringResource(R.string.permission_body_privacy_note),
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
        Spacer(Modifier.height(20.dp)); Button(
        onClick = onGrant,
        modifier = Modifier.fillMaxWidth()
    ) { Text(stringResource(R.string.permission_button_grant_and_scan)) }
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
        Text(stringResource(R.string.about_title), style = MaterialTheme.typography.titleLarge)
        Text(stringResource(R.string.about_body_what_it_does)); Text(stringResource(R.string.about_body_why_permission)); Text(
        stringResource(R.string.about_body_privacy)
    ); Text(stringResource(R.string.about_body_router_note))
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
