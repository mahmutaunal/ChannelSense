package com.mahmutalperenunal.channelsense.wifi.scanner

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.LocationManager
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import androidx.core.content.ContextCompat
import com.mahmutalperenunal.channelsense.util.FrequencyUtils
import com.mahmutalperenunal.channelsense.wifi.model.WifiNetworkInfo
import com.mahmutalperenunal.channelsense.wifi.permissions.WifiPermissionHelper
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume
import kotlin.time.Duration.Companion.milliseconds

sealed interface WifiScanOutcome {
    data class Success(val networks: List<WifiNetworkInfo>, val fresh: Boolean) : WifiScanOutcome
    data object PermissionRequired : WifiScanOutcome
    data object WifiDisabled : WifiScanOutcome
    data object LocationDisabled : WifiScanOutcome
    data object ThrottledOrUnavailable : WifiScanOutcome
    data class Failure(val cause: Throwable? = null) : WifiScanOutcome
}

class WifiScanner(context: Context) {
    private val appContext = context.applicationContext
    private val wifiManager = appContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

    @SuppressLint("MissingPermission")
    suspend fun scan(): WifiScanOutcome {
        if (!WifiPermissionHelper.hasRequiredPermissions(appContext)) return WifiScanOutcome.PermissionRequired
        if (!wifiManager.isWifiEnabled) return WifiScanOutcome.WifiDisabled
        val locationManager = appContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (!locationManager.isLocationEnabled) return WifiScanOutcome.LocationDisabled

        return withTimeoutOrNull(15_000.milliseconds) {
            suspendCancellableCoroutine { continuation ->
                val receiver = object : BroadcastReceiver() {
                    override fun onReceive(receiverContext: Context, intent: Intent) {
                        val fresh = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false)
                        val outcome = runCatching {
                            val mapped = wifiManager.scanResults.orEmpty().mapNotNull(::mapResult)
                            WifiScanOutcome.Success(mapped, fresh)
                        }.getOrElse { WifiScanOutcome.Failure(it) }
                        if (continuation.isActive) continuation.resume(outcome)
                        runCatching { appContext.unregisterReceiver(this) }
                    }
                }

                ContextCompat.registerReceiver(
                    appContext,
                    receiver,
                    IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION),
                    ContextCompat.RECEIVER_NOT_EXPORTED
                )

                val started = runCatching { wifiManager.startScan() }.getOrDefault(false)
                if (!started) {
                    runCatching { appContext.unregisterReceiver(receiver) }
                    val cached = runCatching { wifiManager.scanResults.orEmpty().mapNotNull(::mapResult) }.getOrDefault(emptyList())
                    continuation.resume(
                        if (cached.isNotEmpty()) WifiScanOutcome.Success(cached, fresh = false)
                        else WifiScanOutcome.ThrottledOrUnavailable
                    )
                }

                continuation.invokeOnCancellation {
                    runCatching { appContext.unregisterReceiver(receiver) }
                }
            }
        } ?: WifiScanOutcome.ThrottledOrUnavailable
    }

    @SuppressLint("SwitchIntDef")
    private fun mapResult(result: ScanResult): WifiNetworkInfo? {
        val band = FrequencyUtils.bandForFrequency(result.frequency) ?: return null
        val channel = FrequencyUtils.channelForFrequency(result.frequency) ?: return null
        val widthMhz = when (result.channelWidth) {
            ScanResult.CHANNEL_WIDTH_40MHZ -> 40
            ScanResult.CHANNEL_WIDTH_80MHZ -> 80
            ScanResult.CHANNEL_WIDTH_160MHZ -> 160
            ScanResult.CHANNEL_WIDTH_80MHZ_PLUS_MHZ -> 160
            5 -> 320 // CHANNEL_WIDTH_320MHZ on newer Android versions.
            else -> 20
        }
        return WifiNetworkInfo(
            ssid = result.SSID.takeIf { it.isNotBlank() },
            bssid = result.BSSID,
            rssi = result.level,
            frequency = result.frequency,
            band = band,
            channel = channel,
            channelWidthMhz = widthMhz,
            centerFrequencyMhz = result.centerFreq0.takeIf { it > 0 } ?: result.frequency,
            timestampMicros = result.timestamp
        )
    }
}
