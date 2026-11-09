# CrackMyDroid

🌐 **Languages:** [Italiano](README.it.md) · [Deutsch](README.de.md) · [Français](README.fr.md) · [Español](README.es.md)

[![Kotlin](https://img.shields.io/badge/Kotlin-2.3.21-blue.svg)](https://kotlinlang.org)
[![Compose Multiplatform](https://img.shields.io/badge/Compose%20Multiplatform-1.10.3-purple.svg)](https://www.jetbrains.com/lf/compose-multiplatform)
[![Android API](https://img.shields.io/badge/Android-API%2024%2B-green.svg)](https://developer.android.com)
[![License](https://img.shields.io/badge/license-MIT-lightgrey.svg)](LICENSE)

> Kotlin Multiplatform (Android · Desktop · iOS) security toolkit for mobile penetration testing,
> reverse engineering, and device diagnostics — single shared UI built with Compose Multiplatform.

## Table of Contents

- [Features](#features)
- [UI Screens](#ui-screens)
- [Architecture](#architecture)
- [Android Module](#android-module)
- [Desktop Module](#desktop-module)
- [Combined Usage](#combined-usage)
- [Project Structure](#project-structure)
- [Tech Stack](#tech-stack)
- [Build & Setup](#build--setup)
- [Quality Checks](#quality-checks)
- [iOS Notes](#ios-notes)

---

## Features

| Section                   | Description                                                                                                                                                                                          |
|---------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **Home**                  | Quick overview, app purpose, expandable FAQ                                                                                                                                                          |
| **Device Info**           | Hardware/software, security patch, fingerprint, radio, kernel, bootloader, battery/network, debug flags (ADB, developer options, debuggable), root status; every row copyable, exportable TXT report |
| **Root / Play Integrity** | Root verification via RootBeer and backend-verifiable Play Integrity verdicts for CrackMyDroid and its device                                                                                     |
| **PenTesting**            | Automated security check suite (single target or full sweep); queued/ok/error states, counters, error details, export/share report; history up to 240 events and 12 saved snapshots                  |
| **Activities**            | Per-app activity browser with search/history, favorites, package expansion, direct launch, exposure risk analysis (intent/exported), export                                                          |
| **Installed Apps (APK)**  | Full-text search, filters, favorites, installer provenance (reported Google Play/other/unavailable), app icons, manifest security analysis, APK export to storage and direct sharing                |
| **Permissions**           | Installed app permissions overview with risk classification (Critical/High/Medium/Low), search and details                                                                                           |
| **Shell/Tricks**          | Pre-configured ADB/shell commands (with root-required flag), optional confirmation, output and exit code                                                                                             |
| **Log**                   | App operation log viewer with export                                                                                                                                                                 |
| **Settings**              | Theme (light/dark/system), language (system, IT/EN/DE/FR/ES), log verbosity, accessibility (high contrast, reduced animations, large text), library licenses, app version                          |

---

## UI Screens

See [the screen guide](docs/screens.md) for screen mockups and
[the documentation index](docs/README.md) for the full technical documentation.

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
- **`CachingRepositoryDecorator`** (androidMain): in-memory cache with 60-second TTL for manifest
  reports and components; reduces repeated `PackageManager` calls.
- **SQLDelight** (`CacheDatabase.sq`): persistent SQLite cache with platform-specific driver via
  `expect/actual`.
- **PenTestingViewModel**: lazy internal caches for permissions, root status, activity and risk
  index; history capped at 240 events, max 12 saved snapshots.

### Manifest Security Analysis

`ManifestSecurityAnalyzer.analyze()` inspects the APK manifest and produces a
`ManifestSecurityReport` with weighted risk scoring:

| Check                                     | Level       |
|-------------------------------------------|-------------|
| `android:debuggable=true`                 | 🔴 Critical |
| `testOnly=true`                           | 🟠 High     |
| Debug certificate signed                  | 🟠 High     |
| `allowBackup=true` (no custom agent)      | 🟠 High     |
| `allowBackup=true` (custom agent present) | 🟡 Medium   |
| `usesCleartextTraffic=true`               | 🟡 Medium   |
| Unprotected exported ContentProvider      | 🔴 Critical |
| Unprotected exported Service              | 🟠 High     |
| Unprotected exported BroadcastReceiver    | 🟡 Medium   |

Final score maps to `ManifestRiskLevel` (Low / Medium / High / Critical).

---

## Android Module

The `:androidApp` module runs **directly on the device** with full access to Android system APIs.

### How it works

1. `CrackMyDroidApp` (Application) initializes **Koin** and launches
   `AndroidDeviceSnapshotCoordinator` in background.
2. The coordinator continuously collects device data (hardware, root, Play Integrity, packages,
   permissions…) and serializes it as a **JSON snapshot** to app storage.
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

The `:desktopApp` module is a **JVM/Desktop** app built with Compose Multiplatform. It does not
access Android APIs directly; it uses **ADB as a bridge** to connect to a physical or emulated
Android device.

### How it works

1. On launch, a **device picker screen** is shown.
2. If `adb` is not in the PATH, the binary path can be configured manually (saved in DataStore).
3. Clicking **"Detect devices"** runs `adb devices`; found devices appear in the list.
4. Selecting a device triggers an `adb pull` of the snapshot JSON generated by the Android app.
5. The JSON is deserialized and injected into the same shared repositories; the UI launches
   identically to the Android app, showing the remote device's data.
6. The **top toolbar** shows: model + serial + ADB status, data source (live/snapshot + timestamp),
   rescan/device change/ADB settings buttons.

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

The Android and Desktop apps **complement each other**: the former collects data on the device, the
latter visualizes it on a connected computer.

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

> **Note:** if the Android app is not installed or the snapshot has not been generated yet, the
> desktop app reports *"Snapshot unavailable"* and still shows the UI with basic info retrievable
> directly via ADB (package list, shell commands, etc.).

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

### UI Navigation

- **Bottom tab bar:** Home · Device Info · Settings
- **Header menu:** Installed Apps · PenTest · Permissions · Activities · Root · Tricks · Log

---

## Tech Stack

| Layer              | Library                             | Version        |
|--------------------|-------------------------------------|----------------|
| **UI**             | Compose Multiplatform               | 1.10.3         |
| **UI**             | Material3                           | 1.9.0          |
| **Language**       | Kotlin                              | 2.3.21         |
| **DI**             | Koin                                | 3.5.6          |
| **Async**          | Kotlin Coroutines                   | 1.8.1          |
| **Database**       | SQLDelight                          | 2.0.1          |
| **Serialization**  | kotlinx.serialization               | 1.6.3          |
| **Networking**     | Ktor Client                         | 3.5.0          |
| **Root detection** | RootBeer                            | 0.1.1          |
| **Security**       | Play Integrity API                  | 1.4.0          |
| **Preferences**    | DataStore Preferences               | 1.1.7          |
| **Background work**| WorkManager                         | 2.12.0         |
| **Logging**        | Kermit                              | 2.0.4          |
| **Testing**        | kotlin-test, MockK, coroutines-test | —              |
| **Android**        | AGP                                 | 8.13.2         |
| **Android**        | Core KTX / Activity Compose         | 1.13.1 / 1.8.2 |

---

## Build & Setup

### Prerequisites

- **JDK 17+**
- **Android Studio** (Koala or later) or **IntelliJ IDEA** with the KMP plugin
- **Android SDK** (API 36) with `platform-tools` in PATH
- **`adb`** in PATH (for the desktop app)

### Clone and first builds

```bash
git clone https://github.com/misterAnt92TV/crackMyDroid.git
cd crackMyDroid

# Android
./gradlew :androidApp:installDebug

# Desktop
./gradlew :desktopApp:run

# Tests
./gradlew :shared:desktopTest :shared:testDebugUnitTest
```

### Gradle Configuration

Relevant properties in `gradle.properties`:

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
# Check local links and heading anchors in Markdown files
./scripts/check-doc-links.sh
```

---

## iOS Notes

The `iosMain` source set shares UI and logic cross-platform. Android-specific features (root
detection, ADB, Play Integrity, APK export, shell commands) are not available on iOS and are
replaced by **stubs** that return empty/unsupported states. The SwiftUI wrapper in `iosApp/` mounts
the same Compose UI.
