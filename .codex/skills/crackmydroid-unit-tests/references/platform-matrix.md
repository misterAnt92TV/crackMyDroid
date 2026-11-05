# CrackMyDroid platform matrix

This repository is Kotlin Multiplatform. Select the test source set from the production source set and dependency boundary, not from the IDE module name alone.

| Production target | Test source set | Libraries and style | Validation |
| --- | --- | --- | --- |
| `shared/src/commonMain` | `shared/src/commonTest` | `kotlin.test`, `kotlinx-coroutines-test`; real values and platform-neutral fakes | `./gradlew :shared:allTests` or the narrow common-compatible task |
| Shared JVM-compatible ViewModel/UseCase | `shared/src/jvmCommonTest` | MockK is available; use JUnit 4 when available through the consuming JVM test task; existing tests commonly use `kotlin.test` annotations | `./gradlew :shared:jvmCommonTest` |
| `shared/src/desktopMain` | `shared/src/desktopTest` | `kotlin.test`, coroutine-test; MockK only if the desktop test classpath provides it; prefer fakes for process/filesystem state | `./gradlew :shared:desktopTest` |
| `shared/src/androidMain` | `shared/src/androidUnitTest` | MockK, JUnit 4, coroutine-test; avoid device/framework dependencies in unit tests | `./gradlew :shared:testDebugUnitTest` |
| `androidApp/src/main` | `androidApp/src/test` | MockK where useful and JUnit 4 (`org.junit.Test`, `org.junit.Assert`) | `./gradlew :androidApp:testDebugUnitTest` |
| `shared/src/iosMain` | `shared/src/iosTest` | Kotlin/Native-compatible `kotlin.test`; no JVM-only MockK or JUnit | Use the repository's available iOS test task; do not substitute a JVM task |

## Current dependency facts

`shared/build.gradle.kts` currently provides:

- `commonTest`: `kotlin("test")` and coroutine-test;
- `jvmCommonTest`: MockK and coroutine-test;
- `androidUnitTest`: MockK, JUnit 4, and coroutine-test;
- `desktopTest`: Kotlin test and coroutine-test, inheriting the JVM-common test source set;
- `iosTest`: common test dependencies only.

`androidApp/build.gradle.kts` provides JUnit 4 for Android app unit tests. Check the actual Gradle file before adding any dependency; do not duplicate an existing alias or version.

## Pattern selection

### ViewModel or state holder

Use a deterministic dispatcher, stub repository calls with `coEvery`, invoke the public action, call `advanceUntilIdle()`, and assert the exposed state. Include an empty/error response when the ViewModel maps those states. Verify a repository interaction only when it represents a meaningful contract.

### UseCase

Prefer real domain models and assert the returned result or thrown error. Use MockK for repository boundaries, stub success and failure, and verify that delegation or persistence occurs exactly when it is part of the UseCase contract.

### Repository or platform adapter

For Desktop process, ADB, or filesystem adapters, use a fake bridge or isolated temporary input when stateful behavior matters. Assert the parsed result, generated command, or error mapping. Never depend on a connected device, the user's filesystem, or ambient environment variables.

### Parser, serializer, or analyzer

Use real representative input. Cover valid input, empty/malformed input, and the security or domain boundary that the parser/analyzer is responsible for. Avoid mocks unless an external boundary is genuinely involved.

## Existing project commands

```bash
./gradlew :shared:jvmCommonTest
./gradlew :shared:desktopTest
./gradlew :shared:testDebugUnitTest
./gradlew :androidApp:testDebugUnitTest
./gradlew :shared:allTests
```

When a task name differs by build variant or is unavailable, inspect Gradle tasks and use the closest task for the selected source set; report the exact command used.
