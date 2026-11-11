# Feature structure and UI conventions

## Start with the smallest complete slice

For a feature that changes application behavior, create only the parts justified by the behavior:

```text
domain/model/FeatureResult.kt                    # shared and reusable model
domain/repository/FeatureRepository.kt           # external capability contract
domain/usecase/feature/LoadFeatureUseCase.kt     # one application intention
data/.../FeatureRepositoryAndroid.kt             # platform implementation when needed
ui/feature/FeatureViewModel.kt                   # event handling and state orchestration
ui/feature/model/FeatureUiState.kt               # immutable UI-ready state
ui/feature/model/FeatureEvent.kt                 # typed UDF input events
ui/feature/model/FeatureEffect.kt                # optional one-shot outputs
ui/feature/model/FeatureItem.kt                  # optional complex list item
ui/feature/view/FeatureScreen.kt                 # state wiring entry point
ui/feature/view/FeatureScreenPreview.kt          # deterministic screen fixtures
ui/feature/view/FeatureComponent.kt              # reusable visual components
domain/usecase/feature/LoadFeatureUseCaseTest.kt # corresponding unit test
```

Do not create all production files by default, but always create the corresponding tests for the
behavior implemented. A pure local transformation may need only a model, UseCase, and UseCase test;
an existing repository may mean no new adapter or Koin entry, but its contract behavior still
requires tests when changed.

## Models and parameters

- One public, reusable domain or UI model per file by default.
- Co-locate a small private model when it is inseparable from a single owner.
- A UseCase-specific request belongs inside that UseCase:

```kotlin
class ExportReportUseCase(private val repository: ReportRepository) {
    data class Params(
        val format: ExportFormat,
        val includeDetails: Boolean
    )

    suspend operator fun invoke(params: Params) = repository.export(params.format, params.includeDetails)
}
```

- Promote `Params` to a dedicated file when multiple UseCases, ViewModels, or platform adapters
  share it.
- Do not return UI state or Android/JVM types from a UseCase or repository contract.

## Mapper decision

Create a mapper only when it converts across a real boundary, for example API/ADB/SQL model to
domain model, domain model to UI model, or serialization payload to stable contract. Keep the mapper
close to that boundary and test non-trivial mappings. Do not wrap identical data classes merely to
introduce a mapper.

## ViewModel, UDF and UI

The development skill defines the architecture, coroutine, localization, and migration policy.
This reference only records the feature shape and UI ownership rules.

- State is immutable, implements `UiState`, and exposes only what the screen needs through the
  `StateFlow<FeatureUiState>` owned by `BaseViewModel`. The base receives the initial state and
  provides `currentState`, `updateState`, and `setState`; feature ViewModels never own a mutable
  state flow.
- For new features, define a feature-specific sealed `FeatureEvent : UiEvent()` and extend
  `BaseViewModel<FeatureEvent, FeatureUiState>`. Screens may call only the inherited
  `ViewModel.handleEvent(event)`;
  the ViewModel implements its protected exhaustive `onEvent(event)` hook. Do not add public command
  methods for screens. Existing features may retain their public API during a bounded change unless
  UDF migration is explicitly requested.
- Event names describe the user action and carry typed minimal data: `Refresh`, `AppSelected`,
  `FilterChanged`, `RetryRequested` or `ExportRequested`.
- ViewModel event handling launches cancellable work, calls UseCases and translates outcomes into
  state;
  it does not compose UI or duplicate business rules.
- Use `SharedFlow<FeatureEffect>` only for one-shot outcomes such as navigation, snackbar, share or
  export.
- Screen functions receive immutable UI state and the event dispatcher, delegate visual rendering to
  content functions when useful, and never call UseCases, repositories, APIs, Koin or application
  coroutines.
- Functional filtering, sorting, validation, mapping, persistence, conditional navigation, and
  application error handling belong in UseCases or ViewModels, never in composables.
- A screen must follow `Screen(uiState, onEvent)` and content must follow
  `Content(uiState, onEvent)`;
  the only permitted composable logic is local rendering mechanics such as layout, animation,
  dimensions,
  colors, accessibility and transient control state.
- Components must not obtain dependencies from Koin or construct ViewModels.

## Preview recipe

```kotlin
@AppScreenPreviews
@Composable
private fun FeatureScreenPreview() {
    CrackMyDroidTheme {
        CompositionLocalProvider(LocalStrings provides stringsFor("it")) {
            FeatureContent(
                state = FeatureState(...),
                onRetryRequested = {}
            )
        }
    }
}
```

Use a second preview state for user-visible empty or error conditions. If a 7-inch breakpoint
affects the layout, add a named `@Preview` with `widthDp = 600` and `heightDp = 960`, in both light
and night mode when the visual design changes with theme.

## Readability and completion

Use the development skill's readability and completion gates. For test source-set selection and
test-case requirements, use `crackmydroid-unit-tests` and the shared platform matrix.
