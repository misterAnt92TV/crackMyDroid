# CrackMyDroid — Schermate UI / UI Screens

> Documentazione visiva delle schermate principali dell'app.
> Visual documentation of the main app screens.
>
> *Nota: le schermate seguenti sono mockup ASCII che riproducono fedelmente la UI Compose Multiplatform. Le schermate reali possono differire leggermente in base al tema e al device.*

---

## Indice / Table of Contents

1. [Navigazione generale](#1-navigazione-generale--general-navigation)
2. [Home](#2-home)
3. [Info dispositivo](#3-info-dispositivo--device-info)
4. [Root / Play Integrity](#4-root--play-integrity)
5. [PenTesting (Wizard 4-step)](#5-pentesting-wizard-4-step)
6. [App installate](#6-app-installate--installed-apps)
7. [Attività (Activities)](#7-attività--activities)
8. [Permessi](#8-permessi--permissions)
9. [Shell / Trick](#9-shell--trick)
10. [Log](#10-log)
11. [Impostazioni](#11-impostazioni--settings)

---

## 1. Navigazione generale / General Navigation

### Struttura dell'app

L'app usa un **Bottom Tab Bar** per le sezioni principali e un **Navigation Drawer** per le funzionalità avanzate.

```
┌─────────────────────────────────────────────────┐
│  ●  CrackMyDroid          [?]  [≡]              │  ← SectionHeader
├─────────────────────────────────────────────────┤
│                                                 │
│              [  Contenuto schermata  ]          │
│                                                 │
│                                                 │
│                                                 │
├─────────────────────────────────────────────────┤
│   🏠 Home    📱 Info    ⚙️ Settings              │  ← Bottom Tab Bar
└─────────────────────────────────────────────────┘
```

### Navigation Drawer (aperto dal pulsante ≡)

```
┌──────────────────────────────┐
│ ╔══════════════════════════╗ │
│ ║   [●]  CrackMyDroid     ║ │
│ ║   Strumenti sicurezza   ║ │
│ ╚══════════════════════════╝ │
│                              │
│  📦  App installate          │
│  🔬  PenTesting              │
│  🔑  Permessi                │
│  📋  Attività                │
│  🛡️  Root / Play Integrity   │
│  💻  Shell / Trick           │
│  📄  Log                     │
│                              │
└──────────────────────────────┘
```

---

## 2. Home

La schermata di benvenuto mostra le ultime operazioni eseguite e una sezione FAQ espandibile.

```
┌─────────────────────────────────────────────────┐
│ ≡  Home                                    [?]  │
├─────────────────────────────────────────────────┤
│                                                 │
│  ╔═══════════════════════════════════════════╗  │
│  ║  ╭────────────╮                          ║  │
│  ║  │  [🔐 icon] │   CrackMyDroid           ║  │
│  ║  ╰────────────╯   Mobile Security Tool   ║  │
│  ╚═══════════════════════════════════════════╝  │
│                                                 │
│  Ultime operazioni                              │
│  ╔═════════════════════════════════════════╗    │
│  ║  ✅  Analisi manifest completata        ║    │
│  ║      com.example.app                   ║    │
│  ╠═════════════════════════════════════════╣    │
│  ║  ❌  Permessi critici rilevati          ║    │
│  ║      com.dangerous.app — 3 critici     ║    │
│  ╠═════════════════════════════════════════╣    │
│  ║  ✅  Export APK completato              ║    │
│  ║      /sdcard/Download/myapp.apk        ║    │
│  ╚═════════════════════════════════════════╝    │
│                                                 │
│  Cos'è CrackMyDroid?                           │
│  CrackMyDroid è uno strumento per pentest...   │
│                                                 │
│  FAQ                                            │
│  ╔═════════════════════════════════════════╗    │
│  ║ ▶  Serve il root?                 [↓]  ║    │
│  ╠═════════════════════════════════════════╣    │
│  ║ ▶  Funziona su emulatori?         [↓]  ║    │
│  ╠═════════════════════════════════════════╣    │
│  ║ ▶  I dati vengono inviati online?  [↓]  ║    │
│  ╚═════════════════════════════════════════╝    │
│                                                 │
├─────────────────────────────────────────────────┤
│   🏠 Home    📱 Info    ⚙️ Settings              │
└─────────────────────────────────────────────────┘
```

**Elementi interattivi:**
- `[≡]` apre il Navigation Drawer
- `[?]` mostra un dialog di aiuto contestuale
- Le righe delle operazioni sono cliccabili per dettagli
- Gli accordions FAQ si espandono/contraggono con animazione

---

## 3. Info dispositivo / Device Info

Panoramica completa dell'hardware e software del device, con ogni riga copiabile.

```
┌─────────────────────────────────────────────────┐
│ ≡  Info dispositivo                        [?]  │
├─────────────────────────────────────────────────┤
│                                                 │
│  ╔═════════════════════════════════════════╗    │
│  ║  📱  Dispositivo              [📋 Copia]║    │
│  ╠═════════════════════════════════════════╣    │
│  ║  Produttore     Google                  ║    │
│  ║  Modello        Pixel 7                 ║    │
│  ║  Brand          google                  ║    │
│  ║  Serial         2B041JEG209053          ║    │
│  ║  IMEI           ●●●●●●●●●●●●●●●        ║    │
│  ╠═════════════════════════════════════════╣    │
│  ║  🖥️  Sistema                            ║    │
│  ╠═════════════════════════════════════════╣    │
│  ║  Android        14 (API 34)             ║    │
│  ║  Patch sicurezza 2024-10-05             ║    │
│  ║  Build ID       UQ1A.240405.004         ║    │
│  ║  Fingerprint    google/panther/…        ║    │
│  ║  Kernel         5.15.110-android14…    ║    │
│  ║  Bootloader     slider-1.2-10153280    ║    │
│  ╠═════════════════════════════════════════╣    │
│  ║  📶  Connessione e debug                ║    │
│  ╠═════════════════════════════════════════╣    │
│  ║  Rete           WiFi (ssid: Home_5G)   ║    │
│  ║  USB Debug      ✅ Attivo              ║    │
│  ║  Dev Options    ✅ Attive              ║    │
│  ║  App debuggable ❌ No                  ║    │
│  ║  Root           ❌ Non rilevato        ║    │
│  ╠═════════════════════════════════════════╣    │
│  ║  💾  Display e memoria                  ║    │
│  ╠═════════════════════════════════════════╣    │
│  ║  Schermo        1080 x 2400 px         ║    │
│  ║  Densità        420 dpi                ║    │
│  ║  RAM totale     7823 MB                ║    │
│  ║  RAM libera     2341 MB                ║    │
│  ║  Batteria       87%                    ║    │
│  ╚═════════════════════════════════════════╝    │
│                                                 │
│  ╔═════════════════════════════════════════╗    │
│  ║  System properties                      ║    │
│  ║  ┌───────────────────────────────────┐  ║    │
│  ║  │ ro.build.version.sdk=34           │  ║    │
│  ║  │ ro.product.manufacturer=Google    │  ║    │
│  ║  │ ro.debuggable=0                   │  ║    │
│  ║  │ ...                               │  ║    │
│  ║  └───────────────────────────────────┘  ║    │
│  ╚═════════════════════════════════════════╝    │
│                                              ▲  │
│                                             [↑] │
├─────────────────────────────────────────────────┤
│   🏠 Home    📱 Info    ⚙️ Settings              │
└─────────────────────────────────────────────────┘
```

**Elementi interattivi:**
- Ogni riga `InfoRow` è cliccabile → copia "Label: Valore" negli appunti
- `[📋 Copia]` nella sezione Dispositivo → copia il report completo
- `[↑]` FAB scroll-to-top (appare dopo scroll)
- System properties: area a scorrimento verticale con testo monospace selezionabile

---

## 4. Root / Play Integrity

```
┌─────────────────────────────────────────────────┐
│ ←  Root / Play Integrity                   [?]  │
├─────────────────────────────────────────────────┤
│                                                 │
│  ╔═════════════════════════════════════════╗    │
│  ║  🛡️  Root Detection                    ║    │
│  ╠═════════════════════════════════════════╣    │
│  ║                                         ║    │
│  ║  Stato root:  ✅ Non rilevato           ║    │
│  ║                                         ║    │
│  ║  Indicatori verificati:                 ║    │
│  ║  ────────────────────────────────────   ║    │
│  ║  ✅  Binario su/system/bin/su          ║    │
│  ║  ✅  Binario su/xbin/su               ║    │
│  ║  ✅  App RootBeer                      ║    │
│  ║  ✅  Test-keys build                   ║    │
│  ║  ✅  Dangerous props                   ║    │
│  ║                                         ║    │
│  ║  [ Aggiorna verifica root ]             ║    │
│  ╚═════════════════════════════════════════╝    │
│                                                 │
│  ╔═════════════════════════════════════════╗    │
│  ║  🔏  Play Integrity                    ║    │
│  ╠═════════════════════════════════════════╣    │
│  ║                                         ║    │
│  ║  Nonce (configurabile):                 ║    │
│  ║  ┌─────────────────────────────────┐   ║    │
│  ║  │ abc123def456…                   │   ║    │
│  ║  └─────────────────────────────────┘   ║    │
│  ║                                         ║    │
│  ║  [ Avvia controllo Play Integrity ]     ║    │
│  ║                                         ║    │
│  ║  Risultato: ✅ PASSES_DEVICE_INTEGRITY ║    │
│  ║                                         ║    │
│  ║  MEETS_DEVICE_INTEGRITY     ✅ OK       ║    │
│  ║  MEETS_BASIC_INTEGRITY      ✅ OK       ║    │
│  ║  MEETS_STRONG_INTEGRITY     ✅ OK       ║    │
│  ╚═════════════════════════════════════════╝    │
│                                                 │
├─────────────────────────────────────────────────┤
│   🏠 Home    📱 Info    ⚙️ Settings              │
└─────────────────────────────────────────────────┘
```

---

## 5. PenTesting (Wizard 4-step)

Il PenTesting è organizzato come un wizard a 4 step con una progress card persistente in cima.

### Step 1 — Selezione target

```
┌─────────────────────────────────────────────────┐
│ ←  PenTesting                              [?]  │
├─────────────────────────────────────────────────┤
│  ╔═════════════════════════════════════════╗    │
│  ║  Step 1/4 • Seleziona app              ║    │
│  ║  ████░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░  ║    │
│  ╚═════════════════════════════════════════╝    │
│                                                 │
│  Seleziona un'app da analizzare o avvia         │
│  la scansione automatica su tutte le app.       │
│                                                 │
│  ╔═════════════════════════════════════════╗    │
│  ║  ▶  Ricerca app           [storia ▼]   ║    │
│  ║  ┌─────────────────────────────────┐   ║    │
│  ║  │ 🔍 Cerca pacchetto…         [×] │   ║    │
│  ║  └─────────────────────────────────┘   ║    │
│  ║  ┌──────────────────────────────────┐  ║    │
│  ║  │ com.example  ╳  com.google  ╳   │  ║    │
│  ║  └──────────────────────────────────┘  ║    │
│  ║                                         ║    │
│  ║  ╔──────────────────────────────────╗  ║    │
│  ║  ║  📦  Example App                 ║  ║    │
│  ║  ║      com.example.app             ║  ║    │
│  ║  ║      [ Seleziona ]               ║  ║    │
│  ║  ╚──────────────────────────────────╝  ║    │
│  ║  ╔──────────────────────────────────╗  ║    │
│  ║  ║  📦  Another App                 ║  ║    │
│  ║  ║      com.another.app             ║  ║    │
│  ║  ║      [ Seleziona ]               ║  ║    │
│  ║  ╚──────────────────────────────────╝  ║    │
│  ╚═════════════════════════════════════════╝    │
│                                                 │
│  ╔═════════════════════════════════════════╗    │
│  ║  ✅  Target selezionato               ║    │
│  ║  Example App                           ║    │
│  ║  com.example.app            [ Rimuovi ]║    │
│  ╚═════════════════════════════════════════╝    │
│                                                 │
│  ┌──────────────────┐  ┌───────────────────┐   │
│  │ ▶ Vai ai test    │  │ 🛡 Auto scan tutti │   │
│  └──────────────────┘  └───────────────────┘   │
│                                                 │
├─────────────────────────────────────────────────┤
│   🏠 Home    📱 Info    ⚙️ Settings              │
└─────────────────────────────────────────────────┘
```

### Step 2 — Selezione check

```
┌─────────────────────────────────────────────────┐
│ ←  PenTesting                              [?]  │
├─────────────────────────────────────────────────┤
│  ╔═════════════════════════════════════════╗    │
│  ║  Step 2/4 • Selezione check            ║    │
│  ║  ████████░░░░░░░░░░░░░░░░░░░░░░░░░░░  ║    │
│  ╚═════════════════════════════════════════╝    │
│                                                 │
│  ╔═════════════════════════════════════════╗    │
│  ║  🎯  Target: Example App               ║    │
│  ║      com.example.app                   ║    │
│  ╚═════════════════════════════════════════╝    │
│                                                 │
│  ┌─────────────────┐  ┌──────────────────┐     │
│  │ Seleziona tutti │  │     Svuota       │     │
│  └─────────────────┘  └──────────────────┘     │
│  Test selezionati: 4/5                          │
│                                                 │
│  ╔═════════════════════════════════════════╗    │
│  ║  🔑  Analisi permessi                  ║    │
│  ║  Verifica permessi dichiarati…         ║    │
│  ║  [ ✅ Attivo    ]                      ║    │
│  ╚═════════════════════════════════════════╝    │
│  ╔═════════════════════════════════════════╗    │
│  ║  📄  Analisi manifest                  ║    │
│  ║  Flag di sicurezza nel manifest…       ║    │
│  ║  [ ✅ Attivo    ]                      ║    │
│  ╚═════════════════════════════════════════╝    │
│  ╔═════════════════════════════════════════╗    │
│  ║  📁  File scout          [🔒 Root req.] ║    │
│  ║  Accesso a file sensibili del device…  ║    │
│  ║  [    Seleziona   ]                    ║    │
│  ╚═════════════════════════════════════════╝    │
│  ╔═════════════════════════════════════════╗    │
│  ║  🌐  Network monitor                   ║    │
│  ║  Permessi di rete e dati sensibili…   ║    │
│  ║  [ ✅ Attivo    ]                      ║    │
│  ╚═════════════════════════════════════════╝    │
│  ╔═════════════════════════════════════════╗    │
│  ║  📚  Library scan                      ║    │
│  ║  Rileva librerie con permessi elevati… ║    │
│  ║  [ ✅ Attivo    ]                      ║    │
│  ╚═════════════════════════════════════════╝    │
│                                                 │
│  ┌──────────────┐  ┌─────────────────────────┐ │
│  │  ← Indietro  │  │  ▶ Esegui test (4/5)    │ │
│  └──────────────┘  └─────────────────────────┘ │
│                                                 │
├─────────────────────────────────────────────────┤
│   🏠 Home    📱 Info    ⚙️ Settings              │
└─────────────────────────────────────────────────┘
```

### Step 3 — Esecuzione test

```
┌─────────────────────────────────────────────────┐
│ ←  PenTesting                              [?]  │
├─────────────────────────────────────────────────┤
│  ╔═════════════════════════════════════════╗    │
│  ║  Step 3/4 • Esecuzione                 ║    │
│  ║  ████████████████░░░░░░░░░░░░░░░░░░░  ║    │
│  ╚═════════════════════════════════════════╝    │
│                                                 │
│  ╔═════════════════════════════════════════╗    │
│  ║  Completati 4/4 test                   ║    │
│  ║  ████████████████████████████████████  ║    │
│  ║  Anomalie trovate: 2  •  Verifiche OK: 2 ║  │
│  ╚═════════════════════════════════════════╝    │
│                                                 │
│  ╔═════════════════════════════════════════╗    │
│  ║  [←]  Modifica test   [▶]  Riesegui    ║    │
│  ║  [📤]  Vai al report                   ║    │
│  ╚═════════════════════════════════════════╝    │
│                                                 │
│  ╔═════════════════════════════════════════╗    │
│  ║  ❌  Analisi permessi                  ║    │
│  ║      com.example.app • ANOMALIA        ║    │
│  ║      Permessi critici: CAMERA, MIC…    ║    │
│  ╠═════════════════════════════════════════╣    │
│  ║  ✅  Analisi manifest                  ║    │
│  ║      com.example.app • OK              ║    │
│  ╠═════════════════════════════════════════╣    │
│  ║  ❌  Network monitor                   ║    │
│  ║      com.example.app • ANOMALIA        ║    │
│  ║      INTERNET + accesso SMS            ║    │
│  ╠═════════════════════════════════════════╣    │
│  ║  ✅  Library scan                      ║    │
│  ║      com.example.app • OK              ║    │
│  ╚═════════════════════════════════════════╝    │
│                                                 │
├─────────────────────────────────────────────────┤
│   🏠 Home    📱 Info    ⚙️ Settings              │
└─────────────────────────────────────────────────┘
```

### Step 4 — Report

```
┌─────────────────────────────────────────────────┐
│ ←  PenTesting                              [?]  │
├─────────────────────────────────────────────────┤
│  ╔═════════════════════════════════════════╗    │
│  ║  Step 4/4 • Report                     ║    │
│  ║  ████████████████████████████████████  ║    │
│  ╚═════════════════════════════════════════╝    │
│                                                 │
│  ╔═════════════════════════════════════════╗    │
│  ║  📊  Report PenTest              [↕]   ║    │
│  ║  ─────────────────────────────────────  ║    │
│  ║  [📅 Esporta TXT]  [📤 Condividi]       ║    │
│  ║                                         ║    │
│  ║  Stato: Completato • 2/4 OK • 2 errori ║    │
│  ║  Target: com.example.app               ║    │
│  ║                                         ║    │
│  ║  ❌  Analisi permessi    ANOMALIA       ║    │
│  ║  ✅  Analisi manifest    OK             ║    │
│  ║  ❌  Network monitor     ANOMALIA       ║    │
│  ║  ✅  Library scan        OK             ║    │
│  ╚═════════════════════════════════════════╝    │
│                                                 │
│  ╔═════════════════════════════════════════╗    │
│  ║  📁  Report salvati                    ║    │
│  ╠═════════════════════════════════════════╣    │
│  ║  Report #1                    [📤 Share]║    │
│  ║  SingleTarget • com.example.app        ║    │
│  ║  Test: 4 • OK: 3 • Errori: 1           ║    │
│  ║  • Analisi permessi: OK                ║    │
│  ║  • Manifest: ANOMALIA                  ║    │
│  ║  • ...                                 ║    │
│  ╠═════════════════════════════════════════╣    │
│  ║  Report #2                    [📤 Share]║    │
│  ║  AutoAllApps • 47 app analizzate       ║    │
│  ║  Test: 188 • OK: 152 • Errori: 36      ║    │
│  ╚═════════════════════════════════════════╝    │
│                                                 │
│  ┌───────────────────┐  ┌────────────────────┐ │
│  │ ← Torna esecuzione│  │  ▶  Nuova run      │ │
│  └───────────────────┘  └────────────────────┘ │
│                                                 │
├─────────────────────────────────────────────────┤
│   🏠 Home    📱 Info    ⚙️ Settings              │
└─────────────────────────────────────────────────┘
```

### Auto-scan in corso (Step 3, modalità AutoAllApps)

```
┌─────────────────────────────────────────────────┐
│  ╔═════════════════════════════════════════╗    │
│  ║  App 12/47  •  Controlli 58/235         ║    │
│  ║  ████████░░░░░░░░░░░░░░░░░░░░░░░░░░░  ║    │
│  ║  📦  com.banking.app                   ║    │
│  ╚═════════════════════════════════════════╝    │
│                                                 │
│  ╔═════════════════════════════════════════╗    │
│  ║  App analizzate: 11                    ║    │
│  ║  App con anomalie: 4                   ║    │
│  ║  ─────────────────────────────────────  ║    │
│  ║  • com.dangerous.app: 3 anomalie/4     ║    │
│  ║  • com.leaky.app: 2 anomalie/4         ║    │
│  ║  • com.risky.app: 1 anomalia/4         ║    │
│  ║  • com.old.app: 1 anomalia/4           ║    │
│  ║  ─────────────────────────────────────  ║    │
│  ║  [📤 Condividi risultato parziale]      ║    │
│  ╚═════════════════════════════════════════╝    │
│                                                 │
│  [ ⏹ Interrompi scansione automatica ]         │
└─────────────────────────────────────────────────┘
```

---

## 6. App installate / Installed Apps

```
┌─────────────────────────────────────────────────┐
│ ←  App installate     [🔄 Refresh]  [📤 Share]  │
├─────────────────────────────────────────────────┤
│  Elenca e analizza le app installate.           │
│  Estrai APK e verifica la sicurezza manifest.   │
│                                                 │
│  ╔═════════════════════════════════════════╗    │
│  ║ ▶ Cerca app    Preferiti: 3  [cerca ▼] ║    │
│  ║ ┌─────────────────────────────────┐    ║    │
│  ║ │ 🔍 Nome o package…          [×] │    ║    │
│  ║ └─────────────────────────────────┘    ║    │
│  ║ Ricerca frequente: com.google          ║    │
│  ║ ┌─────────────────────────────────┐    ║    │
│  ║ │ com.google ✕  banking ✕         │    ║    │
│  ║ └─────────────────────────────────┘    ║    │
│  ╚═════════════════════════════════════════╝    │
│                                                 │
│  Risultati: 47 / 312                            │
│                                                 │
│  ╔═════════════════════════════════════════╗    │
│  ║ ▶  [📦] Banking App   [📤] [★]   [↓]  ║    │
│  ╠════════════════════════════════════════ ╣    │
│  ║  com.banking.app                        ║    │
│  ║  ─────────────────────────────────────  ║    │
│  ║  APK: /data/app/com.banking.app/…      ║    │
│  ║                                         ║    │
│  ║  ┌─────────────────┐  ┌──────────────┐ ║    │
│  ║  │  📦 Estrai APK  │  │ 📤 Condividi │ ║    │
│  ║  └─────────────────┘  └──────────────┘ ║    │
│  ║  ┌──────────────────────────────────┐  ║    │
│  ║  │    📶 Condividi Bluetooth        │  ║    │
│  ║  └──────────────────────────────────┘  ║    │
│  ╚═════════════════════════════════════════╝    │
│                                                 │
│  ╔═════════════════════════════════════════╗    │
│  ║ ▶  [📦] Gmail        [📤] [★]   [↓]  ║    │
│  ╠═════════════════════════════════════════╣    │
│  ║ ▶  [📦] Maps         [📤] [⭐]  [↓]  ║    │
│  ╠═════════════════════════════════════════╣    │
│  ║ ▶  [📦] Camera       [📤] [★]   [↓]  ║    │
│  ╚═════════════════════════════════════════╝    │
│                                              ▲  │
│                                             [↑] │
├─────────────────────────────────────────────────┤
│   🏠 Home    📱 Info    ⚙️ Settings              │
└─────────────────────────────────────────────────┘
```

### Analisi sicurezza manifest (espansa)

```
│  ╔═════════════════════════════════════════╗    │
│  ║ ▼  [📦] Dangerous App  [📤] [★]   [↑] ║    │
│  ╠═════════════════════════════════════════╣    │
│  ║  com.dangerous.app                      ║    │
│  ║  ─────────────────────────────────────  ║    │
│  ║  🔴 RISCHIO CRITICO  Score: 13          ║    │
│  ║                                         ║    │
│  ║  🔴 android:debuggable=true             ║    │
│  ║     L'app è compilata in debug mode.   ║    │
│  ║  🟠 allowBackup=true                   ║    │
│  ║     I dati possono essere estratti…   ║    │
│  ║  🔴 ContentProvider esposto (no perm.) ║    │
│  ║     com.dangerous.app.DataProvider     ║    │
│  ║                                         ║    │
│  ║  APK: /data/app/com.dangerous.app/…   ║    │
│  ║  ┌─────────────────┐  ┌──────────────┐ ║    │
│  ║  │  📦 Estrai APK  │  │ 📤 Condividi │ ║    │
│  ║  └─────────────────┘  └──────────────┘ ║    │
│  ╚═════════════════════════════════════════╝    │
```

---

## 7. Attività / Activities

```
┌─────────────────────────────────────────────────┐
│ ←  Attività                    [🔄 Refresh]     │
├─────────────────────────────────────────────────┤
│                                                 │
│  ╔═════════════════════════════════════════╗    │
│  ║ ▶ Filtri e ricerca   Preferiti: 5 [▼] ║    │
│  ║ ┌─────────────────────────────────┐    ║    │
│  ║ │ 🔍 Nome activity o package… [×] │    ║    │
│  ║ └─────────────────────────────────┘    ║    │
│  ╚═════════════════════════════════════════╝    │
│                                                 │
│  Risultati: 23 / 156 attività                   │
│                                                 │
│  ╔═════════════════════════════════════════╗    │
│  ║ ▶  [📦] Example App   [📋] [📤] [★] [↓]║    │
│  ║      com.example.app  •  5 act          ║    │
│  ╠═════════════════════════════════════════╣    │
│  ║ ▼  [📦] Banking App   [📋] [📤] [⭐] [↑]║   │
│  ║      com.banking.app  •  8 act          ║    │
│  ║  ─────────────────────────────────────  ║    │
│  ║  MainActivity                     [⭐]  ║    │
│  ║  .MainActivity  •  am start          ──║────▶ cliccabile
│  ║                                         ║    │
│  ║  LoginActivity                    [★]   ║    │
│  ║  .LoginActivity  •  am start             ║   │
│  ║                                         ║    │
│  ║  DeepLinkActivity              [⚠️][★]  ║    │
│  ║  .DeepLinkActivity  •  intent required  ║    │
│  ║  Hint: Richiede data URI per il lancio  ║    │
│  ║                                         ║    │
│  ║  AdminActivity             [dump only]  ║    │
│  ║  .AdminActivity  •  Non avviabile       ║    │
│  ║  Richiede signature permission          ║    │
│  ╠═════════════════════════════════════════╣    │
│  ║ ▶  [📦] Social App    [📋] [📤] [★] [↓]║    │
│  ╚═════════════════════════════════════════╝    │
│                                              ▲  │
│                                             [↑] │
├─────────────────────────────────────────────────┤
│   🏠 Home    📱 Info    ⚙️ Settings              │
└─────────────────────────────────────────────────┘
```

### Dialog lancio guidato (intent richiesto)

```
┌─────────────────────────────────────────────────┐
│  ╔═════════════════════════════════════════╗    │
│  ║  🚀  Lancia attività                   ║    │
│  ║  DeepLinkActivity                      ║    │
│  ╠═════════════════════════════════════════╣    │
│  ║                                         ║    │
│  ║  Intent action:                         ║    │
│  ║  ┌─────────────────────────────────┐   ║    │
│  ║  │ android.intent.action.VIEW      │   ║    │
│  ║  └─────────────────────────────────┘   ║    │
│  ║                                         ║    │
│  ║  Data URI:                              ║    │
│  ║  ┌─────────────────────────────────┐   ║    │
│  ║  │ https://example.com/deep        │   ║    │
│  ║  └─────────────────────────────────┘   ║    │
│  ║                                         ║    │
│  ║  MIME type (opzionale):                 ║    │
│  ║  ┌─────────────────────────────────┐   ║    │
│  ║  │                                 │   ║    │
│  ║  └─────────────────────────────────┘   ║    │
│  ║                                         ║    │
│  ║        [ Annulla ]   [ Avvia ]          ║    │
│  ╚═════════════════════════════════════════╝    │
└─────────────────────────────────────────────────┘
```

---

## 8. Permessi / Permissions

```
┌─────────────────────────────────────────────────┐
│ ←  Permessi                    [🔄 Refresh]     │
├─────────────────────────────────────────────────┤
│  Analizza i permessi delle app installate       │
│  e classifica il rischio per categoria.         │
│                                                 │
│  ╔═════════════════════════════════════════╗    │
│  ║  🔍  Scansione automatica               ║    │
│  ║  ─────────────────────────────────────  ║    │
│  ║  ┌───────────────────┐  ┌────────────┐ ║    │
│  ║  │ ⚠️ Avvia scan      │  │ ⏹ Interr. │ ║    │
│  ║  └───────────────────┘  └────────────┘ ║    │
│  ║                                         ║    │
│  ║  ████████████████████░░░░░░░░░░░░░░░  ║    │
│  ║  39/47 app  •  📦  com.banking.app     ║    │
│  ║  9 app a rischio • Critici 3 • Alti 5  ║    │
│  ╚═════════════════════════════════════════╝    │
│                                                 │
│  ╔═════════════════════════════════════════╗    │
│  ║ ▶ Ricerca    Rischio: 9  Pref: 2  [▼] ║    │
│  ║ ┌─────────────────────────────────┐    ║    │
│  ║ │ 🔍 Nome permesso o package…     │    ║    │
│  ║ └─────────────────────────────────┘    ║    │
│  ║  [✅ Solo app a rischio]  [Reset]       ║    │
│  ║  ┌──────────────────────────────────┐  ║    │
│  ║  │ com.banking ✕  com.social ✕      │  ║    │
│  ║  └──────────────────────────────────┘  ║    │
│  ╚═════════════════════════════════════════╝    │
│                                                 │
│  Risultati: 9 / 47                              │
│                                                 │
│  ╔═════════════════════════════════════════╗    │
│  ║ ▼  [📦] Dangerous App   [📤] [★]  [↑] ║    │
│  ║      com.dangerous.app  •  12 perm      ║    │
│  ║  ─────────────────────────────────────  ║    │
│  ║  🔴 Rischio CRITICO  •  2 critici       ║    │
│  ║                                         ║    │
│  ║  🔴  RECORD_AUDIO  •  Registrazione     ║    │
│  ║      audio ambientale                   ║    │
│  ║  🔴  READ_SMS  •  Lettura messaggi      ║    │
│  ║  🟠  READ_CONTACTS  •  Rubrica          ║    │
│  ║  🟠  ACCESS_FINE_LOCATION  •  GPS       ║    │
│  ║  🟡  CAMERA  •  Fotocamera              ║    │
│  ║  🟡  READ_EXTERNAL_STORAGE             ║    │
│  ║  ···  6 altri permessi                  ║    │
│  ╠═════════════════════════════════════════╣    │
│  ║ ▶  [📦] Banking App     [📤] [★]  [↓] ║    │
│  ║      com.banking.app  •  8 perm  1 crit║    │
│  ╚═════════════════════════════════════════╝    │
│                                              ▲  │
│                                             [↑] │
├─────────────────────────────────────────────────┤
│   🏠 Home    📱 Info    ⚙️ Settings              │
└─────────────────────────────────────────────────┘
```

**Legenda icone permesso:**
```
🎵  Audio/Mic          📍  Location        📷  Camera
📞  Phone/Call         👥  Contacts        💬  SMS
📶  WiFi/Bluetooth     💾  Storage         📅  Calendar
☁️  Cloud/Internet     🔒  Fingerprint/Bio  🔔  Notifications
```

---

## 9. Shell / Trick

```
┌─────────────────────────────────────────────────┐
│ ←  Shell / Trick                           [?]  │
├─────────────────────────────────────────────────┤
│  Comandi ADB e shell preconfigurati.            │
│  Alcuni richiedono il root sul device.          │
│                                                 │
│  ╔═════════════════════════════════════════╗    │
│  ║  📋  Dump package info                  ║    │
│  ║  adb shell dumpsys package <pkg>        ║    │
│  ║  [  ▶ Esegui  ]                        ║    │
│  ╠═════════════════════════════════════════╣    │
│  ║  📋  Lista attività esportate           ║    │
│  ║  adb shell pm dump <pkg> | grep …      ║    │
│  ║  [  ▶ Esegui  ]                        ║    │
│  ╠═════════════════════════════════════════╣    │
│  ║  🔒  Dump keystore [ROOT]               ║    │
│  ║  adb shell ls /data/misc/keystore/      ║    │
│  ║  ⚠️  Richiede root                      ║    │
│  ║  [  ▶ Esegui  ]                        ║    │
│  ╠═════════════════════════════════════════╣    │
│  ║  📋  Screencap                          ║    │
│  ║  adb exec-out screencap -p > out.png   ║    │
│  ║  [  ▶ Esegui  ]                        ║    │
│  ╚═════════════════════════════════════════╝    │
│                                                 │
│  ╔═════════════════════════════════════════╗    │
│  ║  Output  (exit code: 0)                ║    │
│  ║  ─────────────────────────────────────  ║    │
│  ║  Activity Resolver Table:               ║    │
│  ║    Non-Data Actions:                    ║    │
│  ║      android.intent.action.MAIN:       ║    │
│  ║        2e91c76 com.example/.MainActivity║    │
│  ║        …                               ║    │
│  ╚═════════════════════════════════════════╝    │
│                                                 │
├─────────────────────────────────────────────────┤
│   🏠 Home    📱 Info    ⚙️ Settings              │
└─────────────────────────────────────────────────┘
```

### Dialog di conferma (comandi distruttivi)

```
┌───────────────────────────────────┐
│  ⚠️  Conferma esecuzione          │
│  ────────────────────────────     │
│  Stai per eseguire:               │
│                                   │
│  adb shell pm clear <package>     │
│                                   │
│  Questa operazione cancellerà     │
│  tutti i dati dell'app.           │
│                                   │
│     [ Annulla ]    [ Esegui ]     │
└───────────────────────────────────┘
```

---

## 10. Log

```
┌─────────────────────────────────────────────────┐
│ ←  Log operazioni                          [?]  │
├─────────────────────────────────────────────────┤
│                                                 │
│  ┌──────────────────────────────────────────┐  │
│  │ [📅 Esporta log]                         │  │
│  └──────────────────────────────────────────┘  │
│                                                 │
│  ╔═════════════════════════════════════════╗    │
│  ║  ✅  [10:23:45]  Manifest analizzato    ║    │
│  ║      com.example.app  •  Score: LOW     ║    │
│  ╠═════════════════════════════════════════╣    │
│  ║  ❌  [10:21:30]  PenTest fallito        ║    │
│  ║      com.dangerous.app  •  2 anomalie   ║    │
│  ╠═════════════════════════════════════════╣    │
│  ║  ✅  [10:18:12]  APK esportato          ║    │
│  ║      /sdcard/Download/example.apk       ║    │
│  ╠═════════════════════════════════════════╣    │
│  ║  ✅  [10:15:00]  Root check completato  ║    │
│  ║      Risultato: Non rilevato            ║    │
│  ╠═════════════════════════════════════════╣    │
│  ║  ✅  [10:12:45]  Permessi analizzati    ║    │
│  ║      com.banking.app  •  8 permessi     ║    │
│  ╚═════════════════════════════════════════╝    │
│                                                 │
├─────────────────────────────────────────────────┤
│   🏠 Home    📱 Info    ⚙️ Settings              │
└─────────────────────────────────────────────────┘
```

---

## 11. Impostazioni / Settings

```
┌─────────────────────────────────────────────────┐
│ ≡  Impostazioni                            [?]  │
├─────────────────────────────────────────────────┤
│                                                 │
│  ╔═════════════════════════════════════════╗    │
│  ║  🎨  Tema                               ║    │
│  ╠═════════════════════════════════════════╣    │
│  ║  [✓ Chiaro    ]  [  Scuro    ]          ║    │
│  ║  [  Sistema   ]                         ║    │
│  ╚═════════════════════════════════════════╝    │
│                                                 │
│  ╔═════════════════════════════════════════╗    │
│  ║  ⚙️  Lingua / Language                  ║    │
│  ╠═════════════════════════════════════════╣    │
│  ║  Seleziona la lingua dell'interfaccia.  ║    │
│  ║  [✓ Italiano  ]  [  English  ]          ║    │
│  ╚═════════════════════════════════════════╝    │
│                                                 │
│  ╔═════════════════════════════════════════╗    │
│  ║  📜  Formato export                     ║    │
│  ╠═════════════════════════════════════════╣    │
│  ║  [✓ TXT  ]  [  JSON  ]                 ║    │
│  ╚═════════════════════════════════════════╝    │
│                                                 │
│  ╔═════════════════════════════════════════╗    │
│  ║  🔍  Suggerimenti di ricerca            ║    │
│  ╠═════════════════════════════════════════╣    │
│  ║  Abilita suggerimenti  ────────── [✓●]  ║    │
│  ║  Salva la cronologia di ricerca…        ║    │
│  ║  [🕐 Cancella suggerimenti]             ║    │
│  ╚═════════════════════════════════════════╝    │
│                                                 │
│  ╔═════════════════════════════════════════╗    │
│  ║  🐛  Log verboso                        ║    │
│  ╠═════════════════════════════════════════╣    │
│  ║  Modalità verbose  ──────────── [ ○]    ║    │
│  ║  Attiva per log di debug dettagliati.   ║    │
│  ╚═════════════════════════════════════════╝    │
│                                                 │
│  ╔═════════════════════════════════════════╗    │
│  ║  ♿  Accessibilità                      ║    │
│  ╠═════════════════════════════════════════╣    │
│  ║  Alto contrasto  ──────────── [ ○]      ║    │
│  ║  Animazioni ridotte  ──────── [✓●]      ║    │
│  ║  Testo grande  ──────────── [ ○]        ║    │
│  ╚═════════════════════════════════════════╝    │
│                                                 │
│  ╔═════════════════════════════════════════╗    │
│  ║  ℹ️  Informazioni app                   ║    │
│  ╠═════════════════════════════════════════╣    │
│  ║  Versione       1.0.0-beta              ║    │
│  ║  Package        com.crackmydroid        ║    │
│  ║  SDK min/target 24 / 34                 ║    │
│  ╚═════════════════════════════════════════╝    │
│                                                 │
│  ╔═════════════════════════════════════════╗    │
│  ║  ⚠️  Disclaimer                         ║    │
│  ╠═════════════════════════════════════════╣    │
│  ║  Questo strumento è destinato           ║    │
│  ║  esclusivamente a scopi educativi e di  ║    │
│  ║  test su dispositivi di proprietà…     ║    │
│  ╚═════════════════════════════════════════╝    │
│                                              ▲  │
│                                             [↑] │
├─────────────────────────────────────────────────┤
│   🏠 Home    📱 Info    ⚙️ Settings              │
└─────────────────────────────────────────────────┘
```

---

## Desktop App — Schermate specifiche

### Device Picker

La schermata iniziale della desktop app per selezionare il device Android via ADB.

```
┌─────────────────────────────────────────────────────────────────┐
│                     CrackMyDroid Desktop                        │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│                  ┌─────────────────────────┐                   │
│                  │  [🔐]  CrackMyDroid     │                   │
│                  │   Desktop Edition       │                   │
│                  └─────────────────────────┘                   │
│                                                                 │
│   Seleziona un device Android connesso via ADB                  │
│                                                                 │
│   ┌────────────────────────────────────────────────────────┐   │
│   │ Percorso ADB (opzionale):                              │   │
│   │ ┌────────────────────────────────────────────────────┐ │   │
│   │ │ /usr/local/bin/adb                                 │ │   │
│   │ └────────────────────────────────────────────────────┘ │   │
│   └────────────────────────────────────────────────────────┘   │
│                                                                 │
│          [ 🔄 Rileva device ]                                   │
│                                                                 │
│   Device rilevati:                                              │
│   ╔════════════════════════════════════════════════════╗        │
│   ║  📱  Pixel 7                                        ║        │
│   ║      Serial: 2B041JEG209053  •  🟢 device           ║        │
│   ║                          [ Connetti ]               ║        │
│   ╠════════════════════════════════════════════════════╣        │
│   ║  📱  Samsung Galaxy S24                             ║        │
│   ║      Serial: R5CT801G3YT  •  🟢 device              ║        │
│   ║                          [ Connetti ]               ║        │
│   ╚════════════════════════════════════════════════════╝        │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

### Toolbar Desktop (dopo connessione)

```
┌─────────────────────────────────────────────────────────────────┐
│  📱 Pixel 7 (2B041JEG209053)  │ 🟢 Live  │ 10:24:32  │ [🔄][⚙️][⇄]│
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│               [  UI identica all'app Android  ]                 │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘

Legenda toolbar:
  📱 Pixel 7          → modello device
  (2B041JEG209053)    → serial number
  🟢 Live             → dati live (snapshot aggiornato)
  10:24:32            → timestamp ultimo snapshot
  [🔄]                → forza rescan / pull nuovo snapshot
  [⚙️]                → impostazioni ADB (percorso binario)
  [⇄]                 → cambia device (torna al picker)
```

### Stato "Snapshot non disponibile"

```
┌─────────────────────────────────────────────────────────────────┐
│  📱 Unknown Device (emulator-5554)  │ 🟡 Snapshot non dispon.  │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  ╔═══════════════════════════════════════════════════════╗      │
│  ║  ⚠️  Snapshot non disponibile                         ║      │
│  ║                                                       ║      │
│  ║  L'app Android non è installata o non ha ancora       ║      │
│  ║  generato il file di snapshot.                        ║      │
│  ║                                                       ║      │
│  ║  1. Installa CrackMyDroid sul device                  ║      │
│  ║  2. Avvia l'app e attendi qualche secondo             ║      │
│  ║  3. Clicca "Aggiorna" qui sotto                       ║      │
│  ║                                                       ║      │
│  ║       [ 🔄 Aggiorna ]   [ ⇄ Cambia device ]          ║      │
│  ╚═══════════════════════════════════════════════════════╝      │
│                                                                 │
│       [informazioni base disponibili via ADB diretto]           │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

---

## Legenda componenti comuni / Common Components Legend

```
COMPONENTI RIUTILIZZABILI (shared/presentation/components/)

╔══════════════════╗   AppCard (ElevatedCard, elevation 4dp, radius 22dp)
║  Contenuto card  ║
╚══════════════════╝

[ Primario  ]          AppButton.Primary  (filled, radius 20dp)
[ Secondario]          AppButton.Secondary (outlined, 1dp border)
[ Tonale    ]          AppButton.Tonal    (elevated, secondaryContainer bg)

▶ Titolo         [↓]   AppAccordion (chiuso)
▼ Titolo         [↑]   AppAccordion (aperto)
  └─ contenuto

● Titolo                SectionHeader (CenterAlignedTopAppBar)
  [←] o [≡]     [?]

┌──────────────────┐
│ 🔍 Cerca…    [×] │   SearchFieldWithHistory (+ FilterChips storia)
└──────────────────┘

Label ──────── [✓●]    Switch ON  (M3 Switch)
Label ────────  [ ○]   Switch OFF

[↑]                    ScrollToTopButton (FAB, appare dopo scroll)

████████░░░░░░░░       LinearProgressIndicator (M3)

🔴 Critical  🟠 High  🟡 Medium  🟢 Low    Risk level badges
```
