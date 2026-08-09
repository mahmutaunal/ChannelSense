package com.mahmutalperenunal.channelsense

import android.app.AlertDialog
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.rememberNavController
import com.mahmutalperenunal.channelsense.play.PlayReviewManager
import com.mahmutalperenunal.channelsense.play.ReviewPromptCoordinator
import com.mahmutalperenunal.channelsense.play.PlayUpdateManager
import com.mahmutalperenunal.channelsense.play.ReviewResult
import com.mahmutalperenunal.channelsense.ui.navigation.ChannelSenseNavGraph
import com.mahmutalperenunal.channelsense.ui.theme.ChannelSenseTheme
import com.mahmutalperenunal.channelsense.feature.settings.data.SettingsRepository
import com.mahmutalperenunal.channelsense.feature.settings.model.AppSettings
import com.mahmutalperenunal.channelsense.feature.settings.model.AppThemeMode

class MainActivity : AppCompatActivity(), PlayUpdateManager.Listener {

    private val updateLauncher = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode != RESULT_OK) {
            Toast.makeText(this, R.string.update_cancelled, Toast.LENGTH_SHORT).show()
        }
    }

    private lateinit var playUpdateManager: PlayUpdateManager
    private lateinit var playReviewManager: PlayReviewManager
    private lateinit var reviewPromptCoordinator: ReviewPromptCoordinator
    private var updateDownloadedDialog: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        enableEdgeToEdge()

        playUpdateManager = PlayUpdateManager(
            activity = this,
            updateLauncher = updateLauncher,
            listener = this
        )
        playReviewManager = PlayReviewManager(this)
        reviewPromptCoordinator = ReviewPromptCoordinator(this).also { it.recordSession() }
        SettingsRepository.ensureInitialized(this)

        setContent {
            val appSettings by SettingsRepository.settingsFlow.collectAsState(
                initial = AppSettings()
            )
            val useDarkTheme = when (appSettings.themeMode) {
                AppThemeMode.SYSTEM -> isSystemInDarkTheme()
                AppThemeMode.LIGHT -> false
                AppThemeMode.DARK -> true
            }
            ChannelSenseTheme(
                darkTheme = useDarkTheme,
                dynamicColor = appSettings.materialYouEnabled
            ) {
                ChannelSenseApp(
                    onCheckForUpdate = { playUpdateManager.checkForUpdate(userInitiated = true) },
                    onRequestReview = ::requestInAppReview,
                    onSuccessfulScan = ::onSuccessfulScan,
                    onGuideOpened = ::onGuideOpened
                )
            }
        }

        if (savedInstanceState == null) {
            playUpdateManager.checkForUpdate(userInitiated = false)
        }
    }

    override fun onResume() {
        super.onResume()
        if (::playUpdateManager.isInitialized) {
            playUpdateManager.resumeInterruptedUpdate()
        }
    }

    override fun onDestroy() {
        updateDownloadedDialog?.dismiss()
        if (::playUpdateManager.isInitialized) playUpdateManager.close()
        super.onDestroy()
    }


    private fun onSuccessfulScan(isDetailed: Boolean) {
        reviewPromptCoordinator.recordSuccessfulScan(isDetailed)
        requestAutomaticReviewIfEligible()
    }

    private fun onGuideOpened() {
        reviewPromptCoordinator.recordGuideOpened()
        requestAutomaticReviewIfEligible()
    }

    private fun requestAutomaticReviewIfEligible() {
        if (!reviewPromptCoordinator.shouldRequestAutomaticReview()) return
        reviewPromptCoordinator.recordAutomaticPromptAttempt()
        // Automatic requests stay silent: Play may legally suppress the card due to quota.
        playReviewManager.requestReview { }
    }

    private fun requestInAppReview() {
        playReviewManager.requestReview { result ->
            val message = when (result) {
                ReviewResult.Completed -> R.string.review_flow_completed
                is ReviewResult.Failed -> R.string.review_flow_unavailable
            }
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onNoUpdateAvailable() {
        Toast.makeText(this, R.string.update_not_available, Toast.LENGTH_SHORT).show()
    }

    override fun onUpdateDownloaded() {
        if (isFinishing || isDestroyed || updateDownloadedDialog?.isShowing == true) return
        updateDownloadedDialog = AlertDialog.Builder(this)
            .setTitle(R.string.update_ready_title)
            .setMessage(R.string.update_ready_message)
            .setCancelable(false)
            .setPositiveButton(R.string.update_restart_now) { _, _ ->
                playUpdateManager.completeUpdate()
            }
            .setNegativeButton(R.string.update_restart_later, null)
            .show()
    }

    override fun onUpdateCheckFailed(userInitiated: Boolean) {
        if (userInitiated) {
            Toast.makeText(this, R.string.update_check_failed, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onUpdateNotAllowed() {
        Toast.makeText(this, R.string.update_not_supported, Toast.LENGTH_SHORT).show()
    }

    override fun onUpdateError() {
        Toast.makeText(this, R.string.update_error, Toast.LENGTH_SHORT).show()
    }
}

@Composable
fun ChannelSenseApp(
    onCheckForUpdate: () -> Unit,
    onRequestReview: () -> Unit,
    onSuccessfulScan: (Boolean) -> Unit,
    onGuideOpened: () -> Unit
) {
    Surface(color = MaterialTheme.colorScheme.background) {
        val navController = rememberNavController()
        ChannelSenseNavGraph(
            navController = navController,
            onCheckForUpdate = onCheckForUpdate,
            onRequestReview = onRequestReview,
            onSuccessfulScan = onSuccessfulScan,
            onGuideOpened = onGuideOpened
        )
    }
}
