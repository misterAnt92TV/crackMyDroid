#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
BACKUP_DIR="$ROOT_DIR/backups"
TS="$(date +%Y%m%d_%H%M%S)"

mkdir -p "$BACKUP_DIR/$TS"

cd "$ROOT_DIR"

if [[ ! -f .env ]]; then
  echo "Missing .env file in $ROOT_DIR"
  exit 1
fi

set -a
source .env
set +a

echo "[1/3] Backup PostgreSQL dump..."
docker compose --env-file .env exec -T postgres pg_dump -U "$POSTGRES_USER" -d "$POSTGRES_DB" | gzip > "$BACKUP_DIR/$TS/postgres.sql.gz"

echo "[2/3] Backup named volumes..."
docker run --rm \
  -v penpot_postgres_data:/from/postgres:ro \
  -v penpot_penpot_assets:/from/assets:ro \
  -v penpot_redis_data:/from/redis:ro \
  -v "$BACKUP_DIR/$TS":/to \
  alpine:3.20 \
  sh -c 'tar czf /to/volumes.tgz -C /from postgres assets redis'

echo "[3/3] Backup metadata..."
cp .env "$BACKUP_DIR/$TS/.env.backup"

echo "Backup completed: $BACKUP_DIR/$TS"
