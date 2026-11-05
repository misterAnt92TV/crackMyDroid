#!/usr/bin/env bash
set -euo pipefail

REPO_ROOT="$(cd "$(dirname "$0")/../.." && pwd)"
ENV_FILE="$REPO_ROOT/scripts/penpot/.env.penpot"
if [[ -f "$ENV_FILE" ]]; then
  set -a
  source "$ENV_FILE"
  set +a
fi

MCP_VERSION="${PENPOT_MCP_PACKAGE_VERSION:-stable}"
PNPM_VERSION="${PENPOT_PNPM_VERSION:-10.31.0}"
COREPACK_CACHE="${TMPDIR:-/tmp}/crackmydroid-penpot-corepack"

# A previous launcher instance may already own the bridge ports. Starting a
# second recursive pnpm workspace then makes the plugin fail with EADDRINUSE
# and kills the server sibling. Reuse a complete existing bridge instead.
port_owner() {
  lsof -nP -tiTCP:"$1" -sTCP:LISTEN 2>/dev/null | head -n 1
}

process_command() {
  ps -p "$1" -o command= 2>/dev/null || true
}

process_cwd() {
  lsof -a -p "$1" -d cwd -Fn 2>/dev/null | sed -n 's/^n//p' | head -n 1
}

is_penpot_process() {
  local command_line cwd
  command_line="$(process_command "$1")"
  cwd="$(process_cwd "$1")"
  [[ "$cwd" == *"/@penpot/mcp/"* && ( "$command_line" == *"dist/index.js"* || "$command_line" == *"vite.js build --watch"* || "$command_line" == *"@penpot/mcp"* ) ]]
}

PLUGIN_PID="$(port_owner 4400 || true)"
MCP_PID="$(port_owner 4401 || true)"
WS_PID="$(port_owner 4402 || true)"
if [[ -n "$PLUGIN_PID" || -n "$MCP_PID" || -n "$WS_PID" ]]; then
  if [[ -n "$PLUGIN_PID" && -n "$MCP_PID" && -n "$WS_PID" ]]; then
    if ! is_penpot_process "$PLUGIN_PID" || ! is_penpot_process "$MCP_PID" || ! is_penpot_process "$WS_PID"; then
      echo "Penpot MCP ports are occupied by an unexpected process (4400=$PLUGIN_PID, 4401=$MCP_PID, 4402=$WS_PID)." >&2
      echo "Inspect the processes and free the ports before starting the bridge; no process was terminated." >&2
      exit 1
    fi
    echo "Penpot MCP bridge already running (plugin PID $PLUGIN_PID, MCP PID $MCP_PID, websocket PID $WS_PID)."
    echo "Reuse it; do not start a second instance."
    exit 0
  fi
  echo "Penpot MCP ports are partially occupied (4400=$PLUGIN_PID, 4401=$MCP_PID, 4402=$WS_PID)." >&2
  echo "Stop the incomplete bridge instance, then run this launcher again." >&2
  exit 1
fi

# Keep Corepack isolated from a broken user cache and from repository state.
export COREPACK_HOME="$COREPACK_CACHE"
CURRENT_PNPM_VERSION="$(corepack pnpm --version 2>/dev/null || true)"
if [[ "$CURRENT_PNPM_VERSION" != "$PNPM_VERSION" ]]; then
  echo "Preparing isolated pnpm $PNPM_VERSION runtime for Penpot MCP..."
  corepack install --global "pnpm@$PNPM_VERSION" >/dev/null
  CURRENT_PNPM_VERSION="$(corepack pnpm --version 2>/dev/null || true)"
fi
if [[ "$CURRENT_PNPM_VERSION" != "$PNPM_VERSION" ]]; then
  echo "Unable to prepare pnpm $PNPM_VERSION (found ${CURRENT_PNPM_VERSION:-unavailable})." >&2
  echo "The isolated Corepack cache is incomplete or incompatible; remove only $COREPACK_CACHE and retry." >&2
  exit 1
fi
echo "Using pnpm $CURRENT_PNPM_VERSION for Penpot MCP."

# pnpm blocks native dependency lifecycle scripts by default. The MCP package
# needs only esbuild and sharp to build its server/plugin; approve those builds
# in the npx cache before the package bootstrap runs, avoiding an interactive
# prompt and keeping the approval out of the repository.
prepare_mcp_builds() {
  local npm_cache package_json package_dir
  npm_cache="$(npm config get cache 2>/dev/null || true)"
  [[ -n "$npm_cache" && -d "$npm_cache/_npx" ]] || return 0

  while IFS= read -r package_json; do
    package_dir="$(dirname "$package_json")"
    corepack pnpm --dir "$package_dir" approve-builds esbuild sharp >/dev/null 2>&1 || true
  done < <(find "$npm_cache/_npx" -type f -path '*/node_modules/@penpot/mcp/package.json' -print 2>/dev/null)
}

# Populate the npx cache without invoking the package bootstrap yet.
npx --yes --package="@penpot/mcp@$MCP_VERSION" true >/dev/null
prepare_mcp_builds

echo "Starting Penpot MCP $MCP_VERSION. Keep this terminal open while exporting."
echo "Plugin manifest: ${PENPOT_MCP_PLUGIN_URL:-http://localhost:4400/manifest.json}"
echo "MCP endpoint: ${PENPOT_MCP_URL:-http://localhost:4401/mcp}"
exec npx --yes "@penpot/mcp@$MCP_VERSION"
