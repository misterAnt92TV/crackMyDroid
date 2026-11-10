package com.crackmydroid.shared.presentation

import com.crackmydroid.shared.i18n.Strings
import com.crackmydroid.shared.i18n.UseCasePresentationTexts
import com.crackmydroid.shared.i18n.UseCaseTexts
import com.crackmydroid.shared.i18n.stringsFor
import com.crackmydroid.shared.util.AppDispatchers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch

open class BaseViewModel(
    protected val dispatchers: AppDispatchers,
) {
    private val job = SupervisorJob()
    internal val scope = CoroutineScope(job + dispatchers.main)
    private val latestJobs = mutableMapOf<String, Job>()
    private val latestVersions = mutableMapOf<String, Long>()
    private var activeLanguageCode = "en"

    /** Cataloghi localizzati da usare per ogni copy dinamico prodotto dal ViewModel. */
    protected var strings: Strings = stringsFor(activeLanguageCode)
        private set
    protected var useCaseTexts: UseCaseTexts = UseCaseTexts(activeLanguageCode)
        private set
    protected var presentationTexts: UseCasePresentationTexts = UseCasePresentationTexts(activeLanguageCode)
        private set

    open fun updateLanguage(languageCode: String) {
        val normalizedLanguage = UseCaseTexts(languageCode).languageCode
        if (activeLanguageCode == normalizedLanguage) return

        activeLanguageCode = normalizedLanguage
        strings = stringsFor(normalizedLanguage)
        useCaseTexts = UseCaseTexts(normalizedLanguage)
        presentationTexts = UseCasePresentationTexts(normalizedLanguage)
        onLanguageChanged()
    }

    /** Rimappa lo stato già caricato quando cambia la lingua, senza ricaricare dati esterni. */
    protected open fun onLanguageChanged() = Unit

    protected fun launchTask(block: suspend CoroutineScope.() -> Unit): Job = scope.launch(block = block)

    protected fun launchLatest(
        key: String,
        block: suspend LatestTask.() -> Unit,
    ): Job {
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
