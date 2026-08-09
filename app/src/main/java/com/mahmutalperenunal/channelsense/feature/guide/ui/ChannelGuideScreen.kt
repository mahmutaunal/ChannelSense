package com.mahmutalperenunal.channelsense.feature.guide.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Router
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mahmutalperenunal.channelsense.R
import com.mahmutalperenunal.channelsense.feature.guide.ChannelGuideProvider
import com.mahmutalperenunal.channelsense.feature.guide.model.GuideStep
import com.mahmutalperenunal.channelsense.feature.guide.model.RouterBrand
import com.mahmutalperenunal.channelsense.ui.components.ChannelSenseTopBar
import com.mahmutalperenunal.channelsense.ui.components.SectionHeading
import com.mahmutalperenunal.channelsense.util.NetworkUtils

@Composable
fun ChannelGuideScreen(channel: Int, onBackClick: () -> Unit) {
    val context = LocalContext.current
    var selectedBrand by remember { mutableStateOf(RouterBrand.OTHER) }
    val steps = remember(context, channel, selectedBrand) {
        ChannelGuideProvider.getSteps(context, selectedBrand, channel)
    }

    Scaffold(
        topBar = {
            ChannelSenseTopBar(
                title = stringResource(R.string.guide_title_channel, channel),
                subtitle = stringResource(R.string.guide_topbar_subtitle),
                onBackClick = onBackClick
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.padding(innerPadding).fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            item { ChannelHero(channel) }
            item {
                SectionHeading(
                    title = stringResource(R.string.guide_router_brand_label),
                    subtitle = stringResource(R.string.guide_select_brand_instructions)
                )
            }
            item {
                RouterBrandSelector(
                    selectedBrand = selectedBrand,
                    onBrandSelected = { selectedBrand = it }
                )
            }
            item {
                Button(
                    onClick = { NetworkUtils.openRouterPage(context) },
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(vertical = 15.dp)
                ) {
                    Icon(Icons.Default.OpenInBrowser, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.guide_open_router_interface))
                }
            }
            item {
                SectionHeading(
                    title = stringResource(R.string.guide_step_by_step),
                    subtitle = stringResource(R.string.guide_steps_subtitle)
                )
            }
            items(steps, key = { it.order }) { step ->
                GuideStepItem(step)
            }
        }
    }
}

@Composable
private fun ChannelHero(channel: Int) {
    ElevatedCard(
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Surface(
                shape = MaterialTheme.shapes.extraLarge,
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.65f)
            ) {
                Icon(
                    Icons.Default.Router,
                    contentDescription = null,
                    modifier = Modifier.padding(16.dp).size(28.dp)
                )
            }
            Column {
                Text(
                    text = stringResource(R.string.guide_recommended_setup),
                    style = MaterialTheme.typography.labelLarge
                )
                Text(
                    text = stringResource(R.string.channel_number_only, channel),
                    style = MaterialTheme.typography.headlineLarge
                )
                Text(
                    text = stringResource(R.string.guide_change_safely_note),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun RouterBrandSelector(
    selectedBrand: RouterBrand,
    onBrandSelected: (RouterBrand) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        RouterBrand.entries.forEach { brand ->
            FilterChip(
                selected = selectedBrand == brand,
                onClick = { onBrandSelected(brand) },
                label = { Text(stringResource(brand.nameRes)) }
            )
        }
    }
}

@Composable
private fun GuideStepItem(step: GuideStep) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ) {
            Box(modifier = Modifier.size(38.dp), contentAlignment = Alignment.Center) {
                Text(step.order.toString(), style = MaterialTheme.typography.labelLarge)
            }
        }
        Surface(
            modifier = Modifier.weight(1f),
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surfaceContainerLow
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = stringResource(R.string.guide_step_label, step.order),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(text = step.text, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}
