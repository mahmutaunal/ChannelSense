package com.mahmutalperenunal.channelsense.domain.analyzer

import com.mahmutalperenunal.channelsense.wifi.model.WifiBand
import com.mahmutalperenunal.channelsense.wifi.model.WifiNetworkInfo
import org.junit.Assert.assertEquals
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

    private fun network(channel: Int, frequency: Int, rssi: Int) = WifiNetworkInfo(
        ssid = "test", bssid = channel.toString(), rssi = rssi, frequency = frequency,
        band = WifiBand.TWO_GHZ, channel = channel
    )
}
