package com.crackmydroid.architecture.presentation.model

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class UiLoadStateTest {
    @Test
    fun loadingIsTheOnlyActiveLoadingState() {
        assertTrue(UiLoadState.Loading.isLoading)
        assertFalse(UiLoadState.Idle.isLoading)
        assertFalse(UiLoadState.Success.isLoading)
        assertFalse(UiLoadState.Error("failed").isLoading)
    }
}
