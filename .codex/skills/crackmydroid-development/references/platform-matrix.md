# CrackMyDroid platform matrix

Use the production dependency boundary—not the screen name—to choose a source set.

| Need                                                                                     | Source set and placement                                 | Requirements                                                                             |
|------------------------------------------------------------------------------------------|----------------------------------------------------------|------------------------------------------------------------------------------------------|
| Shared model, rule, UseCase, repository contract, ViewModel, Compose UI                  | `shared/src/commonMain`                                  | No Android/JVM/ADB/filesystem/driver API; interfaces and expect declarations are allowed |
| Android PackageManager, Context, DataStore, RootBeer, Play Integrity, Android SQLDelight | `shared/src/androidMain` or `androidApp/src/main`        | Keep Android details behind a shared contract; bind in Android `platformModule()`        |
| Desktop ADB, JVM process, snapshot import, local filesystem, Desktop SQLDelight          | `shared/src/desktopMain` or `desktopApp/src/desktopMain` | Use adapter/fake boundaries; do not leak process types into common code                  |
| iOS native driver, iOS export, locale or SwiftUI bridge                                  | `shared/src/iosMain` or `iosApp`                         | Keep shared interface compatible with Kotlin/Native                                      |
| Common portable tests                                                                    | `shared/src/commonTest`                                  | `kotlin.test`, coroutine-test, real models/fakes                                         |
| JVM shared ViewModel/UseCase tests                                                       | `shared/src/jvmCommonTest`                               | MockK and coroutine-test; existing tests use `kotlin.test`                               |
| Android unit tests                                                                       | `shared/src/androidUnitTest` or `androidApp/src/test`    | MockK, JUnit 4, coroutine-test; no device required                                       |
| Desktop adapter tests                                                                    | `shared/src/desktopTest`                                 | Fake ADB/process/filesystem boundaries; no connected device required                     |
| iOS tests                                                                                | `shared/src/iosTest`                                     | Kotlin/Native-compatible APIs only; no JVM-only MockK/JUnit                              |

## Gradle validation

```bash
./gradlew :shared:compileKotlinDesktop
./gradlew :shared:desktopTest
./gradlew :shared:testDebugUnitTest
./gradlew :androidApp:testDebugUnitTest
./gradlew :shared:allTests
```

Use a narrower available task for the changed class when possible. Inspect Gradle tasks if a
target-specific task differs by variant.

## Dependency direction

```text
Compose/UI → ViewModel → UseCase → repository contract
Platform repository implementation → repository contract
Koin composition root → concrete implementations
```

`commonMain` must never depend on platform implementation details. Platform adapters may depend on
common contracts, never the inverse.
