# CrackMyDroid — Guida funzionale / Functional guide

Guida ai comportamenti implementati e raggiungibili nell'app, non una roadmap.
Guide to implemented, reachable app behavior, not a roadmap.

| Lingua / Language | Contenuto / Content |
|---|---|
| [Italiano](#italiano) | Scopo, sezioni, percorsi pratici e limiti |
| [English](#english) | Purpose, sections, practical workflows and limitations |
| [Fonti / Sources](#sources) | Collegamenti al codice e alla documentazione tecnica / Code and technical documentation links |

## Diagramma / Diagram

Aprire [functional-flows.drawio](functional-flows.drawio) con Draw.io Desktop usando **File → Apri**,
oppure con diagrams.net usando **File → Apri da → Dispositivo**. Il file è XML non compresso,
modificabile; non occorre caricare il repository né collegarsi a un servizio cloud.
Le schede in basso selezionano queste sei pagine:

Open [functional-flows.drawio](functional-flows.drawio) in Draw.io Desktop using **File → Open**,
or in diagrams.net using **File → Open From → Device**. This editable, uncompressed XML file
does not require uploading the repository or connecting cloud storage.
Use the bottom tabs to select these six pages:

| Italiano | English | Scopo / Purpose |
|---|---|---|
| IT - Mappa funzionale | EN - Feature map | Ingresso, navigazione e sezioni / Entry, navigation and sections |
| IT - Flussi operativi | EN - User flows | Ispezione app, wizard, risultati e report / App inspection, wizard, results and reports |
| IT - Desktop e piattaforme | EN - Desktop and platforms | ADB, snapshot e differenze di piattaforma / ADB, snapshots and platform differences |

**Legenda / Legend:** blu = accesso o sezione / entry or section; giallo = scelta o prerequisito /
decision or prerequisite; verde = risultato / output; rosso = limite o errore / limitation or error.
Le frecce indicano azioni o passaggi, non dipendenze software. I link relativi dei nodi rimandano
a questa guida; la loro risoluzione dipende dal visualizzatore, quindi usare anche l'indice qui sotto.
Arrows represent actions or transitions, not software dependencies. Node links point to this guide;
relative-link resolution depends on the viewer, so the index below is also provided.

<a id="italiano"></a>
## Italiano

[Scopo](#it-purpose) · [Navigazione](#it-navigation) · [Sezioni](#it-sections) ·
[Percorsi pratici](#it-workflows) · [Piattaforme](#it-platforms) · [Limiti](#it-limits)

<a id="it-purpose"></a>
### Scopo e potenzialità attuali

CrackMyDroid raccoglie strumenti di ispezione e diagnostica per **dispositivi Android**.
Permette di censire app e permessi, esaminare punti di ingresso delle app, eseguire checklist
euristiche, controllare indicatori root e produrre report. Il client Desktop lavora su un Android
collegato, non sulle applicazioni installate sul computer. Usare questi strumenti soltanto su
dispositivi e applicazioni per cui si dispone di autorizzazione.

La sua utilità è mettere in relazione inventario, permessi, activity e stato del device per
**orientare le verifiche manuali**, ripetere una checklist e condividere evidenze.
Non è uno scanner che dimostra automaticamente ogni vulnerabilità, né un sistema di correzione.

Termini essenziali:

| Termine | Significato |
|---|---|
| APK | File del pacchetto di un'app Android; estrarlo non equivale a salvare i suoi dati privati. |
| Package | Identificatore dell'app, per esempio `com.example.app`. |
| Manifest | Dichiarazione Android di componenti, permessi e impostazioni dell'app. |
| Activity / intent | Schermata o punto di ingresso Android / messaggio con azione, URI e altri parametri per aprirlo. |
| ADB | Strumento del computer per comunicare con un Android autorizzato al debug. |
| Root | Privilegi amministrativi; trovare un indicatore non dimostra che l'app possa ottenerli. |
| Snapshot | Fotografia serializzata dei dati Android raccolti, non una lettura sempre aggiornata. |

<a id="it-navigation"></a>
### Come orientarsi

All'avvio si entra nella **Home**; può comparire un'introduzione. La barra inferiore porta a
**Home**, **Info** e **Impostazioni**. Il pulsante menu nell'intestazione apre le sezioni avanzate:
**App installate, PenTesting, Permessi, Attività, Root / Play Integrity, Shell / Trick, Log, Aiuto**.
Le sezioni avanzate che espongono la freccia indietro tornano alla Home.

I suggerimenti d'ingresso, quando abilitati e previsti per la sezione, richiedono conferma prima
di aprirla; si possono disattivare. Il pulsante `?` apre l'aiuto contestuale.
**Aiuto** è anche una sezione con collegamenti diretti agli strumenti.
La schermata sorgente `MoreScreen` non è collegata al percorso di navigazione attuale.
Su Desktop, prima di questa navigazione, occorre selezionare un dispositivo.

<a id="it-sections"></a>
### Guida delle sezioni

| Sezione e accesso | Cosa fare | Risultato e vincoli |
|---|---|---|
| **Home** — barra inferiore | Leggere lo scopo e le FAQ; consultare le ultime operazioni e aprire la cronologia; rimuovere una voce con il pulsante dedicato. | Mostra fino a cinque operazioni recenti distinte per descrizione/esito e la cronologia disponibile. È un riepilogo delle operazioni, non il log di sistema né un navigatore verso i report. |
| **Info dispositivo** — barra inferiore | Consultare hardware, sistema, memoria/rete, debug e proprietà; toccare una riga per copiarla; condividere il report dall'intestazione. | Report del device corrente. Campi riservati o non disponibili possono risultare assenti; non garantisce accesso a IMEI/seriale. |
| **Impostazioni** — barra inferiore | Cambiare tema e lingua; gestire suggerimenti di ricerca, loro cancellazione, suggerimenti di sezione, introduzione e log verbosi; scegliere TXT/JSON per le esportazioni che lo supportano; attivare alto contrasto, testo grande e animazioni ridotte; consultare versione e librerie di terze parti. | Personalizza l'esperienza. La sezione librerie elenca le dipendenze runtime open source usate dall'app e apre solo i relativi siti ufficiali; non include strumenti di test/debug. Il formato scelto non trasforma ogni esportazione in JSON: alcuni report hanno un formato proprio. La guida è IT/EN; l'app offre anche altre lingue. |
| **App installate / APK** — menu | Aggiornare; cercare per nome, package o percorso; segnare preferiti; espandere un'app; estrarre o condividere l'APK; condividere l'inventario dall'intestazione. | Mostra nome, package e percorso APK. L'inventario condiviso comprende l'elenco completo caricato, non soltanto il filtro visibile. Su Android è previsto anche il tentativo di condivisione Bluetooth; su Desktop si apre il file/percorso locale. Non è un backup dei dati privati né una garanzia di pacchetto reinstallabile completo per app split-APK. |
| **PenTesting** — menu | Seguire il wizard: **1. target/modalità → 2. controlli → 3. esecuzione → 4. report**. Scegliere una singola app oppure lo scan automatico di tutte le app caricate; leggere avanzamento e dettagli, interrompere l'auto scan, rieseguire, condividere/esportare report e consultare snapshot precedenti. | Checklist euristica basata soprattutto su permessi, rischi delle activity e stato root. La cronologia corrente mantiene fino a 240 eventi; al reset/nuova esecuzione il risultato precedente può essere conservato fra un massimo di 12 snapshot **in memoria del ViewModel**, non un archivio persistente tra riavvii. Leggere i limiti sotto. |
| **Permessi** — menu | Cercare per app/permesso; usare preferiti e filtro delle app a rischio; avviare o annullare la scansione automatica del rischio; espandere i dettagli e condividere il report della singola app. | Classificazione Critical/High/Medium/Low e motivazioni. Si analizzano permessi richiesti/dichiarati: non si dimostra che siano concessi, effettivamente utilizzati o sfruttati malevolmente. |
| **Attività / Activities** — menu | Cercare, espandere i gruppi per package, usare preferiti; consultare report dei rischi e copiarli/esportarli; avviare activity consentite, direttamente o con il dialog guidato per action, URI e MIME type. | Individua punti di ingresso e rischi intent/deep link. Le voci solo informative o protette non diventano avviabili: permessi, esportazione del componente e vincoli del sistema continuano a valere. |
| **Root / Play Integrity** — menu | Aggiornare i controlli passivi, richiedere esplicitamente la verifica di accesso privilegiato, leggere evidenze e condividere il report; inserire un nonce e richiedere Play Integrity. | Distingue indicatori root da accesso `su` dimostrato. Il tentativo privilegiato può richiedere consenso al gestore root o fallire/scadere. Su Android Play Integrity richiede un token ma non decodifica/verifica i verdetti lato server: i campi di integrità restano sconosciuti. |
| **Shell / Trick** — menu | Scegliere un comando predefinito, leggere requisiti e conferma quando prevista; avviarlo e leggere risultato, stdout/stderr ed exit code. | Non è un terminale arbitrario. I comandi non supportati sono segnalati; alcuni richiedono root o privilegi ADB e possono modificare lo stato del dispositivo. Durante un comando non ne viene avviato un altro dalla stessa schermata. |
| **Log** — menu | Aggiornare, filtrare il testo e i livelli, cambiare la colorazione per livello ed esportare. | Log leggibili nel contesto della piattaforma. Android non privilegiato non può necessariamente leggere tutti i log delle altre app; Desktop usa snapshot o logcat via ADB. Distinto dalla cronologia operazioni della Home. |
| **Aiuto** — menu | Leggere il riepilogo degli strumenti e selezionare una sezione. | Guida interna e navigazione diretta; non è una chat o un assistente conversazionale. |

<a id="it-workflows"></a>
### Percorsi pratici

1. **Ispezionare un'app:** menu → App installate → cercare il package → leggere il percorso e,
   se necessario, estrarre l'APK. Aprire poi Permessi e Attività e cercare lo stesso package:
   il passaggio fra sezioni è manuale, non un collegamento automatico con filtro trasferito.
2. **Eseguire una checklist:** menu → PenTesting → scegliere target o auto scan → selezionare
   i controlli → avviare → leggere contatori e dettagli. In auto scan si può interrompere;
   le verifiche non completate non vanno considerate superate. Passare al report o rieseguire.
3. **Conservare evidenze:** dal report PenTesting esportare/condividere il risultato corrente o
   uno snapshot precedente disponibile; dalle altre sezioni usare la rispettiva azione.
   Leggere il percorso restituito o l'eventuale errore. Salvare i report su file se devono
   sopravvivere alla chiusura dell'app; la cronologia PenTesting in memoria non basta.
4. **Lavorare da Desktop:** collegare e autorizzare l'Android per il debug ADB → aprire il client →
   configurare ADB se necessario → aggiornare i dispositivi → selezionare il device →
   controllare sorgente e metadati snapshot nella toolbar → usare le sezioni condivise.
   «Nuova scansione» svuota le cache Desktop e ripete l'importazione; non forza da sola una
   nuova raccolta Android. «Cambia dispositivo» torna alla selezione.

<a id="it-platforms"></a>
### Disponibilità per piattaforma

| Capacità | Android | Desktop con Android collegato | iOS attuale |
|---|---|---|---|
| UI, Home, Aiuto e preferenze | UI condivisa; adapter Android | UI condivisa dopo selezione device | UI condivisa, ma preferenze principali in memoria |
| Inventario, permessi e activity | API Android, con visibilità consentita dal sistema | Sezioni snapshot utilizzabili oppure interrogazioni ADB per i dati non presenti in cache | Repository stub: elenchi vuoti, avvio activity non supportato |
| Info dispositivo | Informazioni Android accessibili | Device Android selezionato, non computer host | Dati segnaposto, non diagnostica iOS completa |
| APK e avvio activity | Operazioni native, soggette ai permessi | Estrazione tramite ADB, avvio remoto dove consentito | Non supportati |
| Root e shell | Controlli locali; comandi limitati dai privilegi | Controlli/comandi sul device via ADB | Root dichiarato non supportato; elenco shell vuoto |
| Play Integrity | Richiesta token; nessuna verifica server dei verdetti | Solo eventuale dato importato; indisponibile in ADB-only. Lo snapshot Android automatico lo marca come non eseguito | Non supportato |
| Log | Log accessibili al processo | Snapshot oppure logcat via ADB | Vuoti; l'export segue il formato report selezionato |
| Report/condivisione | File e condivisione Android dove implementati | File locali; «condividi» rivela/apre il file o la cartella, non invia automaticamente né usa Bluetooth | Non equivale al supporto degli strumenti Android |

Desktop richiede ADB funzionante e un dispositivo accessibile e autorizzato; uno stato offline
o unauthorized non va interpretato come una sessione operativa.
L'importatore tenta di leggere lo snapshot prodotto dall'app Android. Se manca, è incompatibile,
non è leggibile o non contiene sezioni utilizzabili, espone lo stato e usa il percorso ADB.
Uno snapshot parziale può fornire alcune sezioni mentre altre sono recuperate via ADB.
Controllare la data di creazione: «importato» non significa «appena raccolto».

<a id="it-limits"></a>
### Come interpretare risultati ed errori

- **Nessuna anomalia non significa sicurezza dimostrata.** Alcuni controlli possono non produrre
  rilievi anche con dati mancanti: verificare disponibilità e dettagli prima di concludere.
- Nel wizard **Network monitor** valuta combinazioni di permessi, non cattura pacchetti;
  **Library scan** segnala priorità per un controllo esterno, non inventaria librerie/CVE;
  **File scout** valuta root e permessi storage, non scansiona realmente tutti i file privati.
  **Manifest flags** combina permessi e rischi activity: non coincide con l'analizzatore
  manifest dedicato presente nel layer dati, che non è esposto nell'attuale UI App installate.
- Uno stato di anomalia può rappresentare un rischio, dati indisponibili o un limite di analisi.
  Leggere il messaggio, non solo il colore o il conteggio. Le indisponibilità non sono esiti positivi.
- In caso di elenco vuoto distinguere filtro attivo, nessuna app visibile e sorgente non disponibile;
  rimuovere il filtro o aggiornare. Se un'azione fallisce, controllare messaggio, autorizzazioni,
  connessione ADB e requisiti root. Riprovare soltanto dopo avere verificato la causa.
- APK, report, identificatori e log possono contenere dati sensibili. Controllare i file e i
  destinatari prima di condividerli. Play Integrity può contattare i servizi Google:
  l'uso dell'app non va descritto come incondizionatamente offline.

<a id="english"></a>
## English

[Purpose](#en-purpose) · [Navigation](#en-navigation) · [Sections](#en-sections) ·
[Practical workflows](#en-workflows) · [Platforms](#en-platforms) · [Limitations](#en-limits)

<a id="en-purpose"></a>
### Purpose and current capabilities

CrackMyDroid combines inspection and diagnostic tools for **Android devices**.
It inventories apps and permissions, examines app entry points, runs heuristic checklists,
checks root indicators and produces reports. The Desktop client operates on a connected Android
device, not on applications installed on the computer. Use the tools only on devices and
applications you are authorized to inspect.

Its value is correlating inventory, permissions, activities and device status to **prioritize manual
verification**, repeat checklists and share evidence. It does not automatically prove every
vulnerability and does not remediate findings.

| Term | Meaning |
|---|---|
| APK | Android app package file; extracting it does not back up the app's private data. |
| Package | App identifier, for example `com.example.app`. |
| Manifest | Android declaration of app components, permissions and settings. |
| Activity / intent | Android screen or entry point / message carrying an action, URI and other launch parameters. |
| ADB | Computer tool for communicating with an Android device authorized for debugging. |
| Root | Administrative privileges; detecting an indicator does not prove the app can obtain them. |
| Snapshot | Serialized point-in-time Android data, not a continuously updated reading. |

<a id="en-navigation"></a>
### Finding your way around

The app starts on **Home**, possibly showing an introduction. The bottom bar opens **Home**,
**Info** and **Settings**. The header menu opens **Installed apps, PenTesting, Permissions,
Activities, Root / Play Integrity, Shell / Trick, Log and Help**.
Advanced sections exposing a back arrow return to Home.

When enabled and applicable, section-entry hints require confirmation before opening the section;
they can be disabled. The `?` button opens contextual help. **Help** also provides direct links
to the tools. The source `MoreScreen` is not wired into the current navigation.
Desktop requires device selection before this shared navigation becomes available.

<a id="en-sections"></a>
### Section guide

| Section and access | What to do | Output and constraints |
|---|---|---|
| **Home** — bottom bar | Read the purpose and FAQs; review recent operations and expand history; remove an entry using its dedicated button. | Shows up to five recent operations deduplicated by description/outcome and the available history. This is an operation summary, not the system log or a navigator to reports. |
| **Device info** — bottom bar | Inspect hardware, system, memory/network, debugging and properties; tap a row to copy it; share the report from the header. | Current device report. Restricted or unavailable fields may be absent; IMEI/serial access is not guaranteed. |
| **Settings** — bottom bar | Change theme/language; manage search suggestions, clearing suggestions, section hints, introduction and verbose logs; choose TXT/JSON for exports that support it; enable high contrast, large text and reduced motion; read the app version and third-party libraries. | Customizes the experience. The library section lists runtime open-source dependencies used by the app and opens only their official sites; test/debug tools are excluded. The selected format does not turn every export into JSON: some reports have their own format. This guide is IT/EN; the app supports additional languages. |
| **Installed apps / APK** — menu | Refresh; search by name, package or path; favorite apps; expand an app; extract/share its APK; share the inventory from the header. | Displays name, package and APK path. Shared inventory includes the full loaded list, not just visible filtered results. Android also offers a Bluetooth sharing attempt; Desktop reveals the local file/path. This is neither a private-data backup nor a guarantee of a complete reinstallable package for split-APK apps. |
| **PenTesting** — menu | Follow **1. target/mode → 2. checks → 3. execution → 4. report**. Choose one app or an automatic scan of all loaded apps; inspect progress/details, cancel auto scan, rerun, share/export reports and review previous snapshots. | Heuristic checklist primarily based on permissions, activity risks and root state. Current history holds up to 240 events; on reset/new execution, the preceding result may be retained among at most 12 snapshots **in ViewModel memory**, not persistent storage across restarts. See limitations below. |
| **Permissions** — menu | Search by app/permission; use favorites and the risky-app filter; start/cancel automatic risk scanning; expand details and share a single-app report. | Critical/High/Medium/Low classification with reasons. Requested/declared permissions do not prove that permissions are granted, actually used or exploited maliciously. |
| **Activities** — menu | Search, expand package groups and use favorites; inspect risk reports and copy/export them; launch permitted activities directly or through the guided action, URI and MIME-type dialog. | Identifies entry points and intent/deep-link risks. Informational or protected entries do not become launchable: permissions, component export status and system restrictions still apply. |
| **Root / Play Integrity** — menu | Refresh passive checks, explicitly request a privileged-access check, inspect evidence and share the report; enter a nonce and request Play Integrity. | Separates root indicators from demonstrated `su` access. Privileged probing may request root-manager consent or fail/time out. Android requests a Play Integrity token but does not decode/verify verdicts server-side: integrity fields remain unknown. |
| **Shell / Trick** — menu | Select a predefined command, read requirements and confirmation when provided; execute and inspect outcome, stdout/stderr and exit code. | Not an arbitrary terminal. Unsupported commands are reported; some require root/ADB privileges and can change device state. The same screen does not start another command while one is running. |
| **Log** — menu | Refresh, filter text/levels, change level coloring and export. | Logs readable in the platform context. An unprivileged Android app cannot necessarily read every other app's logs; Desktop uses snapshots or ADB logcat. Separate from Home operation history. |
| **Help** — menu | Read tool summaries and select a section. | In-app guide and direct navigation, not a chat or conversational assistant. |

<a id="en-workflows"></a>
### Practical workflows

1. **Inspect an app:** menu → Installed apps → search for the package → read its path and extract
   the APK if needed. Then open Permissions and Activities and search for the same package:
   switching sections is manual, not an automatic link carrying the filter.
2. **Run a checklist:** menu → PenTesting → choose target or auto scan → select checks → run →
   read counters and details. Auto scan can be interrupted; unfinished checks must not be treated
   as passed. Continue to the report or rerun.
3. **Retain evidence:** export/share the current PenTesting report or an available previous
   snapshot; use the corresponding action in other sections. Read the returned path or error.
   Save reports to files if they must survive closing the app; in-memory PenTesting history
   is insufficient.
4. **Work from Desktop:** connect and authorize Android for ADB debugging → open the client →
   configure ADB if necessary → refresh devices → select the device → check the toolbar's data
   source and snapshot metadata → use shared sections. Rescan clears Desktop caches and retries
   import; it does not itself force Android to collect a fresh snapshot. Change device returns
   to device selection.

<a id="en-platforms"></a>
### Platform availability

| Capability | Android | Desktop with connected Android | Current iOS |
|---|---|---|---|
| UI, Home, Help and preferences | Shared UI; Android adapters | Shared UI after device selection | Shared UI, but main preferences are in memory |
| Inventory, permissions and activities | Android APIs, subject to system visibility | Usable snapshot sections or ADB queries for data absent from cache | Stub repositories: empty lists; activity launch unsupported |
| Device info | Accessible Android information | Selected Android device, not host computer | Placeholder data, not full iOS diagnostics |
| APK and activity launch | Native operations subject to permissions | ADB extraction, permitted remote launch | Unsupported |
| Root and shell | Local checks; privilege-limited commands | Checks/commands on the device over ADB | Root marked unsupported; empty command list |
| Play Integrity | Token request; no server verification of verdicts | Only possibly imported data; unavailable in ADB-only mode. Automatic Android snapshots mark it as not executed | Unsupported |
| Logs | Logs accessible to the process | Snapshot or ADB logcat | Empty; export follows the selected report format |
| Reports/sharing | Android files/sharing where implemented | Local files; “share” reveals/opens the file or folder, without automatic sending or Bluetooth | Does not imply support for Android inspection tools |

Desktop requires working ADB and an accessible, authorized device; offline/unauthorized status
does not represent an operational session. The importer attempts to read the snapshot produced
by the Android app. If missing, incompatible, unreadable or lacking usable sections, its status
is exposed and the ADB route is used. A partial snapshot can supply some sections while others
are retrieved via ADB. Check creation time: “imported” does not mean “freshly collected.”

<a id="en-limits"></a>
### Interpreting results and errors

- **No finding does not prove security.** Some checks may produce no findings even when data is
  missing: verify availability and details before drawing conclusions.
- In the wizard, **Network monitor** evaluates permission combinations, not packet captures;
  **Library scan** prioritizes external review, not library/CVE inventory;
  **File scout** evaluates root/storage permissions, not an actual scan of all private files.
  **Manifest flags** combines permissions and activity risks: it is not the dedicated manifest
  analyzer in the data layer, which the current Installed apps UI does not expose.
- An anomaly can represent a risk, unavailable data or an analysis limitation. Read the message,
  not just the color/counter. Unavailable checks are not successful checks.
- For empty lists, distinguish an active filter, no visible apps and an unavailable source;
  clear the filter or refresh. On failure, inspect the message, permissions, ADB connection and
  root requirements. Retry only after checking the cause.
- APKs, reports, identifiers and logs may contain sensitive data. Review files and recipients
  before sharing. Play Integrity may contact Google services: do not describe the app as
  unconditionally offline.

<a id="sources"></a>
## Fonti e manutenzione / Sources and maintenance

Questa guida descrive i percorsi collegati nel codice, incluse le modifiche locali presenti durante
l'analisi; non sostituisce una certificazione o una verifica su ogni dispositivo.
This guide describes paths wired in the code, including local changes present during analysis;
it is not a certification or verification on every device.

| Ambito / Area | Fonte / Source |
|---|---|
| Navigazione / Navigation | [Screens.kt](../shared/src/commonMain/kotlin/com/crackmydroid/shared/presentation/navigation/Screens.kt), [App.kt](../shared/src/commonMain/kotlin/com/crackmydroid/shared/ui/App.kt) |
| Sezioni e azioni / Sections and actions | [Schermate Compose / Compose screens](../shared/src/commonMain/kotlin/com/crackmydroid/shared/ui/screens), [ViewModel](../shared/src/commonMain/kotlin/com/crackmydroid/shared/ui) |
| Semantica checklist / Checklist semantics | [RunPenTestChecksUseCase.kt](../shared/src/commonMain/kotlin/com/crackmydroid/shared/domain/usecase/pentest/RunPenTestChecksUseCase.kt), [PenTestingViewModel.kt](../shared/src/commonMain/kotlin/com/crackmydroid/shared/ui/pentest/PenTestingViewModel.kt) |
| Desktop | [DesktopCrackMyDroidApp.kt](../desktopApp/src/desktopMain/kotlin/com/crackmydroid/desktop/DesktopCrackMyDroidApp.kt), [DeviceSessionControllerDesktop.kt](../shared/src/desktopMain/kotlin/com/crackmydroid/shared/data/device/DeviceSessionControllerDesktop.kt), [RemoteDeviceSnapshotImporter.kt](../shared/src/desktopMain/kotlin/com/crackmydroid/shared/data/snapshot/RemoteDeviceSnapshotImporter.kt) |
| Snapshot Android | [AndroidDeviceSnapshotCoordinator.kt](../shared/src/androidMain/kotlin/com/crackmydroid/shared/data/snapshot/AndroidDeviceSnapshotCoordinator.kt) |
| Root / Integrity | [Android adapter](../shared/src/androidMain/kotlin/com/crackmydroid/shared/data/root/RootCheckRepositoryAndroid.kt), [Desktop adapter](../shared/src/desktopMain/kotlin/com/crackmydroid/shared/data/root/RootCheckRepositoryDesktop.kt) |
| Condivisione Desktop / Desktop sharing | [TextExport.desktop.kt](../shared/src/desktopMain/kotlin/com/crackmydroid/shared/util/TextExport.desktop.kt) |
| Limiti iOS / iOS limitations | [StubRepositories.kt](../shared/src/iosMain/kotlin/com/crackmydroid/shared/data/StubRepositories.kt) |

Documenti correlati / Related documents:
[Schermate / Screens](screens.md) · [Architettura / Architecture](architecture.md) ·
[Guida sviluppo / Developer guide](developer-guide.md) · [Mappa test / Test map](testing-feature-map.md) ·
[Indice / Index](README.md).
