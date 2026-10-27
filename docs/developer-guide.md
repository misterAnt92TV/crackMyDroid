# CrackMyDroid — Guida allo Sviluppo / Developer Guide

> Guida pratica per contribuire al progetto e lavorare con la codebase.
> Practical guide for contributing to the project and working with the codebase.

---

## Setup ambiente / Environment Setup

### Requisiti / Requirements

| Tool | Versione minima | Note |
|---|---|---|
| JDK | 17 | Testate con Temurin/Eclipse Adoptium |
| Android Studio | Koala (2024.1) o superiore | oppure IntelliJ IDEA con plugin KMP |
| Android SDK | API 34 | `compileSdk=34`, `targetSdk=34` |
| `adb` | qualsiasi | Solo per la desktop app |
| Xcode | 15+ | Solo per build iOS |

### Clone e verifica build

```bash
git clone https://github.com/<org>/CrackMyDroid.git
cd newCrackMyDroid

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
# Test JVM condivisi (commonMain + androidMain logic)
./gradlew :shared:jvmCommonTest

# Test Desktop
./gradlew :shared:desktopTest

# Tutti i test
./gradlew :shared:allTests

# Con report HTML
./gradlew :shared:jvmCommonTest --info
# Report: shared/build/reports/tests/jvmCommonTest/index.html
```

### Shared / KMP

```bash
# Compila tutti i target
./gradlew :shared:compileKotlinAll

# Controlla metadati KMP
./gradlew :shared:commonizeHmpp
```

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

| Tipo | Convenzione | Esempio |
|---|---|---|
| ViewModel | `<Feature>ViewModel` | `InstalledAppsViewModel` |
| State | `<Feature>State` | `InstalledAppsState` |
| UseCase | `<Verb><Noun>UseCase` | `ListPermissionsUseCase` |
| Repository (interface) | `<Noun>Repository` | `ActivityRepository` |
| Repository (impl Android) | `<Noun>RepositoryAndroid` | `ActivityRepositoryAndroid` |
| Screen | `<Feature>Screen` | `ActivityListScreen` |

### Compose

- Componenti riutilizzabili in `presentation/components/`
- Usa `AppText`, `AppButton`, `AppCard`, `SectionHeader` invece dei primitivi Material
- `@Preview` solo in debug, mai in commonMain

---

## Risoluzione problemi comuni / Troubleshooting

### `adb: command not found` (desktop app)

Configura il percorso ADB dall'app: **Impostazioni → Percorso ADB** oppure aggiungi `platform-tools` al PATH:

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

Verifica che la versione di MockK sia compatibile con la versione Kotlin corrente (`1.13.13` per Kotlin `2.x`).

### Compose preview non funziona su Desktop

Le preview Compose Multiplatform richiedono IntelliJ IDEA 2024.1+ con il plugin **Compose Multiplatform IDE Support** attivo.
