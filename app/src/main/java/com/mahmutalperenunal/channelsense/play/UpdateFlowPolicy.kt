package com.mahmutalperenunal.channelsense.play

import com.google.android.play.core.install.model.AppUpdateType

/** Keeps update urgency decisions deterministic and independent from the Play Core lifecycle. */
internal object UpdateFlowPolicy {
    private const val IMMEDIATE_UPDATE_PRIORITY = 4
    private const val IMMEDIATE_UPDATE_STALENESS_DAYS = 7

    fun selectType(
        priority: Int,
        stalenessDays: Int?,
        flexibleAllowed: Boolean,
        immediateAllowed: Boolean
    ): Int? {
        val immediateRecommended =
            priority >= IMMEDIATE_UPDATE_PRIORITY ||
                (stalenessDays ?: 0) >= IMMEDIATE_UPDATE_STALENESS_DAYS

        return when {
            immediateRecommended && immediateAllowed -> AppUpdateType.IMMEDIATE
            flexibleAllowed -> AppUpdateType.FLEXIBLE
            immediateAllowed -> AppUpdateType.IMMEDIATE
            else -> null
        }
    }
}
