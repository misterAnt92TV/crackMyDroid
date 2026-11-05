# Feature structure and UI conventions

## Start with the smallest complete slice

For a feature that changes application behavior, create only the parts justified by the behavior:

```text
domain/model/FeatureResult.kt                 # shared and reusable model
domain/repository/FeatureRepository.kt        # external capability contract
domain/usecase/feature/LoadFeatureUseCase.kt  # one application intention
data/.../FeatureRepositoryAndroid.kt           # platform implementation when needed
ui/feature/FeatureViewModel.kt                 # state orchestration
ui/feature/FeatureState.kt                     # if state is not trivial
ui/screens/FeatureScreen.kt                    # public screen and pure content
ui/screens/FeatureScreenPreview.kt             # fixtures and previews
domain/usecase/feature/LoadFeatureUseCaseTest.kt # corresponding unit tests in the test source set
```

Do not create all production files by default, but always create the corresponding tests for the behavior implemented. A pure local transformation may need only a model, UseCase, and UseCase test; an existing repository may mean no new adapter or Koin entry, but its contract behavior still requires tests when changed.

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

- Promote `Params` to a dedicated file when multiple UseCases, ViewModels, or platform adapters share it.
- Do not return UI state or Android/JVM types from a UseCase or repository contract.

## Mapper decision

Create a mapper only when it converts across a real boundary, for example API/ADB/SQL model to domain model, domain model to UI model, or serialization payload to stable contract. Keep the mapper close to that boundary and test non-trivial mappings. Do not wrap identical data classes merely to introduce a mapper.

## ViewModel and UI

- State is immutable and exposes only what the screen needs.
- Events have names that describe the user action: `onRefreshRequested`, `onAppSelected`, `onRetryRequested`.
- ViewModel functions call UseCases and translate outcomes into state; they do not compose UI.
- Screen functions delegate visual rendering to content functions when that keeps preview data and callbacks simple.
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

Use a second preview state for user-visible empty or error conditions. If a 7-inch breakpoint affects the layout, add a named `@Preview` with `widthDp = 600` and `heightDp = 960`, in both light and night mode when the visual design changes with theme.

## File size and readability gate

At 200 lines, check whether the file already has multiple responsibilities. At 250 lines, split by responsibility unless the code is a single coherent algorithm or declarative screen that would become harder to follow when fragmented.

Names should expose intent. Comments document decisions that are not obvious from names and control flow, especially security decisions, cache invalidation, concurrency sequencing, platform workarounds, and external-format assumptions.

## Feature completion gate

Before considering a feature complete, verify that:

- the UseCase, ViewModel, mapper, repository, or adapter behavior changed by the feature has a corresponding focused test;
- success and at least one meaningful error, empty, or boundary case are covered where the behavior supports them;
- asynchronous state transitions use deterministic coroutine testing;
- test files are placed in the source set matching the production platform;
- the relevant Gradle test task has executed successfully.
