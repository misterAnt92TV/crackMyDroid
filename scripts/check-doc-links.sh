#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

DOC_FILES="$(find README.md docs -type f -name '*.md' 2>/dev/null | sort)"

if [ -z "$DOC_FILES" ]; then
  echo "No markdown files found in README/docs."
  exit 0
fi

missing=0
checked=0
while IFS= read -r file; do
  [ -z "$file" ] && continue
  checked=$((checked + 1))
  while IFS= read -r link; do
    target="${link%%#*}"

    [[ -z "$target" ]] && continue
    [[ "$target" =~ ^https?:// ]] && continue
    [[ "$target" =~ ^mailto: ]] && continue

    if [[ "$target" = /* ]]; then
      resolved="$ROOT_DIR${target}"
    else
      resolved="$(cd "$(dirname "$file")" && pwd)/$target"
    fi

    if [[ ! -e "$resolved" ]]; then
      echo "Missing link target: $file -> $link"
      missing=1
    fi
  done <<EOF_LINKS
$(rg -o '\[[^\]]+\]\(([^)]+)\)' "$file" | sed -E 's/^.*\(([^)]+)\)$/\1/' | sort -u)
EOF_LINKS
done <<EOF_FILES
$DOC_FILES
EOF_FILES

if [[ "$missing" -ne 0 ]]; then
  echo "Doc link check failed."
  exit 1
fi

echo "Doc link check passed (${checked} files checked)."
