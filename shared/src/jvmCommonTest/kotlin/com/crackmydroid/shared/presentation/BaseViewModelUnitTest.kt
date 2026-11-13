package com.crackmydroid.shared.presentation

import com.crackmydroid.architecture.testing.*

import kotlin.test.AfterTest
import kotlin.test.BeforeTest

/** Binds the reusable event-test fixture to this source set's test lifecycle. */
abstract class BaseViewModelUnitTest : com.crackmydroid.architecture.testing.BaseViewModelUnitTest() {
    @BeforeTest
    override fun setUp() {
        super.setUp()
    }

    @AfterTest
    override fun tearDown() {
        super.tearDown()
    }
}
