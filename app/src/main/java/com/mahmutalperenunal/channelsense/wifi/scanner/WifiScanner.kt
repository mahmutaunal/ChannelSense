package com.mahmutalperenunal.channelsense.wifi.scanner

import android.content.Context
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import com.mahmutalperenunal.channelsense.wifi.model.WifiNetworkInfo
import com.mahmutalperenunal.channelsense.util.FrequencyUtils
import com.mahmutalperenunal.channelsense.wifi.permissions.WifiPermissionHelper

class WifiScanner(private val context: Context) {

    private val wifiManager =
        context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

    fun scan(): List<WifiNetworkInfo> {
        if (!WifiPermissionHelper.hasRequiredPermissions(context)) {
            return emptyList()
        }

        return try {
            wifiManager.startScan()

            val results = wifiManager.scanResults ?: return emptyList()

            results.mapNotNull { scanResult ->
                mapResult(scanResult)
            }
        } catch (_: SecurityException) {
            emptyList()
        }
    }

    private fun mapResult(result: ScanResult): WifiNetworkInfo? {
        val band = FrequencyUtils.bandForFrequency(result.frequency) ?: return null
        val channel = FrequencyUtils.channelForFrequency(result.frequency) ?: return null

        return WifiNetworkInfo(
            ssid = result.SSID,
            bssid = result.BSSID,
            rssi = result.level,
            frequency = result.frequency,
            band = band,
            channel = channel
        )
    }
}