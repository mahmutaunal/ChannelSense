package com.mahmutalperenunal.channelsense.util

import com.mahmutalperenunal.channelsense.wifi.model.WifiBand

object FrequencyUtils {
    fun bandForFrequency(frequency: Int): WifiBand? = when (frequency) {
        in 2400..2500 -> WifiBand.TWO_GHZ
        in 4900..5899 -> WifiBand.FIVE_GHZ
        in 5925..7125 -> WifiBand.SIX_GHZ
        else -> null
    }

    fun channelForFrequency(frequency: Int): Int? = when (frequency) {
        2484 -> 14
        in 2412..2472 -> if ((frequency - 2407) % 5 == 0) (frequency - 2407) / 5 else null
        in 4915..4980 -> if ((frequency - 4000) % 5 == 0) (frequency - 4000) / 5 else null
        in 5000..5899 -> if ((frequency - 5000) % 5 == 0) (frequency - 5000) / 5 else null
        in 5955..7115 -> if ((frequency - 5950) % 5 == 0) (frequency - 5950) / 5 else null
        else -> null
    }

    fun frequencyForChannel(channel: Int, band: WifiBand): Int? = when (band) {
        WifiBand.TWO_GHZ -> when {
            channel == 14 -> 2484
            channel in 1..13 -> 2407 + channel * 5
            else -> null
        }
        WifiBand.FIVE_GHZ -> when {
            channel in 183..196 -> 4000 + channel * 5 // 4.9 GHz public-safety allocation
            channel in 1..177 -> 5000 + channel * 5
            else -> null
        }
        WifiBand.SIX_GHZ -> if (channel in 1..233) 5950 + channel * 5 else null
    }

    /**
     * Complete 20 MHz channel plan used by the analyzer UI.
     *
     * Availability is ultimately determined by the device firmware and its regulatory domain.
     * Observed channels are always merged into this plan by [displayChannels], so a valid scan
     * result can never be silently discarded merely because it is absent from this baseline.
     */
    private fun standardChannels(band: WifiBand): List<Int> = when (band) {
        WifiBand.TWO_GHZ -> (1..14).toList()
        WifiBand.FIVE_GHZ -> buildList {
            addAll((36..64 step 4).toList())
            addAll((100..144 step 4).toList())
            addAll((149..177 step 4).toList())
        }
        WifiBand.SIX_GHZ -> (1..233 step 4).toList()
    }

    fun displayChannels(band: WifiBand, observedChannels: Collection<Int> = emptyList()): List<Int> =
        (standardChannels(band) + observedChannels)
            .distinct()
            .filter { frequencyForChannel(it, band) != null }
            .sorted()

    /**
     * Channels used for recommendations. The UI still displays every channel from [displayChannels].
     * On 2.4 GHz, recommending the conventional non-overlapping 20 MHz set avoids misleading users.
     */
    fun candidateChannels(band: WifiBand, observedChannels: Collection<Int> = emptyList()): List<Int> =
        when (band) {
            WifiBand.TWO_GHZ -> listOf(1, 6, 11)
            WifiBand.FIVE_GHZ -> displayChannels(band, observedChannels)
            WifiBand.SIX_GHZ -> (5..229 step 16).toList()
        }
}
