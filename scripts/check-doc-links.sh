#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
PYTHONDONTWRITEBYTECODE=1 exec python3 "$ROOT_DIR/scripts/check_doc_links.py" "$ROOT_DIR"
