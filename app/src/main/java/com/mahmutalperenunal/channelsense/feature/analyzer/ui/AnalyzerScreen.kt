package com.mahmutalperenunal.channelsense.feature.analyzer.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Refresh
import com.mahmutalperenunal.channelsense.feature.analyzer.AnalyzerViewModel
import com.mahmutalperenunal.channelsense.ui.components.ChannelBarChart
import com.mahmutalperenunal.channelsense.wifi.permissions.WifiPermissionHelper
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.Alignment
import com.mahmutalperenunal.channelsense.wifi.model.ChannelUsage
import com.mahmutalperenunal.channelsense.wifi.model.WifiBand
import androidx.compose.ui.res.stringResource
import com.mahmutalperenunal.channelsense.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyzerScreen(
    onChannelSelected: (Int) -> Unit,
    onOpenSettings: () -> Unit,
    viewModel: AnalyzerViewModel = viewModel()
) {
    val context = LocalContext.current

    val showInfoSheet = remember { mutableStateOf(false) }

    var hasPermission by remember {
        mutableStateOf(WifiPermissionHelper.hasRequiredPermissions(context))
    }

    val requiredPermissions = remember { WifiPermissionHelper.requiredPermissions() }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        val granted = result.values.all { it }
        hasPermission = granted
        if (granted) {
            viewModel.onRefreshRequested()
        }
    }

    val state = viewModel.uiState

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name_channel_analyzer)) },
                actions = {
                    IconButton(onClick = {showInfoSheet.value = true }) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = stringResource(R.string.cd_info)
                        )
                    }
                    IconButton(onClick = onOpenSettings) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = stringResource(R.string.cd_settings)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        if (!hasPermission) {
            PermissionRationale(
                modifier = Modifier
                    .padding(innerPadding)
                    .padding(16.dp),
                onRequestPermission = {
                    permissionLauncher.launch(requiredPermissions)
                }
            )
        } else {
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .padding(16.dp)
                    .fillMaxSize()
            ) {
                BandSelector(
                    selectedBand = state.selectedBand,
                    onBandSelected = { viewModel.onBandSelected(it) },
                    onRefresh = { viewModel.onRefreshRequested() }
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (state.isScanning) {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.scanning_nearby_networks),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                state.errorMessage?.let { error ->
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                state.recommendation?.let { recommendation ->
                    RecommendedChannelCard(
                        channel = recommendation.recommendedChannel,
                        band = recommendation.band,
                        reason = recommendation.reason,
                        onClick = { onChannelSelected(recommendation.recommendedChannel) }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                ChannelBarChart(
                    channels = state.channels,
                    recommendedChannel = state.recommendation?.recommendedChannel,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    onChannelClick = { usage ->
                        onChannelSelected(usage.channel)
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = stringResource(R.string.channels_and_details),
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))

                if (state.channels.isEmpty() && !state.isScanning) {
                    Text(
                        text = stringResource(R.string.no_channel_info_try_refresh),
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    ChannelList(
                        channels = state.channels,
                        onChannelClick = { usage ->
                            onChannelSelected(usage.channel)
                        }
                    )
                }
            }
        }
    }

    if (showInfoSheet.value) {
        val sheetState = rememberModalBottomSheetState()

        ModalBottomSheet(
            onDismissRequest = { showInfoSheet.value = false },
            sheetState = sheetState
        ) {
            InfoBottomSheetContent()
        }
    }
}

@Composable
private fun PermissionRationale(
    modifier: Modifier = Modifier,
    onRequestPermission: () -> Unit
) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(R.string.permission_title_need_permission),
            style = MaterialTheme.typography.titleMedium
        )

        Text(
            text = stringResource(R.string.permission_body_location_required),
            style = MaterialTheme.typography.bodyMedium
        )

        Text(
            text = stringResource(R.string.permission_body_privacy_note),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.secondary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = onRequestPermission,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.permission_button_grant_and_scan))
        }

        Text(
            text = stringResource(R.string.permission_footer_settings_path),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.secondary
        )
    }
}

@Composable
private fun InfoBottomSheetContent() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
    ) {
        Text(
            text = stringResource(R.string.about_title),
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = stringResource(R.string.about_body_what_it_does),
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(R.string.about_body_why_permission),
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(R.string.about_body_privacy),
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(R.string.about_body_router_note),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}


@Composable
private fun BandSelector(
    selectedBand: WifiBand,
    onBandSelected: (WifiBand) -> Unit,
    onRefresh: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = selectedBand == WifiBand.TWO_GHZ,
                onClick = { onBandSelected(WifiBand.TWO_GHZ) },
                label = { Text(stringResource(R.string.wifi_band_24ghz)) }
            )
            FilterChip(
                selected = selectedBand == WifiBand.FIVE_GHZ,
                onClick = { onBandSelected(WifiBand.FIVE_GHZ) },
                label = { Text(stringResource(R.string.wifi_band_5ghz)) }
            )
            FilterChip(
                selected = selectedBand == WifiBand.SIX_GHZ,
                onClick = { onBandSelected(WifiBand.SIX_GHZ) },
                label = { Text(stringResource(R.string.wifi_band_6ghz)) }
            )
        }

        IconButton(onClick = onRefresh) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = stringResource(R.string.action_refresh)
            )
        }
    }
}

@Composable
private fun RecommendedChannelCard(
    channel: Int,
    band: WifiBand,
    reason: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.recommended_channel_label),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(
                    R.string.recommended_channel_title,
                    channel,
                    if (band == WifiBand.TWO_GHZ) "2.4 GHz" else "5 GHz"
                ),
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = localizedReasonText(reason),
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.tap_for_detailed_guide),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}

@Composable
private fun localizedReasonText(reason: String): String {
    // Expected format from logic layer: "<string_key>|<arg1>|<arg2>"
    val parts = remember(reason) { reason.split('|') }
    if (parts.isEmpty()) return reason

    return when (parts[0]) {
        "reason_least_neighbors" -> {
            val deviceCount = parts.getOrNull(1)?.toIntOrNull() ?: 0
            val avgRssi = parts.getOrNull(2)?.toIntOrNull() ?: 0
            stringResource(R.string.reason_least_neighbors, deviceCount, avgRssi)
        }
        else -> reason
    }
}

@Composable
private fun ChannelList(
    channels: List<ChannelUsage>,
    onChannelClick: (ChannelUsage) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(channels) { usage: ChannelUsage ->
            ChannelItem(
                usage = usage,
                onClick = { onChannelClick(usage) }
            )
        }
    }
}

@Composable
private fun ChannelItem(
    usage: ChannelUsage,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = stringResource(R.string.channel_item_title, usage.channel),
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.channel_item_device_count, usage.deviceCount),
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = stringResource(R.string.channel_item_average_neighbor_signal, usage.averageRssi),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}