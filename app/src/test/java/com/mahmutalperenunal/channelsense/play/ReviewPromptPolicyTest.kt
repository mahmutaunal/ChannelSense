package com.mahmutalperenunal.channelsense.play

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ReviewPromptPolicyTest {
    private val day = 24L * 60L * 60L * 1_000L
    private val now = 200L * day

    private fun eligibleSnapshot() = ReviewPromptSnapshot(
        firstUseEpochMillis = now - 10 * day,
        successfulScanCount = 3,
        detailedScanCount = 1,
        guideOpenCount = 0,
        sessionCount = 2,
        promptAttemptCount = 0,
        lastPromptAttemptEpochMillis = null
    )

    @Test fun eligibleAfterMeaningfulUsage() {
        assertTrue(ReviewPromptPolicy.isEligible(eligibleSnapshot(), now))
    }

    @Test fun notEligibleWithoutEnoughSuccessfulScans() {
        assertFalse(ReviewPromptPolicy.isEligible(eligibleSnapshot().copy(successfulScanCount = 2), now))
    }

    @Test fun guideOpenCanReplaceDetailedScanSignal() {
        assertTrue(ReviewPromptPolicy.isEligible(eligibleSnapshot().copy(detailedScanCount = 0, guideOpenCount = 1), now))
    }

    @Test fun respectsCooldown() {
        assertFalse(ReviewPromptPolicy.isEligible(eligibleSnapshot().copy(lastPromptAttemptEpochMillis = now - 30 * day), now))
    }

    @Test fun stopsAfterMaximumAutomaticAttempts() {
        assertFalse(ReviewPromptPolicy.isEligible(eligibleSnapshot().copy(promptAttemptCount = ReviewPromptPolicy.MAX_AUTOMATIC_ATTEMPTS), now))
    }
}
