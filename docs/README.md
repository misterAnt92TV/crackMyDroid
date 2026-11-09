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

| File                                                                       | Descrizione                                                                                                                |
|----------------------------------------------------------------------------|----------------------------------------------------------------------------------------------------------------------------|
| [`../README.md`](../README.md)                                             | Overview del progetto, quick start e stack tecnico in inglese                                                              |
| [`../README.it.md`](../README.it.md)                                       | Guida del progetto in italiano: funzionalità, schermate, architettura e setup                                              |
| [`functional-guide.md`](functional-guide.md)                               | **Guida funzionale IT/EN** — sezioni, azioni, casi d'uso supportati, prerequisiti e limiti Android/Desktop/iOS             |
| [`functional-flows.drawio`](functional-flows.drawio)                       | **Flowchart IT/EN** — sei pagine modificabili: mappa funzionale, flussi operativi e Desktop/piattaforme                    |
| [`screens.md`](screens.md)                                                 | **Schermate UI** — mockup ASCII di tutte le schermate (Home, PenTesting, App, Permessi, Attività, Info, Settings, Desktop) |
| [`android-screenshots-en.md`](android-screenshots-en.md)                   | English Android runtime screenshot gallery, manifest, and reproduction guide                                               |
| [`architecture.md`](architecture.md)                                       | Architettura KMP, layer, flussi dati Android e Desktop, caching, DI, manifest analyzer                                     |
| [`architecture-specification.md`](architecture-specification.md)           | **Specifica architetturale** — principi, source set, layer, flussi, DI, persistenza, vincoli e strategia di test           |
| [`database-architecture.drawio`](database-architecture.drawio)             | **Schema database e persistenza** — tabelle SQLDelight, chiavi, componenti e storage per piattaforma                       |
| [`clean-code-structure.md`](clean-code-structure.md)                       | **Struttura Clean Code** — responsabilità, naming, SOLID, anti-pattern, workflow feature e checklist review                |
| [`clean-code-structure.drawio`](clean-code-structure.drawio)               | Diagramma Draw.io della struttura Clean Code e delle dipendenze KMP                                                        |
| [`developer-guide.md`](developer-guide.md)                                 | Setup ambiente, comandi Gradle, struttura test, guida aggiunta feature, convenzioni, troubleshooting                       |
| [`developer-guide.md#compose-preview`](developer-guide.md#compose-preview) | Convenzioni per preview Compose rappresentative, stato demo e annotation condivise                                        |
| [`api-reference.md`](api-reference.md)                                     | Domain models, repository interfaces, use cases, ViewModels/state, caching API, utilities                                  |
| [`play-integrity-backend-contract.md`](play-integrity-backend-contract.md) | Secure client/backend contract for decoded Play Integrity verdicts; no credentials in the app or repository                |

### Test

| File                                                       | Descrizione                                                                                            |
|------------------------------------------------------------|--------------------------------------------------------------------------------------------------------|
| [`testing-overview.md`](testing-overview.md)               | **Indice test** — struttura suite, source set, dipendenze, comandi Gradle, pattern, mappa di copertura |
| [`testing-feature-map.md`](testing-feature-map.md)         | **Feature → Test Map** — tracciabilità veloce tra funzionalità e test principali, con gap noti         |
| [`testing-viewmodels.md`](testing-viewmodels.md)           | Dettagli di sei suite ViewModel selezionate; l'inventario completo è in `testing-overview.md`          |
| [`testing-usecases-models.md`](testing-usecases-models.md) | Test use case, serializzazione `DeviceScanSnapshot`, `SearchHistoryStore`                              |
| [`testing-desktop-adb.md`](testing-desktop-adb.md)         | Test Desktop/ADB: parser, bridge, repository, session controller, snapshot importer e report PDF/JSON  |

### Design

| File                                                               | Descrizione                                                                           |
|--------------------------------------------------------------------|---------------------------------------------------------------------------------------|
| [`design/penpot/README.md`](design/penpot/README.md)               | Sorgente Penpot, pagine richieste e procedura di backup/ripristino                    |
| [`design/penpot/export/README.md`](design/penpot/export/README.md) | Bundle SVG di schermate e componenti importabili in Penpot, con manifest e istruzioni |
| [`design/exports/README.md`](design/exports/README.md)             | Setup del flusso Penpot-to-Compose, export versionati e policy per gli agenti         |

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
- [Android runtime screenshot gallery (English)](android-screenshots-en.md)

### Design

- [Sorgente Penpot e backup](design/penpot/README.md)
- [Bundle SVG importabile in Penpot](design/penpot/export/README.md)
- [Handoff Penpot-to-Compose](design/exports/README.md)

### Architettura e sviluppo

- [Architettura KMP](architecture.md#visione-dinsieme--overview)
- [Specifica architetturale](architecture-specification.md)
- [Schema database e componenti Draw.io](database-architecture.drawio)
- [Struttura Clean Code](clean-code-structure.md)
- [Schema Clean Code Draw.io](clean-code-structure.drawio)
- [Flusso dati Android](architecture.md#flusso-dati-android--android-data-flow)
- [Flusso dati Desktop](architecture.md#flusso-dati-desktop--desktop-data-flow)
- [Caching & Paginazione](architecture.md#caching--paginazione--caching--pagination)
- [Aggiungere una funzionalità](developer-guide.md#aggiungere-una-nuova-funzionalità--adding-a-new-feature)
- [Convenzioni Compose Preview](developer-guide.md#compose-preview)
- [Troubleshooting](developer-guide.md#risoluzione-problemi-comuni--troubleshooting)
- [API Reference — Domain Models](api-reference.md#domain-models)

### Test

- [Comandi Gradle test](testing-overview.md#comandi-gradle)
- [Pattern ViewModel test](testing-overview.md#pattern-viewmodel-esempi)
- [Pattern Repository Desktop](testing-overview.md#pattern-repository-desktop-esempi)
- [Mappa di copertura](testing-overview.md#mappa-di-copertura)
- [PenTestingViewModelTest](testing-viewmodels.md#pentestingviewmodeltest)
- [AdbParsersTest](testing-desktop-adb.md#adbparserstest)
- [AppManifestSecurityTest](testing-desktop-adb.md#appmanifestsecuritytest)
- [DeviceSessionControllerDesktopTest](testing-desktop-adb.md#devicesessioncontrollerdesktoptest)
- [RemoteDeviceSnapshotImporterTest](testing-desktop-adb.md#remotedevicesnapshotimportertest)
