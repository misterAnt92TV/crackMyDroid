# CrackMyDroid — Struttura Clean Code

Questo documento definisce come organizzare nuovo codice e modifiche nel progetto. La struttura deve rendere espliciti responsabilità, confini e dipendenze, mantenendo il comportamento verificabile con test unitari.

## 1. Responsabilità per layer

| Layer | Deve contenere | Non deve contenere |
|---|---|---|
| UI/Presentation | composable, eventi, stato, ViewModel, navigazione | chiamate dirette a SDK, SQLDelight o ADB |
| Domain | modelli, contratti, UseCase, regole e analizzatori | Compose, Context, Activity, Koin globale, filesystem |
| Data | repository concreti, mapper, cache, persistenza e adapter | stato UI o decisioni di navigazione |
| Platform | API native, processi, driver, entry point e configurazione | regole applicative duplicate |
| Infrastructure | DI, serializzazione, logging, dispatcher e database | logica di feature non riutilizzabile |

La regola pratica è: il codice più vicino al dominio deve conoscere meno dettagli esterni.

## 2. ViewModel e stato

- Un ViewModel espone stato immutabile e azioni pubbliche comprensibili dalla UI.
- Le trasformazioni di input e le chiamate ai UseCase avvengono nel ViewModel o nel Domain, non nel composable.
- Loading, successo, errore e stato vuoto devono essere rappresentati esplicitamente quando hanno significato per l'utente.
- Le coroutine usano lo scope del ViewModel e devono essere cancellabili.
- `onCleared()` deve rilasciare lo scope e non lasciare lavori in background.
- Il ViewModel non istanzia repository: riceve le dipendenze dal composition root.

## 3. UseCase e dominio

- Un UseCase rappresenta un'intenzione applicativa, non un generico helper.
- Il nome deve usare un verbo o un'intenzione: `ListInstalledApps`, `AnalyzePermissionRisks`, `ExportReport`.
- Le dipendenze sono interfacce di dominio o servizi esplicitamente definiti.
- La validazione di input e la normalizzazione dei risultati devono essere deterministiche.
- La logica pura deve restare in `commonMain` quando non necessita di API native.
- Le decisioni di business non devono essere duplicate nei ViewModel e nei repository.

## 4. Repository e adapter

- Le interfacce vivono in `domain/repository` o in un contratto comune chiaramente nominato.
- L'implementazione vive nel source set che possiede la tecnologia necessaria.
- Un repository traduce il mondo esterno in modelli o risultati di dominio.
- Un adapter non deve esporre tipi Android, JVM, ADB o SQLDelight oltre il proprio confine.
- Errori di rete, processo, parsing o permessi devono essere convertiti in un risultato gestibile.
- Cache e retry sono responsabilità del Data layer e non devono alterare silenziosamente il contratto.

## 5. Modelli, risultati ed errori

- I modelli di dominio descrivono dati e invarianti, non dettagli di rendering.
- Preferire `data class`, sealed hierarchy e tipi espliciti a mappe non tipizzate.
- `ResultWrapper` o tipi equivalenti devono distinguere successo, errore e dati mancanti quando necessario.
- Non usare `null` per rappresentare indistintamente errore, loading e assenza legittima.
- I messaggi destinati alla UI devono essere adattati nel layer di presentazione; il dominio restituisce informazioni semantiche.

## 6. Naming e package

- Package minuscoli, organizzati per responsabilità e feature: `domain/usecase/permissions`, `ui/pentest`, `data/adb`.
- I nomi platform-specific terminano con il target quando l'implementazione non è già evidente dal source set: `...Android`, `...Desktop`, `...Ios`.
- I test terminano con `Test` e descrivono il target testato.
- I metodi pubblici descrivono intenzioni, non dettagli implementativi (`loadPermissions`, non `executeStep2`).
- Evitare classi `Utils`, `Manager` o `Helper` generiche se una responsabilità più precisa è possibile.

## 7. SOLID applicato al progetto

- **Single Responsibility**: ViewModel, UseCase, repository e mapper hanno responsabilità separate.
- **Open/Closed**: nuovi adapter di piattaforma implementano contratti esistenti senza modificare il dominio.
- **Liskov Substitution**: fake e implementazioni reali devono rispettare lo stesso contratto osservabile.
- **Interface Segregation**: preferire repository focalizzati a interfacce monolitiche.
- **Dependency Inversion**: Presentation e Domain dipendono da astrazioni; Koin collega le implementazioni.

## 8. Anti-pattern da evitare

- Composable che chiama repository o accede direttamente a `Context`.
- ViewModel che crea dipendenze con costruttori concreti o locator globali.
- UseCase che contiene codice specifico Android/Desktop.
- Repository che restituisce JSON grezzo, eccezioni native o tipi di UI.
- Copia della stessa regola in Android, Desktop e iOS.
- Mock di ogni oggetto, inclusi modelli e funzioni pure.
- Test che verificano solo numero di chiamate senza un effetto osservabile.
- Cache usata come unica fonte di verità o senza strategia di invalidazione.

## 9. Flusso per una nuova feature

1. Definire il comportamento e il modello di dominio in `commonMain`.
2. Definire il contratto del repository necessario.
3. Implementare il UseCase e i test per successo, errore e limite.
4. Implementare gli adapter nei source set platform-specific.
5. Collegare le implementazioni in `platformModule()`/Koin.
6. Creare stato, azioni e ViewModel.
7. Collegare il composable alla sola API del ViewModel.
8. Aggiungere test ViewModel con dispatcher deterministico e MockK sui collaboratori.
9. Aggiornare documentazione, navigazione e schema solo se la feature introduce un nuovo confine architetturale.

## 10. Checklist di code review

- [ ] La responsabilità della classe è descrivibile in una frase.
- [ ] Le dipendenze puntano verso contratti e non verso dettagli di piattaforma.
- [ ] La logica condivisibile è in `commonMain`.
- [ ] Le implementazioni platform-specific sono nel source set corretto.
- [ ] Gli errori e gli stati vuoti hanno un comportamento definito.
- [ ] Le coroutine sono cancellabili e testabili.
- [ ] I test verificano risultati o effetti reali.
- [ ] Non ci sono duplicazioni di regole tra layer o piattaforme.
- [ ] La cache ha invalidazione e fallback coerenti.
- [ ] La documentazione o il diagramma sono aggiornati se cambia un confine.
