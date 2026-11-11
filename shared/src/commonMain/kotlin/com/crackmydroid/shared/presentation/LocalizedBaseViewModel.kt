package com.crackmydroid.shared.presentation

import com.crackmydroid.architecture.coroutines.AppDispatchers
import com.crackmydroid.architecture.presentation.BaseViewModel
import com.crackmydroid.architecture.presentation.model.UiEvent
import com.crackmydroid.shared.i18n.Strings
import com.crackmydroid.shared.i18n.stringsFor
import com.crackmydroid.shared.i18n.usecase.UseCasePresentationTexts
import com.crackmydroid.shared.i18n.usecase.UseCaseTexts

abstract class LocalizedBaseViewModel<E : UiEvent>(dispatchers: AppDispatchers) : BaseViewModel<E>(dispatchers) {
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
}
