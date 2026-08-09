package com.mahmutalperenunal.channelsense.monitoring

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.mahmutalperenunal.channelsense.feature.analyzer.logic.ChannelAnalyzer
import com.mahmutalperenunal.channelsense.wifi.connection.ConnectedWifiProvider
import com.mahmutalperenunal.channelsense.wifi.scanner.WifiScanOutcome
import com.mahmutalperenunal.channelsense.wifi.scanner.WifiScanner

class WifiCongestionWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val connectedWifiProvider = ConnectedWifiProvider(applicationContext)
        return try {
            val connected = connectedWifiProvider.current() ?: return Result.success()
            val band = connected.band ?: return Result.success()
            val currentChannel = connected.channel ?: return Result.success()
            val outcome = WifiScanner(applicationContext).scan()
            if (outcome !is WifiScanOutcome.Success) return Result.success()

            val analysis = ChannelAnalyzer.analyze(
                networks = outcome.networks,
                band = band,
                sampleCount = 1,
                currentChannel = currentChannel
            )
            CongestionAlertPolicy.evaluate(connected, analysis)?.let {
                WifiCongestionNotifier.notify(applicationContext, it)
            }
            Result.success()
        } finally {
            connectedWifiProvider.close()
        }
    }
}
