#!/usr/bin/env bash
set -euo pipefail

REPO_ROOT="$(cd "$(dirname "$0")/../.." && pwd)"
ENV_FILE="$REPO_ROOT/scripts/penpot/.env.penpot"
if [[ -f "$ENV_FILE" ]]; then
  set -a
  source "$ENV_FILE"
  set +a
fi

UI_URL="${PENPOT_UI_URL:-http://localhost:9001}"
BACKEND_URL="${PENPOT_BACKEND_URL:-http://localhost:6060}"
MCP_URL="${PENPOT_MCP_URL:-http://localhost:4401/mcp}"

echo "Checking Penpot UI: $UI_URL"
curl -fsS "$UI_URL" >/dev/null
echo "OK UI reachable"

echo "Checking Penpot backend: $BACKEND_URL/readyz"
curl -fsS "$BACKEND_URL/readyz" >/dev/null
echo "OK backend ready"

echo "Checking Penpot MCP: $MCP_URL"
node "$REPO_ROOT/scripts/penpot/mcp_client.mjs" check "$MCP_URL"

echo "Done"
