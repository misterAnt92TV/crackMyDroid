# API per sicurezza APK e librerie Android

**Analyze APK Content** esegue l'analisi locale offline e offre una verifica OSV opzionale delle
librerie per cui è possibile ricostruire coordinate Maven e versione in modo deterministico.

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

## Configurazione e flusso

Configura l'URL HTTPS del backend advisory dall'esterno dell'app:

- Android: proprietà Gradle `securityAdvisoryEndpoint`, passata come `-PsecurityAdvisoryEndpoint=https://host/percorso`.
- Desktop JVM: proprietà di sistema `crackmydroid.securityAdvisoryEndpoint`, passata come `-Dcrackmydroid.securityAdvisoryEndpoint=https://host/percorso`.

Se l'endpoint manca o non è HTTPS, l'app mostra il servizio come non configurato; l'analisi APK
offline resta disponibile. La richiesta parte solo quando l'utente preme **Verifica vulnerabilità
online**. Invia esclusivamente PURL Maven e versione delle librerie verificate. Package name
dell'app analizzata, APK, hash, entry, certificati e contenuti non vengono inviati. Il backend
normalizza le risposte OSV e l'app mostra fonte, ID, severità, sommario, versione corretta e link.

## Limiti e privacy

- Una libreria senza versione deducibile deve essere indicata come **non verificabile online**. Non
  bisogna associare CVE soltanto per nome, perché produrrebbe falsi positivi.
- La verifica con servizi antimalware come VirusTotal richiede consenso separato: hash, package name
  e soprattutto file APK possono rappresentare dati sensibili o identificativi.
- Nessun APK, hash, package name dell'app, entry, certificato o contenuto viene trasmesso dal flusso
  advisory.
- Il risultato resta un indicatore tecnico: non certifica l'assenza di malware o vulnerabilità.
