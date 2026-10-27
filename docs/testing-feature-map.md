# Feature → Test Map

Mappa rapida tra funzionalità core e suite di test esistenti, utile per review e gap analysis.

| Feature | Componenti principali | Test principali |
|---|---|---|
| PenTesting workflow | `PenTestingViewModel`, `RunPenTestChecksUseCase` | `shared/src/jvmCommonTest/kotlin/com/crackmydroid/shared/presentation/PenTestingViewModelTest.kt` |
| Permessi app | `PermissionsViewModel` | `shared/src/jvmCommonTest/kotlin/com/crackmydroid/shared/presentation/PermissionsViewModelTest.kt` |
| Attività app | `ActivityListViewModel` | `shared/src/jvmCommonTest/kotlin/com/crackmydroid/shared/presentation/ActivityListViewModelTest.kt` |
| App installate / rischio manifest | `InstalledAppsViewModel`, `AppManifestSecurity` | `shared/src/jvmCommonTest/kotlin/com/crackmydroid/shared/presentation/InstalledAppsViewModelTest.kt` |
| Use case domain core | `RunPenTestChecksUseCase`, `ExportReportUseCase` | `shared/src/jvmCommonTest/kotlin/com/crackmydroid/shared/domain/usecase/UseCaseTests.kt` |
| Parsing/ADB Desktop | parser/repository desktop | `docs/testing-desktop-adb.md` |
| Log workflow | `LogViewModel`, `GetLogsUseCase`, `ExportLogsUseCase` | `shared/src/jvmCommonTest/kotlin/com/crackmydroid/shared/presentation/LogViewModelTest.kt` |
| Root and Play Integrity | `RootViewModel`, root use cases | `shared/src/jvmCommonTest/kotlin/com/crackmydroid/shared/presentation/RootViewModelTest.kt` |
| Report export | `ExportReportUseCase` | `shared/src/jvmCommonTest/kotlin/com/crackmydroid/shared/domain/usecase/UseCaseTests.kt` |

La copertura dei flussi core elencati è ora esplicita nella suite desktop condivisa.
