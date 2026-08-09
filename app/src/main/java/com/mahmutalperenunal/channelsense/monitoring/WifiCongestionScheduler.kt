package com.mahmutalperenunal.channelsense.monitoring

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.mahmutalperenunal.channelsense.feature.settings.model.AppSettings
import java.util.concurrent.TimeUnit

object WifiCongestionScheduler {
    private const val UNIQUE_WORK_NAME = "wifi_congestion_monitor"

    fun sync(context: Context, settings: AppSettings) {
        val workManager = WorkManager.getInstance(context.applicationContext)
        if (!settings.congestionAlertsEnabled) {
            workManager.cancelUniqueWork(UNIQUE_WORK_NAME)
            return
        }

        val request = PeriodicWorkRequestBuilder<WifiCongestionWorker>(
            settings.congestionAlertInterval.minutes,
            TimeUnit.MINUTES
        ).setConstraints(
            Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresBatteryNotLow(true)
                .build()
        ).build()

        workManager.enqueueUniquePeriodicWork(
            UNIQUE_WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }
}
