package com.crackmydroid.shared.presentation

import com.crackmydroid.shared.domain.model.operation.OperationEntry
import com.crackmydroid.shared.i18n.stringsFor
import com.crackmydroid.shared.ui.home.model.OperationOutcome
import com.crackmydroid.shared.ui.home.model.HomeState
import com.crackmydroid.shared.ui.home.model.HomeUiStateMapper
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class HomeUiStateMapperTest {
    private val mapper = HomeUiStateMapper()

    @Test
    fun map_createsLocalizedLastOperationItemsWithStableIdentifiers() {
        val entry = OperationEntry(
            description = "legacy description",
            success = true,
            timestamp = 0L,
            operationId = "operation-1",
        )

        val item = mapper.map(HomeState(recentOperations = listOf(entry)), stringsFor("en"), "en")
            .latestOperations.single()

        assertEquals("operation-1", item.operationId)
        assertEquals("legacy description", item.description)
        assertEquals("Success", item.result)
        assertEquals(OperationOutcome.Success, item.outcome)
    }

    @Test
    fun map_keepsMissingDetailsHiddenAndBuildsLegacyFallbackIdentifier() {
        val entry = OperationEntry(
            description = "scan",
            success = false,
            details = "",
            timestamp = 42L,
        )

        val item = mapper.map(HomeState(recentOperations = listOf(entry)), stringsFor("it"), "it")
            .operationHistory.single()

        assertEquals("42:scan:false", item.operationId)
        assertEquals("", item.result)
        assertEquals(OperationOutcome.Failure, item.outcome)
        assertNull(item.result?.takeIf(String::isNotEmpty))
    }
}
