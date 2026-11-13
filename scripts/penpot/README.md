# Penpot MCP

Il repository usa Penpot hosted per UI e file di design, con plugin e bridge MCP
locali. Questa procedura non installa Penpot e non avvia Docker.

## Flusso supportato

```bash
cp scripts/penpot/.env.penpot.example scripts/penpot/.env.penpot
./scripts/penpot/start_penpot_mcp.sh
```

Il launcher prepara automaticamente la cache npx e autorizza soltanto gli
script nativi `esbuild` e `sharp` richiesti dal pacchetto MCP; non serve eseguire
manualmente `pnpm approve-builds`. Usa pnpm `10.31.0` in una cache Corepack
isolata (`PENPOT_PNPM_VERSION`); non eseguire `pnpm recursive run` direttamente
nella cache npx.

I default sono:

- UI/file: `https://design.penpot.app`;
- backend: non configurato;
- plugin MCP: `http://localhost:4400/manifest.json`;
- endpoint MCP: `http://localhost:4401/mcp`;
- WebSocket del plugin: `localhost:4402`.

Il launcher avvia il bridge MCP locale; non avvia Penpot hosted. Nel browser:

Se la rete aziendale intercetta TLS con Zscaler, configura il certificato CA
approvato senza disabilitare la verifica:

```env
PENPOT_CA_BUNDLE=/percorso/alla/zscaler-root-ca.pem
```

Il certificato deve essere fornito/approvato dall’amministratore IT. Non usare
`curl -k` o `--insecure`.

1. apri `https://design.penpot.app` e il file `CrackMyDroid`;
2. vai in Plugins → Load from URL;
3. carica `http://localhost:4400/manifest.json`;
4. avvia il plugin e premi **Connect to MCP server**;
5. mantieni aperta la finestra del plugin.

Se il browser chiede il permesso di accesso alla rete locale, consentilo per
Penpot. Poi verifica la sessione con:

```bash
./scripts/penpot/check_penpot_mcp.sh
```

Il launcher è idempotente: se le porte `4400`, `4401` e `4402` appartengono già
al bridge Penpot, stampa i PID e riutilizza l’istanza. Se solo una parte delle
porte è occupata, si interrompe senza terminare processi.

## Troubleshooting

| Errore                                                | Causa e correzione                                                                                                                                  |
|-------------------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------|
| `EADDRINUSE ... 4400`                                 | È stata avviata una seconda istanza. Chiudi il secondo comando e riusa il bridge già attivo.                                                        |
| `ERR_PNPM_RECURSIVE_RUN_FIRST_FAIL`                   | È l’errore finale del bootstrap parallelo. Controlla il primo errore del plugin: spesso è una porta duplicata o una cache Corepack corrotta.        |
| `Cannot find ... pnpm.cjs`                            | La cache Corepack isolata è incompleta. Riesegui il launcher; se persiste, rimuovi solo la cache indicata dal messaggio e riprova.                  |
| `No Penpot plugin instances are currently connected`  | Apri `CrackMyDroid`, ricarica `http://localhost:4400/manifest.json`, apri il plugin e premi **Connect to MCP server**. Mantieni il pannello aperto. |
| Certificato TLS non verificabile                      | Imposta `PENPOT_CA_BUNDLE` con la CA aziendale approvata. Non usare `--insecure`.                                                                   |
| `localhost:4401` non risponde ma il processo è attivo | Verifica il listener IPv6; per la diagnostica diretta usa `http://[::1]:4401/mcp`, lasciando il manifest su `localhost`.                            |
| Plugin collegato al file sbagliato                    | Apri `CrackMyDroid` e riconnetti il plugin prima di modificare Penpot.                                                                              |

La procedura non usa Docker e non avvia un backend Penpot locale.

Se la UI hosted non risponde o il certificato TLS non è verificabile, interrompi
la procedura. In presenza di una CA aziendale, configura `PENPOT_CA_BUNDLE` con
il percorso approvato; non usare `curl -k` o `--insecure`. Le eventuali chiavi
restano nel file `.env.penpot` non versionato.

