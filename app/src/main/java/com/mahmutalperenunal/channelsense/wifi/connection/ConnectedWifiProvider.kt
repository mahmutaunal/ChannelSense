package com.mahmutalperenunal.channelsense.wifi.connection

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Build
import com.mahmutalperenunal.channelsense.util.FrequencyUtils
import com.mahmutalperenunal.channelsense.wifi.model.WifiBand

data class ConnectedWifiInfo(val ssid: String?, val channel: Int?, val band: WifiBand?, val signalDbm: Int?)

class ConnectedWifiProvider(context: Context) {
    private val appContext = context.applicationContext
    private val connectivityManager = appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private val wifiManager = appContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

    @Suppress("DEPRECATION")
    @SuppressLint("MissingPermission", "InlinedApi")
    fun current(): ConnectedWifiInfo? {
        val info: WifiInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val network = connectivityManager.activeNetwork ?: return null
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return null
            if (!capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) return null
            capabilities.transportInfo as? WifiInfo ?: return null
        } else {
            wifiManager.connectionInfo ?: return null
        }
        val frequency = info.frequency
        return ConnectedWifiInfo(
            ssid = info.ssid?.removeSurrounding("\"")?.takeUnless { it == WifiManager.UNKNOWN_SSID },
            channel = FrequencyUtils.channelForFrequency(frequency),
            band = FrequencyUtils.bandForFrequency(frequency),
            signalDbm = info.rssi
        )
    }
}
