# Feature → Test Map

Mappa rapida tra funzionalità core e suite di test esistenti, utile per review e gap analysis.

| Feature                           | Componenti principali                                 | Test principali                                                                                      |
|-----------------------------------|-------------------------------------------------------|------------------------------------------------------------------------------------------------------|
| Wiring shell / schermate          | `presentationModule`, `AppPresentationDependencies`, `AppShellViewModel` | `shared/src/jvmCommonTest/kotlin/com/crackmydroid/shared/presentation/AppPresentationDependenciesTest.kt` (caricamento DeviceInfo e altre schermate, inizializzazione singola, settings condivise e isolamento tra grafi) |
| DeviceInfo                        | `InfoViewModel`, `BuildDeviceInfoReportUseCase`         | `shared/src/jvmCommonTest/kotlin/com/crackmydroid/shared/presentation/InfoViewModelTest.kt` (caricamento, errori, export e condivisione) |
| Impostazioni multilingua          | `SettingsContent`, `Strings`, `SettingsViewModel`      | `shared/src/jvmCommonTest/kotlin/com/crackmydroid/shared/i18n/SettingsStringsTest.kt`, `LanguageResolverTest.kt`, `presentation/SettingsViewModelTest.kt` (IT/EN/DE/FR/ES: sezioni, opzioni, descrizioni, metadati, aiuto, accessibilità e cambio lingua) |
| PenTesting workflow               | `PenTestingViewModel`, `RunPenTestChecksUseCase`      | `shared/src/jvmCommonTest/kotlin/com/crackmydroid/shared/presentation/PenTestingViewModelTest.kt` (query, wizard, selezione check e stato derivato) |
| Permessi app                      | `PermissionsViewModel`                                | `shared/src/jvmCommonTest/kotlin/com/crackmydroid/shared/presentation/PermissionsViewModelTest.kt`   |
| Attività app                      | `ActivityListViewModel`                               | `shared/src/jvmCommonTest/kotlin/com/crackmydroid/shared/presentation/ActivityListViewModelTest.kt`  |
| App installate / rischio manifest | `InstalledAppsViewModel`, `AppManifestSecurity`       | `shared/src/jvmCommonTest/kotlin/com/crackmydroid/shared/presentation/InstalledAppsViewModelTest.kt` |
| Use case domain (45)              | Deleghe, settings, analisi permessi, check PenTesting, report | [Matrice completa dei test diretti](testing-usecases-models.md#copertura-diretta-degli-usecase) |
| Parsing/ADB Desktop               | parser/repository desktop                             | `docs/testing-desktop-adb.md`                                                                        |
| Log workflow                      | `LogViewModel`, `GetLogsUseCase`, `ExportLogsUseCase` | `shared/src/jvmCommonTest/kotlin/com/crackmydroid/shared/presentation/LogViewModelTest.kt`           |
| Root and Play Integrity           | `RootViewModel`, root use cases                       | `shared/src/jvmCommonTest/kotlin/com/crackmydroid/shared/presentation/RootViewModelTest.kt`          |
| Report export                     | `ExportReportUseCase`                                 | `shared/src/jvmCommonTest/kotlin/com/crackmydroid/shared/domain/usecase/reports/ExportReportUseCaseTest.kt` |
| SQLDelight schema e cache         | `CacheDatabaseSchema`, `CacheDatabase`                | `shared/src/jvmCommonTest/kotlin/com/crackmydroid/shared/data/db/CacheDatabaseMigrationTest.kt` (SQLite reale: upgrade v1/v2, schemi parziali/completi, mapping colonne, rollback, callback, riapertura, invalidazione selettiva e query plan delle PK) |

Tutti i 45 UseCase hanno test diretti nel source set `jvmCommonTest`, eseguito tramite
`desktopTest` e `testDebugUnitTest`. I test dei ViewModel restano complementari e non sostituiscono
le verifiche delle regole o degli effetti degli UseCase reali.

## Compose component regression checks

Run `bash scripts/check-ui-component-consistency.sh` to reject `TODO()` and
`NotImplementedError` in screens, as well as local `AppCard` / `ExportDetailsCard`
declarations that bypass the shared implementations. These checks complement the
ViewModel tests: compilation alone does not detect a `TODO()` executed during composition.

Manual UI regression scenarios: open installed apps, apply a filter with no matches,
reset the filter and refresh the list; the empty-state card must render without a crash.
With test data, verify export details in Activity and PenTesting use the shared card.
Export validation must not expose real device data.

The same guard rejects hardcoded copy in Settings text, titles, labels and buttons.
`SettingsScreenPreview.kt` supplies five-language fixtures plus missing SDK/permissions
and synthetic-error variants, without DI or platform access. Preview fixtures are not
automated UI rendering tests. Manual localization scenario: switch language in Settings,
review all sections and help, then restore the initial language; do not clear suggestions
or change unrelated preferences. Technical package/permission identifiers and original
repository/OS diagnostic messages intentionally remain unchanged.

## SQLDelight migration regression checks

`CacheDatabaseMigrationTest` runs on Android JVM and desktop with the test-only SQLite
JDBC driver and synthetic legacy fixtures. It reproduces missing-column inserts,
then verifies the actual `CacheDatabaseSchema` upgrade, generated reads/writes and
preservation of unaffected rows. It also checks complete DeviceInfo invalidation.
SQL-only migration verification does not exercise the conditional Kotlin callback.

Manual Android upgrade: retain the installed database, apply the new build, check
schema version/column metadata and verify installed-app/activity scans without SQL
errors. Do not copy the device database or export real application inventory for tests.

## Provenance and integrity verification

The installed-app provenance path is covered by `AdbParsersTest` (reported Google Play and missing
installer) and `DeviceScanSnapshotJsonTest` (serialized provenance). Backend verification is an
external HTTP boundary and must be tested with a fake backend response; no test may use Google
credentials or a real service account.

`IntegrityBackendClientTest` uses Ktor `MockEngine` to cover a verified verdict, request-binding
mismatch, HTTP failure and malformed JSON without sending a token to a real endpoint.
