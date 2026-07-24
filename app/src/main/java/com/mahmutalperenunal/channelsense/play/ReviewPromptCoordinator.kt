package com.mahmutalperenunal.channelsense.play

import android.content.Context
import androidx.core.content.edit

/**
 * Persists lightweight, device-local engagement signals used by [ReviewPromptPolicy].
 *
 * No personal data, Wi-Fi details, analytics SDK, or network communication is involved.
 */
class ReviewPromptCoordinator(context: Context) {
    private val preferences = context.applicationContext.getSharedPreferences(
        PREFERENCES_NAME,
        Context.MODE_PRIVATE
    )

    init {
        if (!preferences.contains(KEY_FIRST_USE)) {
            preferences.edit { putLong(KEY_FIRST_USE, System.currentTimeMillis()) }
        }
    }

    fun recordSession(nowEpochMillis: Long = System.currentTimeMillis()) {
        val lastSession = preferences.getLong(KEY_LAST_SESSION, 0L)
        if (lastSession == 0L || nowEpochMillis - lastSession >= SESSION_GAP_MILLIS) {
            preferences.edit {
                putInt(KEY_SESSION_COUNT, preferences.getInt(KEY_SESSION_COUNT, 0) + 1)
                    .putLong(KEY_LAST_SESSION, nowEpochMillis)
            }
        }
    }

    fun recordSuccessfulScan(isDetailed: Boolean) {
        increment(KEY_SUCCESSFUL_SCAN_COUNT)
        if (isDetailed) increment(KEY_DETAILED_SCAN_COUNT)
    }

    fun recordGuideOpened() {
        increment(KEY_GUIDE_OPEN_COUNT)
    }

    fun shouldRequestAutomaticReview(nowEpochMillis: Long = System.currentTimeMillis()): Boolean {
        return ReviewPromptPolicy.isEligible(snapshot(), nowEpochMillis)
    }

    /** Record before asking Play Core, so process death cannot cause an immediate repeat. */
    fun recordAutomaticPromptAttempt(nowEpochMillis: Long = System.currentTimeMillis()) {
        preferences.edit {
            putInt(KEY_PROMPT_ATTEMPT_COUNT, preferences.getInt(KEY_PROMPT_ATTEMPT_COUNT, 0) + 1)
                .putLong(KEY_LAST_PROMPT_ATTEMPT, nowEpochMillis)
        }
    }

    private fun snapshot(): ReviewPromptSnapshot = ReviewPromptSnapshot(
        firstUseEpochMillis = preferences.getLong(KEY_FIRST_USE, System.currentTimeMillis()),
        successfulScanCount = preferences.getInt(KEY_SUCCESSFUL_SCAN_COUNT, 0),
        detailedScanCount = preferences.getInt(KEY_DETAILED_SCAN_COUNT, 0),
        guideOpenCount = preferences.getInt(KEY_GUIDE_OPEN_COUNT, 0),
        sessionCount = preferences.getInt(KEY_SESSION_COUNT, 0),
        promptAttemptCount = preferences.getInt(KEY_PROMPT_ATTEMPT_COUNT, 0),
        lastPromptAttemptEpochMillis = preferences.getLong(KEY_LAST_PROMPT_ATTEMPT, 0L)
            .takeIf { it > 0L }
    )

    private fun increment(key: String) {
        preferences.edit { putInt(key, preferences.getInt(key, 0) + 1) }
    }

    private companion object {
        const val PREFERENCES_NAME = "review_prompt_state"
        const val KEY_FIRST_USE = "first_use_epoch_millis"
        const val KEY_SUCCESSFUL_SCAN_COUNT = "successful_scan_count"
        const val KEY_DETAILED_SCAN_COUNT = "detailed_scan_count"
        const val KEY_GUIDE_OPEN_COUNT = "guide_open_count"
        const val KEY_SESSION_COUNT = "session_count"
        const val KEY_LAST_SESSION = "last_session_epoch_millis"
        const val KEY_PROMPT_ATTEMPT_COUNT = "prompt_attempt_count"
        const val KEY_LAST_PROMPT_ATTEMPT = "last_prompt_attempt_epoch_millis"
        const val SESSION_GAP_MILLIS = 30L * 60L * 1_000L
    }
}
