# CrackMyDroid

🌐 **Sprachen:** [English](README.md) · [Italiano](README.it.md) · [Français](README.fr.md) · [Español](README.es.md)

---

Kotlin Multiplatform (Android + Desktop + iOS) für mobile Sicherheit, Reverse Engineering und Gerätediagnose — alles in einer gemeinsamen UI, die mit Compose Multiplatform entwickelt wurde.

---

## Hauptfunktionen

| Bereich | Beschreibung |
|---|---|
| **Home** | Kurzübersicht, App-Zweck und erweiterbare FAQ |
| **Geräteinformationen** | Hardware/Software, Sicherheits-Patches, Fingerprint, Radio, Kernel, Bootloader, Akku/Netzwerk, Debug-Flags (ADB, Entwickleroptionen, debuggable), Root-Status; jede Zeile kopierbar, Report als TXT exportierbar |
| **Root / Play Integrity** | Root-Prüfung mit RootBeer, Play Integrity-Checks mit konfigurierbarem Nonce und Testdetails |
| **PenTesting** | Sicherheitsprüfliste mit Status (queued/ok/error), Zähler, Fehlerdetails, Report-Export/-Teilen |
| **Activities** | Aktivitätenliste pro App mit Suche/Verlauf, Favoriten, Paketerweiterung, direktem Start und Export |
| **Installierte Apps (APK)** | Suche, Filter, Favoriten, App-Icons, APK-Export in den Speicher und direktes Teilen |
| **Berechtigungen** | Übersicht der App-Berechtigungen mit Risikoanalyse, Suche und Details |
| **Shell/Tricks** | Vorkonfigurierte ADB/Shell-Befehle (mit Root-required-Flag), optionale Bestätigung, Ausgabe und Exit-Code |
| **Log** | Anwendungslog-Viewer mit Export |
| **Einstellungen** | Thema (Hell/Dunkel/System), Log-Ausführlichkeit, Barrierefreiheit (hoher Kontrast, reduzierte Animationen, großer Text), Bibliothekslizenzen, App-Version |

---

## Android-App

Das Modul `:androidApp` ist die native Android-Anwendung. Sie läuft **direkt auf dem Gerät** und hat vollständigen Zugriff auf alle System-APIs.

### Funktionsweise

```
CrackMyDroidApp (Application)
  └─ initKoin()                          // startet die Dependency Injection
  └─ AndroidDeviceSnapshotCoordinator    // sammelt Gerätedaten im Hintergrund
       └─ speichert JSON-Snapshot in /sdcard/Android/data/.../snapshot.json

MainActivity
  └─ setContent { CrackMyDroidApp() }   // gemeinsame UI aus dem :shared-Modul
```

1. Beim Start initialisiert `CrackMyDroidApp` (Application) **Koin** und startet `AndroidDeviceSnapshotCoordinator` im Hintergrund.
2. Der Coordinator sammelt kontinuierlich Geräteinformationen (Hardware, Root-Status, Play Integrity, Pakete, Berechtigungen…) und serialisiert sie in eine **JSON-Datei** im gemeinsamen Gerätespeicher.
3. `MainActivity` lädt die gemeinsame UI (`CrackMyDroidApp` Composable aus dem `:shared`-Modul), die Daten direkt aus den nativen Android-Repositories liest.

### Voraussetzungen

- Android **API 24+** (Android 7.0)
- JDK 17, Android SDK mit `platform-tools`/`adb` im PATH

### Build & Installation

```bash
# Debug-APK kompilieren
./gradlew :androidApp:assembleDebug

# Auf verbundenem Gerät installieren
adb install -r androidApp/build/outputs/apk/debug/androidApp-debug.apk

# Oder direkt installieren
./gradlew :androidApp:installDebug
```

---

## Desktop-App (Compose Multiplatform)

Das Modul `:desktopApp` ist eine **JVM/Desktop**-Anwendung, die mit Compose Multiplatform entwickelt wurde. Sie greift nicht direkt auf Android-APIs zu, sondern nutzt **ADB als Brücke**, um sich mit einem physischen oder emulierten Android-Gerät zu verbinden und dessen Daten zu lesen.

### Funktionsweise

```
Desktop App
  └─ DevicePickerScreen                 // listet verfügbare ADB-Geräte auf
       └─ adb devices
  └─ DeviceSessionControllerDesktop     // verwaltet die ADB-Sitzung
       └─ RemoteDeviceSnapshotImporter  // adb pull des Snapshot-JSON
            └─ snapshot.json → Kotlin-Datenstrukturen
  └─ DesktopToolbar                     // Geräteinfo, Rescan, Gerätewechsel
  └─ CrackMyDroidApp()                  // gleiche UI wie :shared-Modul
```

1. Beim Start wird der **Geräteauswahlbildschirm** angezeigt.
2. Ist `adb` nicht im System-PATH, kann der Binärpfad manuell konfiguriert werden (wird in DataStore gespeichert und bei späteren Starts nicht erneut abgefragt).
3. Mit Klick auf **„Gerät erkennen"** wird `adb devices` ausgeführt; erkannte Geräte erscheinen in der Liste.
4. Bei Auswahl eines Geräts führt die App ein `adb pull` der JSON-Snapshot-Datei durch, die von der Android-App auf dem Gerät erzeugt wurde.
5. Das JSON wird deserialisiert und in dieselben gemeinsamen Repositories injiziert: die **`CrackMyDroidApp()`-UI** startet identisch zur Android-App, zeigt jedoch die Daten des Remote-Geräts.
6. Die **obere Toolbar** zeigt: Modell + Serial + ADB-Status, Datenquelle (live/Snapshot + Zeitstempel), Schaltflächen für Rescan, Gerätewechsel und ADB-Einstellungen.

### Voraussetzungen

- JDK 17+
- macOS, Linux oder Windows
- `adb` installiert (Android SDK platform-tools) und im PATH **oder** in der App konfigurierbarer Pfad

### Build & Starten

```bash
# Desktop-App direkt starten
./gradlew :desktopApp:run

# Verteilbares Paket erstellen (macOS .dmg, Linux .deb, Windows .msi)
./gradlew :desktopApp:packageDistributionForCurrentOS
```

---

## Android + Desktop gemeinsam nutzen

Die Android-App und die Desktop-App **ergänzen sich gegenseitig**: Erstere sammelt die Daten auf dem Gerät, Letztere zeigt sie auf einem via ADB verbundenen Computer an.

```
[PC/Mac]                          [Android-Gerät]
  desktopApp                          androidApp
      │                                   │
      │  adb pull snapshot.json ◄─────────┤ AndroidDeviceSnapshotCoordinator
      │                                   │ (erzeugt das JSON im Hintergrund)
      ▼                                   │
  UI mit allen Gerätedaten                │
  des Remote-Geräts                       │
```

### Schritt-für-Schritt-Ablauf

1. **Android-App installieren** (siehe oben).
2. **Android-App starten** und einige Sekunden laufen lassen — der Coordinator erzeugt den Snapshot-JSON.
3. **Gerät via USB mit dem PC verbinden**, **USB-Debugging aktiviert** (oder einen laufenden Emulator nutzen).
4. **Prüfen, ob ADB das Gerät erkennt:**
   ```bash
   adb devices
   # erwartet: <serial>   device
   ```
   Bei Status `unauthorized` das Gerät auf dem Bildschirm autorisieren.
5. **Desktop-App starten:**
   ```bash
   ./gradlew :desktopApp:run
   ```
6. Im Picker-Bildschirm auf **„Gerät erkennen"** klicken → Gerät auswählen.
7. Die App importiert den Snapshot und zeigt die vollständige UI mit den Gerätedaten.

> **Hinweis:** Ist die Android-App nicht installiert oder wurde der Snapshot noch nicht erzeugt, meldet die Desktop-App *„Snapshot nicht verfügbar"* und zeigt trotzdem die UI mit den via ADB direkt abrufbaren Basisinformationen (Paketliste, Shell-Befehle usw.).

---

## Projektstruktur

```
newCrackMyDroid/
├── androidApp/          # Native Android-App (Activity, Application, BroadcastReceiver)
├── desktopApp/          # JVM Desktop-App (Einstiegspunkt, Gerätepicker, Toolbar)
├── shared/              # Gemeinsames KMP-Modul
│   ├── commonMain/      # Compose UI, ViewModel, UseCases, Modelle, DI
│   ├── androidMain/     # Native Android-Repositories (RootBeer, Play Integrity, DataStore…)
│   ├── desktopMain/     # Desktop-Repositories via ADB, SQLite-Treiber, Pfade
│   ├── iosMain/         # iOS-Stubs (Android-Funktionen nicht verfügbar)
│   ├── jvmCommonTest/   # Gemeinsame JVM-Tests (MockK, Android + Desktop)
│   └── desktopTest/     # Desktop/ADB-Layer-spezifische Tests
├── iosApp/              # iOS-Anwendung (SwiftUI-Wrapper)
└── gradle/
    └── libs.versions.toml  # Zentralisierter Versionskatalog
```

### UI-Navigation (shared)

- **Untere Tab-Leiste:** Home · Geräteinformationen · Einstellungen
- **Seitenmenü:** Installierte Apps · PenTest · Berechtigungen · Activities · Root · Tricks · Log

---

## Tech-Stack

| Layer | Bibliotheken |
|---|---|
| UI | Compose Multiplatform, Material3 |
| DI | Koin (core, Android, Compose) |
| Async | Kotlin Coroutines + Flow |
| Lokale Datenbank | SQLDelight (Android-Treiber / SQLite JVM-Treiber) |
| Android-Sicherheit | RootBeer, Play Integrity API |
| Einstellungen | DataStore Preferences |
| Logging | Kermit |
| Tests | kotlin-test, MockK, kotlinx-coroutines-test |

---

## iOS-Hinweise

Das iOS-Modul ist enthalten, um UI und Logik zu teilen. Funktionen, die von Android-APIs abhängen (Root, ADB, Play Integrity, APK-Export), sind unter iOS nicht verfügbar und werden durch Stubs ersetzt.
