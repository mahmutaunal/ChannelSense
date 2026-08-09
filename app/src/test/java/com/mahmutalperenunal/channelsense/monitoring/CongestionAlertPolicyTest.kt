package com.mahmutalperenunal.channelsense.monitoring

import com.mahmutalperenunal.channelsense.domain.analyzer.ChannelRecommendationEngine
import com.mahmutalperenunal.channelsense.feature.analyzer.logic.ChannelRecommendation
import com.mahmutalperenunal.channelsense.feature.analyzer.logic.RecommendationConfidence
import com.mahmutalperenunal.channelsense.wifi.connection.ConnectedWifiInfo
import com.mahmutalperenunal.channelsense.wifi.model.ChannelUsage
import com.mahmutalperenunal.channelsense.wifi.model.WifiBand
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CongestionAlertPolicyTest {
    @Test
    fun `creates alert when current channel is highly congested and a better channel exists`() {
        val alert = CongestionAlertPolicy.evaluate(connected(), analysis(interferenceScore = 8.0))

        assertEquals("Home WiFi", alert?.networkName)
        assertEquals(6, alert?.currentChannel)
        assertEquals(80, alert?.occupancyPercent)
        assertEquals(11, alert?.recommendedChannel)
    }

    @Test
    fun `does not alert below high congestion threshold`() {
        assertNull(CongestionAlertPolicy.evaluate(connected(), analysis(interferenceScore = 5.0)))
    }

    @Test
    fun `does not alert when current channel is acceptable`() {
        assertNull(
            CongestionAlertPolicy.evaluate(
                connected(),
                analysis(interferenceScore = 12.0, currentAcceptable = true)
            )
        )
    }

    private fun connected() = ConnectedWifiInfo(
        ssid = "Home WiFi",
        channel = 6,
        band = WifiBand.TWO_GHZ,
        signalDbm = -55
    )

    private fun analysis(
        interferenceScore: Double,
        currentAcceptable: Boolean = false
    ) = ChannelRecommendationEngine.Analysis(
        usages = listOf(
            ChannelUsage(6, WifiBand.TWO_GHZ, 8, -58, interferenceScore, 0),
            ChannelUsage(11, WifiBand.TWO_GHZ, 2, -75, 2.0, 20)
        ),
        recommendation = ChannelRecommendation(
            band = WifiBand.TWO_GHZ,
            recommendedChannel = 11,
            score = 2.0,
            confidence = RecommendationConfidence.MEDIUM,
            sampleCount = 1,
            detectedAccessPoints = 10,
            alternativeChannels = listOf(1),
            isCurrentChannelAcceptable = currentAcceptable
        )
    )
}
