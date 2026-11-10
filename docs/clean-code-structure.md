# CrackMyDroid — Struttura Clean Code

Questo documento definisce come organizzare nuovo codice e modifiche nel progetto. La struttura deve
rendere espliciti responsabilità, confini e dipendenze, mantenendo il comportamento verificabile con
test unitari.

## 1. Responsabilità per layer

| Layer           | Deve contenere                                             | Non deve contenere                                   |
|-----------------|------------------------------------------------------------|------------------------------------------------------|
| UI/Presentation | composable, eventi, stato, ViewModel, navigazione          | chiamate dirette a SDK, SQLDelight o ADB             |
| Domain          | modelli, contratti, UseCase, regole e analizzatori         | Compose, Context, Activity, Koin globale, filesystem |
| Data            | repository concreti, mapper, cache, persistenza e adapter  | stato UI o decisioni di navigazione                  |
| Platform        | API native, processi, driver, entry point e configurazione | regole applicative duplicate                         |
| Infrastructure  | DI, serializzazione, logging, dispatcher e database        | logica di feature non riutilizzabile                 |

Le fondamenta KMP riusabili (`BaseUseCase`, `BaseViewModel`, contratti UDF, dispatcher e logger)
vivono in `:architectureModule`. `:shared` ospita `LocalizedBaseViewModel`, che aggiunge i cataloghi
di localizzazione specifici di CrackMyDroid al lifecycle comune.

La regola pratica è: il codice più vicino al dominio deve conoscere meno dettagli esterni.

### Localizzazione dei componenti

- Ogni testo visibile prodotto da screen e componenti runtime deve provenire da `LocalStrings` o da
  un parametro già localizzato.
- `Strings.kt` è la fonte unica per italiano, inglese, tedesco, spagnolo e francese; le nuove chiavi
  devono essere valorizzate in ogni catalogo.
- I testi runtime degli screen devono usare `LocalStrings.current`; per gruppi estesi e specifici
  della UI usare un catalogo localizzato dedicato costruito dalla lingua attiva. Sono ammessi
  literal solo per fixture di preview e valori tecnici non mostrati come copy.
- Le preview possono usare testo fixture deterministico soltanto dopo l'annotazione di preview e non
  devono diventare fallback della UI runtime.

## 2. ViewModel e stato

- Un ViewModel espone stato immutabile e azioni pubbliche comprensibili dalla UI.
- Una nuova feature espone `StateFlow<FeatureUiState>` e un unico `onEvent(FeatureEvent)` tipizzato;
  gli effetti transitori usano un flusso separato, non flag consumabili nello state.
- Le chiamate ai UseCase, la gestione degli eventi e le transizioni di stato avvengono nel ViewModel
  o nel Domain, mai nel composable.
- Loading, successo, errore e stato vuoto devono essere rappresentati esplicitamente quando hanno
  significato per l'utente.
- Le coroutine usano `BaseViewModel.launchTask` o `launchLatest(key)` e devono essere cancellabili;
  i flussi sostituibili non duplicano contatori di richiesta o mappe di job.
- `onCleared()` deve rilasciare lo scope e non lasciare lavori in background.
- Il ViewModel non istanzia repository: riceve le dipendenze dal composition root.
- Il ViewModel coordina la presentazione; non duplica regole di business già appartenenti al
  UseCase.

## 3. UseCase e dominio

- Un UseCase rappresenta un'intenzione applicativa, non un generico helper.
- Il nome deve usare un verbo o un'intenzione: `ListInstalledApps`, `AnalyzePermissionRisks`,
  `ExportReport`.
- Le dipendenze sono interfacce di dominio o servizi esplicitamente definiti.
- La validazione di input e la normalizzazione dei risultati devono essere deterministiche.
- La logica pura deve restare in `commonMain` quando non necessita di API native.
- Le decisioni di business non devono essere duplicate nei ViewModel e nei repository.
- Filtering, sorting, mapping e validazione funzionali appartengono al UseCase, non alla UI.
- Le operazioni principali sono `suspend Result<T>` e passano da `BaseUseCase.execute`;
  le scritture sono `Result<Unit>`, gli helper puri restano sincroni.
- La base non crea scope/job: propaga cancellazione e usa dispatcher CPU iniettabili
  solo per analisi/report. Il dispatching I/O rimane nei repository.
- Non usare `runCatching` sulle operazioni suspend, ignorare failure dei setter o
  restituire `Result<Result<T>>`. Nelle composizioni usare `getOrThrow` dentro
  un confine cancellation-safe.
- I log della base identificano operazione/esito/durata, mai payload o messaggi
  grezzi; debug segue Log verbosi, fallimenti error, cancellazione solo debug.
- Futuri stream espongono `Flow<Result<T>>`, senza catturare cancellazione o
  errori del collector; non aggiungere una base Flow priva di utilizzatori.

## 4. Repository e adapter

- Le interfacce vivono in `domain/repository` o in un contratto comune chiaramente nominato.
- L'implementazione vive nel source set che possiede la tecnologia necessaria.
- Un repository traduce il mondo esterno in modelli o risultati di dominio.
- Un adapter non deve esporre tipi Android, JVM, ADB o SQLDelight oltre il proprio confine.
- Errori di rete, processo, parsing o permessi devono essere convertiti in un risultato gestibile.
- Cache e retry sono responsabilità del Data layer e non devono alterare silenziosamente il
  contratto.

## 5. Modelli, risultati ed errori

- I modelli di dominio descrivono dati e invarianti, non dettagli di rendering.
- Preferire `data class`, sealed hierarchy e tipi espliciti a mappe non tipizzate.
- Gli use case usano Kotlin `Result<T>` per distinguere successo e fallimento;
  l'assenza legittima di dati mantiene la propria semantica nel valore di successo.
- Non usare `null` per rappresentare indistintamente errore, loading e assenza legittima.
- I messaggi destinati alla UI devono essere adattati nel layer di presentazione; il dominio
  restituisce informazioni semantiche.

## 6. Naming e package

- Package minuscoli, organizzati per responsabilità e feature: `domain/usecase/permissions`,
  `ui/pentest`, `data/adb`.
- I nomi platform-specific terminano con il target quando l'implementazione non è già evidente dal
  source set: `...Android`, `...Desktop`, `...Ios`.
- I test terminano con `Test` e descrivono il target testato.
- I metodi pubblici descrivono intenzioni, non dettagli implementativi (`loadPermissions`, non
  `executeStep2`).
- Evitare classi `Utils`, `Manager` o `Helper` generiche se una responsabilità più precisa è
  possibile.

## 7. SOLID applicato al progetto

- **Single Responsibility**: ViewModel, UseCase, repository e mapper hanno responsabilità separate.
- **Open/Closed**: nuovi adapter di piattaforma implementano contratti esistenti senza modificare il
  dominio.
- **Liskov Substitution**: fake e implementazioni reali devono rispettare lo stesso contratto
  osservabile.
- **Interface Segregation**: preferire repository focalizzati a interfacce monolitiche.
- **Dependency Inversion**: Presentation e Domain dipendono da astrazioni; Koin collega le
  implementazioni.

## 8. Anti-pattern da evitare

- Composable che chiama repository o accede direttamente a `Context`.
- Screen che chiama UseCase, database, SDK, API native, Koin o coroutine applicative.
- Screen che esegue `filter`, `sorted`, mapping di dominio, validazione, `runCatching` o gestione di
  errori applicativi.
- Screen con decisioni di business, autorizzazione, persistenza o navigazione condizionale.
- Stato di dominio mantenuto con `remember`/`mutableStateOf` invece che nel ViewModel.
- Screen che formattano modelli di dominio o costruiscono direttamente righe/card complesse: usare
  un
  `XxxUiStateMapper`, item UI immutabili specifici della feature e un componente dedicato.
- Flusso diretto `Screen → Repository`: il percorso corretto è
  `Screen(uiState, onEvent) → ViewModel.onEvent → UseCase → Repository`.
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
9. Verificare che la UI non contenga logica applicativa o accessi a dati/coroutine.
10. Aggiornare documentazione, navigazione e schema solo se la feature introduce un nuovo confine
    architetturale.

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
- [ ] La UI riceve solo stato immutabile e callback e non contiene logica applicativa.
- [ ] Le nuove regole applicative hanno test UseCase e le transizioni evento/stato hanno test
  ViewModel.
- [ ] È stata eseguita una ricerca dei pattern vietati nelle directory UI (`Repository`, `UseCase`,
  `viewModelScope`, `launch`, `async`, `collect`, API dirette).
- [ ] La documentazione o il diagramma sono aggiornati se cambia un confine.
