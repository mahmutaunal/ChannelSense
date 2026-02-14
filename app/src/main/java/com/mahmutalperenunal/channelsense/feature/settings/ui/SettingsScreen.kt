package com.mahmutalperenunal.channelsense.feature.settings.ui

import androidx.compose.ui.res.stringResource
import com.mahmutalperenunal.channelsense.R
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mahmutalperenunal.channelsense.wifi.model.WifiBand

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    viewModel: SettingsViewModel = viewModel()
) {
    val state by androidx.compose.runtime.remember {
        androidx.compose.runtime.derivedStateOf { viewModel.uiState }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = stringResource(R.string.cd_back)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SettingsSectionCard(
                title = stringResource(R.string.settings_section_wifi_analyzer_title),
                subtitle = stringResource(R.string.settings_section_wifi_analyzer_subtitle)
            ) {
                Spacer(modifier = Modifier.height(4.dp))
                DefaultBandSettingRow(
                    selectedBand = state.defaultBand,
                    onBandSelected = { viewModel.onDefaultBandChanged(it) }
                )
                AutoRefreshSettingRow(
                    enabled = state.autoRefreshEnabled,
                    onToggle = { viewModel.onAutoRefreshChanged(it) }
                )
            }

            Text(
                text = stringResource(R.string.settings_app_version, "1.0.1"),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}

@Composable
private fun SettingsSectionCard(
    title: String,
    subtitle: String? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = CardDefaults.elevatedShape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            content()
        }
    }
}

@Composable
private fun DefaultBandSettingRow(
    selectedBand: WifiBand,
    onBandSelected: (WifiBand) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = Icons.Default.Wifi,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = stringResource(R.string.settings_default_band_title),
                style = MaterialTheme.typography.titleSmall
            )
            Text(
                text = stringResource(R.string.settings_default_band_description),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
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
        }
    }
}

@Composable
private fun AutoRefreshSettingRow(
    enabled: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Autorenew,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = stringResource(R.string.settings_auto_refresh_title),
                style = MaterialTheme.typography.titleSmall
            )
            Text(
                text = stringResource(R.string.settings_auto_refresh_description),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary
            )
        }

        Switch(
            checked = enabled,
            onCheckedChange = onToggle
        )
    }
}