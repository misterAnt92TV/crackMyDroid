# Penpot in locale (senza dominio)

Questa guida spiega come avviare Penpot sul tuo PC in locale e aprire la UI nel browser con un solo comando.

## Prerequisiti

- Docker Desktop installato e avviato
- Docker Compose disponibile (`docker compose`)

Verifica rapida:

```bash
docker --version
docker compose version
```

## 1) Preparazione iniziale

```bash
cd infra/penpot
cp .env.local.example .env
```

Apri `.env` e imposta almeno questi valori:

```env
PENPOT_PUBLIC_URI=http://localhost:9001
PENPOT_SECRET_KEY=<secret generata con openssl rand -hex 32>
POSTGRES_PASSWORD=<password forte>
```

Per generare una secret:

```bash
openssl rand -hex 32
```

Non usare `https://penpot.app`, `CHANGE_ME`, `REPLACE_WITH` o le credenziali
SMTP di esempio nel profilo locale. SMTP può restare vuoto perché il profilo
locale disabilita la verifica email.

In locale SMTP è opzionale. L'override `docker-compose.local.yml` disabilita la
verifica email soltanto per `localhost`, così gli account appena creati diventano
subito attivi. Non usare questo override in un deploy raggiungibile in rete.

## 2) Avvio con un solo comando

```bash
./start-local.sh
```

Questo script avvia automaticamente lo stack locale con override `docker-compose.local.yml`.

## 3) Aprire la UI

Apri nel browser:

- http://localhost:9001

## 4) Stop con un solo comando

```bash
./stop-local.sh
```

## 5) Comandi utili

Stato servizi:

```bash
docker compose -f docker-compose.yml -f docker-compose.local.yml ps
```

Per mantenere lo stesso `.env` usato dallo script:

```bash
docker compose --env-file .env -f docker-compose.yml -f docker-compose.local.yml ps
```

Log backend:

```bash
docker compose -f docker-compose.yml -f docker-compose.local.yml logs -f penpot-backend
```

Log frontend:

```bash
docker compose -f docker-compose.yml -f docker-compose.local.yml logs -f penpot-frontend
```

Stato aggregato degli account locali (non stampa email o password):

```bash
./check-local-auth.sh
```

## Recupero di un account locale inattivo

Se un account creato prima dell'override mostra `Email or password incorrect`,
riattivalo senza resettare volumi o design:

```bash
./activate-local-account.sh you@example.com
```

Lo script modifica un solo profilo, solo se esiste, è inattivo, non bloccato,
non eliminato e dispone di password. Non stampa email o credenziali.

Se non ricordi l'indirizzo registrato, usa `--only-inactive` solo quando
`./check-local-auth.sh` mostra esattamente un profilo inattivo:

```bash
./activate-local-account.sh --only-inactive
```

## 6) Applicare il nuovo profilo locale

Dopo aver aggiornato questa configurazione, ricrea soltanto frontend e backend:

```bash
docker compose -f docker-compose.yml -f docker-compose.local.yml up -d --force-recreate penpot-frontend penpot-backend
```

Poi esegui `./check-local-auth.sh` e accedi con la password già impostata.

## 7) Troubleshooting rapido

1. Verifica Docker Desktop avviato.
2. Se `localhost:9001` è occupata:

```bash
lsof -i :9001
```

3. Cambia porta in `docker-compose.local.yml` (es. `9010:8080`) e aggiorna `PENPOT_PUBLIC_URI` in `.env`.
4. Riavvia con `./stop-local.sh && ./start-local.sh`.

## 8) Backup locale (opzionale)

```bash
chmod +x scripts/*.sh
./scripts/backup.sh
```

I backup sono in `infra/penpot/backups/<timestamp>/`.
