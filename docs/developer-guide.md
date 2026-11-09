# CrackMyDroid — Guida allo Sviluppo / Developer Guide

> Guida pratica per contribuire al progetto e lavorare con la codebase.
> Practical guide for contributing to the project and working with the codebase.

---

## Setup ambiente / Environment Setup

### Requisiti / Requirements

| Tool           | Versione minima            | Note                                 |
|----------------|----------------------------|--------------------------------------|
| JDK            | 17                         | Testate con Temurin/Eclipse Adoptium |
| Android Studio | Koala (2024.1) o superiore | oppure IntelliJ IDEA con plugin KMP  |
| Android SDK    | API 36                     | `compileSdk=36`, `targetSdk=35`      |
| `adb`          | qualsiasi                  | Solo per la desktop app              |
| Xcode          | 15+                        | Solo per build iOS                   |

Ktor resta allineato alla versione 3.5.0, che usa OkHttp 5.3.2 ed è compatibile con
`compileSdk=36`. Ktor 3.6.0 introduce OkHttp Android 5.5.0, che richiede API 37:
prima di aggiornare Ktor, ricontrollare i requisiti SDK delle dipendenze transitive.

### Configurazione Gradle condivisa / Shared Gradle Configuration

`gradle/libs.versions.toml` centralizza coordinate e versioni richieste di
librerie e plugin, inclusi plugin ktlint e relativa CLI, con versioni distinte.
Le impostazioni di build non sono versioni di dipendenze: SDK e JVM sono
proprieta comuni in `gradle.properties`, lette direttamente dai moduli.

| Proprieta | Valore | Utilizzo |
|-----------|--------|----------|
| `android.compileSdk` | `36` | shared e androidApp |
| `android.minSdk` | `24` | shared e androidApp |
| `android.targetSdk` | `35` | androidApp |
| `build.jvmVersion` | `17` | toolchain shared/desktopApp e target Java/Kotlin Android |

Non duplicare questi valori nei `.kts` o nel catalogo. Proprieta mancanti o
non numeriche devono produrre un errore di configurazione, non un valore
di fallback. Per una prova locale possono essere sovrascritte con `-P`,
ma cambiarle nella build distribuita richiede una verifica di compatibilita.
Identificativi app, versionCode/versionName e opzioni packaging restano
nel modulo che li possiede.

`kotlin("test")` resta l'helper del plugin Kotlin per allineare la libreria di
test; `compose.desktop.currentOs` sceglie gli artefatti nativi del sistema
operativo. Non sostituirli con coordinate/versioni fisse nel catalogo.
Le dipendenze comuni dei test sono dichiarate in `commonTest` e
`jvmCommonTest`, senza ripeterle nei source set che le ereditano.
La gerarchia KMP iOS/JVM resta esplicita.

Le versioni del catalogo sono **richieste**, non vincoli forzati sulle
dipendenze transitive: il grafo puo risolvere una versione diversa.
Verificare con `dependencies` o `dependencyInsight` prima di modificare
il catalogo; una pulizia degli script non deve cambiare il grafo risolto.

### Clone e verifica build

```bash
git clone https://github.com/misterAnt92TV/crackMyDroid.git
cd crackMyDroid

# Verifica che Gradle funzioni
./gradlew tasks

# Build Android
./gradlew :androidApp:assembleDebug

# Build Desktop
./gradlew :desktopApp:run
```

---

## Comandi Gradle utili / Useful Gradle Commands

### Android

```bash
# Build APK debug
./gradlew :androidApp:assembleDebug

# Build APK release (richiede firma configurata)
./gradlew :androidApp:assembleRelease

# Installa su device
./gradlew :androidApp:installDebug

# Lint
./gradlew :androidApp:lint
```

### Desktop

```bash
# Avvia l'app desktop
./gradlew :desktopApp:run

# Build pacchetto distribuibile per l'OS corrente
./gradlew :desktopApp:packageDistributionForCurrentOS

# macOS .dmg
./gradlew :desktopApp:packageDmg

# Linux .deb
./gradlew :desktopApp:packageDeb

# Windows .msi
./gradlew :desktopApp:packageMsi
```

### Test

```bash
# Test condivisi sui target Desktop e Android unit
./gradlew :shared:desktopTest :shared:testDebugUnitTest

# Test Desktop
./gradlew :shared:desktopTest

# Tutti i test
./gradlew :shared:allTests

# Esegui i test Desktop con log dettagliato
./gradlew :shared:desktopTest --info
# Report: shared/build/reports/tests/desktopTest/index.html
```

### Shared / KMP

```bash
# Compila tutti i target
./gradlew :shared:compileKotlinAll

# Controlla metadati KMP
./gradlew :shared:commonizeHmpp
```

### Qualita Kotlin / Kotlin Code Quality

ktlint e integrato in Gradle: non richiede una CLI installata sul computer.
Il plugin e il motore sono fissati nel version catalog. La `.editorconfig`
definisce lo stile Android Studio, trailing comma e naming Compose:
le funzioni annotate `@Composable` possono mantenere PascalCase.
Il controllo copre sorgenti Kotlin, test, source set KMP e script `.kts`;
directory `build` e `generated` sono escluse.

```bash
# Controllo incrementale: fallisce su violazioni non presenti nella baseline
./gradlew ktlintCheck

# Report completo del debito, senza baseline (puo terminare con exit code 1)
./gradlew ktlintCheck -PktlintFullReport=true --continue

# Controllo rigoroso di un file modificato, senza baseline
./gradlew ktlintFilesCheck \
  -PktlintFiles=shared/src/commonMain/kotlin/com/crackmydroid/shared/ui/permissions/PermissionsViewModel.kt

# Formattazione limitata ai file indicati, non all'intero progetto
./gradlew ktlintFilesFormat \
  -PktlintFiles=shared/src/commonMain/kotlin/com/crackmydroid/shared/ui/permissions/PermissionsViewModel.kt

# Analisi Android: separata dalle convenzioni stilistiche Kotlin
./gradlew :shared:lintDebug :androidApp:lintDebug
```

`ktlintFiles` accetta path relativi alla root del repository, separati da virgole.
I task dedicati `ktlintFilesCheck`/`ktlintFilesFormat` usano la CLI fissata tramite
Gradle, senza baseline o risultati incrementali di altri file. I task generali
del plugin non supportano questo selettore.
I report Checkstyle/SARIF sono in `<modulo>/build/reports/ktlint/`.
Android Lint produce HTML/XML in `<modulo>/build/reports/`.
ktlint verifica stile, non prestazioni o correttezza funzionale; Android Lint
non sostituisce test e compilazione Desktop/iOS.

I file `ktlint-baseline.xml` versionati registrano esclusivamente debito
preesistente fuori dal perimetro del refactor. Non rigenerarli automaticamente
per far passare un check: i file modificati devono passare il controllo
rigoroso senza baseline, rimuovendo le loro eventuali voci legacy.
`ktlintGenerateBaseline` e riservato a un aggiornamento esplicito e revisionato
del debito. `ktlintFormat` ignora la baseline e formatta anche codice legacy:
non usarlo indiscriminatamente, preferire `ktlintFilesFormat`.

I warning Android residui relativi a `HardwareIds` sono intenzionali nel
repository diagnostico del dispositivo. Le raccomandazioni di upgrade
dipendenze/targetSdk e di icone monocromatiche richiedono interventi separati;
non vengono soppressi globalmente.

---

## Struttura test / Test Structure

```
shared/src/
├── jvmCommonTest/kotlin/com/crackmydroid/shared/
│   └── presentation/
│       └── PenTestingViewModelTest.kt    # test ViewModel con MockK
└── desktopTest/kotlin/com/crackmydroid/shared/
    └── ...                               # test layer ADB/desktop
```

### Dipendenze di test

- `kotlin-test` — assertions
- `MockK` `1.13.13` — mocking coroutine-friendly
- `kotlinx-coroutines-test` — `TestScope`, `runTest`, `advanceUntilIdle`

### Ownership delle scansioni

Nei ViewModel Permessi e PenTest una nuova esecuzione, un refresh o un reset
invalida e cancella l'operazione precedente. Solo la generazione corrente
puo pubblicare risultati, errori o azzerare il progresso nel `finally`.
La cancellazione non diventa un errore utente, e risposte tardive non popolano
le cache dopo un refresh. Le anteprime mantengono la propria ownership.

La presentazione riusa finding tradotti, indici e riepiloghi quando i relativi
input restano invariati; un cambio lingua rimappa i messaggi semantici senza
una nuova scansione. Il ritmo delle scansioni, score e ordinamento rimangono
invariati. I test con dispatcher virtuale coprono riavvii, risposte fuori ordine,
cancellazione non cooperativa e riuso delle strutture derivate: non attestano
un guadagno temporale o una riduzione del jank senza profiling.

### Esempio test ViewModel

```kotlin
@Test
fun `pentest single target should produce ok result`() = runTest {
    // Arrange
    val mockRepo = mockk<InstalledAppsRepository>()
    coEvery { mockRepo.listInstalled() } returns listOf(fakeApp)
    val vm = PenTestingViewModel(mockRepo, ...)

    // Act
    vm.runSingleTarget("com.example.app")
    advanceUntilIdle()

    // Assert
    val state = vm.state.value
    assertTrue(state.results.isNotEmpty())
}
```

---

## Aggiungere una nuova funzionalità / Adding a New Feature

### 1. Definire il modello (Domain)

```kotlin
// shared/src/commonMain/kotlin/.../domain/model/MyModel.kt
data class MyModel(
    val id: String,
    val value: String
)
```

### 2. Definire il repository interface (Domain)

```kotlin
// shared/src/commonMain/kotlin/.../domain/repository/MyRepository.kt
interface MyRepository {
    suspend fun getData(): List<MyModel>
}
```

### 3. Creare il use case (Domain)

```kotlin
// shared/src/commonMain/kotlin/.../domain/usecase/my/GetMyDataUseCase.kt
class GetMyDataUseCase(private val repository: MyRepository) {
    suspend operator fun invoke(): List<MyModel> = repository.getData()
}
```

### 4. Implementare il repository (Platform)

```kotlin
// shared/src/androidMain/kotlin/.../data/my/MyRepositoryAndroid.kt
class MyRepositoryAndroid(private val context: Context) : MyRepository {
    override suspend fun getData(): List<MyModel> = withContext(Dispatchers.IO) {
        // implementazione Android
    }
}
```

### 5. Creare il ViewModel

```kotlin
// shared/src/commonMain/kotlin/.../ui/my/MyViewModel.kt
data class MyState(val items: List<MyModel> = emptyList(), val isLoading: Boolean = false)

class MyViewModel(private val getMyData: GetMyDataUseCase) : BaseViewModel() {
    private val _state = MutableStateFlow(MyState())
    val state: StateFlow<MyState> = _state.asStateFlow()

    fun load() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val items = getMyData()
            _state.update { it.copy(items = items, isLoading = false) }
        }
    }
}
```

### 6. Creare la schermata Compose

```kotlin
// shared/src/commonMain/kotlin/.../ui/screens/MyScreen.kt
@Composable
fun MyScreen(viewModel: MyViewModel) {
    val state by viewModel.state.collectAsState()
    // UI
}
```

### 7. Registrare in Koin

```kotlin
// shared/src/commonMain/kotlin/.../di/Koin.kt
val sharedModule = module {
    factory { GetMyDataUseCase(get()) }
    viewModel { MyViewModel(get()) }
}

// shared/src/androidMain/kotlin/.../di/PlatformModule.kt
val platformModule = module {
    single<MyRepository> { MyRepositoryAndroid(androidContext()) }
}
```

### 8. Aggiungere le stringhe i18n

```kotlin
// shared/src/commonMain/kotlin/.../i18n/Strings.kt
data class Strings(
    // ... esistenti
    val myFeatureTitle: String,
    val myFeatureEmpty: String,
)

val italianStrings = Strings(
    myFeatureTitle = "La mia funzionalità",
    myFeatureEmpty = "Nessun dato disponibile",
)

val englishStrings = Strings(
    myFeatureTitle = "My Feature",
    myFeatureEmpty = "No data available",
)
```

---

## Convenzioni di codice / Code Conventions

### Kotlin / KMP

- Kotlin idiomatico: `data class`, `sealed class`, `when`, extension functions
- Coroutines: `withContext(Dispatchers.IO)` per operazioni I/O, `viewModelScope` nei ViewModel
- `StateFlow` per lo stato UI; nessun `LiveData` (non KMP-compatible)
- `expect/actual` solo dove necessario (driver DB, locale, export file)

### Naming

| Tipo                      | Convenzione               | Esempio                     |
|---------------------------|---------------------------|-----------------------------|
| ViewModel                 | `<Feature>ViewModel`      | `InstalledAppsViewModel`    |
| State                     | `<Feature>State`          | `InstalledAppsState`        |
| UseCase                   | `<Verb><Noun>UseCase`     | `ListPermissionsUseCase`    |
| Repository (interface)    | `<Noun>Repository`        | `ActivityRepository`        |
| Repository (impl Android) | `<Noun>RepositoryAndroid` | `ActivityRepositoryAndroid` |
| Screen                    | `<Feature>Screen`         | `ActivityListScreen`        |

### Compose

- Componenti riutilizzabili in `presentation/components/`
- Usa `AppText`, `AppButton`, `AppCard`, `SectionHeader` invece dei primitivi Material
- `@Preview` solo in debug, mai in commonMain

---

## Risoluzione problemi comuni / Troubleshooting

### `adb: command not found` (desktop app)

Configura il percorso ADB dall'app: **Impostazioni → Percorso ADB** oppure aggiungi `platform-tools`
al PATH:

```bash
export PATH="$PATH:/path/to/android-sdk/platform-tools"
```

### Snapshot non disponibile (desktop app)

1. Verifica che l'app Android sia installata e avviata sul device.
2. Aspetta almeno 5 secondi dopo il lancio.
3. Verifica che ADB abbia accesso allo storage: `adb shell ls /sdcard/Android/data/`.

### Build fallisce con `Kotlin daemon OOM`

Aumenta la heap Gradle in `gradle.properties`:

```properties
org.gradle.jvmargs=-Xmx4g -XX:MaxMetaspaceSize=512m
```

### MockK `Method not found` nei test

Verifica che la versione di MockK sia compatibile con la versione Kotlin corrente (`1.13.13` per
Kotlin `2.x`).

### Compose preview non funziona su Desktop

Le preview Compose Multiplatform richiedono IntelliJ IDEA 2024.1+ con il plugin **Compose
Multiplatform IDE Support** attivo.
