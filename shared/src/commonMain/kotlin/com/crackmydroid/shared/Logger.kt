package com.crackmydroid.shared

import co.touchlab.kermit.Logger
import kotlinx.coroutines.flow.MutableStateFlow

object AppLogger {
    val logger: Logger = Logger.withTag("CrackMyDroid")
    private val verbose = MutableStateFlow(false)

    fun setVerbose(enabled: Boolean) {
        verbose.value = enabled
    }

    fun isVerbose(): Boolean = verbose.value
}
