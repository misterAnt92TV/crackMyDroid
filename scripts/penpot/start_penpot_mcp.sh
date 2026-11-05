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
COREPACK_CACHE="${TMPDIR:-/tmp}/crackmydroid-penpot-corepack"

# Keep Corepack isolated from a broken user cache and from repository state.
export COREPACK_HOME="$COREPACK_CACHE"
if ! corepack pnpm --version >/dev/null 2>&1; then
  echo "Preparing isolated pnpm runtime for Penpot MCP..."
  corepack install --global pnpm@10 >/dev/null
fi

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
    pnpm --dir "$package_dir" approve-builds esbuild sharp >/dev/null 2>&1 || true
  done < <(find "$npm_cache/_npx" -type f -path '*/node_modules/@penpot/mcp/package.json' -print 2>/dev/null)
}

# Populate the npx cache without invoking the package bootstrap yet.
npx --yes --package="@penpot/mcp@$MCP_VERSION" true >/dev/null
prepare_mcp_builds

echo "Starting Penpot MCP $MCP_VERSION. Keep this terminal open while exporting."
echo "Plugin manifest: ${PENPOT_MCP_PLUGIN_URL:-http://localhost:4400/manifest.json}"
echo "MCP endpoint: ${PENPOT_MCP_URL:-http://localhost:4401/mcp}"
exec npx --yes "@penpot/mcp@$MCP_VERSION"
