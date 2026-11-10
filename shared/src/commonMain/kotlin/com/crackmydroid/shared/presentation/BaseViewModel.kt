package com.crackmydroid.shared.presentation

import com.crackmydroid.shared.i18n.Strings
import com.crackmydroid.shared.i18n.UseCasePresentationTexts
import com.crackmydroid.shared.i18n.UseCaseTexts
import com.crackmydroid.shared.i18n.stringsFor
import com.crackmydroid.shared.util.AppDispatchers
import com.crackmydroid.shared.util.DefaultAppDispatchers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

open class BaseViewModel(
    protected val dispatchers: AppDispatchers = DefaultAppDispatchers,
) {
    private val job = SupervisorJob()
    protected val scope = CoroutineScope(job + dispatchers.main)
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

    open fun onCleared() {
        scope.cancel()
    }
}
