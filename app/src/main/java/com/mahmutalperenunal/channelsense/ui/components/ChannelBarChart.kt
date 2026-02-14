package com.mahmutalperenunal.channelsense.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
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
    if (channels.isEmpty()) {
        Surface(
            modifier = modifier,
            tonalElevation = 1.dp
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.bar_chart_no_channels),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
        return
    }

    val maxDeviceCount = channels.maxOf { it.deviceCount }.coerceAtLeast(1)

    Surface(
        modifier = modifier,
        tonalElevation = 1.dp,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = stringResource(R.string.bar_chart_title_channel_density),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(maxBarHeight + 56.dp),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                channels.forEach { usage ->
                    val fraction = usage.deviceCount.toFloat() / maxDeviceCount.toFloat()
                    val barHeight = (maxBarHeight * fraction.coerceIn(0f, 1f))

                    ChannelBar(
                        usage = usage,
                        height = barHeight,
                        maxBarHeight = maxBarHeight,
                        isRecommended = usage.channel == recommendedChannel,
                        onClick = { onChannelClick(usage) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ChannelBar(
    usage: ChannelUsage,
    height: Dp,
    maxBarHeight: Dp,
    isRecommended: Boolean,
    onClick: () -> Unit
) {
    val barColor = if (isRecommended) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.secondaryContainer
    }

    Column(
        modifier = Modifier
            .widthIn(min = 32.dp)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = usage.deviceCount.toString(),
            style = MaterialTheme.typography.labelSmall
        )
        Spacer(modifier = Modifier.height(4.dp))

        Box(
            modifier = Modifier
                .height(maxBarHeight)
                .width(18.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Box(
                modifier = Modifier
                    .height(height.coerceAtLeast(4.dp))
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                    .background(barColor)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = usage.channel.toString(),
            style = MaterialTheme.typography.labelSmall
        )
    }
}