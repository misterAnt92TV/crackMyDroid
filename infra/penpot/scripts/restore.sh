#!/usr/bin/env bash
set -euo pipefail

if [[ $# -ne 1 ]]; then
  echo "Usage: $0 <backup-folder-path>"
  exit 1
fi

BACKUP_PATH="$1"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"

if [[ ! -d "$BACKUP_PATH" ]]; then
  echo "Backup path not found: $BACKUP_PATH"
  exit 1
fi

if [[ ! -f "$BACKUP_PATH/postgres.sql.gz" ]]; then
  echo "Missing postgres.sql.gz in $BACKUP_PATH"
  exit 1
fi

if [[ ! -f "$BACKUP_PATH/volumes.tgz" ]]; then
  echo "Missing volumes.tgz in $BACKUP_PATH"
  exit 1
fi

cd "$ROOT_DIR"

if [[ ! -f .env ]]; then
  echo "Missing .env file in $ROOT_DIR"
  exit 1
fi

set -a
source .env
set +a

echo "Stopping services..."
docker compose down

echo "Restoring named volumes..."
docker run --rm \
  -v penpot_postgres_data:/to/postgres \
  -v penpot_penpot_assets:/to/assets \
  -v penpot_redis_data:/to/redis \
  -v "$BACKUP_PATH":/from \
  alpine:3.20 \
  sh -c 'rm -rf /to/postgres/* /to/assets/* /to/redis/* && tar xzf /from/volumes.tgz -C /tmp && cp -a /tmp/postgres/. /to/postgres/ && cp -a /tmp/assets/. /to/assets/ && cp -a /tmp/redis/. /to/redis/'

echo "Starting database..."
docker compose up -d postgres redis
sleep 8

echo "Restoring PostgreSQL dump..."
gunzip -c "$BACKUP_PATH/postgres.sql.gz" | docker compose exec -T postgres psql -U "$POSTGRES_USER" -d "$POSTGRES_DB"

echo "Starting full stack..."
docker compose up -d

echo "Restore completed."
