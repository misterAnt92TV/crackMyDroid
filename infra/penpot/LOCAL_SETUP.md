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
cp .env.example .env
```

Apri `.env` e imposta almeno questi valori:

```env
PENPOT_PUBLIC_URI=http://localhost:9001
PENPOT_SECRET_KEY=METTI_UNA_SECRET_RANDOM_LUNGA
POSTGRES_PASSWORD=METTI_UNA_PASSWORD_FORTE
```

Per generare una secret:

```bash
openssl rand -hex 32
```

In locale SMTP è opzionale (serve solo per inviti/reset password via email).

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

Log backend:

```bash
docker compose -f docker-compose.yml -f docker-compose.local.yml logs -f penpot-backend
```

Log frontend:

```bash
docker compose -f docker-compose.yml -f docker-compose.local.yml logs -f penpot-frontend
```

## 6) Troubleshooting rapido

1. Verifica Docker Desktop avviato.
2. Se `localhost:9001` è occupata:

```bash
lsof -i :9001
```

3. Cambia porta in `docker-compose.local.yml` (es. `9010:8080`) e aggiorna `PENPOT_PUBLIC_URI` in `.env`.
4. Riavvia con `./stop-local.sh && ./start-local.sh`.

## 7) Backup locale (opzionale)

```bash
chmod +x scripts/*.sh
./scripts/backup.sh
```

I backup sono in `infra/penpot/backups/<timestamp>/`.
