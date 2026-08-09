package com.mahmutalperenunal.channelsense.domain.quality

import com.mahmutalperenunal.channelsense.wifi.connection.ConnectedWifiInfo
import com.mahmutalperenunal.channelsense.wifi.connection.WifiStandard
import com.mahmutalperenunal.channelsense.wifi.model.ChannelUsage
import com.mahmutalperenunal.channelsense.wifi.model.WifiBand
import org.junit.Assert.assertEquals
import org.junit.Test

class ConnectionQualityEvaluatorTest {
    @Test
    fun `excellent signal fast link and clear channel produce excellent quality`() {
        val quality = ConnectionQualityEvaluator.evaluate(
            wifi(signal = -48, receive = 1_200, transmit = 1_100),
            usage(interferenceScore = 0.2)
        )

        assertEquals(ConnectionQualityGrade.EXCELLENT, quality.grade)
        assertEquals(ConnectionLimitingFactor.NONE, quality.limitingFactor)
        assertEquals(97, quality.score)
    }

    @Test
    fun `heavy interference identifies congestion as limiting factor`() {
        val quality = ConnectionQualityEvaluator.evaluate(
            wifi(signal = -55, receive = 600, transmit = 600),
            usage(interferenceScore = 12.0)
        )

        assertEquals(ConnectionLimitingFactor.CONGESTION, quality.limitingFactor)
        assertEquals(86, quality.congestionPercent)
    }

    @Test
    fun `weak signal identifies signal as limiting factor`() {
        val quality = ConnectionQualityEvaluator.evaluate(
            wifi(signal = -82, receive = 300, transmit = 300),
            usage(interferenceScore = 0.5)
        )

        assertEquals(ConnectionLimitingFactor.SIGNAL, quality.limitingFactor)
        assertEquals(ConnectionQualityGrade.FAIR, quality.grade)
    }

    @Test
    fun `missing optional telemetry uses neutral values`() {
        val quality = ConnectionQualityEvaluator.evaluate(
            wifi(signal = null, receive = null, transmit = null),
            currentChannelUsage = null
        )

        assertEquals(60, quality.score)
        assertEquals(ConnectionQualityGrade.FAIR, quality.grade)
    }

    private fun wifi(signal: Int?, receive: Int?, transmit: Int?) = ConnectedWifiInfo(
        ssid = "Home WiFi",
        channel = 6,
        band = WifiBand.TWO_GHZ,
        signalDbm = signal,
        receiveLinkSpeedMbps = receive,
        transmitLinkSpeedMbps = transmit,
        wifiStandard = WifiStandard.WIFI_6
    )

    private fun usage(interferenceScore: Double) = ChannelUsage(
        channel = 6,
        band = WifiBand.TWO_GHZ,
        detectedAccessPoints = 4,
        averageRssi = -60,
        interferenceScore = interferenceScore,
        occupancyPercent = 0
    )
}
