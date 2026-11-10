package com.crackmydroid.architecture.presentation

import com.crackmydroid.architecture.coroutines.AppDispatchers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch

open class BaseViewModel(protected val dispatchers: AppDispatchers) {
    private val job = SupervisorJob()
    protected val scope = CoroutineScope(job + dispatchers.main)
    private val latestJobs = mutableMapOf<String, Job>()
    private val latestVersions = mutableMapOf<String, Long>()

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

    open fun onCleared() {
        latestJobs.values.toList().forEach(Job::cancel)
        latestJobs.clear()
        scope.cancel()
    }
}
