package com.mahmutalperenunal.channelsense.feature.settings.ui

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mahmutalperenunal.channelsense.R
import com.mahmutalperenunal.channelsense.wifi.model.WifiBand
import kotlinx.coroutines.launch
import androidx.core.net.toUri

private const val STUDIO_WEBSITE_URL = "https://www.alpwarestudio.com/"
private const val SOURCE_CODE_URL = "https://github.com/mahmutaunal/ChannelSense"
private const val PRIVACY_POLICY_URL = "https://mahmutaunal.github.io/ChannelSense/"
private const val MORE_APPS_URL = "https://play.google.com/store/apps/dev?id=5245599652065968716"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    onCheckForUpdate: () -> Unit,
    onRequestReview: () -> Unit,
    viewModel: SettingsViewModel = viewModel()
) {
    val state = viewModel.uiState
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val linkErrorMessage = stringResource(R.string.settings_link_open_error)
    val versionName = remember(context) { context.appVersionName() }

    fun openLink(url: String) {
        if (!context.openExternalLink(url)) {
            scope.launch { snackbarHostState.showSnackbar(linkErrorMessage) }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.cd_back)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
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
                        onClick = onRequestReview
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
}

@Composable
private fun SettingsSection(
    title: String,
    subtitle: String? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Column(
            modifier = Modifier.padding(horizontal = 4.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
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
            shape = CardDefaults.elevatedShape,
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            tonalElevation = 1.dp
        ) {
            Column(content = content)
        }
    }
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
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.padding(8.dp).size(20.dp)
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
