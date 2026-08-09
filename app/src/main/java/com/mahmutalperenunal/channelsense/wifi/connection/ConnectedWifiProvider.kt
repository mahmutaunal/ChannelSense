package com.mahmutalperenunal.channelsense.wifi.connection

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.net.wifi.ScanResult
import android.os.Build
import androidx.annotation.RequiresApi
import com.mahmutalperenunal.channelsense.util.FrequencyUtils
import com.mahmutalperenunal.channelsense.wifi.model.WifiBand

data class ConnectedWifiInfo(
    val ssid: String?,
    val channel: Int?,
    val band: WifiBand?,
    val signalDbm: Int?,
    val receiveLinkSpeedMbps: Int? = null,
    val transmitLinkSpeedMbps: Int? = null,
    val wifiStandard: WifiStandard = WifiStandard.UNKNOWN
)

enum class WifiStandard {
    LEGACY,
    WIFI_4,
    WIFI_5,
    WIFI_6,
    WIFI_6_AD,
    WIFI_7,
    UNKNOWN
}

class ConnectedWifiProvider(context: Context) {
    private val appContext = context.applicationContext
    private val connectivityManager = appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private val wifiManager = appContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
    private val callbackLock = Any()
    @Volatile private var callbackConnection: CallbackConnection? = null
    @Volatile private var callbackRegistered = false

    private val networkCallback = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        LocationAwareNetworkCallback()
    } else {
        ConnectivityManager.NetworkCallback()
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private inner class LocationAwareNetworkCallback : ConnectivityManager.NetworkCallback(
        FLAG_INCLUDE_LOCATION_INFO
    ) {
        override fun onCapabilitiesChanged(network: Network, capabilities: NetworkCapabilities) {
            callbackConnection = (capabilities.transportInfo as? WifiInfo)?.let {
                CallbackConnection(network, it)
            }
        }

        override fun onLost(network: Network) {
            if (callbackConnection?.network == network) callbackConnection = null
        }
    }

    @Suppress("DEPRECATION")
    @SuppressLint("MissingPermission", "InlinedApi")
    fun current(): ConnectedWifiInfo? {
        val network = connectivityManager.activeNetwork ?: return null
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return null
        if (!capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) return null

        registerLocationAwareCallbackIfNeeded()

        val callbackInfo = callbackConnection?.takeIf { it.network == network }?.wifiInfo
        val capabilityInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            capabilities.transportInfo as? WifiInfo
        } else {
            null
        }
        val managerInfo = runCatching { wifiManager.connectionInfo }.getOrNull()
        val candidates = listOfNotNull(callbackInfo, capabilityInfo, managerInfo)
        val info = candidates.firstOrNull { normalizeSsid(it.ssid) != null }
            ?: candidates.firstOrNull()
            ?: return null
        val frequency = info.frequency
        return ConnectedWifiInfo(
            ssid = normalizeSsid(info.ssid),
            channel = FrequencyUtils.channelForFrequency(frequency),
            band = FrequencyUtils.bandForFrequency(frequency),
            signalDbm = info.rssi.takeIf { it in -126..0 },
            receiveLinkSpeedMbps = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                info.rxLinkSpeedMbps.takeIf { it > 0 }
            } else {
                info.linkSpeed.takeIf { it > 0 }
            },
            transmitLinkSpeedMbps = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                info.txLinkSpeedMbps.takeIf { it > 0 }
            } else {
                info.linkSpeed.takeIf { it > 0 }
            },
            wifiStandard = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                info.wifiStandard.toWifiStandard()
            } else {
                WifiStandard.UNKNOWN
            }
        )
    }

    fun close() {
        if (!callbackRegistered) return
        runCatching { connectivityManager.unregisterNetworkCallback(networkCallback) }
        callbackRegistered = false
        callbackConnection = null
    }

    @SuppressLint("MissingPermission")
    private fun registerLocationAwareCallbackIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S || callbackRegistered) return
        synchronized(callbackLock) {
            if (callbackRegistered) return
            callbackRegistered = runCatching {
                connectivityManager.registerDefaultNetworkCallback(networkCallback)
                true
            }.getOrDefault(false)
        }
    }

    private data class CallbackConnection(val network: Network, val wifiInfo: WifiInfo)
}

internal fun normalizeSsid(rawSsid: String?): String? = rawSsid
    ?.removeSurrounding("\"")
    ?.trim()
    ?.takeUnless {
        it.isEmpty() ||
            it.equals("<unknown ssid>", ignoreCase = true) ||
            it.equals("unknown ssid", ignoreCase = true)
    }

@RequiresApi(Build.VERSION_CODES.R)
private fun Int.toWifiStandard(): WifiStandard = when (this) {
    ScanResult.WIFI_STANDARD_LEGACY -> WifiStandard.LEGACY
    ScanResult.WIFI_STANDARD_11N -> WifiStandard.WIFI_4
    ScanResult.WIFI_STANDARD_11AC -> WifiStandard.WIFI_5
    ScanResult.WIFI_STANDARD_11AX -> WifiStandard.WIFI_6
    ScanResult.WIFI_STANDARD_11AD -> WifiStandard.WIFI_6_AD
    ScanResult.WIFI_STANDARD_11BE -> WifiStandard.WIFI_7
    else -> WifiStandard.UNKNOWN
}
