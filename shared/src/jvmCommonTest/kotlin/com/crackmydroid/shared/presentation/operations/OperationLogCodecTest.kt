package com.crackmydroid.shared.presentation.operations

import com.crackmydroid.shared.domain.model.operation.OperationEntry
import com.crackmydroid.shared.domain.model.operation.OperationEvent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class OperationLogCodecTest {
    @Test
    fun decodesLegacyEntriesWithoutChangingTheirCopy() {
        val entries = OperationLogCodec.decode("Old description\u001f1\u001fdetails\u001f42") { 99L }

        assertEquals("Old description", entries.single().description)
        assertNull(entries.single().event)
        assertEquals("Old description", entries.single().legacyDescription)
        assertTrue(entries.single().operationId.isNotBlank())
        assertEquals(false, entries.single().hiddenFromRecent)
    }

    @Test
    fun roundTripsSemanticEventsAndArguments() {
        val original = OperationEntry(
            description = "activity.export",
            success = true,
            details = "technical/path",
            timestamp = 42L,
            event = OperationEvent("activity.export", listOf("com.example.app")),
            legacyDescription = null,
            operationId = "operation-1",
            hiddenFromRecent = true,
        )

        val decoded = OperationLogCodec.decode(OperationLogCodec.encode(listOf(original))) { 99L }.single()

        assertEquals(original.event, decoded.event)
        assertEquals(original.timestamp, decoded.timestamp)
        assertNull(decoded.legacyDescription)
        assertEquals(original.operationId, decoded.operationId)
        assertEquals(original.hiddenFromRecent, decoded.hiddenFromRecent)
    }
}
