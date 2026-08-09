package com.mahmutalperenunal.channelsense.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mahmutalperenunal.channelsense.R
import com.mahmutalperenunal.channelsense.wifi.model.ChannelUsage

@Composable
fun ChannelBarChart(
    channels: List<ChannelUsage>,
    recommendedChannel: Int?,
    modifier: Modifier = Modifier,
    maxBarHeight: Dp = 120.dp,
    onChannelClick: (ChannelUsage) -> Unit
) {
    ElevatedCard(
        modifier = modifier,
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 0.dp)
    ) {
        Column(Modifier
            .fillMaxSize()
            .padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    stringResource(R.string.bar_chart_title_channel_density),
                    style = MaterialTheme.typography.titleMedium
                )
                recommendedChannel?.let {
                    StatusBadge(text = stringResource(R.string.chart_best_channel, it))
                }
            }
            Text(
                text = stringResource(R.string.chart_density_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(14.dp))
            if (channels.isEmpty()) Box(
                Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) { Text(stringResource(R.string.bar_chart_no_channels)) }
            else Row(
                Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .height(maxBarHeight + 62.dp),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                channels.forEach { usage ->
                    Column(
                        Modifier
                            .width(38.dp)
                            .clickable { onChannelClick(usage) },
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "${usage.occupancyPercent}%",
                            style = MaterialTheme.typography.labelSmall
                        )
                        Box(
                            Modifier
                                .height(maxBarHeight)
                                .width(26.dp),
                            contentAlignment = Alignment.BottomCenter
                        ) {
                            Box(
                                Modifier
                                    .fillMaxWidth()
                                    .height((maxBarHeight * (usage.occupancyPercent.coerceAtLeast(4) / 100f)))
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        when {
                                            usage.channel == recommendedChannel -> MaterialTheme.colorScheme.primary
                                            usage.occupancyPercent >= 75 -> MaterialTheme.colorScheme.tertiary
                                            else -> MaterialTheme.colorScheme.secondaryContainer
                                        }
                                    )
                            )
                        }
                        Spacer(Modifier.height(4.dp)); Text(
                        usage.channel.toString(),
                        style = MaterialTheme.typography.labelSmall
                    )
                    }
                }
            }
        }
    }
}
