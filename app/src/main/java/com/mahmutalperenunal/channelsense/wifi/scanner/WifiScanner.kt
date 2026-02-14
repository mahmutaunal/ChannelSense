package com.mahmutalperenunal.channelsense.wifi.scanner

import android.content.Context
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.util.Log
import com.mahmutalperenunal.channelsense.wifi.model.WifiNetworkInfo
import com.mahmutalperenunal.channelsense.util.FrequencyUtils
import com.mahmutalperenunal.channelsense.wifi.permissions.WifiPermissionHelper
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Intent
import android.content.IntentFilter
import android.location.LocationManager
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class WifiScanner(private val context: Context) {

    private val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

    @SuppressLint("MissingPermission")
    suspend fun scan(): List<WifiNetworkInfo> = suspendCancellableCoroutine { cont ->

        val hasPermission = WifiPermissionHelper.hasRequiredPermissions(context)
        Log.d("SCAN", "hasPermission=$hasPermission")
        if (!hasPermission) {
            cont.resume(emptyList())
            return@suspendCancellableCoroutine
        }

        val locationManager =
            context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        Log.d("SCAN", "locationEnabled=${locationManager.isLocationEnabled}")

        val receiver = object : BroadcastReceiver() {
            override fun onReceive(c: Context, intent: Intent) {
                try {
                    val results = wifiManager.scanResults.orEmpty()

                    Log.d("SCAN", "scanResults size=${results.size}")
                    results.forEach {
                        Log.d(
                            "SCAN",
                            "ssid=${it.SSID} bssid=${it.BSSID} freq=${it.frequency} level=${it.level}"
                        )
                    }

                    val mapped = results.mapNotNull { mapResult(it) }
                    cont.resume(mapped)
                } catch (_: SecurityException) {
                    cont.resume(emptyList())
                } finally {
                    runCatching { context.unregisterReceiver(this) }
                }
            }
        }

        context.registerReceiver(
            receiver,
            IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
        )

        val started = wifiManager.startScan()
        Log.d("SCAN", "startScan returned=$started")

        if (!started) {
            runCatching { context.unregisterReceiver(receiver) }
            cont.resume(emptyList())
            return@suspendCancellableCoroutine
        }

        cont.invokeOnCancellation {
            runCatching { context.unregisterReceiver(receiver) }
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