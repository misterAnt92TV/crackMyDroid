# CrackMyDroid

> **Kotlin Multiplatform** (Android · Desktop · iOS) — sicurezza mobile, reverse engineering e diagnostica del dispositivo, tutto in un'unica UI condivisa costruita con **Compose Multiplatform**.

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
- [English Documentation →](#english-documentation)

---

## Funzionalità principali

| Sezione | Descrizione |
|---|---|
| **Home** | Panoramica rapida, scopo dell'app e FAQ espandibili |
| **Info dispositivo** | Hardware/software, patch di sicurezza, fingerprint, radio, kernel, bootloader, batteria/rete, flag di debug (ADB, developer options, debuggable), root status; ogni riga copiabile, report esportabile in TXT |
| **Root / Play Integrity** | Verifica root con RootBeer, controlli Play Integrity con nonce configurabile e dettaglio dei test |
| **PenTesting** | Suite di check di sicurezza automatizzati (singolo target o scansione completa); stati queued/ok/error, contatori, dettaglio errori, export/share report; cronologia fino a 240 eventi e 12 snapshot salvati |
| **Attività (Activities)** | Lista attività per app con ricerca/cronologia, preferiti, espansione per pacchetto, lancio diretto, analisi dei rischi di esposizione (intent/exported) ed export |
| **App installate (APK)** | Ricerca full-text, filtri, preferiti, icone app, analisi sicurezza manifest (debuggable, backup, cleartext, componenti esposti), punteggio di rischio, export APK su storage e condivisione diretta |
| **Permessi** | Panoramica permessi app installate con classificazione del rischio (Critical/High/Medium/Low), ricerca e dettagli |
| **Shell/Trick** | Comandi ADB/shell preconfigurati (con flag root-required), conferma opzionale, output ed exit code |
| **Log** | Visualizzazione log applicativi con export |
| **Impostazioni** | Tema (chiaro/scuro/sistema), lingua (IT/EN), verbosità log, accessibilità (alto contrasto, animazioni ridotte, testo grande), licenze librerie, versione app |

---

## Schermate UI / UI Screens

> Mockup ASCII fedeli alla UI Compose Multiplatform reale. Vedi [`docs/screens.md`](docs/screens.md) per la documentazione visiva completa di tutte le schermate.

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

- **`PaginationHelper`** (`commonMain`): utility stateless per slice offset-based (`paginate`, `countPages`).
- **`CachingRepositoryDecorator`** (`androidMain`): decorator in-memory con TTL di 60 secondi per report manifest e componenti; riduce le chiamate ripetute al `PackageManager`.
- **SQLDelight** (`CacheDatabase.sq`): cache persistente SQLite con driver platform-specific via `expect/actual`.
- **PenTestingViewModel**: lazy cache interna per permessi, root status, activity e risk index; history cappata a 240 eventi, max 12 snapshot salvati.

### Analisi sicurezza manifest

`ManifestSecurityAnalyzer.analyze()` ispeziona il manifest APK e produce un `ManifestSecurityReport` con:

| Check | Livello |
|---|---|
| `android:debuggable=true` | 🔴 Critical |
| `testOnly=true` | 🟠 High |
| Debug certificate | 🟠 High |
| `allowBackup=true` (senza custom agent) | 🟠 High |
| `allowBackup=true` (con custom agent) | 🟡 Medium |
| `usesCleartextTraffic=true` | 🟡 Medium |
| ContentProvider esposto senza permission | 🔴 Critical |
| Service esposto senza permission | 🟠 High |
| BroadcastReceiver esposto senza permission | 🟡 Medium |

Il punteggio finale determina un `ManifestRiskLevel` (Low / Medium / High / Critical).

---

## Modulo Android

Il modulo `:androidApp` è l'applicazione nativa Android che gira **direttamente sul device** con accesso completo alle API di sistema.

### Come funziona

```
CrackMyDroidApp (Application)
  └─ initKoin()                          // avvia l'iniezione delle dipendenze
  └─ AndroidDeviceSnapshotCoordinator    // raccoglie dati del device in background
       └─ serializza snapshot JSON → /sdcard/Android/data/.../snapshot.json

MainActivity
  └─ setContent { CrackMyDroidApp() }   // UI condivisa dal modulo :shared
```

1. All'avvio, `CrackMyDroidApp` inizializza **Koin** e avvia `AndroidDeviceSnapshotCoordinator` in background.
2. Il coordinator raccoglie continuamente le informazioni del device (hardware, root, Play Integrity, pacchetti, permessi…) e le serializza in un **file JSON** sullo storage dell'app.
3. `MainActivity` monta la UI condivisa, che legge i dati direttamente dalle repository Android native.

### Requisiti

- Android **API 24+** (Android 7.0 Nougat)
- JDK 17, Android SDK con `platform-tools`/`adb` nel PATH
- `compileSdk=34`, `targetSdk=34`

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

Il modulo `:desktopApp` è un'app **JVM/Desktop** costruita con Compose Multiplatform. Non accede direttamente alle API Android: usa **ADB come bridge** per connettersi a un device fisico o emulato.

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
5. Il JSON viene deserializzato e iniettato nelle stesse repository condivise: la UI si avvia identica all'app Android, ma con i dati del device remoto.
6. La **toolbar superiore** mostra modello, serial, stato ADB, sorgente dati (live/snapshot + timestamp), pulsanti per rescan, cambio device e impostazioni ADB.

## Quality Checks

```bash
# Verifica link locali nella documentazione (README + docs)
./scripts/check-doc-links.sh
```

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

L'app Android e l'app desktop si **complementano**: la prima raccoglie i dati sul device, la seconda li visualizza su computer via ADB.

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
2. **Avvia l'app Android** e lasciala girare qualche secondo — il coordinator genera il JSON di snapshot.
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

> **Nota:** se l'app Android non è installata o lo snapshot non è ancora stato generato, la desktop app segnala *"Snapshot non disponibile"* e mostra comunque la UI con le informazioni base ricavabili via ADB (package list, shell commands, ecc.).

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

| Layer | Libreria | Versione |
|---|---|---|
| **UI** | Compose Multiplatform | 1.10.3 |
| **UI** | Material3 | 1.9.0 |
| **Linguaggio** | Kotlin | 2.3.21 |
| **DI** | Koin | 3.5.6 |
| **Async** | Kotlin Coroutines | 1.8.1 |
| **Database** | SQLDelight | 2.0.1 |
| **Serializzazione** | kotlinx.serialization | 1.6.3 |
| **Root detection** | RootBeer | 0.1.1 |
| **Sicurezza** | Play Integrity API | 1.4.0 |
| **Preferenze** | DataStore Preferences | 1.1.7 |
| **Logging** | Kermit | 2.0.4 |
| **Test** | kotlin-test, MockK, coroutines-test | — |
| **Android** | AGP | 8.13.0 |
| **Android** | Core KTX / Activity Compose | 1.13.1 / 1.8.2 |

---

## Build & Setup

### Prerequisiti

- **JDK 17+**
- **Android Studio** (Koala o superiore) oppure **IntelliJ IDEA** con plugin KMP
- **Android SDK** (API 34) con `platform-tools` nel PATH
- **`adb`** nel PATH (per la desktop app)

### Clone e prime build

```bash
git clone https://github.com/<org>/CrackMyDroid.git
cd newCrackMyDroid

# Android
./gradlew :androidApp:installDebug

# Desktop
./gradlew :desktopApp:run

# Test
./gradlew :shared:jvmCommonTest
./gradlew :shared:desktopTest
```

### Configurazione Gradle

Le proprietà rilevanti in `gradle.properties`:

```properties
android.compileSdk=34
android.targetSdk=34
android.minSdk=24
org.gradle.parallel=true
org.gradle.caching=true
kotlin.mpp.enableCInteropCommonization=true
```

---

## Note iOS

Il modulo `iosMain` è incluso per condividere UI e logica cross-platform. Le funzionalità dipendenti da API Android (Root, ADB, Play Integrity, APK export, shell commands) non sono disponibili su iOS e vengono rimpiazzate da **stub** che restituiscono stati vuoti/non supportati. Il wrapper SwiftUI in `iosApp/` monta la stessa UI Compose.

---

---

# English Documentation

> Kotlin Multiplatform (Android · Desktop · iOS) security toolkit for mobile penetration testing, reverse engineering, and device diagnostics — single shared UI built with Compose Multiplatform.

## Table of Contents

- [Features](#features)
- [Architecture](#architecture)
- [Android Module](#android-module)
- [Desktop Module](#desktop-module)
- [Combined Usage](#combined-usage)
- [Project Structure](#project-structure)
- [Tech Stack](#tech-stack)
- [Build & Setup (EN)](#build--setup-en)

---

## Features

| Section | Description |
|---|---|
| **Home** | Quick overview, app purpose, expandable FAQ |
| **Device Info** | Hardware/software, security patch, fingerprint, radio, kernel, bootloader, battery/network, debug flags (ADB, developer options, debuggable), root status; every row copyable, exportable TXT report |
| **Root / Play Integrity** | Root verification via RootBeer, Play Integrity checks with configurable nonce and detailed test results |
| **PenTesting** | Automated security check suite (single target or full sweep); queued/ok/error states, counters, error details, export/share report; history up to 240 events and 12 saved snapshots |
| **Activities** | Per-app activity browser with search/history, favorites, package expansion, direct launch, exposure risk analysis (intent/exported), export |
| **Installed Apps (APK)** | Full-text search, filters, favorites, app icons, manifest security analysis (debuggable, backup, cleartext, exposed components), risk score, APK export to storage and direct sharing |
| **Permissions** | Installed app permissions overview with risk classification (Critical/High/Medium/Low), search and details |
| **Shell/Tricks** | Pre-configured ADB/shell commands (with root-required flag), optional confirmation, output and exit code |
| **Log** | App operation log viewer with export |
| **Settings** | Theme (light/dark/system), language (IT/EN), log verbosity, accessibility (high contrast, reduced animations, large text), library licenses, app version |

---

## Architecture

Clean Architecture with three layers and strict platform separation:

```
┌─────────────────────────────────────────────────────────────────┐
│                         :shared (KMP)                           │
│                                                                 │
│  Presentation ──► Domain (Use Cases, Models) ──► Data (Repos)  │
│                                                                 │
│  commonMain: PaginationHelper, SQLDelight cache DB              │
└───────────────────────────────┬─────────────────────────────────┘
                                │
                ┌───────────────┴───────────────┐
                │                               │
         androidMain                      desktopMain
    (native Android APIs,            (ADB bridge: adb shell,
     RootBeer, Play Integrity,        adb pull, snapshot JSON
     DataStore, Caching/Pagination)   import)
```

### Caching & Pagination

- **`PaginationHelper`** (commonMain): stateless utility for offset-based list slicing.
- **`CachingRepositoryDecorator`** (androidMain): in-memory cache with 60-second TTL for manifest reports and components; reduces repeated `PackageManager` calls.
- **SQLDelight** (`CacheDatabase.sq`): persistent SQLite cache with platform-specific driver via `expect/actual`.
- **PenTestingViewModel**: lazy internal caches for permissions, root status, activity and risk index; history capped at 240 events, max 12 saved snapshots.

### Manifest Security Analysis

`ManifestSecurityAnalyzer.analyze()` inspects the APK manifest and produces a `ManifestSecurityReport` with weighted risk scoring:

| Check | Level |
|---|---|
| `android:debuggable=true` | 🔴 Critical |
| `testOnly=true` | 🟠 High |
| Debug certificate signed | 🟠 High |
| `allowBackup=true` (no custom agent) | 🟠 High |
| `allowBackup=true` (custom agent present) | 🟡 Medium |
| `usesCleartextTraffic=true` | 🟡 Medium |
| Unprotected exported ContentProvider | 🔴 Critical |
| Unprotected exported Service | 🟠 High |
| Unprotected exported BroadcastReceiver | 🟡 Medium |

Final score maps to `ManifestRiskLevel` (Low / Medium / High / Critical).

---

## Android Module

The `:androidApp` module runs **directly on the device** with full access to Android system APIs.

### How it works

1. `CrackMyDroidApp` (Application) initializes **Koin** and launches `AndroidDeviceSnapshotCoordinator` in background.
2. The coordinator continuously collects device data (hardware, root, Play Integrity, packages, permissions…) and serializes it as a **JSON snapshot** to app storage.
3. `MainActivity` mounts the shared UI, which reads data from the native Android repositories.

### Requirements

- Android **API 24+** (Android 7.0 Nougat)
- JDK 17, Android SDK with `platform-tools`/`adb` in PATH

### Build & Install

```bash
# Build debug APK
./gradlew :androidApp:assembleDebug

# Install on connected device
adb install -r androidApp/build/outputs/apk/debug/androidApp-debug.apk

# Or build & install in one step
./gradlew :androidApp:installDebug
```

---

## Desktop Module

The `:desktopApp` module is a **JVM/Desktop** app built with Compose Multiplatform. It does not access Android APIs directly; it uses **ADB as a bridge** to connect to a physical or emulated Android device.

### How it works

1. On launch, a **device picker screen** is shown.
2. If `adb` is not in the PATH, the binary path can be configured manually (saved in DataStore).
3. Clicking **"Detect devices"** runs `adb devices`; found devices appear in the list.
4. Selecting a device triggers an `adb pull` of the snapshot JSON generated by the Android app.
5. The JSON is deserialized and injected into the same shared repositories; the UI launches identically to the Android app, showing the remote device's data.
6. The **top toolbar** shows: model + serial + ADB status, data source (live/snapshot + timestamp), rescan/device change/ADB settings buttons.

### Requirements

- JDK 17+
- macOS, Linux or Windows
- `adb` (Android SDK platform-tools) in PATH **or** configurable binary path in the app

### Build & Run

```bash
# Run the desktop app
./gradlew :desktopApp:run

# Create a distributable package (macOS .dmg, Linux .deb, Windows .msi)
./gradlew :desktopApp:packageDistributionForCurrentOS
```

---

## Combined Usage

The Android and Desktop apps **complement each other**: the former collects data on the device, the latter visualizes it on a connected computer.

```
[PC/Mac]                          [Android Device]
  desktopApp                          androidApp
      │                                   │
      │  adb pull snapshot.json ◄─────────┤ AndroidDeviceSnapshotCoordinator
      │                                   │ (generates JSON in background)
      ▼                                   │
  Full UI with remote                     │
  device data                             │
```

### Step-by-step

1. **Install the Android app**: `./gradlew :androidApp:installDebug`
2. **Launch the Android app** and wait a few seconds for the snapshot to be generated.
3. **Connect the device** via USB with **USB Debugging enabled** (or use an emulator).
4. **Verify ADB sees the device**: `adb devices` → expected: `<serial>   device`
5. **Launch the desktop app**: `./gradlew :desktopApp:run`
6. In the picker, click **"Detect devices"** → select the device.
7. The app imports the snapshot and shows the full UI.

> **Note:** if the Android app is not installed or the snapshot has not been generated yet, the desktop app reports *"Snapshot unavailable"* and still shows the UI with basic info retrievable directly via ADB (package list, shell commands, etc.).

---

## Project Structure

```
newCrackMyDroid/
├── androidApp/              # Native Android app
├── desktopApp/              # JVM Desktop app
├── shared/                  # KMP shared module
│   └── src/
│       ├── commonMain/      # Compose UI, ViewModels, UseCases, models, DI
│       ├── androidMain/     # Android-native repository implementations
│       ├── desktopMain/     # Desktop repository implementations (ADB)
│       ├── iosMain/         # iOS stubs
│       ├── jvmCommonTest/   # Shared JVM tests (MockK)
│       └── desktopTest/     # Desktop/ADB-specific tests
├── iosApp/                  # iOS app (SwiftUI wrapper)
└── gradle/
    └── libs.versions.toml   # Centralized version catalog
```

---

## Tech Stack

| Layer | Library | Version |
|---|---|---|
| **UI** | Compose Multiplatform | 1.10.3 |
| **UI** | Material3 | 1.9.0 |
| **Language** | Kotlin | 2.3.21 |
| **DI** | Koin | 3.5.6 |
| **Async** | Kotlin Coroutines | 1.8.1 |
| **Database** | SQLDelight | 2.0.1 |
| **Serialization** | kotlinx.serialization | 1.6.3 |
| **Root detection** | RootBeer | 0.1.1 |
| **Security** | Play Integrity API | 1.4.0 |
| **Preferences** | DataStore Preferences | 1.1.7 |
| **Logging** | Kermit | 2.0.4 |
| **Testing** | kotlin-test, MockK, coroutines-test | — |
| **Android** | AGP | 8.13.0 |
| **Android** | Core KTX / Activity Compose | 1.13.1 / 1.8.2 |

---

## Build & Setup (EN)

### Prerequisites

- **JDK 17+**
- **Android Studio** (Koala or later) or **IntelliJ IDEA** with the KMP plugin
- **Android SDK** (API 34) with `platform-tools` in PATH
- **`adb`** in PATH (for the desktop app)

### Clone and first builds

```bash
git clone https://github.com/<org>/CrackMyDroid.git
cd newCrackMyDroid

# Android
./gradlew :androidApp:installDebug

# Desktop
./gradlew :desktopApp:run

# Tests
./gradlew :shared:jvmCommonTest
./gradlew :shared:desktopTest
```

### iOS Notes

The `iosMain` source set shares UI and logic cross-platform. Android-specific features (root detection, ADB, Play Integrity, APK export, shell commands) are not available on iOS and are replaced by **stubs** that return empty/unsupported states. The SwiftUI wrapper in `iosApp/` mounts the same Compose UI.
