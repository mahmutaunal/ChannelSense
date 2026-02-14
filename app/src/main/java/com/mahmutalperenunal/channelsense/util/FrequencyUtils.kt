package com.mahmutalperenunal.channelsense.util

import com.mahmutalperenunal.channelsense.wifi.model.WifiBand

object FrequencyUtils {

    fun bandForFrequency(frequency: Int): WifiBand? {
        return when (frequency) {
            in 2400..2500 -> WifiBand.TWO_GHZ
            // 5 GHz Wi‑Fi (incl. DFS). Typical range roughly 5000–5895 MHz.
            in 4900..5899 -> WifiBand.FIVE_GHZ
            // If your app later supports 6 GHz, you can add a new band enum and map it here.
            in 5925..7125 -> WifiBand.SIX_GHZ
            else -> null
        }
    }

    fun channelForFrequency(frequency: Int): Int? = when (frequency) {
        2484 -> 14
        in 2412..2472 -> if ((frequency - 2407) % 5 == 0) (frequency - 2407) / 5 else null

        // 4.9 GHz (Japan): 4915->183, 4920->184, ... 4980->196
        in 4915..4980 -> if ((frequency - 4000) % 5 == 0) (frequency - 4000) / 5 else null

        // 5 GHz (incl. DFS): 5180->36 ... 5825->165
        in 5000..5899 -> if ((frequency - 5000) % 5 == 0) (frequency - 5000) / 5 else null

        // 6 GHz (Wi-Fi 6E): 5955->1, 5975->5, ...
        in 5955..7115 -> if ((frequency - 5950) % 5 == 0) (frequency - 5950) / 5 else null

        else -> null
    }
}