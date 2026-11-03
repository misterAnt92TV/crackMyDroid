#!/usr/bin/env bash
set -euo pipefail

ENV_FILE="scripts/penpot/.env.penpot"
if [[ -f "$ENV_FILE" ]]; then
  set -a
  source "$ENV_FILE"
  set +a
fi

UI_URL="${PENPOT_UI_URL:-http://localhost:9001}"
MCP_URL="${PENPOT_MCP_URL:-http://localhost:6060}"
MCP_KEY="${PENPOT_MCP_KEY:-}"

echo "Checking Penpot UI: $UI_URL"
curl -fsS "$UI_URL" >/dev/null
echo "OK UI reachable"

echo "Checking Penpot MCP/Backend ready: $MCP_URL/readyz"
curl -fsS "$MCP_URL/readyz" >/dev/null
echo "OK MCP readyz reachable"

if [[ -n "$MCP_KEY" ]]; then
  echo "Checking MCP key presence: set"
else
  echo "Checking MCP key presence: empty (allowed for local backend readyz)"
fi

echo "Done"
