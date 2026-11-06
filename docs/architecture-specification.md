# CrackMyDroid — Specifica architetturale

## 1. Scopo e principi

CrackMyDroid è un'applicazione Kotlin Multiplatform per l'ispezione, l'analisi e il pen-testing di dispositivi Android. L'architettura deve permettere di condividere la logica di dominio e di presentazione mantenendo isolate le integrazioni con il sistema operativo, ADB e il filesystem.

I principi guida sono:

- **separazione delle responsabilità**: ogni layer ha un solo motivo principale per cambiare;
- **dipendenze verso contratti**: il dominio usa interfacce, non implementazioni di piattaforma;
- **common-first**: modelli, regole e use case restano condivisi quando non dipendono da API native;
- **platform isolation**: Android, Desktop e iOS implementano gli adapter nei rispettivi source set;
- **stato unidirezionale**: la UI invia azioni, il ViewModel aggiorna uno stato osservabile;
- **testabilità**: le dipendenze esterne sono iniettate e sostituibili con fake o mock;
- **nessuna dipendenza dalla UI nel dominio**: il dominio non conosce Compose, Activity o lifecycle Android.

## 2. Moduli e source set

| Modulo/source set | Responsabilità |
|---|---|
| `:shared` | Logica condivisa, dominio, UI Compose, contratti, DI e implementazioni platform-specific |
| `:androidApp` | Entry point Android, lifecycle applicativo, Activity e integrazioni Android app-specific |
| `:desktopApp` | Entry point JVM Desktop e composizione della finestra Desktop |
| `iosApp` | Wrapper SwiftUI che ospita la UI condivisa |
| `shared/src/commonMain` | Modelli, repository contract, use case, ViewModel, schermate e API comuni |
| `shared/src/androidMain` | Repository Android, RootBeer, DataStore, Play Integrity, SQLDelight Android e adapter Android |
| `shared/src/desktopMain` | ADB, processi JVM, import snapshot, filesystem e repository Desktop |
| `shared/src/iosMain` | Adapter iOS, database driver e implementazioni `actual` |

La struttura di test segue la stessa separazione: `commonTest` per logica portabile, `jvmCommonTest` per i test JVM condivisi, `desktopTest` per gli adapter Desktop e `androidApp/src/test`/`androidUnitTest` per Android.

## 3. Layer logici

### 3.1 Presentation

Si trova principalmente in `com.crackmydroid.shared.ui` e `com.crackmydroid.shared.presentation`.

Comprende:

- schermate e componenti Compose;
- ViewModel e relativo stato immutabile;
- navigazione e gestione delle azioni UI;
- operazioni di presentazione come log, tema e preferenze visuali.

Il flusso è UDF/MVI:

```text
User interaction → UI event/callback → ViewModel action → UseCase → Repository/data source → result → immutable StateFlow → Compose UI
```

I ViewModel usano `BaseViewModel` per possedere uno scope cancellabile. Il lavoro asincrono deve essere lanciato nello scope del ViewModel, con dispatcher forniti tramite `AppDispatchers`, e deve pubblicare stati coerenti per loading, successo ed errore.

La UI non contiene logica applicativa: Screen e composable ricevono `UiState` immutabile e callback,
renderizzano il contenuto e inoltrano gli eventi. Non possono accedere a UseCase, repository, database,
SDK, API native, Koin o coroutine applicative, né eseguire filtering, sorting, mapping, validazione,
gestione errori o navigazione condizionale. Sono ammessi solo calcoli locali strettamente visuali,
come layout, animazioni, dimensioni, colori, accessibilità e stato transitorio di un controllo.

Le liste con righe o card strutturate attraversano un confine esplicito di presentazione:
`FeatureState → XxxUiStateMapper → XxxUiState → XxxContent`. I mapper puri ricevono la localizzazione
attiva e producono item immutabili specifici della feature; le screen non passano modelli di dominio ai
componenti di lista. Le azioni degli item usano identificatori stabili, risolti dal ViewModel.

Le regole di dominio e le trasformazioni funzionali appartengono ai UseCase; il ViewModel gestisce
solo orchestrazione della presentazione, eventi, coroutine e mapping verso `UiState`.

### 3.2 Domain

Si trova in `com.crackmydroid.shared.domain` e contiene la logica indipendente dalla piattaforma:

- `domain/model`: modelli, result wrapper, rischi e contratti dati;
- `domain/repository`: interfacce dei repository;
- `domain/usecase`: casi d'uso applicativi;
- analizzatori e regole di classificazione.

Un UseCase coordina una singola intenzione applicativa. Può validare input, chiamare uno o più repository, trasformare i dati e restituire un risultato di dominio. Non deve conoscere Compose, Koin, Activity, Context o API ADB.

Le operazioni one-shot sono `suspend` e restituiscono `Result<T>` (anche analisi e
report); le scritture restituiscono `Result<Unit>`. Tutti gli use case estendono
`BaseUseCase`, che centralizza conversione delle eccezioni ordinarie e diagnostica,
senza possedere scope o job. `CancellationException` viene sempre propagata,
anche se contenuta in un risultato del repository; gli `Error` fatali non vengono
catturati. Un esito di dominio negativo o l'assenza legittima di finding non sono
automaticamente errori tecnici.

Ogni use case dichiara `private const val TAG = "NomeClasse"` nel proprio
`private companion object` e passa `TAG` a `BaseUseCase`, mantenendo stabile
l'identificativo diagnostico senza stringhe letterali nel costruttore della base.

Gli use case CPU usano `AppDispatchers.default` iniettabile, con controlli
cooperativi di cancellazione. Gli altri mantengono il contesto del chiamante e il
dispatching I/O dei repository. `AppDispatchers` risiede in `shared.util`, non
nel layer Presentation. I ViewModel consumano i risultati e preparano report
fuori da `StateFlow.update`; le anteprime asincrone espongono stato osservabile.

La base usa Kermit per eventi con nome use case/operazione, esito e durata
monotona: debug solo con Log verbosi, error per fallimenti. La preferenza viene
letta prima dei caricamenti della shell e aggiornata solo dopo persistenza
riuscita. I nuovi eventi non contengono input/output, messaggi grezzi, cause o
throwable; la cancellazione non viene registrata come errore. La policy non
modifica i livelli degli altri logger.

Non esistono attualmente use case Flow. Per futuri stream il contratto previsto
e `Flow<Result<T>>`, con errori upstream gestiti nello stream e cancellazione
propagata, senza intercettare gli errori del collector.

### Localizzazione dei risultati e dei report

L'analisi permessi produce `PermissionFindingKind` e parametri tecnici;
i controlli PenTest producono messaggi `PenTestMessage` tipizzati. Questi
risultati non memorizzano frasi nella lingua della scansione. Il catalogo puro
`UseCaseTexts` li rende in italiano, inglese, tedesco, spagnolo e francese, con
fallback inglese, condividendo descrizioni e severita tra UI e report.

I builder di report ricevono esplicitamente `UseCaseReportText`, contratto
definito nel dominio: non leggono preferenze, `LocalStrings` o locale globale.
La shell carica la lingua prima delle feature e coordina i mapper dei ViewModel.
Cambiare lingua rimappa i risultati e gli snapshot PenTest in memoria senza
rifare scansioni; il report dispositivo viene rigenerato dai dati caricati.
Le preview obsolete vengono cancellate e ogni export/share cattura la lingua
all'inizio dell'operazione. I file gia esportati non vengono riscritti.

TAG, permessi, package, URI, nomi file e termini tecnici (APK, ADB, shell,
intent/deeplink, overlay, accessibility, score, finding) rimangono invariati.
I messaggi raw dei repository/dispositivo e i testi dell'analizzatore activity
preesistente non fanno parte di questa localizzazione.

### 3.3 Data

Il layer Data contiene adapter, persistenza, cache e accesso a sorgenti esterne. Le interfacce consumate dal dominio sono dichiarate in `commonMain`; le implementazioni sono selezionate per piattaforma.

Esempi:

- repository Android basati su PackageManager, DataStore, RootBeer, Play Integrity e SQLDelight;
- repository Desktop alimentati da snapshot e ADB;
- `RemoteDeviceSnapshotImporter` per convertire JSON in `DeviceScanSnapshot`;
- `CachingRepositoryDecorator` e manutenzione cache;
- repository di report, log, shell command e device info.

Il Data layer traduce errori e formati esterni in tipi di dominio prima di esporli alla Presentation.

### 3.4 Platform e infrastruttura

Le integrazioni native sono confinate nei source set platform-specific e negli entry point applicativi. `expect/actual`, implementazioni Koin e adapter espliciti permettono di scegliere la variante senza contaminare il dominio.

Le responsabilità trasversali includono:

- Koin per la composizione delle dipendenze;
- SQLDelight per il database locale;
- kotlinx.serialization per snapshot e report JSON;
- coroutines per concorrenza e cancellazione strutturata;
- Ktor Client per tutte le chiamate REST, con engine confinati nel source set di piattaforma;
- ADB e process bridge esclusivamente nel percorso Desktop/ADB.

## 4. Flusso applicativo

Il percorso nominale è:

```text
Compose Screen
    ↓ evento pubblico
ViewModel / StateFlow
    ↓ invocazione
UseCase
    ↓ contratto
Repository interface
    ↓ implementazione Koin/expect-actual
Platform adapter
    ↓
Android API, ADB, snapshot, SQLDelight o filesystem
```

La risposta percorre il cammino inverso sotto forma di risultato di dominio e nuovo stato. La UI non deve interrogare direttamente repository, database o API native.

### Android

Package Manager e servizi Android alimentano i repository Android. Cache e SQLDelight possono ridurre operazioni ripetute; `AndroidDeviceSnapshotCoordinator` produce lo snapshot condivisibile con Desktop.

### Desktop

La sessione seleziona un device tramite ADB, importa lo snapshot e costruisce i repository Desktop a partire dai dati disponibili. La Presentation e il Domain restano gli stessi dell'app condivisa.

### iOS

Il wrapper SwiftUI ospita la UI condivisa. Le integrazioni iOS restano negli adapter `iosMain`; la logica comune non deve assumere la presenza di API Android o JVM.

## 5. Dependency Injection

`shared/src/commonMain/kotlin/com/crackmydroid/shared/di/Koin.kt` compone:

- `commonModule()` per UseCase, logger e dispatcher condivisi;
- `presentationModule()` per ViewModel;
- `platformModule()` come `expect`, con implementazioni nei source set di piattaforma.

Le dipendenze devono essere costruite nel composition root e passate esplicitamente ai componenti. Un ViewModel non deve creare repository o avviare Koin autonomamente.

Ogni risoluzione di `AppPresentationDependencies` crea un grafo di presentazione indipendente.
La factory risolve una sola volta i ViewModel delle schermate e passa le stesse istanze ad
`AppShellViewModel`: i caricamenti della shell aggiornano quindi lo stato osservato dalla UI.
Non risolvere nuovamente questi ViewModel per costruire la shell e non convertirli in singleton
globali: contenitori distinti devono mantenere stato indipendente.

## 6. Persistenza, cache e snapshot

- SQLDelight fornisce il database locale tramite driver di piattaforma.
- La cache è un'ottimizzazione e non deve cambiare il risultato funzionale quando è assente o invalidata.
- Gli eventi di aggiornamento/rimozione pacchetto devono invalidare le cache interessate.
- Gli snapshot sono serializzati con un contratto stabile e importati tramite un componente dedicato.
- ADB è una boundary esterna: timeout, processo fallito, output non valido e device non disponibile devono essere trasformati in risultati o errori gestibili.

### CacheDatabase: migrazioni e indici

I driver Android e iOS devono ricevere `CacheDatabaseSchema`, non direttamente lo schema
generato. L'adattatore conserva le migrazioni SQLDelight e ripara nel passaggio v3 i DB
legacy che condividono la versione 1/2 ma non le colonne manifest/activity. `2.sqm`
versiona questo passaggio con un UPDATE senza righe; il callback `AfterVersion(2)`
controlla `PRAGMA table_info` e aggiunge solo le colonne mancanti. Non modificare
`1.sqm` già applicato e non aggirare gli errori cancellando il database.

Solo la cache app/activity a cui vengono aggiunte colonne viene invalidata, nella
transazione di upgrade del driver: i default dei nuovi flag non rappresentano una
scansione effettiva. I DB già completi mantengono le righe. Le SELECT di queste tabelle
elencano esplicitamente le colonne, perché l'ordine fisico dopo ALTER TABLE può differire
da una nuova installazione. `clearDeviceInfo` elimina sia i campi key/value sia
l'eventuale chiave JSON legacy.

Le quattro cache sono indipendenti, senza foreign key: `packageName` è un collegamento
logico, non richiede un ordine di popolamento. Le PK `(packageName, activityName)` e
`(packageName, permission)` coprono già le ricerche per package; le altre PK sono
`installed_apps_cache(packageName)` e `device_info_cache(key)`. Non aggiungere indici
ridondanti sul package o sui flag: le query attuali leggono tutte le app e filtrano in
memoria. La paginazione SQL, la rappresentazione delle app senza permessi e la
normalizzazione delle cache richiedono un intervento separato, non nuovi indici isolati.

## 7. Regole di dipendenza

Sono consentite queste direzioni:

```text
Presentation → Domain
Data implementation → Domain contracts
Platform adapter → Data/Domain contracts
Composition root → tutti i componenti concreti
```

Sono vietate:

- Presentation → implementazione concreta Android/Desktop;
- Domain → Compose, Android SDK, ADB, SQLDelight driver o filesystem;
- UseCase → Koin globale;
- Repository interface → dettagli del repository concreto;
- modello di dominio → stato UI o annotazioni di framework non necessarie.

## 8. Strategia di test

| Area | Source set | Focus |
|---|---|---|
| Regole e modelli portabili | `commonTest` o `jvmCommonTest` | risultati, invarianti, serializzazione e casi limite |
| ViewModel/UseCase JVM | `jvmCommonTest` | MockK, coroutine-test, stato e interazioni significative |
| Adapter Desktop/ADB | `desktopTest` | parser, comandi, mapping errori e snapshot senza device reale |
| Android app e adapter | `androidApp/src/test`/`androidUnitTest` | JUnit 4, MockK e contratti Android isolati |
| iOS | `iosTest` | solo API compatibili Kotlin/Native |

Ogni test deve proteggere un comportamento osservabile e coprire almeno il percorso nominale e un errore o limite rilevante. I test non devono dipendere da rete, device ADB reale o filesystem dell'utente.

## 9. Decisioni e vincoli

1. Il dominio condiviso è la fonte delle regole applicative; la piattaforma fornisce capacità, non decisioni di business.
2. La cache non è una fonte di verità autonoma.
3. Gli adapter esterni devono essere sostituibili nei test.
4. Le modifiche a un contratto condiviso richiedono verifica di tutte le implementazioni platform-specific.
5. Il codice iOS non eredita automaticamente le librerie JVM di test.
