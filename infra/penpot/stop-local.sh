#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$ROOT_DIR"

echo "Stopping Penpot local stack..."
docker compose -f docker-compose.yml -f docker-compose.local.yml down

echo "Done."
