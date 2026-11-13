package com.crackmydroid.architecture.presentation

import app.cash.turbine.test
import com.crackmydroid.architecture.presentation.model.UiEffect
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest

@OptIn(ExperimentalCoroutinesApi::class)
class UiEffectChannelTest {
    @Test
    fun effectIsRetainedUntilCollectorStarts() = runTest {
        val channel = UiEffectChannel<TestEffect>()

        channel.emit(TestEffect("copied"))

        channel.effects.test {
            assertTrue(awaitItem().value == "copied")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun closeCompletesTheEffectFlow() = runTest {
        val channel = UiEffectChannel<TestEffect>()

        channel.effects.test {
            channel.close()
            awaitComplete()
        }
    }

    private data class TestEffect(val value: String) : UiEffect
}
