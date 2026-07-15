package com.mahmutalperenunal.channelsense.domain.analyzer

import com.mahmutalperenunal.channelsense.feature.analyzer.logic.ChannelRecommendation
import com.mahmutalperenunal.channelsense.feature.analyzer.logic.RecommendationConfidence
import com.mahmutalperenunal.channelsense.util.FrequencyUtils
import com.mahmutalperenunal.channelsense.wifi.model.ChannelUsage
import com.mahmutalperenunal.channelsense.wifi.model.WifiBand
import com.mahmutalperenunal.channelsense.wifi.model.WifiNetworkInfo
import kotlin.math.max
import kotlin.math.min

/** Pure, Android-independent RF approximation suitable for deterministic unit tests. */
object ChannelRecommendationEngine {

    data class Analysis(
        val usages: List<ChannelUsage>,
        val recommendation: ChannelRecommendation?
    )

    fun analyze(
        networks: List<WifiNetworkInfo>,
        band: WifiBand,
        sampleCount: Int,
        currentChannel: Int? = null
    ): Analysis {
        val observed = networks.filter { it.band == band }
        if (observed.isEmpty()) return Analysis(emptyList(), null)

        val candidates = FrequencyUtils.candidateChannels(band)
        val rawScores = candidates.associateWith { candidate ->
            val candidateFrequency = FrequencyUtils.frequencyForChannel(candidate, band) ?: return@associateWith Double.MAX_VALUE
            observed.sumOf { accessPoint ->
                interferenceContribution(candidateFrequency, accessPoint)
            }
        }
        val finiteScores = rawScores.filterValues { it.isFinite() && it < Double.MAX_VALUE }
        val maxScore = finiteScores.values.maxOrNull()?.coerceAtLeast(0.01) ?: 1.0

        val usages = candidates.mapNotNull { candidate ->
            val frequency = FrequencyUtils.frequencyForChannel(candidate, band) ?: return@mapNotNull null
            val nearby = observed.filter { accessPoint ->
                frequencyOverlapRatio(frequency, 20, accessPoint.centerFrequencyMhz, accessPoint.channelWidthMhz) > 0.0
            }
            val score = finiteScores[candidate] ?: return@mapNotNull null
            ChannelUsage(
                channel = candidate,
                band = band,
                detectedAccessPoints = nearby.size,
                averageRssi = nearby.map { it.rssi }.average().takeUnless { it.isNaN() }?.toInt() ?: -100,
                interferenceScore = score,
                occupancyPercent = ((score / maxScore) * 100.0).toInt().coerceIn(0, 100)
            )
        }

        val ranked = usages.sortedBy { it.interferenceScore }
        val best = ranked.firstOrNull() ?: return Analysis(usages, null)
        val second = ranked.getOrNull(1)
        val margin = if (second == null || second.interferenceScore <= 0.0) 1.0
        else (second.interferenceScore - best.interferenceScore) / second.interferenceScore
        val confidence = when {
            sampleCount >= 3 && margin >= 0.25 -> RecommendationConfidence.HIGH
            sampleCount >= 2 || margin >= 0.12 -> RecommendationConfidence.MEDIUM
            else -> RecommendationConfidence.LOW
        }
        val currentScore = usages.firstOrNull { it.channel == currentChannel }?.interferenceScore
        val acceptable = currentScore != null && currentScore <= best.interferenceScore * 1.15 + 0.05

        return Analysis(
            usages = usages,
            recommendation = ChannelRecommendation(
                band = band,
                recommendedChannel = best.channel,
                score = best.interferenceScore,
                confidence = confidence,
                sampleCount = sampleCount.coerceAtLeast(1),
                detectedAccessPoints = observed.distinctBy { it.bssid ?: "${it.ssid}-${it.frequency}" }.size,
                alternativeChannels = ranked.drop(1).take(2).map { it.channel },
                isCurrentChannelAcceptable = acceptable
            )
        )
    }

    private fun interferenceContribution(candidateFrequency: Int, network: WifiNetworkInfo): Double {
        val overlap = frequencyOverlapRatio(
            candidateCenter = candidateFrequency,
            candidateWidth = 20,
            accessPointCenter = network.centerFrequencyMhz,
            accessPointWidth = network.channelWidthMhz
        )
        if (overlap <= 0.0) return 0.0
        return overlap * rssiWeight(network.rssi) * widthWeight(network.channelWidthMhz)
    }

    internal fun rssiWeight(rssi: Int): Double = when {
        rssi >= -50 -> 1.0
        rssi >= -60 -> 0.75
        rssi >= -70 -> 0.45
        rssi >= -80 -> 0.20
        else -> 0.08
    }

    internal fun frequencyOverlapRatio(
        candidateCenter: Int,
        candidateWidth: Int,
        accessPointCenter: Int,
        accessPointWidth: Int
    ): Double {
        val candidateStart = candidateCenter - candidateWidth / 2.0
        val candidateEnd = candidateCenter + candidateWidth / 2.0
        val accessPointStart = accessPointCenter - accessPointWidth / 2.0
        val accessPointEnd = accessPointCenter + accessPointWidth / 2.0
        val overlap = max(0.0, min(candidateEnd, accessPointEnd) - max(candidateStart, accessPointStart))
        return (overlap / candidateWidth.toDouble()).coerceIn(0.0, 1.0)
    }

    private fun widthWeight(widthMhz: Int): Double = when {
        widthMhz >= 320 -> 1.45
        widthMhz >= 160 -> 1.30
        widthMhz >= 80 -> 1.18
        widthMhz >= 40 -> 1.08
        else -> 1.0
    }
}
