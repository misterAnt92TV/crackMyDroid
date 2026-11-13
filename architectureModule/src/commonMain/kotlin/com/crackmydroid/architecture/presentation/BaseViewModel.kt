package com.crackmydroid.architecture.presentation

import com.crackmydroid.architecture.coroutines.AppDispatchers
import com.crackmydroid.architecture.presentation.model.UiEvent
import com.crackmydroid.architecture.presentation.model.UiEventDispatcher
import com.crackmydroid.architecture.presentation.model.UiEventHandler
import com.crackmydroid.architecture.presentation.model.UiEffect
import com.crackmydroid.architecture.presentation.model.UiState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

abstract class BaseViewModel<E : UiEvent, S : UiState>(
    initialState: S,
    protected val dispatchers: AppDispatchers,
) : UiEventDispatcher<E>, UiEventHandler<E> {
    private val job = SupervisorJob()
    protected val scope = CoroutineScope(job + dispatchers.main)
    private val latestJobs = mutableMapOf<String, Job>()
    private val latestVersions = mutableMapOf<String, Long>()
    private val effectChannels = mutableListOf<UiEffectChannel<*>>()
    private val mutableState = MutableStateFlow(initialState)

    /** Immutable state observed by the screen. */
    val state: StateFlow<S> = mutableState.asStateFlow()

    /** Current state for deriving the next immutable state. */
    protected val currentState: S
        get() = mutableState.value

    /** Atomically derives and publishes the next rendering state. */
    protected fun updateState(transform: (S) -> S) {
        mutableState.update(transform)
    }

    /** Replaces the complete rendering state. */
    protected fun emitState(state: S) {
        mutableState.value = state
    }

    /** Semantic entry point used by screens, coordinators and tests. */
    final override fun emitEvent(event: E) {
        handleEvent(event)
    }

    /**
     * Synchronous dispatch boundary retained for the architecture contract.
     * Screens should call [emitEvent] so the public UDF entry point is consistent.
     */
    final override fun handleEvent(event: E) {
        onEvent(event)
    }

    protected abstract fun onEvent(event: E)

    /** Creates an effect stream owned by this ViewModel without making effects rendering state. */
    protected fun <F : UiEffect> effectChannel(): UiEffectChannel<F> = UiEffectChannel<F>().also(effectChannels::add)

    protected fun launchTask(block: suspend CoroutineScope.() -> Unit): Job = scope.launch(block = block)

    protected fun launchLatest(key: String, block: suspend LatestTask.() -> Unit): Job {
        val version = (latestVersions[key] ?: 0L) + 1L
        latestVersions[key] = version
        latestJobs.remove(key)?.cancel()
        lateinit var task: LatestTask
        val launched = scope.launch(start = CoroutineStart.LAZY) { task.block() }
        task = LatestTask(key, version) {
            latestVersions[key] == version && latestJobs[key] === launched
        }
        latestJobs[key] = launched
        launched.invokeOnCompletion {
            if (latestJobs[key] === launched) latestJobs.remove(key)
        }
        launched.start()
        return launched
    }

    protected fun cancelLatest(key: String) {
        latestVersions[key] = (latestVersions[key] ?: 0L) + 1L
        latestJobs.remove(key)?.cancel()
    }

    protected class LatestTask internal constructor(
        private val key: String,
        private val version: Long,
        private val currentCheck: () -> Boolean,
    ) {
        fun isCurrent(): Boolean = currentCheck()

        suspend fun ensureCurrent() {
            currentCoroutineContext().ensureActive()
            if (!currentCheck()) throw kotlinx.coroutines.CancellationException("Superseded work: $key#$version")
        }
    }

    /**
     * Final lifecycle boundary: feature cleanup cannot bypass cancellation of jobs, effects and
     * the ViewModel scope.
     */
    final fun onCleared() {
        onClearing()
        latestJobs.values.toList().forEach(Job::cancel)
        latestJobs.clear()
        effectChannels.forEach(UiEffectChannel<*>::close)
        effectChannels.clear()
        scope.cancel()
    }

    /** Optional feature cleanup invoked before the ViewModel scope is cancelled. */
    protected open fun onClearing() = Unit
}
