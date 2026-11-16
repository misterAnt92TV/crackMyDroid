# Refactor navigazione: Dashboard e drawer organizzato

## Obiettivo

La Dashboard sostituisce la Home come destinazione iniziale. Offre accessi diretti a APK installati, PenTest, Analisi contenuto APK e Permessi, senza duplicare cronologia o risultati delle feature.

La bottom navigation non viene utilizzata. Su smartphone il menu e un drawer modale; da 720 dp il drawer resta visibile a sinistra.

## Architettura informativa

- Dashboard
- Analizza app: APK installati, Analisi contenuto APK, Permessi, Lista activity
- Sicurezza dispositivo: PenTest, Test rooting, Info dispositivo
- Strumenti: Trick, Log
- Attivita: Storico
- Supporto: Help e Impostazioni

Lo Storico e una destinazione dedicata, alimentata dal repository delle operazioni. Mostra l’elenco completo e conserva le azioni di rimozione; nascondere un elemento dalle operazioni recenti non lo elimina dallo storico.

## Vincoli

Il refactor modifica solo shell, accessi e organizzazione delle destinazioni. Le feature, i loro componenti e i relativi flussi di analisi non cambiano. La chrome continua a mantenere cronologia back, ricerca contestuale, Help, hint e stato salvabile di ogni schermata.

## Verifica

- Controllare il drawer modale su telefono e il drawer permanente su tablet/desktop.
- Verificare Dashboard iniziale, accessi rapidi, selezione idempotente e navigazione indietro.
- Verificare Storico vuoto, completo, rimozione e persistenza.
