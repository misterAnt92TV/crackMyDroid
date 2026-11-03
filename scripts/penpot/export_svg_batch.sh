#!/usr/bin/env bash
set -euo pipefail

ENV_FILE="scripts/penpot/.env.penpot"
if [[ -f "$ENV_FILE" ]]; then
  set -a
  source "$ENV_FILE"
  set +a
fi

MCP_URL="${PENPOT_MCP_URL:-http://localhost:6060}"
MCP_KEY="${PENPOT_MCP_KEY:-}"
OUT_ROOT="docs/design/exports"
SCREENS_DIR="$OUT_ROOT/screens"
COMPONENTS_DIR="$OUT_ROOT/components"
INDEX_FILE="$OUT_ROOT/EXPORT_INDEX.md"

mkdir -p "$SCREENS_DIR" "$COMPONENTS_DIR"

if ! command -v jq >/dev/null 2>&1; then
  echo "Error: jq is required." >&2
  exit 1
fi

HDRS=("-H" "Content-Type: application/json")
if [[ -n "$MCP_KEY" ]]; then
  HDRS+=("-H" "Authorization: Bearer $MCP_KEY")
fi

slugify() {
  local value="$1"
  value="$(echo "$value" | tr '[:upper:]' '[:lower:]' | sed -E 's/[^a-z0-9]+/-/g; s/^-+//; s/-+$//')"
  if [[ -z "$value" ]]; then
    value="unnamed"
  fi
  echo "$value"
}

fetch_json() {
  local endpoint="$1"
  curl -fsS "${HDRS[@]}" "$MCP_URL/$endpoint"
}

download_svg() {
  local endpoint="$1"
  local outfile="$2"
  curl -fsS "${HDRS[@]}" "$MCP_URL/$endpoint" -o "$outfile"
}

echo "[1/4] Fetching screens list..."
SCREENS_JSON="$(fetch_json "screens")"

echo "[2/4] Exporting screens..."
SCREEN_COUNT=0
while IFS= read -r row; do
  id="$(echo "$row" | jq -r '.id')"
  name="$(echo "$row" | jq -r '.name')"
  file="$(slugify "$name")-${id}.svg"
  download_svg "screens/${id}/export.svg" "$SCREENS_DIR/$file"
  SCREEN_COUNT=$((SCREEN_COUNT + 1))
  echo "  - $name -> $SCREENS_DIR/$file"
done < <(echo "$SCREENS_JSON" | jq -c '.items[]?')

echo "[3/4] Fetching components list..."
COMPONENTS_JSON="$(fetch_json "components")"

echo "[4/4] Exporting components..."
COMP_COUNT=0
while IFS= read -r row; do
  id="$(echo "$row" | jq -r '.id')"
  name="$(echo "$row" | jq -r '.name')"
  file="$(slugify "$name")-${id}.svg"
  download_svg "components/${id}/export.svg" "$COMPONENTS_DIR/$file"
  COMP_COUNT=$((COMP_COUNT + 1))
  echo "  - $name -> $COMPONENTS_DIR/$file"
done < <(echo "$COMPONENTS_JSON" | jq -c '.items[]?')

cat > "$INDEX_FILE" <<MD
# Penpot SVG Export Index

Generated at: $(date -u +"%Y-%m-%dT%H:%M:%SZ")
MCP endpoint: $MCP_URL

## Screens
Count: $SCREEN_COUNT

$(find "$SCREENS_DIR" -maxdepth 1 -type f -name '*.svg' -print | sed "s#^#- #" | sort)

## Components
Count: $COMP_COUNT

$(find "$COMPONENTS_DIR" -maxdepth 1 -type f -name '*.svg' -print | sed "s#^#- #" | sort)
MD

echo "Done. Export index: $INDEX_FILE"
