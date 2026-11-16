# API per sicurezza APK e librerie Android

Questo documento raccoglie le opzioni per estendere in futuro **Analyze APK Content** con verifiche
online di vulnerabilità e sicurezza. Non modifica l'attuale analisi: la modalità predefinita resta
interamente offline.

## API consigliate

### OSV API — sorgente primaria

[OSV](https://google.github.io/osv.dev/api/) è la sorgente primaria consigliata per verificare le
vulnerabilità note delle librerie. Supporta query per pacchetto e versione, oltre a query batch
tramite [`POST /v1/querybatch`](https://google.github.io/osv.dev/post-v1-querybatch/).

Per librerie Android, le richieste devono usare coordinate Maven o Package URL (PURL) quando
disponibili. La risposta può essere presentata nel report come advisory OSV, CVE/GHSA associati,
severità, intervallo di versioni interessate e versione corretta quando dichiarata dall'advisory.

### GitHub Security Advisories — complemento

Le [GitHub Security Advisories REST API](https://docs.github.com/en/rest/security-advisories/global-advisories)
possono integrare OSV per advisory globali, filtrando per ecosistema `maven`, severità e identificativo
CVE o GHSA. Sono una fonte complementare, non un sostituto della correlazione precisa
pacchetto-versione.

## Servizi con scopo differente

| Servizio | Uso appropriato | Non sostituisce |
| --- | --- | --- |
| OSV / GitHub Advisories | CVE e advisory di dipendenze riconosciute | Un antivirus o un'analisi dinamica |
| VirusTotal | Reputazione antimalware di hash o file, solo con consenso esplicito | Verifica CVE delle librerie |
| [Play Integrity](https://developer.android.com/google/play/integrity/overview) | Integrità della propria app e segnali sul dispositivo | Analisi di APK arbitrari o dipendenze |

Play Integrity è pensata per proteggere l'app proprietaria e richiede configurazione Google Play e
backend. Può offrire segnali di integrità del dispositivo e Play Protect, ma non è un catalogo di
vulnerabilità per librerie Android.

## Architettura proposta

1. Conservare l'inventario e l'analisi euristica offline come comportamento predefinito.
2. Esporre una verifica online opzionale, chiarendo dati inviati, sorgente consultata e limiti.
3. Inviare al backend solo coordinate Maven/PURL e versione delle librerie identificate; non inviare
   l'APK, le sue entry, certificati o contenuti.
4. Il backend applica autenticazione, rate limit, cache e normalizzazione delle risposte OSV/GitHub;
   l'app riceve un modello di advisory già filtrato e localizzabile.
5. Mostrare ogni risultato con fonte, identificativo, severità, evidenza della versione rilevata e
   remediation consigliata.

## Limiti e privacy

- Una libreria senza versione deducibile deve essere indicata come **non verificabile online**. Non
  bisogna associare CVE soltanto per nome, perché produrrebbe falsi positivi.
- La verifica con servizi antimalware come VirusTotal richiede consenso separato: hash, package name
  e soprattutto file APK possono rappresentare dati sensibili o identificativi.
- Nessun APK deve essere caricato o conservato dal flusso di verifica advisory proposto.
- Il risultato resta un indicatore tecnico: non certifica l'assenza di malware o vulnerabilità.
