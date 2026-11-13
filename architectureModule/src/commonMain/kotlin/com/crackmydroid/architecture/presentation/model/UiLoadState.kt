package com.crackmydroid.architecture.presentation.model

/** Shared lifecycle for a user-visible asynchronous operation. */
sealed interface UiLoadState {
    data object Idle : UiLoadState
    data object Loading : UiLoadState
    data object Success : UiLoadState
    data class Error(val message: String) : UiLoadState
}

val UiLoadState.isLoading: Boolean
    get() = this is UiLoadState.Loading

/** User-visible failure text when this operation ended unsuccessfully. */
val UiLoadState.errorMessage: String?
    get() = (this as? UiLoadState.Error)?.message
