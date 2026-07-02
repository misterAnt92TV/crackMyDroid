# CrackMyDroid

Kotlin Multiplatform (Android/iOS) app built with Jetpack Compose Multiplatform that concentrates common mobile security, reverse-engineering and diagnostics tools in a single UI.

## Funzionalità principali
- **Home** – panoramica rapida con scopi dell’app e FAQ espandibili.
- **Info dispositivo** – hardware/software, patch di sicurezza, fingerprint, radio, kernel, bootloader, stato batteria/rete, flag di debug (ADB, developer options, app debuggable), stato root (RootBeer) e dettaglio; ogni riga è copiabile e l’intero report è esportabile in TXT.
- **Root / Play Integrity** – verifica root con RootBeer e controlli Play Integrity con nonce configurabile; mostra dettagli dei test.
- **PenTesting** – elenco ultimo esito dei check di sicurezza con stato (queued/ok/error), contatori, dettaglio errori e pulsanti di export/share del report.
- **Attività (Activities)** – elenco attività per app con ricerca/storia, preferiti, espansione per pacchetto, lancio diretto e export dell’elenco.
- **App installate (APK)** – ricerca/filtri, preferiti, icone app, export APK su storage e condivisione diretta.
- **Permessi** – panoramica dei permessi app installate con ricerca e dettagli (read‑only).
- **Shell/Trick** – raccolta di comandi ADB/shell preconfigurati (segnalati quando richiedono root) con conferma opzionale e visualizzazione output/exit code.
- **Log** – visualizza log applicativi e consente export.
- **Impostazioni** – tema (chiaro/scuro/sistema), verbosità log, preferenze di accessibilità (alto contrasto, riduci animazioni, testo grande), link librerie terze, versione app.

## Struttura di navigazione
- Tab in basso: Home, Info, Impostazioni.
- Drawer laterale: App installate, Pentest, Permessi, Attività, Root, Trick, Log.

## Build e installazione (Android)
1) Assicurati di avere JDK 17 e Android SDK (platform-tools/adb nel PATH).  
2) Compila: `./gradlew :androidApp:assembleDebug`  
3) Installa su device collegato: `adb install -r androidApp/build/outputs/apk/debug/androidApp-debug.apk`

## Note iOS
Il modulo iOS è incluso per UI/logic condivisa; feature dipendenti da Root/ADB/Play Integrity non sono disponibili su iOS (stub).

## Stack tecnico
- Compose Multiplatform, Kotlin Coroutines, Koin DI, SQLDelight cache, RootBeer, Play Integrity API, DataStore, MockK (test).
