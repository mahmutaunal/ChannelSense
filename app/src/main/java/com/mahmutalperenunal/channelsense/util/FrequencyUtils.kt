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
        WifiBand.TWO_GHZ -> when (channel) { 14 -> 2484; in 1..13 -> 2407 + channel * 5; else -> null }
        WifiBand.FIVE_GHZ -> if (channel in 1..196) 5000 + channel * 5 else null
        WifiBand.SIX_GHZ -> if (channel in 1..233) 5950 + channel * 5 else null
    }

    fun candidateChannels(band: WifiBand): List<Int> = when (band) {
        WifiBand.TWO_GHZ -> listOf(1, 6, 11)
        WifiBand.FIVE_GHZ -> listOf(36, 40, 44, 48, 149, 153, 157, 161)
        WifiBand.SIX_GHZ -> listOf(5, 21, 37, 53, 69, 85, 101, 117, 133, 149, 165, 181, 197, 213, 229)
    }
}
