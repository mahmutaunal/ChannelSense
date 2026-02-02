package com.mahmutalperenunal.channelsense.util

import android.content.Context
import android.content.Intent
import android.net.wifi.WifiManager
import android.widget.Toast
import androidx.core.net.toUri
import com.mahmutalperenunal.channelsense.R

object NetworkUtils {

    fun getGatewayIp(context: Context): String? {
        val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
            ?: return null

        val dhcpInfo = wifiManager.dhcpInfo ?: return null

        val ip = dhcpInfo.gateway
        if (ip == 0) return null

        return intToIp(ip)
    }

    private fun intToIp(ip: Int): String {
        return "${ip and 0xFF}." +
                "${ip shr 8 and 0xFF}." +
                "${ip shr 16 and 0xFF}." +
                "${ip shr 24 and 0xFF}"
    }

    fun openRouterPage(context: Context) {
        val gateway = getGatewayIp(context)

        if (gateway.isNullOrBlank()) {
            Toast.makeText(
                context,
                context.getString(R.string.error_router_gateway_not_found),
                Toast.LENGTH_LONG
            ).show()
            return
        }

        try {
            val intent = Intent(Intent.ACTION_VIEW, "http://$gateway".toUri())
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (_: Exception) {
            Toast.makeText(
                context,
                context.getString(R.string.error_router_page_open_failed, gateway),
                Toast.LENGTH_LONG
            ).show()
        }
    }
}