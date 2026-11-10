package com.crackmydroid.architecture.presentation.model

import kotlin.uuid.Uuid
import kotlin.uuid.ExperimentalUuidApi

/** Base input for every screen interaction. Each dispatchable instance has a stable identity. */
@OptIn(ExperimentalUuidApi::class)
abstract class UiEvent(
    val id: Uuid = Uuid.random(),
)
