# Penpot Self-Hosted Pack

Pacchetto pronto per deploy self-hosted di Penpot con:
- Docker Compose (Penpot + Postgres + Redis + Exporter + Nginx)
- configurazione HTTPS via Nginx
- script backup/restore

## 1) Setup rapido

```bash
cd infra/penpot
cp .env.example .env
```

Aggiorna `.env` con valori reali, soprattutto:
- `PENPOT_PUBLIC_URI`
- `PENPOT_SECRET_KEY`
- `POSTGRES_PASSWORD`
- SMTP

Genera una secret robusta:

```bash
openssl rand -hex 32
```

## 2) Configura dominio e TLS

Modifica `nginx/conf.d/penpot.conf` sostituendo `design.example.com` col tuo dominio.

Inserisci i certificati in:
- `nginx/certs/fullchain.pem`
- `nginx/certs/privkey.pem`

> Nota: per ottenere i certificati puoi usare Certbot sul server host.

## 3) Avvio

```bash
docker compose pull
docker compose up -d
```

Verifica:

```bash
docker compose ps
docker compose logs -f penpot-backend
```

## 4) Backup

```bash
chmod +x scripts/*.sh
./scripts/backup.sh
```

I backup finiscono in `backups/<timestamp>/`.

### Esempio cron giornaliero (02:30)

```cron
30 2 * * * cd /path/to/infra/penpot && ./scripts/backup.sh >> /var/log/penpot-backup.log 2>&1
```

## 5) Restore

```bash
./scripts/restore.sh backups/20261103_023000
```

## 6) Hardening raccomandato

- Esponi solo `80/443` verso Internet.
- Mantieni Docker immagini aggiornate.
- Disabilita `enable-registration` dopo bootstrap se non serve.
- Esegui test periodico di restore.
