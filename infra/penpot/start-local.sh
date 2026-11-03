#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$ROOT_DIR"

./check-local.sh

if [[ ! -f .env ]]; then
  echo "Missing .env. Creating it from .env.example..."
  cp .env.example .env
  echo "Created .env. Update PENPOT_PUBLIC_URI to http://localhost:9001 and set strong secrets, then rerun."
  exit 1
fi

if ! grep -q '^PENPOT_PUBLIC_URI=http://localhost:9001$' .env; then
  echo "WARNING: PENPOT_PUBLIC_URI is not set to http://localhost:9001"
  echo "Current value:"
  grep '^PENPOT_PUBLIC_URI=' .env || true
  echo "Recommended for local use: PENPOT_PUBLIC_URI=http://localhost:9001"
fi

echo "Starting Penpot local stack..."
docker compose -f docker-compose.yml -f docker-compose.local.yml up -d

echo "Penpot is starting. Open: http://localhost:9001"
echo "Check status: docker compose -f docker-compose.yml -f docker-compose.local.yml ps"
