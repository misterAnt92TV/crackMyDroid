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
├── commonTest/          # Dichiarato, attualmente vuoto
├── iosTest/             # Dichiarato, attualmente vuoto
│
├── jvmCommonTest/       # 9 file — test condivisi JVM (ViewModel, UseCase, Model)
│   └── kotlin/com/crackmydroid/shared/
│       ├── domain/
│       │   ├── usecase/
│       │   │   └── UseCaseTests.kt
│       │   └── model/
│       │       └── DeviceScanSnapshotJsonTest.kt
│       └── presentation/
│           ├── PenTestingViewModelTest.kt
│           ├── PermissionsViewModelTest.kt
│           ├── ActivityListViewModelTest.kt
│           ├── InstalledAppsViewModelTest.kt
│           ├── LogViewModelTest.kt
│           ├── TrickViewModelTest.kt
│           └── components/
│               └── SearchHistoryStoreTest.kt
│
└── desktopTest/         # 8 file — test layer Desktop/ADB
    └── kotlin/com/crackmydroid/shared/
        ├── data/
        │   ├── adb/
        │   │   ├── AdbParsersTest.kt
        │   │   └── ProcessAdbBridgeTest.kt
        │   ├── activity/
        │   │   └── ActivityRepositoryDesktopTest.kt
        │   ├── device/
        │   │   └── DeviceSessionControllerDesktopTest.kt
        │   ├── packages/
        │   │   └── RemotePackageManagerDesktopTest.kt
        │   ├── shell/
        │   │   └── ShellCommandRepositoryDesktopTest.kt
        │   └── snapshot/
        │       └── RemoteDeviceSnapshotImporterTest.kt
        └── domain/
            └── model/
                └── AppManifestSecurityTest.kt
```

**Totale:** 17 file di test · ~65 metodi `@Test`

---

## Source set e dipendenze

La gerarchia dei source set di test segue questa struttura:

```
commonTest
  └── jvmCommonTest      ← MockK disponibile
        ├── androidUnitTest  (JUnit 4 aggiunto)
        └── desktopTest
```

| Source Set | Dipendenze aggiuntive | Eseguito con |
|---|---|---|
| `commonTest` | `kotlin("test")`, `coroutines-test 1.8.1` | tutti i target |
| `jvmCommonTest` | + `mockk 1.13.13` | JVM (Android unit + Desktop) |
| `androidUnitTest` | + `junit4 4.13.2` | `./gradlew :shared:testDebugUnitTest` |
| `desktopTest` | nessuna aggiuntiva | `./gradlew :shared:desktopTest` |

### Versioni librerie di test

| Libreria | Versione |
|---|---|
| kotlin-test | 2.3.21 |
| kotlinx-coroutines-test | 1.8.1 |
| MockK | 1.13.13 |
| JUnit 4 | 4.13.2 |

---

## Comandi Gradle

```bash
# Esegui tutti i test JVM condivisi (jvmCommonTest → include androidUnitTest + desktopTest)
./gradlew :shared:jvmCommonTest

# Solo desktop test
./gradlew :shared:desktopTest

# Solo Android unit test
./gradlew :shared:testDebugUnitTest

# Tutti i test disponibili
./gradlew :shared:allTests

# Con report HTML dettagliato
./gradlew :shared:jvmCommonTest --info
# Report: shared/build/reports/tests/jvmCommonTest/index.html

./gradlew :shared:desktopTest --info
# Report: shared/build/reports/tests/desktopTest/index.html

# Esegui un singolo test per classe
./gradlew :shared:jvmCommonTest --tests "*.PenTestingViewModelTest"

# Esegui un singolo metodo
./gradlew :shared:desktopTest --tests "*.AppManifestSecurityTest.analyzesDebuggableAppAsCritical"
```

---

## Pattern e convenzioni

### Pattern ViewModel (tutti i 6 ViewModel test)

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

**Perché `StandardTestDispatcher`?** Fornisce un orologio virtuale: `advanceUntilIdle()` esegue tutte le coroutine in coda in modo deterministo, senza aspettare tempo reale.

**Perché `vm.onCleared()` manuale?** I ViewModel in KMP non hanno un lifecycle legato a un `ViewModelStore` nei test JVM. Chiamarlo esplicitamente cancella il `viewModelScope` ed evita leak.

---

### Pattern Repository Desktop (tutti i 6 repository test)

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

**Perché fakes invece di MockK?** Le classi desktop implementano interfacce con comportamenti complessi (stato, caching, side-effect su filesystem). I fakes permettono di verificare interazioni stateful in modo più leggibile e senza riflession MockK.

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

### `jvmCommonTest`

| File | Classe testata | # Test | Aree coperte |
|---|---|---|---|
| `UseCaseTests.kt` | 5 use case | 5 | Delegazione a repository, valori di ritorno, `coVerify` |
| `PenTestingViewModelTest.kt` | `PenTestingViewModel` | 2 | Manifest check con intent exposure, deduplicazione chiamate permessi |
| `PermissionsViewModelTest.kt` | `PermissionsViewModel` | 2 | Frequency tracking query, filtro "solo a rischio" post-scan |
| `ActivityListViewModelTest.kt` | `ActivityListViewModel` | 5 | Load, filtro, top-query, guided launch, report aggregation |
| `InstalledAppsViewModelTest.kt` | `InstalledAppsViewModel` | 7 | Load, export, exportAll, share OK, share error, filtro+preferiti, topQuery |
| `LogViewModelTest.kt` | `LogViewModel` | 1 | Filtro immediato su stato |
| `TrickViewModelTest.kt` | `TrickViewModel` | 2 | Load comandi, blocco esecuzione concorrente |
| `DeviceScanSnapshotJsonTest.kt` | serializzazione snapshot | 2 | Round-trip completo, tolleranza campi sconosciuti |
| `SearchHistoryStoreTest.kt` | `SearchHistoryStore` | 3 | Deduplication, prefix sorting, disabled mode |

### `desktopTest`

| File | Classe testata | # Test | Aree coperte |
|---|---|---|---|
| `AdbParsersTest.kt` | `AdbParsers` | 3 | Parse `adb devices`, `pm list packages`, `dumpsys package` (permessi + attività) |
| `ProcessAdbBridgeTest.kt` | `ProcessAdbBridge` | 2 | Rifiuto directory come ADB, rifiuto file non eseguibile |
| `AppManifestSecurityTest.kt` | `ManifestSecurityAnalyzer` | 6 | Tutti i livelli di rischio, score composito, stringhe italiane |
| `ActivityRepositoryDesktopTest.kt` | `ActivityRepositoryDesktop` | 1 | Costruzione comando `am start` con inner class `$` e argomenti quotati |
| `ShellCommandRepositoryDesktopTest.kt` | `ShellCommandRepositoryDesktop` | 1 | Marcatura comandi root non supportati quando `su` assente |
| `RemotePackageManagerDesktopTest.kt` | `RemotePackageManagerDesktop` | 1 | Caching ADB: ogni comando eseguito esattamente una volta per sessione |
| `DeviceSessionControllerDesktopTest.kt` | `DeviceSessionControllerDesktop` | 2 | Selezione device + invalidazione cache, rescan + clear selection |
| `RemoteDeviceSnapshotImporterTest.kt` | `RemoteDeviceSnapshotImporter` | 2 | Import snapshot valido in cache, fallback su schema incompatibile |

---

## Documentazione dettagliata per area

Vedi i file specifici per la documentazione completa test-per-test:

| Documento | Contenuto |
|---|---|
| [`testing-viewmodels.md`](testing-viewmodels.md) | Test di tutti i ViewModel (PenTesting, Permissions, ActivityList, InstalledApps, Log, Trick) con fixture, mock e assertions |
| [`testing-usecases-models.md`](testing-usecases-models.md) | Test dei use case, modelli di dominio, serializzazione snapshot, SearchHistoryStore |
| [`testing-desktop-adb.md`](testing-desktop-adb.md) | Test del layer Desktop/ADB: parser, bridge, repository, session controller, snapshot importer |
