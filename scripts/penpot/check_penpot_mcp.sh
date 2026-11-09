#!/usr/bin/env bash
set -euo pipefail

REPO_ROOT="$(cd "$(dirname "$0")/../.." && pwd)"
ENV_FILE="$REPO_ROOT/scripts/penpot/.env.penpot"
if [[ -f "$ENV_FILE" ]]; then
  set -a
  source "$ENV_FILE"
  set +a
fi

UI_URL="${PENPOT_UI_URL:-https://design.penpot.app}"
BACKEND_URL="${PENPOT_BACKEND_URL:-}"
MCP_URL="${PENPOT_MCP_URL:-http://localhost:4401/mcp}"
PLUGIN_URL="${PENPOT_MCP_PLUGIN_URL:-http://localhost:4400/manifest.json}"
WS_PORT="${PENPOT_MCP_WEBSOCKET_PORT:-4402}"
CURL=(curl --fail --silent --show-error)

require_local_url() {
  local name="$1"
  local url="$2"
  case "$url" in
    http://localhost:*|http://127.0.0.1:*) ;;
    *)
      echo "$name must use localhost or 127.0.0.1 (got a non-local URL)" >&2
      exit 1
      ;;
  esac
}

require_local_url "PENPOT_MCP_URL" "$MCP_URL"
require_local_url "PENPOT_MCP_PLUGIN_URL" "$PLUGIN_URL"

if [[ -n "${PENPOT_CA_BUNDLE:-}" ]]; then
  CURL+=(--cacert "$PENPOT_CA_BUNDLE")
elif curl --help all 2>/dev/null | grep -q -- '--ca-native'; then
  # Prefer the macOS Keychain when available (notably for enterprise CAs).
  CURL+=(--ca-native)
fi
CURL+=(--connect-timeout 3 --max-time 5)

echo "Checking Penpot UI: $UI_URL"
if ! "${CURL[@]}" "$UI_URL" >/dev/null; then
  echo "Unable to verify Penpot TLS certificate. If the network uses Zscaler or another corporate CA, set PENPOT_CA_BUNDLE to the approved CA bundle." >&2
  exit 1
fi
echo "OK UI reachable"

if [[ -n "$BACKEND_URL" ]]; then
  echo "Checking optional Penpot backend: $BACKEND_URL/readyz"
  "${CURL[@]}" "$BACKEND_URL/readyz" >/dev/null
  echo "OK backend ready"
else
  echo "Skipping backend check (Penpot hosted workflow)"
fi

echo "Checking Penpot MCP plugin manifest: $PLUGIN_URL"
if ! "${CURL[@]}" "$PLUGIN_URL" >/dev/null; then
  echo "Plugin manifest unavailable at $PLUGIN_URL. Start the bridge once with scripts/penpot/start_penpot_mcp.sh." >&2
  exit 1
fi
echo "OK plugin manifest reachable"

echo "Checking Penpot MCP WebSocket listener: localhost:$WS_PORT"
if ! command -v lsof >/dev/null 2>&1 || ! lsof -nP -iTCP:"$WS_PORT" -sTCP:LISTEN 2>/dev/null | grep -q .; then
  echo "WebSocket listener unavailable on localhost:$WS_PORT. Start or reuse the complete bridge; do not start a duplicate." >&2
  exit 1
fi
echo "OK WebSocket listener reachable"

echo "Checking Penpot MCP: $MCP_URL"
if ! node "$REPO_ROOT/scripts/penpot/mcp_client.mjs" check "$MCP_URL"; then
  echo "MCP endpoint is reachable but the plugin/file session is not ready. Open CrackMyDroid, load the localhost manifest, and click Connect to MCP server." >&2
  exit 1
fi

echo "Done"
