# CrackMyDroid

🌐 **Lingue:** [English](README.md) · [Deutsch](README.de.md) · [Français](README.fr.md) · [Español](README.es.md)

---

Kotlin Multiplatform (Android + Desktop + iOS) per la sicurezza mobile, il reverse engineering e la diagnostica del dispositivo — tutto in un'unica UI condivisa costruita con Compose Multiplatform.

---

## Funzionalità principali

| Sezione | Descrizione |
|---|---|
| **Home** | Panoramica rapida, scopo dell'app e FAQ espandibili |
| **Info dispositivo** | Hardware/software, patch di sicurezza, fingerprint, radio, kernel, bootloader, batteria/rete, flag di debug (ADB, developer options, debuggable), root status; ogni riga copiabile, report esportabile in TXT |
| **Root / Play Integrity** | Verifica root con RootBeer, controlli Play Integrity con nonce configurabile e dettaglio dei test |
| **PenTesting** | Elenco check di sicurezza con stato (queued/ok/error), contatori, dettaglio errori, export/share report |
| **Attività (Activities)** | Lista attività per app con ricerca/cronologia, preferiti, espansione per pacchetto, lancio diretto ed export |
| **App installate (APK)** | Ricerca, filtri, preferiti, icone app, export APK su storage e condivisione diretta |
| **Permessi** | Panoramica permessi app installate con analisi del rischio, ricerca e dettagli |
| **Shell/Trick** | Comandi ADB/shell preconfigurati (con flag root-required), conferma opzionale, output ed exit code |
| **Log** | Visualizzazione log applicativi con export |
| **Impostazioni** | Tema (chiaro/scuro/sistema), verbosità log, accessibilità (alto contrasto, animazioni ridotte, testo grande), licenze librerie, versione app |

---

## App Android

Il modulo `:androidApp` è l'applicazione nativa Android. Gira **direttamente sul device** e ha accesso completo a tutte le API di sistema.

### Come funziona

```
CrackMyDroidApp (Application)
  └─ initKoin()                          // avvia l'iniezione delle dipendenze
  └─ AndroidDeviceSnapshotCoordinator    // raccoglie dati del device in background
       └─ salva snapshot JSON in /sdcard/Android/data/.../snapshot.json

MainActivity
  └─ setContent { CrackMyDroidApp() }   // UI condivisa dal modulo :shared
```

1. All'avvio, `CrackMyDroidApp` (Application) inizializza **Koin** e avvia `AndroidDeviceSnapshotCoordinator` in background.
2. Il coordinator raccoglie in modo continuativo le informazioni del device (hardware, root status, Play Integrity, pacchetti, permessi…) e le serializza in un **file JSON** sullo storage condiviso del device.
3. `MainActivity` monta la UI condivisa (`CrackMyDroidApp` Composable dal modulo `:shared`), che legge i dati direttamente dalle repository Android native.

### Requisiti

- Android **API 24+** (Android 7.0)
- JDK 17, Android SDK con `platform-tools`/`adb` nel PATH

### Build e installazione

```bash
# Compila debug APK
./gradlew :androidApp:assembleDebug

# Installa su device collegato
adb install -r androidApp/build/outputs/apk/debug/androidApp-debug.apk

# Oppure installa direttamente
./gradlew :androidApp:installDebug
```

---

## App Desktop (Compose Multiplatform)

Il modulo `:desktopApp` è un'applicazione **JVM/Desktop** costruita con Compose Multiplatform. Non accede direttamente alle API Android: usa **ADB come bridge** per connettersi a un device Android fisico o emulato e leggerne i dati.

### Come funziona

```
Desktop App
  └─ DevicePickerScreen                 // lista i device ADB disponibili
       └─ adb devices
  └─ DeviceSessionControllerDesktop     // gestisce la sessione ADB
       └─ RemoteDeviceSnapshotImporter  // adb pull del JSON di snapshot
            └─ snapshot.json → strutture dati Kotlin
  └─ DesktopToolbar                     // device info, rescan, cambio device
  └─ CrackMyDroidApp()                  // stessa UI del modulo :shared
```

1. All'avvio viene mostrata la **schermata di selezione device**.
2. Se `adb` non è nel PATH di sistema, si può configurare manualmente il percorso del binario (salvato in DataStore, non viene più richiesto ai run successivi).
3. Cliccando **"Rileva device"** viene eseguito `adb devices`; i device rilevati compaiono in lista.
4. Selezionando un device, l'app esegue `adb pull` del file di snapshot JSON generato dall'app Android sul device.
5. Il JSON viene deserializzato e iniettato nelle stesse repository condivise: la **UI `CrackMyDroidApp()`** si avvia identica all'app Android, ma mostra i dati del device remoto.
6. La **toolbar superiore** mostra: modello + serial + stato ADB, sorgente dati (live/snapshot + timestamp), pulsanti per rescan, cambio device e impostazioni ADB.

### Requisiti

- JDK 17+
- macOS, Linux o Windows
- `adb` installato (Android SDK platform-tools) e nel PATH **oppure** percorso configurabile nell'app

### Build e avvio

```bash
# Avvia l'app desktop direttamente
./gradlew :desktopApp:run

# Crea un pacchetto distribuibile (macOS .dmg, Linux .deb, Windows .msi)
./gradlew :desktopApp:packageDistributionForCurrentOS
```

---

## Come usare Android + Desktop insieme

L'app Android e l'app desktop si **complementano**: la prima raccoglie i dati sul device, la seconda li visualizza su un computer collegato via ADB.

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

1. **Installa l'app Android** sul device (vedi sopra).
2. **Avvia l'app Android** sul device e lasciala girare qualche secondo — il coordinator genera il JSON di snapshot.
3. **Collega il device al PC** via USB con **debug USB abilitato** (o usa un emulatore già attivo).
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
7. L'app importa lo snapshot e mostra la UI completa con i dati del device.

> **Nota:** se l'app Android non è installata o lo snapshot non è ancora stato generato, la desktop app segnala *"Snapshot non disponibile"* e mostra comunque la UI con le informazioni base ricavabili via ADB direttamente (package list, shell commands, ecc.).

---

## Struttura del progetto

```
newCrackMyDroid/
├── androidApp/          # App nativa Android (Activity, Application, BroadcastReceiver)
├── desktopApp/          # App desktop JVM (entry point, device picker, toolbar)
├── shared/              # Modulo KMP condiviso
│   ├── commonMain/      # UI Compose, ViewModel, UseCases, modelli, DI
│   ├── androidMain/     # Repository Android native (RootBeer, Play Integrity, DataStore…)
│   ├── desktopMain/     # Repository desktop via ADB, SQLite driver, paths
│   ├── iosMain/         # Stub per iOS (funzionalità Android non disponibili)
│   ├── jvmCommonTest/   # Test condivisi JVM (MockK, Android + Desktop)
│   └── desktopTest/     # Test specifici del layer desktop/ADB
├── iosApp/              # Applicazione iOS (SwiftUI wrapper)
└── gradle/
    └── libs.versions.toml  # Version catalog centralizzato
```

### Navigazione UI (shared)

- **Tab inferiore:** Home · Info dispositivo · Impostazioni
- **Drawer laterale:** App installate · PenTest · Permessi · Attività · Root · Trick · Log

---

## Stack tecnico

| Layer | Librerie |
|---|---|
| UI | Compose Multiplatform, Material3 |
| DI | Koin (core, Android, Compose) |
| Async | Kotlin Coroutines + Flow |
| Database locale | SQLDelight (Android driver / SQLite JVM driver) |
| Sicurezza Android | RootBeer, Play Integrity API |
| Preferenze | DataStore Preferences |
| Logging | Kermit |
| Test | kotlin-test, MockK, kotlinx-coroutines-test |

---

## Note iOS

Il modulo iOS è incluso per condividere UI e logica. Le funzionalità dipendenti da API Android (Root, ADB, Play Integrity, APK export) non sono disponibili su iOS e vengono rimpiazzate da stub.
