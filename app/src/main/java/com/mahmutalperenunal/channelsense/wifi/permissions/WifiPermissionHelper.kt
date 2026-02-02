package com.mahmutalperenunal.channelsense.wifi.permissions

import android.Manifest
import android.content.Context
import android.os.Build
import androidx.core.content.ContextCompat

object WifiPermissionHelper {

    fun requiredPermissions(): Array<String> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(Manifest.permission.NEARBY_WIFI_DEVICES, Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    fun hasRequiredPermissions(context: Context): Boolean {
        val permissions = requiredPermissions()
        return permissions.all { permission ->
            ContextCompat.checkSelfPermission(
                context,
                permission
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        }
    }
}