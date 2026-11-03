---
name: crackmydroid-development
description: Develop Android and Kotlin Multiplatform features for CrackMyDroid using its Clean Architecture, platform source sets, Compose previews, readable Kotlin, and focused tests.
metadata:
  short-description: Build platform-aware CrackMyDroid features
---

# CrackMyDroid Development

Use this skill for new features or feature-level changes in CrackMyDroid. Keep behavior in the correct Clean Architecture layer, select the correct Kotlin Multiplatform source set, and preserve the project’s existing conventions.

Read [docs/architecture-specification.md](../../../docs/architecture-specification.md) and [docs/clean-code-structure.md](../../../docs/clean-code-structure.md) before designing a new architectural boundary. For source-set, feature layout, preview, and validation details, read the relevant reference below.

## Discovery and platform routing

1. Inspect the requested feature, adjacent implementation, Gradle dependencies, and existing tests before editing.
2. Choose the narrowest target source set using [references/platform-matrix.md](references/platform-matrix.md).
3. Keep models, contracts, business rules, and shared Compose code in `commonMain` whenever they have no platform API dependency.
4. Put Android, Desktop, and iOS integration in their corresponding source sets. Do not import Android, JVM, ADB, filesystem, or driver APIs into `commonMain`.
5. When a shared contract changes, find and update every platform implementation and relevant Koin binding.
6. Treat production code, its unit tests, and UI previews as one feature slice; do not declare the feature complete with tests deferred.

## Feature implementation

Follow [references/feature-structure.md](references/feature-structure.md) for package layout and file organization.

- Presentation invokes UseCases through a ViewModel; composables receive state and callbacks only.
- UseCases own business decisions, validation, and orchestration. Repository implementations own external access and error translation.
- Define repository contracts in `domain/repository`; inject implementations through Koin `platformModule()` or another composition root.
- Add a mapper only for a meaningful boundary transformation. Do not create one-to-one wrappers with no domain or platform purpose.
- Keep pure models in `domain/model`. Keep a `Params` data class nested in its UseCase only when it is private to that UseCase; otherwise create a dedicated model file.
- Never hardcode user-visible strings in composables, ViewModels, UseCases, or UI models. The sole exception is deterministic sample copy inside a function annotated as a Compose preview. Add every production UI string to `shared/src/commonMain/kotlin/com/crackmydroid/shared/i18n/Strings.kt` for all languages already supported by the project. Read strings from `LocalStrings.current` in Compose UI; pass or inject the `Strings` model into ViewModels, UseCases, and other non-composable logic according to the existing conventions. Update preview fixtures whenever a new user-visible string is introduced. Hardcoded technical constants that are not shown to users, such as identifiers, keys, codes, regular expressions, and protocol values, are allowed.
- Name a screen entry point `XxxScreen`; it owns state collection and dependency wiring only. Render its stateless visual subtree in `XxxContent`, which receives state and callbacks explicitly. Name reusable Compose widgets with meaningful hierarchy or interaction state `XxxComponent`; keep simple primitives such as `AppText`, `AppButton`, and `PackageIcon` descriptive without an artificial suffix.
- Use immutable state and explicit loading, success, empty, and error states when visible to the user. Use `BaseViewModel`, injectable dispatchers, and cancellable coroutines.

## Readability and comments

- Name classes, functions, properties, parameters, and files by their domain intent and responsibility. Prefer `AnalyzePermissionRisksUseCase`, `loadInstalledApps`, and `isSnapshotAvailable` to ambiguous names such as `data`, `value`, `item`, `helper`, or `manager`.
- Prefer short functions, guard clauses, clear expressions, and well-named intermediate values over deep nesting or implicit transformations.
- Extract a private function when it gives a non-trivial business, mapping, validation, or platform step a recognizable name.
- Add comments for the **why** of complex logic: security invariants, algorithmic choices, cache behavior, concurrency, platform limitations, interoperability, and workarounds.
- Do not add comments that merely restate self-explanatory code. Ordinary code must remain readable through its structure and names.
- Treat 250 lines as the target maximum for a file. Before exceeding it, separate distinct responsibilities; do not split a small cohesive model simply to meet a number.

## Compose UI and previews

- For every new screen or material visual component, create deterministic preview data with no DI, real ViewModel, network, database, ADB, or filesystem dependency.
- Use `CrackMyDroidTheme`, `LocalStrings`, and the existing `@AppScreenPreviews` annotation whenever applicable. It supplies phone/tablet and light/dark previews.
- Add an explicit 7-inch preview (`widthDp = 600`, `heightDp = 960`) when the screen’s layout or behavior depends on that breakpoint.
- Preview the normal state and each user-visible empty or error state. Keep preview functions separate from production screen logic when that improves readability.

## Unit tests are part of every feature

Every new feature or behavior change must create or update the tests that protect it in the same implementation cycle. A feature without meaningful tests is incomplete unless a technical limitation is explicitly documented.

- Create UseCase tests for success, failure, boundary/empty input, and meaningful collaborator effects when applicable.
- Create ViewModel tests for initial, loading, success, error, and retry states when applicable.
- Create mapper tests for valid, empty, and malformed input when the mapper contains non-trivial logic.
- Create repository/adapter tests for result mapping, error translation, command construction, persistence, or other observable side effects.
- Select the test source set from [references/platform-matrix.md](references/platform-matrix.md); use MockK and coroutine-test on JVM/Android and portable fakes on common/iOS.
- Do not write tests that only increase line coverage, assert private implementation details, or verify calls without a behavioral reason.

The minimum feature completion contract is:

```text
production code + meaningful unit tests + previews when applicable + Gradle validation
```

## Tests and validation

- Execute the tests created or updated for the feature before reporting completion.
- Use MockK and coroutine-test for JVM/Android collaborators; use platform-compatible fakes for Native and platform boundaries. Never require a real device, ADB instance, network, or user filesystem.
- Run the narrowest applicable Gradle compile/test task from the platform matrix, then widen only for shared contract changes.
- Before finishing, check preview fixtures, modified-file line counts, architectural dependency direction, meaningful comments for complex code, and `git diff --check`.
- Verify that no new user-visible strings are hardcoded and that every new UI string is represented in `Strings.kt` for all currently supported languages.
- Report production files and test files separately, chosen source set, dependencies/bindings added, test cases covered, test commands run, previews supplied, and any platform limitations.
