package com.crackmydroid.shared.i18n

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class FormatTest {
    @Test
    fun formatsStringAndIntegerPlaceholders() {
        assertEquals("Results: 2 / 5 activities", "Results: %d / %d %s".format(2, 5, "activities"))
    }

    @Test
    fun formatsNullAndEscapedPercent() {
        assertEquals("null: 100%", "%s: 100%%".format(null))
    }

    @Test
    fun rejectsMissingAndMismatchedArguments() {
        assertFailsWith<IllegalArgumentException> { "%d".format() }
        assertFailsWith<IllegalArgumentException> { "%d".format("two") }
    }
}
