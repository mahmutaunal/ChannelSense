package com.mahmutalperenunal.channelsense.domain.analyzer

import com.mahmutalperenunal.channelsense.util.FrequencyUtils
import com.mahmutalperenunal.channelsense.wifi.model.WifiBand
import com.mahmutalperenunal.channelsense.wifi.model.WifiNetworkInfo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ChannelRecommendationEngineTest {
    @Test fun strongChannelOneMakesElevenPreferable() {
        val networks = listOf(
            network(channel = 1, frequency = 2412, rssi = -42),
            network(channel = 6, frequency = 2437, rssi = -65)
        )
        val result = ChannelRecommendationEngine.analyze(networks, WifiBand.TWO_GHZ, 3)
        assertEquals(11, result.recommendation?.recommendedChannel)
    }

    @Test fun widerNetworkProducesOverlapPenalty() {
        val overlap = ChannelRecommendationEngine.frequencyOverlapRatio(5180, 20, 5210, 80)
        assertTrue(overlap > 0.0)
    }

    @Test fun fiveGhzDfsChannelsAreIncludedInChannelPlan() {
        val channels = FrequencyUtils.displayChannels(WifiBand.FIVE_GHZ)
        assertTrue(channels.containsAll(listOf(52, 56, 60, 64, 100, 116, 132, 144)))
    }

    @Test fun sixGhzShowsAllTwentyMhzChannelsInsteadOfOnlyPscChannels() {
        val channels = FrequencyUtils.displayChannels(WifiBand.SIX_GHZ)
        assertTrue(channels.containsAll(listOf(1, 5, 9, 13, 17, 21, 229, 233)))
        assertEquals(59, channels.size)
    }

    @Test fun observedChannelIsNeverDroppedFromAnalysis() {
        val result = ChannelRecommendationEngine.analyze(
            networks = listOf(
                WifiNetworkInfo(
                    ssid = "dfs", bssid = "00:11:22:33:44:55", rssi = -48,
                    frequency = 5260, band = WifiBand.FIVE_GHZ, channel = 52
                )
            ),
            band = WifiBand.FIVE_GHZ,
            sampleCount = 1
        )
        val channel52 = result.usages.firstOrNull { it.channel == 52 }
        assertNotNull(channel52)
        assertEquals(1, channel52?.detectedAccessPoints)
    }

    @Test fun emptyBandStillReturnsCompleteZeroUsageChannelPlan() {
        val result = ChannelRecommendationEngine.analyze(emptyList(), WifiBand.FIVE_GHZ, 1)
        assertTrue(result.usages.any { it.channel == 52 })
        assertEquals(null, result.recommendation)
    }

    private fun network(channel: Int, frequency: Int, rssi: Int) = WifiNetworkInfo(
        ssid = "test", bssid = channel.toString(), rssi = rssi, frequency = frequency,
        band = WifiBand.TWO_GHZ, channel = channel
    )
}
