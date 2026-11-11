package com.crackmydroid.android

import java.io.IOException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SnapshotWorkTest {
    @Test
    fun constraints_requireChargingAndBatteryNotLow() {
        val constraints = SnapshotWorkScheduler.constraints()

        assertTrue(constraints.requiresCharging())
        assertTrue(constraints.requiresBatteryNotLow())
    }

    @Test
    fun executeSnapshot_completedOrSkipped_returnsSuccess() = runTest {
        assertEquals(SnapshotWorkResult.Success, executeSnapshot { true })
        assertEquals(SnapshotWorkResult.Success, executeSnapshot { false })
    }

    @Test
    fun executeSnapshot_ioFailure_requestsRetry() = runTest {
        assertEquals(
            SnapshotWorkResult.Retry,
            executeSnapshot { throw IOException("storage temporarily unavailable") },
        )
    }

    @Test
    fun executeSnapshot_nonRecoverableFailure_returnsFailure() = runTest {
        assertEquals(
            SnapshotWorkResult.Failure,
            executeSnapshot { error("invalid snapshot state") },
        )
    }
}
