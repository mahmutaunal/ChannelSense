package com.mahmutalperenunal.channelsense.play

import com.google.android.play.core.install.model.AppUpdateType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class UpdateFlowPolicyTest {
    @Test
    fun `uses flexible flow for a recent regular update`() {
        val type = UpdateFlowPolicy.selectType(0, 1, flexibleAllowed = true, immediateAllowed = true)

        assertEquals(AppUpdateType.FLEXIBLE, type)
    }

    @Test
    fun `uses immediate flow for a high priority update`() {
        val type = UpdateFlowPolicy.selectType(4, 0, flexibleAllowed = true, immediateAllowed = true)

        assertEquals(AppUpdateType.IMMEDIATE, type)
    }

    @Test
    fun `uses immediate flow when an update has been stale for seven days`() {
        val type = UpdateFlowPolicy.selectType(0, 7, flexibleAllowed = true, immediateAllowed = true)

        assertEquals(AppUpdateType.IMMEDIATE, type)
    }

    @Test
    fun `falls back to an allowed flow`() {
        val type = UpdateFlowPolicy.selectType(4, 7, flexibleAllowed = true, immediateAllowed = false)

        assertEquals(AppUpdateType.FLEXIBLE, type)
    }

    @Test
    fun `returns null when Play allows no update flow`() {
        val type = UpdateFlowPolicy.selectType(0, null, flexibleAllowed = false, immediateAllowed = false)

        assertNull(type)
    }
}
