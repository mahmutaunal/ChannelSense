package com.mahmutalperenunal.channelsense.play

import android.app.Activity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability

/** Coordinates Google Play flexible/immediate in-app updates without leaking Play Core into UI code. */
class PlayUpdateManager(
    activity: Activity,
    private val updateLauncher: ActivityResultLauncher<IntentSenderRequest>,
    private val listener: Listener
) {
    private val manager: AppUpdateManager = AppUpdateManagerFactory.create(activity)
    private var listenerRegistered = false

    private val installStateListener = InstallStateUpdatedListener { state ->
        when (state.installStatus()) {
            InstallStatus.DOWNLOADED -> listener.onUpdateDownloaded()
            InstallStatus.FAILED -> listener.onUpdateError()
        }
    }

    fun checkForUpdate(userInitiated: Boolean) {
        manager.appUpdateInfo
            .addOnSuccessListener { info -> handleUpdateInfo(info, userInitiated) }
            .addOnFailureListener { listener.onUpdateCheckFailed(userInitiated) }
    }

    fun resumeInterruptedUpdate() {
        manager.appUpdateInfo.addOnSuccessListener { info ->
            when {
                info.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS -> {
                    launch(info, AppUpdateType.IMMEDIATE)
                }
                info.installStatus() == InstallStatus.DOWNLOADED -> listener.onUpdateDownloaded()
                info.installStatus() in FLEXIBLE_UPDATE_ACTIVE_STATUSES -> registerInstallListener()
            }
        }
    }

    fun completeUpdate() {
        manager.completeUpdate()
    }

    fun close() {
        if (listenerRegistered) {
            manager.unregisterListener(installStateListener)
            listenerRegistered = false
        }
    }

    private fun handleUpdateInfo(info: AppUpdateInfo, userInitiated: Boolean) {
        if (info.updateAvailability() != UpdateAvailability.UPDATE_AVAILABLE) {
            if (userInitiated) listener.onNoUpdateAvailable()
            return
        }

        val type = UpdateFlowPolicy.selectType(
            priority = info.updatePriority(),
            stalenessDays = info.clientVersionStalenessDays(),
            flexibleAllowed = info.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE),
            immediateAllowed = info.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)
        )

        if (type == null) {
            listener.onUpdateNotAllowed()
            return
        }

        if (type == AppUpdateType.FLEXIBLE) registerInstallListener()
        launch(info, type)
    }

    private fun launch(info: AppUpdateInfo, updateType: Int) {
        runCatching {
            val started = manager.startUpdateFlowForResult(
                info,
                updateLauncher,
                AppUpdateOptions.newBuilder(updateType).build()
            )
            if (!started) listener.onUpdateError()
        }.onFailure { listener.onUpdateError() }
    }

    private fun registerInstallListener() {
        if (!listenerRegistered) {
            manager.registerListener(installStateListener)
            listenerRegistered = true
        }
    }

    interface Listener {
        fun onNoUpdateAvailable()
        fun onUpdateDownloaded()
        fun onUpdateCheckFailed(userInitiated: Boolean)
        fun onUpdateNotAllowed()
        fun onUpdateError()
    }

    private companion object {
        val FLEXIBLE_UPDATE_ACTIVE_STATUSES = setOf(
            InstallStatus.PENDING,
            InstallStatus.DOWNLOADING,
            InstallStatus.INSTALLING
        )
    }
}
