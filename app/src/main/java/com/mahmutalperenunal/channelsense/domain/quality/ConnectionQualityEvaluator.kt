package com.mahmutalperenunal.channelsense.domain.quality

import com.mahmutalperenunal.channelsense.wifi.connection.ConnectedWifiInfo
import com.mahmutalperenunal.channelsense.wifi.model.ChannelUsage
import kotlin.math.roundToInt

enum class ConnectionQualityGrade { EXCELLENT, GOOD, FAIR, POOR }

enum class ConnectionLimitingFactor { NONE, SIGNAL, CONGESTION, LINK_SPEED }

data class ConnectionQuality(
    val score: Int,
    val grade: ConnectionQualityGrade,
    val limitingFactor: ConnectionLimitingFactor,
    val congestionPercent: Int?
)

/** An explainable local score; it does not claim to measure internet throughput. */
object ConnectionQualityEvaluator {
    fun evaluate(
        wifi: ConnectedWifiInfo,
        currentChannelUsage: ChannelUsage?
    ): ConnectionQuality {
        val signalScore = signalScore(wifi.signalDbm)
        val linkScore = linkScore(wifi.receiveLinkSpeedMbps, wifi.transmitLinkSpeedMbps)
        val congestionPercent = currentChannelUsage?.let {
            estimatedCongestionPercent(it.interferenceScore)
        }
        val congestionScore = congestionPercent
            ?.let { ((100 - it) * 0.25).toInt() }
            ?: 15
        val score = (signalScore + linkScore + congestionScore).coerceIn(0, 100)
        val limitingFactor = listOf(
            ConnectionLimitingFactor.SIGNAL to (signalScore / 45.0),
            ConnectionLimitingFactor.LINK_SPEED to (linkScore / 30.0),
            ConnectionLimitingFactor.CONGESTION to (congestionScore / 25.0)
        ).minBy { it.second }.let { (factor, ratio) ->
            if (ratio >= 0.72) ConnectionLimitingFactor.NONE else factor
        }

        return ConnectionQuality(
            score = score,
            grade = when {
                score >= 85 -> ConnectionQualityGrade.EXCELLENT
                score >= 70 -> ConnectionQualityGrade.GOOD
                score >= 50 -> ConnectionQualityGrade.FAIR
                else -> ConnectionQualityGrade.POOR
            },
            limitingFactor = limitingFactor,
            congestionPercent = congestionPercent
        )
    }

    internal fun signalScore(rssi: Int?): Int = when {
        rssi == null -> 27
        rssi >= -50 -> 45
        rssi >= -60 -> 40
        rssi >= -67 -> 34
        rssi >= -75 -> 23
        rssi >= -85 -> 10
        else -> 2
    }

    internal fun linkScore(receiveMbps: Int?, transmitMbps: Int?): Int {
        val effectiveSpeed = listOfNotNull(receiveMbps, transmitMbps).minOrNull() ?: return 18
        return when {
            effectiveSpeed >= 1_000 -> 30
            effectiveSpeed >= 600 -> 28
            effectiveSpeed >= 300 -> 25
            effectiveSpeed >= 150 -> 21
            effectiveSpeed >= 72 -> 16
            effectiveSpeed >= 30 -> 10
            else -> 4
        }
    }

    /** Maps an unbounded RF interference score to a stable, absolute 0–100 estimate. */
    fun estimatedCongestionPercent(interferenceScore: Double): Int {
        val score = interferenceScore.coerceAtLeast(0.0)
        return ((score / (score + 2.0)) * 100.0).roundToInt().coerceIn(0, 100)
    }
}
