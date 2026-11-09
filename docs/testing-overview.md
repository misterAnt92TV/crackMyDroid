# CrackMyDroid — Documentazione di Test

> Guida completa alla suite di test del progetto: struttura, pattern, copertura e convenzioni.

---

## Indice

- [Struttura della test suite](#struttura-della-test-suite)
- [Source set e dipendenze](#source-set-e-dipendenze)
- [Comandi Gradle](#comandi-gradle)
- [Pattern e convenzioni](#pattern-e-convenzioni)
- [Mappa di copertura](#mappa-di-copertura)
- [Documentazione dettagliata per area](#documentazione-dettagliata-per-area)

---

## Struttura della test suite

```
shared/src/
├── commonTest/          # 3 test file comuni
├── jvmCommonTest/       # 38 test file JVM condivisi
├── androidUnitTest/     # 1 test file specifico Android
├── desktopTest/         # 9 test file specifici Desktop/ADB
└── iosTest/             # source set dichiarato, senza test file

androidApp/src/test/     # 2 test file del modulo Android
```

Inventario verificato: 51 file di test nel modulo `shared` e 2 in `androidApp`.
`jvmCommonTest` è un source set, non un task Gradle; i test JVM condivisi vengono
eseguiti tramite i task `desktopTest` e `testDebugUnitTest`.

---

## Source set e dipendenze

La gerarchia dei source set di test segue questa struttura:

```
commonTest
  └── jvmCommonTest      ← MockK disponibile
        ├── androidUnitTest  (JUnit 4 aggiunto)
        └── desktopTest
```

| Source Set        | Dipendenze aggiuntive                     | Eseguito con                          |
|-------------------|-------------------------------------------|---------------------------------------|
| `commonTest`      | `kotlin("test")`, `coroutines-test 1.8.1` | tutti i target                        |
| `jvmCommonTest`   | + `mockk 1.13.13`                         | JVM (Android unit + Desktop)          |
| `androidUnitTest` | + `junit4 4.13.2`                         | `./gradlew :shared:testDebugUnitTest` |
| `desktopTest`     | nessuna aggiuntiva                        | `./gradlew :shared:desktopTest`       |

### Versioni librerie di test

| Libreria                | Versione |
|-------------------------|----------|
| kotlin-test             | 2.3.21   |
| kotlinx-coroutines-test | 1.8.1    |
| MockK                   | 1.13.13  |
| JUnit 4                 | 4.13.2   |

---

## Comandi Gradle

```bash
# Esegui i test JVM condivisi sui target Desktop e Android unit
./gradlew :shared:desktopTest :shared:testDebugUnitTest

# Solo desktop test
./gradlew :shared:desktopTest

# Solo Android unit test
./gradlew :shared:testDebugUnitTest

# Tutti i test disponibili
./gradlew :shared:allTests

# Con report HTML dettagliato
./gradlew :shared:desktopTest --info
# Report: shared/build/reports/tests/desktopTest/index.html

./gradlew :shared:testDebugUnitTest --info
# Report: shared/build/reports/tests/testDebugUnitTest/index.html

# Esegui un singolo test per classe
./gradlew :shared:desktopTest --tests "*.PenTestingViewModelTest"

# Esegui un singolo metodo
./gradlew :shared:desktopTest --tests "*.AppManifestSecurityTest.analyzesDebuggableAppAsCritical"
```

`jvmCommonTest` è un source set, non un task Gradle. Per eseguire solo i test diretti dei
45 UseCase, aggiungere `--tests 'com.crackmydroid.shared.domain.usecase.*'` a uno dei due task JVM.
La [matrice UseCase → Test](testing-usecases-models.md#copertura-diretta-degli-usecase)
elenca i soggetti e i comportamenti verificati.

---

## Pattern e convenzioni

### Pattern ViewModel (esempi)

```kotlin
@OptIn(ExperimentalCoroutinesApi::class)
class MyViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher)   // installa dispatcher deterministico
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `description of test`() = runTest(dispatcher) {
        // Arrange
        val mockRepo = mockk<MyRepository>()
        coEvery { mockRepo.getData() } returns listOf(fakeItem)

        val vm = MyViewModel(mockRepo)

        // Act
        vm.load()
        advanceUntilIdle()               // svuota la coda di coroutine

        // Assert
        val state = vm.state.value
        assertEquals(1, state.items.size)
        coVerify { mockRepo.getData() }

        // Teardown
        vm.onCleared()                   // pulisce il CoroutineScope del ViewModel
    }
}
```

**Perché `StandardTestDispatcher`?** Fornisce un orologio virtuale: `advanceUntilIdle()` esegue
tutte le coroutine in coda in modo deterministo, senza aspettare tempo reale.

**Perché `vm.onCleared()` manuale?** I ViewModel in KMP non hanno un lifecycle legato a un
`ViewModelStore` nei test JVM. Chiamarlo esplicitamente cancella il `viewModelScope` ed evita leak.

---

### Pattern Repository Desktop (esempi)

```kotlin
class MyRepositoryDesktopTest {

    @Test
    fun `description`() = runTest {
        // Fakes hand-written (no MockK)
        val fakeBridge = FakeAdbBridge(...)
        val fakeController = FakeSessionController(...)

        val repo = MyRepositoryDesktop(fakeBridge, fakeController)

        // Act
        val result = repo.doSomething()

        // Assert
        assertEquals(expected, result)
        assertEquals("expected adb command", fakeBridge.lastCommand)
    }
}
```

**Perché fakes invece di MockK?** Le classi desktop implementano interfacce con comportamenti
complessi (stato, caching, side-effect su filesystem). I fakes permettono di verificare interazioni
stateful in modo più leggibile e senza riflession MockK.

---

### Pattern test puri (serializzazione, parser, analyzer)

```kotlin
class MyPureTest {
    @Test
    fun `round trip`() {
        val obj = MyModel(...)
        val json = format.encodeToString(obj)
        val decoded = format.decodeFromString<MyModel>(json)
        assertEquals(obj, decoded)
    }
}
```

Nessun dispatcher, nessun mock: input → output → assertion.

---

### Isolamento singleton (`SearchHistoryStoreTest`)

```kotlin
@BeforeTest
fun setUp() {
    SearchHistoryStore.setEnabled(true)
    SearchHistoryStore.clearAll()        // reset globale prima di ogni test
}

@AfterTest
fun tearDown() {
    SearchHistoryStore.clearAll()        // cleanup dopo ogni test
    SearchHistoryStore.setEnabled(true)  // ripristina stato default
}
```

Necessario perché `SearchHistoryStore` è un object singleton con stato persistente in memoria.

---

## Mappa di copertura

Le tabelle riportano le suite documentate in dettaglio, non l'intero inventario dei test.
Il conteggio complessivo e i source set sono riepilogati nella sezione iniziale.

### `jvmCommonTest`

| File                                               | Classe testata                  | # Test | Aree coperte                                                               |
|----------------------------------------------------|---------------------------------|--------|----------------------------------------------------------------------------|
| `UseCaseTests.kt`                                  | Deleghe e report                | 6      | Delegazione a repository, valori di ritorno, ordine save/share             |
| `UseCaseDelegationTest.kt`                         | 13 UseCase deleganti            | 11     | Risultati, default, parametri, liste vuote, fallimenti ed eccezioni        |
| `SettingsUseCasesTest.kt`                          | 28 UseCase settings             | 7      | Tutti i valori, effetti di ogni setter, errori lettura/scrittura           |
| `pentest/RunPenTestChecksUseCaseTest.kt`           | `RunPenTestChecksUseCase`       | 12     | Check success/finding, dati assenti, soglie e fallback                     |
| `permissions/AnalyzePermissionRisksUseCaseTest.kt` | `AnalyzePermissionRisksUseCase` | 8      | Famiglie, pattern, score, normalizzazione, etichette e ordinamento         |
| `permissions/BuildPermissionsReportUseCaseTest.kt` | `BuildPermissionsReportUseCase` | 5      | Report vuoti/singoli/aggregati, totali, ordinamento, filename              |
| `reports/ExportReportUseCaseTest.kt`               | `ExportReportUseCase`           | 5      | Save/share, ordine, Result.failure ed eccezioni                            |
| `PenTestingViewModelTest.kt`                       | `PenTestingViewModel`           | 2      | Manifest check con intent exposure, deduplicazione chiamate permessi       |
| `PermissionsViewModelTest.kt`                      | `PermissionsViewModel`          | 2      | Frequency tracking query, filtro "solo a rischio" post-scan                |
| `ActivityListViewModelTest.kt`                     | `ActivityListViewModel`         | 5      | Load, filtro, top-query, guided launch, report aggregation                 |
| `InstalledAppsViewModelTest.kt`                    | `InstalledAppsViewModel`        | 7      | Load, export, exportAll, share OK, share error, filtro+preferiti, topQuery |
| `LogViewModelTest.kt`                              | `LogViewModel`                  | 1      | Filtro immediato su stato                                                  |
| `TrickViewModelTest.kt`                            | `TrickViewModel`                | 2      | Load comandi, blocco esecuzione concorrente                                |
| `DeviceScanSnapshotJsonTest.kt`                    | serializzazione snapshot        | 2      | Round-trip completo, tolleranza campi sconosciuti                          |
| `SearchHistoryStoreTest.kt`                        | `SearchHistoryStore`            | 3      | Deduplication, prefix sorting, disabled mode                               |

### `desktopTest`

| File                                    | Classe testata                   | # Test | Aree coperte                                                                     |
|-----------------------------------------|----------------------------------|--------|----------------------------------------------------------------------------------|
| `AdbParsersTest.kt`                     | `AdbParsers`                     | 3      | Parse `adb devices`, `pm list packages`, `dumpsys package` (permessi + attività) |
| `ProcessAdbBridgeTest.kt`               | `ProcessAdbBridge`               | 2      | Rifiuto directory come ADB, rifiuto file non eseguibile                          |
| `AppManifestSecurityTest.kt`            | `ManifestSecurityAnalyzer`       | 6      | Tutti i livelli di rischio, score composito, stringhe italiane                   |
| `ActivityRepositoryDesktopTest.kt`      | `ActivityRepositoryDesktop`      | 1      | Costruzione comando `am start` con inner class `$` e argomenti quotati           |
| `ShellCommandRepositoryDesktopTest.kt`  | `ShellCommandRepositoryDesktop`  | 1      | Marcatura comandi root non supportati quando `su` assente                        |
| `RemotePackageManagerDesktopTest.kt`    | `RemotePackageManagerDesktop`    | 1      | Caching ADB: ogni comando eseguito esattamente una volta per sessione            |
| `DeviceSessionControllerDesktopTest.kt` | `DeviceSessionControllerDesktop` | 2      | Selezione device + invalidazione cache, rescan + clear selection                 |
| `RemoteDeviceSnapshotImporterTest.kt`   | `RemoteDeviceSnapshotImporter`   | 2      | Import snapshot valido in cache, fallback su schema incompatibile                |
| `ReportFileRepositoryDesktopTest.kt`    | `ReportFileRepositoryDesktop`    | 3      | PDF multipagina e accenti, salvataggio PDF, envelope JSON                        |

---

## Documentazione dettagliata per area

Vedi i file specifici per la documentazione completa test-per-test:

| Documento                                                  | Contenuto                                                                                                                         |
|------------------------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------|
| [`testing-viewmodels.md`](testing-viewmodels.md)           | Sei suite ViewModel selezionate (PenTesting, Permissions, ActivityList, InstalledApps, Log, Trick) con fixture, mock e assertions |
| [`testing-usecases-models.md`](testing-usecases-models.md) | Test dei use case, modelli di dominio, serializzazione snapshot, SearchHistoryStore                                               |
| [`testing-desktop-adb.md`](testing-desktop-adb.md)         | Test del layer Desktop/ADB: parser, bridge, repository, session controller, snapshot importer                                     |
