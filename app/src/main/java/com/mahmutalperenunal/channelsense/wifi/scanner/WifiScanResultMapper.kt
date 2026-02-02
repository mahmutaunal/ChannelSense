package com.mahmutalperenunal.channelsense.wifi.scanner

import com.mahmutalperenunal.channelsense.wifi.model.ChannelUsage
import com.mahmutalperenunal.channelsense.wifi.model.WifiBand
import com.mahmutalperenunal.channelsense.wifi.model.WifiNetworkInfo

object WifiScanResultMapper {

    fun toChannelUsage(networks: List<WifiNetworkInfo>): List<ChannelUsage> {
        return networks.groupBy { it.channel }.map { (channel, list) ->
            val band = list.first().band
            val avgRssi =
                list.map { it.rssi }.average().toInt()

            ChannelUsage(
                channel = channel,
                band = band,
                deviceCount = list.size,
                averageRssi = avgRssi
            )
        }.sortedBy { it.channel }
    }

    fun filterByBand(
        usage: List<ChannelUsage>,
        band: WifiBand
    ) = usage.filter { it.band == band }
}