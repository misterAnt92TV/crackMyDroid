# CrackMyDroid

🌐 **Lingue:** [English](README.md) · [Deutsch](README.de.md) · [Français](README.fr.md) · [Español](README.es.md)

> **Kotlin Multiplatform** (Android · Desktop · iOS) — sicurezza mobile, reverse engineering e
> diagnostica del dispositivo, tutto in un'unica UI condivisa costruita con **Compose Multiplatform**.

[![Kotlin](https://img.shields.io/badge/Kotlin-2.3.21-blue.svg)](https://kotlinlang.org)
[![Compose Multiplatform](https://img.shields.io/badge/Compose%20Multiplatform-1.10.3-purple.svg)](https://www.jetbrains.com/lf/compose-multiplatform)
[![Android API](https://img.shields.io/badge/Android-API%2024%2B-green.svg)](https://developer.android.com)
[![License](https://img.shields.io/badge/license-MIT-lightgrey.svg)](LICENSE)

---

## Indice

- [Funzionalità principali](#funzionalità-principali)
- [Schermate UI](#schermate-ui--ui-screens)
- [Architettura](#architettura)
- [Modulo Android](#modulo-android)
- [Modulo Desktop](#modulo-desktop)
- [Utilizzo combinato Android + Desktop](#utilizzo-combinato-android--desktop)
- [Struttura del progetto](#struttura-del-progetto)
- [Stack tecnico](#stack-tecnico)
- [Build & Setup](#build--setup)
- [Quality Checks](#quality-checks)
- [Note iOS](#note-ios)

---

## Funzionalità principali

| Sezione                   | Descrizione                                                                                                                                                                                                   |
|---------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **Home**                  | Panoramica rapida, scopo dell'app e FAQ espandibili                                                                                                                                                           |
| **Info dispositivo**      | Hardware/software, patch di sicurezza, fingerprint, radio, kernel, bootloader, batteria/rete, flag di debug (ADB, developer options, debuggable), root status; ogni riga copiabile, report esportabile in TXT |
| **Root / Play Integrity** | Verifica root con RootBeer e verdict Play Integrity verificabili dal backend, riferiti alla sola CrackMyDroid e al device                                                                                     |
| **PenTesting**            | Suite di check di sicurezza automatizzati (singolo target o scansione completa); stati queued/ok/error, contatori, dettaglio errori, export/share report; cronologia fino a 240 eventi e 12 snapshot salvati  |
| **Attività (Activities)** | Lista attività per app con ricerca/cronologia, preferiti, espansione per pacchetto, lancio diretto, analisi dei rischi di esposizione (intent/exported) ed export                                             |
| **App installate (APK)**  | Ricerca full-text, filtri, preferiti, provenienza installer (Google Play segnalato/altro/non disponibile), icone app, analisi sicurezza manifest, export APK e condivisione diretta                         |
| **Permessi**              | Panoramica permessi app installate con classificazione del rischio (Critical/High/Medium/Low), ricerca e dettagli                                                                                             |
| **Shell/Trick**           | Comandi ADB/shell preconfigurati (con flag root-required), conferma opzionale, output ed exit code                                                                                                            |
| **Log**                   | Visualizzazione log applicativi con export                                                                                                                                                                    |
| **Impostazioni**          | Tema (chiaro/scuro/sistema), lingua (sistema, IT/EN/DE/FR/ES), verbosità log, accessibilità (alto contrasto, animazioni ridotte, testo grande), licenze librerie, versione app                       |

---

## Schermate UI / UI Screens

> Mockup ASCII illustrativi della UI Compose Multiplatform. Vedi [`docs/screens.md`](docs/screens.md)
> per la guida visiva completa e [`docs/README.md`](docs/README.md) per l'indice della documentazione tecnica.

### Home & Navigazione

```
┌─────────────────────────────────────────────────┐
│ ≡  Home                                    [?]  │
├─────────────────────────────────────────────────┤
│  ╔═══════════════════════════════════════════╗  │
│  ║  ╭──────────╮   CrackMyDroid             ║  │
│  ║  │  [🔐]   │   Mobile Security Tool     ║  │
│  ║  ╰──────────╯                            ║  │
│  ╚═══════════════════════════════════════════╝  │
│  Ultime operazioni                              │
│  ╔═════════════════════════════════════════╗    │
│  ║ ✅  Analisi manifest completata         ║    │
│  ║ ❌  Permessi critici rilevati (3)       ║    │
│  ║ ✅  Export APK completato               ║    │
│  ╚═════════════════════════════════════════╝    │
├─────────────────────────────────────────────────┤
│   🏠 Home    📱 Info    ⚙️ Settings              │
└─────────────────────────────────────────────────┘
```

### PenTesting — Wizard 4 step

```
┌─────────────────────────────────────────────────┐
│ ←  PenTesting                              [?]  │
│  ╔═════════════════════════════════════════╗    │
│  ║  Step 3/4 • Esecuzione                 ║    │
│  ║  ████████████████░░░░░░░░░░░░░░░░░░░  ║    │
│  ╚═════════════════════════════════════════╝    │
│  ╔═════════════════════════════════════════╗    │
│  ║  Completati 4/4  •  Anomalie: 2  OK: 2 ║    │
│  ╚═════════════════════════════════════════╝    │
│  ║ ❌  Analisi permessi    ANOMALIA        ║    │
│  ║ ✅  Analisi manifest    OK              ║    │
│  ║ ❌  Network monitor     ANOMALIA        ║    │
│  ║ ✅  Library scan        OK              ║    │
├─────────────────────────────────────────────────┤
│   🏠 Home    📱 Info    ⚙️ Settings              │
└─────────────────────────────────────────────────┘
```

### App installate — Analisi manifest

```
│  ╔═════════════════════════════════════════╗    │
│  ║ ▼  [📦] Dangerous App   [📤] [★]       ║    │
│  ║  🔴 RISCHIO CRITICO  Score: 13          ║    │
│  ║  🔴 android:debuggable=true             ║    │
│  ║  🟠 allowBackup=true (no custom agent) ║    │
│  ║  🔴 ContentProvider esposto (no perm.) ║    │
│  ╚═════════════════════════════════════════╝    │
```

### Permessi — Classificazione rischio

```
│  ║ ▼  Dangerous App  •  12 perm  🔴 CRITICO   ║  │
│  ║  🔴  RECORD_AUDIO   🔴  READ_SMS           ║  │
│  ║  🟠  READ_CONTACTS  🟠  ACCESS_LOCATION    ║  │
│  ║  🟡  CAMERA         🟡  READ_STORAGE       ║  │
```

→ Schermate complete con tutti i dettagli: **[docs/screens.md](docs/screens.md)**

---

## Architettura

Il progetto segue una **Clean Architecture** a tre layer con separazione netta tra piattaforme:

```
┌─────────────────────────────────────────────────────────────────┐
│                         :shared (KMP)                           │
│                                                                 │
│  ┌─────────────┐   ┌──────────────┐   ┌────────────────────┐   │
│  │  Presentation│   │    Domain    │   │       Data         │   │
│  │             │   │              │   │                    │   │
│  │ ViewModels  │──▶│  Use Cases   │──▶│   Repositories     │   │
│  │  Screens    │   │  Models      │   │   (interfaces)     │   │
│  │  Components │   │  Analyzers   │   │                    │   │
│  └─────────────┘   └──────────────┘   └────────────────────┘   │
│                                              │                  │
│           ┌──────────────────────────────────┤                  │
│           │ commonMain: PaginationHelper,    │                  │
│           │ SQLDelight (CacheDatabase)        │                  │
└───────────┼──────────────────────────────────┼──────────────────┘
            │                                  │
    ┌───────▼──────┐                  ┌────────▼──────┐
    │  androidMain │                  │  desktopMain  │
    │              │                  │               │
    │ Repository   │                  │ Repository    │
    │ impl native  │                  │ impl via ADB  │
    │ (Android API,│                  │ (adb shell,   │
    │  RootBeer,   │                  │  adb pull,    │
    │  Play Integ.,│                  │  snapshot     │
    │  DataStore,  │                  │  JSON import) │
    │  Caching/    │                  │               │
    │  Pagination) │                  └───────────────┘
    └──────────────┘
```

### Caching & Paginazione

- **`PaginationHelper`** (`commonMain`): utility stateless per slice offset-based (`paginate`,
  `countPages`).
- **`CachingRepositoryDecorator`** (`androidMain`): decorator in-memory con TTL di 60 secondi per
  report manifest e componenti; riduce le chiamate ripetute al `PackageManager`.
- **SQLDelight** (`CacheDatabase.sq`): cache persistente SQLite con driver platform-specific via
  `expect/actual`.
- **PenTestingViewModel**: lazy cache interna per permessi, root status, activity e risk index;
  history cappata a 240 eventi, max 12 snapshot salvati.

### Analisi sicurezza manifest

`ManifestSecurityAnalyzer.analyze()` ispeziona il manifest APK e produce un `ManifestSecurityReport`
con:

| Check                                      | Livello     |
|--------------------------------------------|-------------|
| `android:debuggable=true`                  | 🔴 Critical |
| `testOnly=true`                            | 🟠 High     |
| Debug certificate                          | 🟠 High     |
| `allowBackup=true` (senza custom agent)    | 🟠 High     |
| `allowBackup=true` (con custom agent)      | 🟡 Medium   |
| `usesCleartextTraffic=true`                | 🟡 Medium   |
| ContentProvider esposto senza permission   | 🔴 Critical |
| Service esposto senza permission           | 🟠 High     |
| BroadcastReceiver esposto senza permission | 🟡 Medium   |

Il punteggio finale determina un `ManifestRiskLevel` (Low / Medium / High / Critical).

---

## Modulo Android

Il modulo `:androidApp` è l'applicazione nativa Android che gira **direttamente sul device** con
accesso completo alle API di sistema.

### Come funziona

```
CrackMyDroidApp (Application)
  └─ initKoin()                          // avvia l'iniezione delle dipendenze
  └─ AndroidDeviceSnapshotCoordinator    // raccoglie dati del device in background
       └─ serializza snapshot JSON → /sdcard/Android/data/.../snapshot.json

MainActivity
  └─ setContent { CrackMyDroidApp() }   // UI condivisa dal modulo :shared
```

1. All'avvio, `CrackMyDroidApp` inizializza **Koin** e avvia `AndroidDeviceSnapshotCoordinator` in
   background.
2. Il coordinator raccoglie continuamente le informazioni del device (hardware, root, Play
   Integrity, pacchetti, permessi…) e le serializza in un **file JSON** sullo storage dell'app.
3. `MainActivity` monta la UI condivisa, che legge i dati direttamente dalle repository Android
   native.

### Requisiti

- Android **API 24+** (Android 7.0 Nougat)
- JDK 17, Android SDK con `platform-tools`/`adb` nel PATH
- `compileSdk=36`, `targetSdk=35`

### Build e installazione

```bash
# Compila debug APK
./gradlew :androidApp:assembleDebug

# Installa su device collegato
adb install -r androidApp/build/outputs/apk/debug/androidApp-debug.apk

# Oppure compila e installa in un solo comando
./gradlew :androidApp:installDebug
```

---

## Modulo Desktop

Il modulo `:desktopApp` è un'app **JVM/Desktop** costruita con Compose Multiplatform. Non accede
direttamente alle API Android: usa **ADB come bridge** per connettersi a un device fisico o emulato.

### Come funziona

```
Desktop App
  └─ DevicePickerScreen                 // lista i device ADB disponibili (adb devices)
  └─ DeviceSessionControllerDesktop     // gestisce la sessione ADB
       └─ RemoteDeviceSnapshotImporter  // adb pull del JSON di snapshot
            └─ snapshot.json → strutture dati Kotlin
  └─ DesktopToolbar                     // device info, rescan, cambio device
  └─ CrackMyDroidApp()                  // stessa UI del modulo :shared
```

1. All'avvio viene mostrata la **schermata di selezione device**.
2. Se `adb` non è nel PATH, si può configurare manualmente il percorso (salvato in DataStore).
3. Cliccando **"Rileva device"** viene eseguito `adb devices`; i device compaiono in lista.
4. Selezionando un device, l'app esegue `adb pull` del file JSON generato dall'app Android.
5. Il JSON viene deserializzato e iniettato nelle stesse repository condivise: la UI si avvia
   identica all'app Android, ma con i dati del device remoto.
6. La **toolbar superiore** mostra modello, serial, stato ADB, sorgente dati (live/snapshot +
   timestamp), pulsanti per rescan, cambio device e impostazioni ADB.

### Requisiti

- JDK 17+
- macOS, Linux o Windows
- `adb` (Android SDK platform-tools) nel PATH **oppure** percorso configurabile nell'app

### Build e avvio

```bash
# Avvia l'app desktop direttamente
./gradlew :desktopApp:run

# Crea un pacchetto distribuibile (macOS .dmg, Linux .deb, Windows .msi)
./gradlew :desktopApp:packageDistributionForCurrentOS
```

---

## Utilizzo combinato Android + Desktop

L'app Android e l'app desktop si **complementano**: la prima raccoglie i dati sul device, la seconda
li visualizza su computer via ADB.

```
[PC/Mac]                          [Device Android]
  desktopApp                          androidApp
      │                                   │
      │  adb pull snapshot.json ◄─────────┤ AndroidDeviceSnapshotCoordinator
      │                                   │ (genera il JSON in background)
      ▼                                   │
  UI con tutti i dati                     │
  del device remoto                       │
```

### Flusso passo-passo

1. **Installa l'app Android** sul device:
   ```bash
   ./gradlew :androidApp:installDebug
   ```
2. **Avvia l'app Android** e lasciala girare qualche secondo — il coordinator genera il JSON di
   snapshot.
3. **Collega il device al PC** via USB con **USB Debugging abilitato** (o usa un emulatore).
4. **Verifica che ADB veda il device:**
   ```bash
   adb devices
   # atteso: <serial>   device
   ```
   Se lo stato è `unauthorized`, autorizza il computer sul device.
5. **Avvia l'app desktop:**
   ```bash
   ./gradlew :desktopApp:run
   ```
6. Nella schermata picker, clicca **"Rileva device"** → seleziona il device.
7. L'app importa lo snapshot e mostra la UI completa.

> **Nota:** se l'app Android non è installata o lo snapshot non è ancora stato generato, la desktop
> app segnala *"Snapshot non disponibile"* e mostra comunque la UI con le informazioni base ricavabili
> via ADB (package list, shell commands, ecc.).

---

## Struttura del progetto

```
newCrackMyDroid/
├── androidApp/              # App nativa Android
│   └── src/main/java/
│       ├── MainActivity.kt
│       ├── CrackMyDroidApp.kt   (Application + Koin init)
│       └── BootCompletedReceiver.kt
├── desktopApp/              # App desktop JVM
│   └── src/desktopMain/kotlin/
│       ├── Main.kt
│       └── DesktopCrackMyDroidApp.kt
├── shared/                  # Modulo KMP condiviso
│   └── src/
│       ├── commonMain/      # UI Compose, ViewModel, UseCases, modelli, DI
│       │   ├── domain/
│       │   │   ├── model/   # AppManifestSecurity, PermissionRisk, Models, …
│       │   │   ├── usecase/ # 8 gruppi di use case
│       │   │   └── repository/ # Interfacce repository
│       │   ├── ui/
│       │   │   ├── screens/ # 10 schermate Compose
│       │   │   └── */       # 8 ViewModel + state
│       │   ├── presentation/# BaseViewModel, navigazione, componenti UI
│       │   ├── data/cache/  # PaginationHelper, SQLDelight schema
│       │   └── di/          # Koin modules
│       ├── androidMain/     # Implementazioni Android
│       │   ├── data/        # Repository native, CachingRepositoryDecorator
│       │   └── snapshot/    # AndroidDeviceSnapshotCoordinator
│       ├── desktopMain/     # Implementazioni desktop via ADB
│       ├── iosMain/         # Stub iOS
│       ├── jvmCommonTest/   # Test JVM condivisi (MockK)
│       └── desktopTest/     # Test layer ADB/desktop
├── iosApp/                  # Applicazione iOS (SwiftUI wrapper)
└── gradle/
    └── libs.versions.toml   # Version catalog centralizzato
```

### Navigazione UI

- **Tab inferiore:** Home · Info dispositivo · Impostazioni
- **Drawer laterale:** App installate · PenTest · Permessi · Attività · Root · Trick · Log

---

## Stack tecnico

| Layer               | Libreria                            | Versione       |
|---------------------|-------------------------------------|----------------|
| **UI**              | Compose Multiplatform               | 1.10.3         |
| **UI**              | Material3                           | 1.9.0          |
| **Linguaggio**      | Kotlin                              | 2.3.21         |
| **DI**              | Koin                                | 3.5.6          |
| **Async**           | Kotlin Coroutines                   | 1.8.1          |
| **Database**        | SQLDelight                          | 2.0.1          |
| **Serializzazione** | kotlinx.serialization               | 1.6.3          |
| **Networking**      | Ktor Client                         | 3.5.0          |
| **Root detection**  | RootBeer                            | 0.1.1          |
| **Sicurezza**       | Play Integrity API                  | 1.4.0          |
| **Preferenze**      | DataStore Preferences               | 1.1.7          |
| **Lavoro in background** | WorkManager                    | 2.12.0         |
| **Logging**         | Kermit                              | 2.0.4          |
| **Test**            | kotlin-test, MockK, coroutines-test | —              |
| **Android**         | AGP                                 | 8.13.2         |
| **Android**         | Core KTX / Activity Compose         | 1.13.1 / 1.8.2 |

---

## Build & Setup

### Prerequisiti

- **JDK 17+**
- **Android Studio** (Koala o superiore) oppure **IntelliJ IDEA** con plugin KMP
- **Android SDK** (API 36) con `platform-tools` nel PATH
- **`adb`** nel PATH (per la desktop app)

### Clone e prime build

```bash
git clone https://github.com/misterAnt92TV/crackMyDroid.git
cd crackMyDroid

# Android
./gradlew :androidApp:installDebug

# Desktop
./gradlew :desktopApp:run

# Test
./gradlew :shared:desktopTest :shared:testDebugUnitTest
```

### Configurazione Gradle

Le proprietà rilevanti in `gradle.properties`:

```properties
android.compileSdk=36
android.targetSdk=35
android.minSdk=24
org.gradle.parallel=true
org.gradle.caching=true
kotlin.mpp.enableCInteropCommonization=true
```

---

## Quality Checks

```bash
# Verifica link e ancore locali nei file Markdown
./scripts/check-doc-links.sh
```

---

## Note iOS

Il modulo `iosMain` è incluso per condividere UI e logica cross-platform. Le funzionalità dipendenti
da API Android (Root, ADB, Play Integrity, APK export, shell commands) non sono disponibili su iOS e
vengono rimpiazzate da **stub** che restituiscono stati vuoti/non supportati. Il wrapper SwiftUI in
`iosApp/` monta la stessa UI Compose.
