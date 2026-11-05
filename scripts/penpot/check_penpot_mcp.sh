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
"${CURL[@]}" "$PLUGIN_URL" >/dev/null
echo "OK plugin manifest reachable"

echo "Checking Penpot MCP: $MCP_URL"
node "$REPO_ROOT/scripts/penpot/mcp_client.mjs" check "$MCP_URL"

echo "Done"
