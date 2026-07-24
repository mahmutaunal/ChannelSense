package com.mahmutalperenunal.channelsense.play

/**
 * Pure decision policy for the automatic in-app review prompt.
 *
 * The prompt is intentionally conservative: it is only eligible after the user has received
 * repeated value from the app, and attempts are separated by a long cooldown. Google Play still
 * has the final say on whether the review card is displayed.
 */
data class ReviewPromptSnapshot(
    val firstUseEpochMillis: Long,
    val successfulScanCount: Int,
    val detailedScanCount: Int,
    val guideOpenCount: Int,
    val sessionCount: Int,
    val promptAttemptCount: Int,
    val lastPromptAttemptEpochMillis: Long?
)

object ReviewPromptPolicy {
    const val MIN_SUCCESSFUL_SCANS = 3
    const val MIN_SESSIONS = 2
    const val MIN_DAYS_SINCE_FIRST_USE = 2
    const val MAX_AUTOMATIC_ATTEMPTS = 2
    const val COOLDOWN_DAYS = 120

    private const val DAY_MILLIS = 24L * 60L * 60L * 1_000L

    fun isEligible(snapshot: ReviewPromptSnapshot, nowEpochMillis: Long): Boolean {
        if (snapshot.promptAttemptCount >= MAX_AUTOMATIC_ATTEMPTS) return false
        if (snapshot.successfulScanCount < MIN_SUCCESSFUL_SCANS) return false
        if (snapshot.sessionCount < MIN_SESSIONS) return false

        val hasExploredValue = snapshot.detailedScanCount >= 1 || snapshot.guideOpenCount >= 1
        if (!hasExploredValue) return false

        val accountAge = nowEpochMillis - snapshot.firstUseEpochMillis
        if (accountAge < MIN_DAYS_SINCE_FIRST_USE * DAY_MILLIS) return false

        val lastAttempt = snapshot.lastPromptAttemptEpochMillis ?: return true
        return nowEpochMillis - lastAttempt >= COOLDOWN_DAYS * DAY_MILLIS
    }
}
