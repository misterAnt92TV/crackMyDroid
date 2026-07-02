package com.crackmydroid.shared.presentation.components

import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SearchHistoryStoreTest {
    @BeforeTest
    fun setUp() {
        SearchHistoryStore.setEnabled(true)
        SearchHistoryStore.clearAll()
    }

    @AfterTest
    fun tearDown() {
        SearchHistoryStore.clearAll()
        SearchHistoryStore.setEnabled(true)
    }

    @Test
    fun addNormalizesAndDeduplicatesCaseInsensitive() {
        SearchHistoryStore.add("apps", "  Demo   Query  ")
        SearchHistoryStore.add("apps", "demo query")
        SearchHistoryStore.add("apps", "Second Query")

        assertEquals(
            listOf("Second Query", "demo query"),
            SearchHistoryStore.suggestions("apps")
        )
    }

    @Test
    fun suggestionsPrioritizePrefixAndHideExactMatch() {
        val key = "logs"
        SearchHistoryStore.add(key, "error report")
        SearchHistoryStore.add(key, "warning detail")
        SearchHistoryStore.add(key, "logcat crash")
        SearchHistoryStore.add(key, "logs by tag")
        SearchHistoryStore.add(key, "adb log")
        SearchHistoryStore.add(key, "verbose log")
        SearchHistoryStore.add(key, "system log")
        SearchHistoryStore.add(key, "log")

        val suggestions = SearchHistoryStore.suggestions(key, "log")

        assertTrue(suggestions.size <= 6)
        assertTrue(suggestions.none { it.equals("log", ignoreCase = true) })
        assertTrue(suggestions.first().startsWith("log", ignoreCase = true))
    }

    @Test
    fun disabledSuggestionsDoNotStoreOrShowHistory() {
        SearchHistoryStore.setEnabled(false)
        SearchHistoryStore.add("permissions", "camera")

        assertTrue(SearchHistoryStore.suggestions("permissions").isEmpty())
    }
}
