package com.mahmutalperenunal.channelsense.util

import com.mahmutalperenunal.channelsense.wifi.model.WifiBand

object FrequencyUtils {

    fun bandForFrequency(frequency: Int): WifiBand? {
        return when (frequency) {
            in 2400..2500 -> WifiBand.TWO_GHZ
            in 4900..5900 -> WifiBand.FIVE_GHZ
            else -> null
        }
    }

    fun channelForFrequency(frequency: Int): Int? {
        if (frequency in 2412..2484) {
            return when (frequency) {
                2484 -> 14
                else -> ((frequency - 2407) / 5)
            }
        }

        return when (frequency) {
            5180 -> 36
            5200 -> 40
            5220 -> 44
            5240 -> 48
            5260 -> 52
            5280 -> 56
            5300 -> 60
            5320 -> 64
            5500 -> 100
            5520 -> 104
            5540 -> 108
            5560 -> 112
            5580 -> 116
            5600 -> 120
            5620 -> 124
            5640 -> 128
            5660 -> 132
            5680 -> 136
            5700 -> 140
            5745 -> 149
            5765 -> 153
            5785 -> 157
            5805 -> 161
            5825 -> 165
            else -> null
        }
    }
}