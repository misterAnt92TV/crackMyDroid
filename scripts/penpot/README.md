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
manualmente `pnpm approve-builds`.

I default sono:

- UI/file: `https://design.penpot.app`;
- backend: non configurato;
- plugin MCP: `http://localhost:4400/manifest.json`;
- endpoint MCP: `http://localhost:4401/mcp`.

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

Se la UI hosted non risponde o il certificato TLS non è verificabile, interrompi
la procedura. In presenza di una CA aziendale, configura `PENPOT_CA_BUNDLE` con
il percorso approvato; non usare `curl -k` o `--insecure`. Le eventuali chiavi
restano nel file `.env.penpot` non versionato.

