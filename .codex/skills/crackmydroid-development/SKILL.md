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
- Enforce this unidirectional data flow for every feature:
  `User interaction → UI event/callback → ViewModel action → UseCase → Repository/data source → result → immutable StateFlow → Screen rendering`.
- Keep all application logic out of screens and composables. Put reusable business rules, validation,
  functional filtering/sorting, transformations, and collaborator orchestration in a UseCase. Put
  presentation orchestration, coroutine launching, event handling, retry, and mapping to `UiState`
  in the ViewModel.
- Screens and composables must only render immutable state and dispatch callbacks. They must not call
  UseCases, repositories, databases, SDKs, native APIs, Koin/DI, or application coroutines.
- Do not place application decisions in UI code: no domain filtering/sorting/mapping, validation,
  authorization, conditional navigation, persistence, `launch`, `async`, `runCatching`, or application
  error handling in a screen/composable. Do not keep mutable domain state with `remember` or
  `mutableStateOf`, and do not convert DTOs or domain models to UI models there.
- UI-only calculations are limited to local rendering mechanics such as layout, animation, dimensions,
  colors, accessibility, and transient state of a non-application control. If a decision affects data,
  behavior, or flow, move it to the ViewModel or UseCase.
- A screen contract consists of immutable `UiState`, explicit event callbacks/event dispatcher, and
  explicitly declared visual dependencies. Components must not use a service locator or obtain Koin
  dependencies directly.
- Every ViewModel constructor dependency, including every UseCase, must be non-null and must not use a `null` default. Model optional behavior with an explicit non-null dependency; configure test fakes and mocks explicitly rather than omitting dependencies.
- UseCases own business decisions, validation, and orchestration. Repository implementations own external access and error translation.
- Define repository contracts in `domain/repository`; inject implementations through Koin `platformModule()` or another composition root.
- Add a mapper only for a meaningful boundary transformation. Do not create one-to-one wrappers with no domain or platform purpose.
- Keep pure models in `domain/model`. Keep a `Params` data class nested in its UseCase only when it is private to that UseCase; otherwise create a dedicated model file.
- Never hardcode user-visible strings in composables, ViewModels, UseCases, or UI models. The sole exception is deterministic sample copy inside a function annotated as a Compose preview. Add every production UI string to `shared/src/commonMain/kotlin/com/crackmydroid/shared/i18n/Strings.kt` for all languages already supported by the project. Read strings from `LocalStrings.current` in Compose UI; pass or inject the `Strings` model into ViewModels, UseCases, and other non-composable logic according to the existing conventions. Update preview fixtures whenever a new user-visible string is introduced. Hardcoded technical constants that are not shown to users, such as identifiers, keys, codes, regular expressions, and protocol values, are allowed.
- Reusable components follow the same localization rule: runtime labels, actions, accessibility text, and status copy must come from `LocalStrings` or an explicit localized parameter. Preview-only examples may use deterministic fixture text after the preview annotation, but must not be copied into production defaults or fallback labels.
- Name a screen entry point `XxxScreen`; it owns state collection and dependency wiring only. Render its stateless visual subtree in `XxxContent`, which receives state and callbacks explicitly. Name reusable Compose widgets with meaningful hierarchy or interaction state `XxxComponent`; keep simple primitives such as `AppText`, `AppButton`, and `PackageIcon` descriptive without an artificial suffix.
- Use immutable state and explicit loading, success, empty, and error states when visible to the user. Use `BaseViewModel`, injectable dispatchers, and cancellable coroutines.

### Canonical feature slice and UDF events

Every new app section must use this structure in `commonMain` unless a platform boundary requires otherwise:

```text
ui/<feature>/
├── <Feature>ViewModel.kt
├── model/
│   ├── <Feature>UiState.kt
│   ├── <Feature>Event.kt
│   ├── <Feature>Effect.kt       # only for one-shot effects
│   └── <Feature>Item.kt         # only for complex UI list items
└── view/
    ├── <Feature>Screen.kt
    ├── <Feature>ScreenPreview.kt
    └── <Feature>Component.kt
```

- The ViewModel exposes `StateFlow<FeatureUiState>` and one public event entry point:
  `fun onEvent(event: FeatureEvent)`. Do not expose a growing set of application commands directly to the screen.
- `FeatureEvent` is a feature-specific sealed interface with typed events and minimal stable identifiers,
  for example `Refresh`, `FilterChanged`, `ItemSelected`, `FavoriteToggled`, `ExportRequested` and
  `RetryRequested`. Do not use stringly typed event names or untyped maps.
- `FeatureUiState` is immutable and UI-ready. It owns loading, empty, error and visible item state;
  domain objects must not be exposed directly through complex UI lists.
- Persistent rendering data belongs in `StateFlow`; transient snackbar, navigation, share and export
  notifications belong in an optional `SharedFlow<FeatureEffect>`. Effects must not be represented by
  one-time booleans in `FeatureUiState`.
- `FeatureScreen` collects state and wires `viewModel::onEvent`; `FeatureContent` receives only the
  immutable UI state, visual dependencies and event callback. It must not call ViewModel methods directly.
- The ViewModel handles the event `when`, launches cancellable work, invokes UseCases and maps results
  to `FeatureUiState`/`FeatureEffect`. UseCases retain business rules and repository orchestration.
- Static sections may use a minimal state and event contract, but must not acquire artificial repository
  or coroutine dependencies solely to satisfy the pattern.

### UDF and layer ownership

Use the following ownership rules when deciding where new logic belongs:

- **UseCase**: business rules, validation, domain transformations, functional filtering/sorting,
  repository orchestration, and reusable semantic error handling.
- **ViewModel**: UI event handling, coroutine lifecycle, UseCase coordination, loading/success/error/
  retry state transitions, and mapping domain outcomes to immutable `UiState`.
- **Screen/Composable**: declarative rendering and event forwarding only; never application logic.

Forbidden flow: `Screen → Repository` or `Screen → UseCase`.
Required flow: `Screen(uiState, onEvent) → ViewModel.onEvent → UseCase → Repository`.

When reviewing a feature, reject logic in the UI even if it is small or convenient. Move it to the
UseCase when it is a domain/application rule; move it to the ViewModel only when it is presentation
orchestration. Keep only strictly visual calculations in composables.

## Readability and comments

- Name classes, functions, properties, parameters, and files by their domain intent and responsibility. Prefer `AnalyzePermissionRisksUseCase`, `loadInstalledApps`, and `isSnapshotAvailable` to ambiguous names such as `data`, `value`, `item`, `helper`, or `manager`.
- Do not introduce fixed values as mutable variables or generic global `Constants`. Represent reused stable values with `const val`, a focused constants object, or an enum, and keep that declaration next to the contract or boundary that defines it (for example preference keys beside the settings contract and protocol identifiers beside the adapter). Do not extract one-off literals merely to reduce line length.
- Prefer short functions, guard clauses, clear expressions, and well-named intermediate values over deep nesting or implicit transformations.
- Extract a private function when it gives a non-trivial business, mapping, validation, or platform step a recognizable name.
- Add comments for the **why** of complex logic: security invariants, algorithmic choices, cache behavior, concurrency, platform limitations, interoperability, and workarounds.
- Do not add comments that merely restate self-explanatory code. Ordinary code must remain readable through its structure and names.
- Treat 250 lines as the target maximum for a file. Before exceeding it, separate distinct responsibilities; do not split a small cohesive model simply to meet a number.

## Compose UI and previews

- For every new screen or material visual component, create deterministic preview data with no DI, real ViewModel, network, database, ADB, or filesystem dependency.
- Use `CrackMyDroidTheme`, `LocalStrings`, and the existing `@AppScreenPreviews` annotation whenever applicable. It supplies phone/tablet and light/dark previews.
- Keep each reusable component's `@AppComponentPreviews` functions in the same Kotlin file as the component they preview. Do not create package-level aggregate preview files; shared theme/local setup belongs in `presentation.preview.ComponentPreviewContainer`.
- Use `@AppComponentPreviews` for isolated reusable components (light and dark variants) and reserve `@AppScreenPreviews` for complete screens (phone/tablet and light/dark variants). Keep preview functions private and place them after the production component.
- Every new reusable Compose UI component must include at least one private `@AppComponentPreviews` function in the same file, in the same implementation change. The preview is mandatory for completion, not optional follow-up work.
- Component previews must use deterministic fixture data, `ComponentPreviewContainer` (or the equivalent `CrackMyDroidTheme` and `LocalStrings` setup), and no-op callbacks. They must not create or resolve ViewModels, DI, repositories, filesystem access, network clients, or platform APIs.
- When a KMP Android library declares previews, keep `ui-tooling-preview` available to its source set and add `debugImplementation(libs.compose.ui.tooling)` to that same module. The latter provides `ComposeViewAdapter` to Android Studio's renderer; an app module dependency alone does not cover previews opened from the library.
- Preview fixtures must cover the normal state and every meaningful visual variant (for example expanded/collapsed, enabled/disabled, empty/error, accessibility preferences, optional content, action availability, or heterogeneous status values) while keeping callbacks no-op and data deterministic.
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

Documentation is part of every feature completion: when behavior, public contracts, persistence,
UI, platform integration, or a security limitation changes, update the relevant README and `docs/`
references in the same change. Verify affected documentation links and ensure the documented
contract matches the implementation.

For UDF/architecture changes, also update `docs/architecture-specification.md`,
`docs/clean-code-structure.md`, and `references/feature-structure.md` in the same cycle.

## REST networking

- Use Ktor Client exclusively for REST calls. Do not introduce Retrofit, `HttpURLConnection`, raw
  `URL` connections, or hand-built JSON request bodies.
- Keep DTOs serializable, transport details in data adapters, and HTTP/status/error mapping in one
  focused client boundary. Domain contracts must not expose Ktor types.
- Configure a Ktor engine in the owning platform source set and shared plugins in common code.
- Enable diagnostic HTTP logging only for debuggable builds, at headers-only level. Never log
  request/response bodies, integrity tokens, bindings, cookies, authorization values, API keys, or
  other secrets; explicitly sanitize sensitive headers.
- Add `MockEngine` tests for each HTTP adapter's success, malformed response, status failure,
  network failure, and security validation paths. Never call a real backend in unit tests.

## Tests and validation

- Execute the tests created or updated for the feature before reporting completion.
- Treat a new reusable UI component without its same-file `@AppComponentPreviews` and meaningful fixture variants as incomplete, even when production code and tests pass.
- Use MockK and coroutine-test for JVM/Android collaborators; use platform-compatible fakes for Native and platform boundaries. Never require a real device, ADB instance, network, or user filesystem.
- Run the narrowest applicable Gradle compile/test task from the platform matrix, then widen only for shared contract changes.
- Before finishing, check preview fixtures, modified-file line counts, architectural dependency direction, meaningful comments for complex code, and `git diff --check`.
- During review, search UI directories for forbidden patterns such as repository/UseCase access,
  `viewModelScope`, `launch`, `async`, application `collect`, direct API calls, and domain
  transformations. Confirm that each new business rule has UseCase tests and each state/event
  transition has ViewModel tests; UI tests must remain limited to rendering and interaction.
- Verify that no new user-visible strings are hardcoded and that every new UI string is represented in `Strings.kt` for all currently supported languages.
- Report production files and test files separately, chosen source set, dependencies/bindings added, test cases covered, test commands run, previews supplied, and any platform limitations.
