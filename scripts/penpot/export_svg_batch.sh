#!/usr/bin/env bash
set -euo pipefail

REPO_ROOT="$(cd "$(dirname "$0")/../.." && pwd)"
ENV_FILE="$REPO_ROOT/scripts/penpot/.env.penpot"
if [[ -f "$ENV_FILE" ]]; then
  set -a
  source "$ENV_FILE"
  set +a
fi

MCP_URL="${PENPOT_MCP_URL:-http://localhost:4401/mcp}"

echo "Exporting the active Penpot Home page through MCP: $MCP_URL"
node "$REPO_ROOT/scripts/penpot/mcp_client.mjs" export-home "$MCP_URL" "$@"
