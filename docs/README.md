# CrackMyDroid — Documentazione

Benvenuto nella documentazione tecnica di CrackMyDroid.

Per una lettura funzionale in **italiano e inglese**, partire dalla
[guida delle sezioni e dei casi d'uso](functional-guide.md) e dal
[flowchart Draw.io modificabile](functional-flows.drawio).
For a functional overview in **Italian and English**, start with the
[section and workflow guide](functional-guide.md#english) and the
[editable Draw.io flowchart](functional-flows.drawio). Both describe implemented features only.

---

## Documenti disponibili

### Progetto

| File                                       | Descrizione                                                                                                                |
|--------------------------------------------|----------------------------------------------------------------------------------------------------------------------------|
| [`../README.md`](../README.md)             | Overview del progetto, quick start, stack tecnico (IT + EN)                                                                |
| [`functional-guide.md`](functional-guide.md) | **Guida funzionale IT/EN** — sezioni, azioni, casi d'uso supportati, prerequisiti e limiti Android/Desktop/iOS |
| [`functional-flows.drawio`](functional-flows.drawio) | **Flowchart IT/EN** — sei pagine modificabili: mappa funzionale, flussi operativi e Desktop/piattaforme |
| [`screens.md`](screens.md)                 | **Schermate UI** — mockup ASCII di tutte le schermate (Home, PenTesting, App, Permessi, Attività, Info, Settings, Desktop) |
| [`architecture.md`](architecture.md)       | Architettura KMP, layer, flussi dati Android e Desktop, caching, DI, manifest analyzer                                     |
| [`architecture-specification.md`](architecture-specification.md) | **Specifica architetturale** — principi, source set, layer, flussi, DI, persistenza, vincoli e strategia di test |
| [`clean-code-structure.md`](clean-code-structure.md) | **Struttura Clean Code** — responsabilità, naming, SOLID, anti-pattern, workflow feature e checklist review |
| [`clean-code-structure.drawio`](clean-code-structure.drawio) | Diagramma Draw.io della struttura Clean Code e delle dipendenze KMP |
| [`developer-guide.md`](developer-guide.md) | Setup ambiente, comandi Gradle, struttura test, guida aggiunta feature, convenzioni, troubleshooting                       |
| [`api-reference.md`](api-reference.md)     | Domain models, repository interfaces, use cases, ViewModels/state, caching API, utilities                                  |

### Test

| File                                                       | Descrizione                                                                                                               |
|------------------------------------------------------------|---------------------------------------------------------------------------------------------------------------------------|
| [`testing-overview.md`](testing-overview.md)               | **Indice test** — struttura suite, source set, dipendenze, comandi Gradle, pattern, mappa di copertura                    |
| [`testing-feature-map.md`](testing-feature-map.md)         | **Feature → Test Map** — tracciabilità veloce tra funzionalità e test principali, con gap noti                            |
| [`testing-viewmodels.md`](testing-viewmodels.md)           | Test ViewModel: PenTesting, Permissions, ActivityList, InstalledApps, Log, Trick — fixture, mock e assertions dettagliate |
| [`testing-usecases-models.md`](testing-usecases-models.md) | Test use case, serializzazione `DeviceScanSnapshot`, `SearchHistoryStore`                                                 |
| [`testing-desktop-adb.md`](testing-desktop-adb.md)         | Test layer Desktop/ADB: parser, `ProcessAdbBridge`, manifest analyzer, repository, session controller, snapshot importer  |

---

## Quick links

### Funzionalità

- [Guida funzionale — Italiano](functional-guide.md#italiano)
- [Functional guide — English](functional-guide.md#english)
- [Flowchart Draw.io IT/EN](functional-flows.drawio)
- [Home Screen](screens.md#2-home)
- [PenTesting Wizard](screens.md#5-pentesting-wizard-4-step)
- [App installate](screens.md#6-app-installate--installed-apps)
- [Permessi](screens.md#8-permessi--permissions)
- [Attività](screens.md#7-attività--activities)
- [Info dispositivo](screens.md#3-info-dispositivo--device-info)
- [Desktop App screens](screens.md#desktop-app--schermate-specifiche)
- [Aiuto / Help](screens.md#12-aiuto--help)

### Architettura e sviluppo

- [Architettura KMP](architecture.md#visione-dinsieme--overview)
- [Specifica architetturale](architecture-specification.md)
- [Struttura Clean Code](clean-code-structure.md)
- [Schema Clean Code Draw.io](clean-code-structure.drawio)
- [Flusso dati Android](architecture.md#flusso-dati-android--android-data-flow)
- [Flusso dati Desktop](architecture.md#flusso-dati-desktop--desktop-data-flow)
- [Caching & Paginazione](architecture.md#caching--paginazione--caching--pagination)
- [Aggiungere una funzionalità](developer-guide.md#aggiungere-una-nuova-funzionalità--adding-a-new-feature)
- [Troubleshooting](developer-guide.md#risoluzione-problemi-comuni--troubleshooting)
- [API Reference — Domain Models](api-reference.md#domain-models)

### Test

- [Comandi Gradle test](testing-overview.md#comandi-gradle)
- [Pattern ViewModel test](testing-overview.md#pattern-viewmodel-tutti-i-6-viewmodel-test)
- [Pattern Repository Desktop](testing-overview.md#pattern-repository-desktop-tutti-i-6-repository-test)
- [Mappa di copertura](testing-overview.md#mappa-di-copertura)
- [PenTestingViewModelTest](testing-viewmodels.md#pentestingviewmodeltest)
- [AdbParsersTest](testing-desktop-adb.md#adbparserstest)
- [AppManifestSecurityTest](testing-desktop-adb.md#appmanifestsecuritytest)
- [DeviceSessionControllerDesktopTest](testing-desktop-adb.md#devicesessioncontrollerdesktoptest)
- [RemoteDeviceSnapshotImporterTest](testing-desktop-adb.md#remotedevicesnapshotimportertest)
