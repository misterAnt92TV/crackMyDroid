#!/usr/bin/env bash
set -euo pipefail

screens_root='shared/src/commonMain/kotlin/com/crackmydroid/shared/ui/screens'
if rg -n -U '\bTODO\s*\(|\bNotImplementedError\b|\bfun\s+(AppCard|ExportDetailsCard)\s*\(' "$screens_root" --glob '*.kt'; then
  echo "UI component guard failed: unimplemented code or local shared-card declarations in screens." >&2
  exit 1
else
  status=$?
  if ((status != 1)); then
    echo "UI component guard failed: could not inspect screen sources." >&2
    exit "$status"
  fi
fi

settings_screen="$screens_root/SettingsScreen.kt"
if rg -n -U '(CardBlock|AppText)\s*\(\s*"\p{L}|(label|text|contentDescription)\s*=\s*"\p{L}' "$settings_screen"; then
  echo "UI localization guard failed: Settings copy must come from LocalStrings." >&2
  exit 1
else
  status=$?
  if ((status != 1)); then
    echo "UI localization guard failed: could not inspect Settings source." >&2
    exit "$status"
  fi
fi

activity_screen='shared/src/commonMain/kotlin/com/crackmydroid/shared/ui/screens/ActivityListScreen.kt'
activity_literals=$(awk '
  $0 !~ /[•]/ &&
  ($0 ~ /AppText[[:space:]]*\([[:space:]]*"/ ||
   $0 ~ /showSnackbar[[:space:]]*\([[:space:]]*"/ ||
   $0 ~ /(title|subtitle|summary|label|text|contentDescription)[[:space:]]*=[[:space:]]*"/) &&
  $0 !~ /historyKey[[:space:]]*=[[:space:]]*"activities"/ { print NR ":" $0 }
' "$activity_screen")
if [[ -n "$activity_literals" ]]; then
  printf '%s\n' "$activity_literals"
  echo "UI localization guard failed: ActivityListScreen contains hardcoded user-visible copy." >&2
  exit 1
fi

# Runtime component copy must come from the shared Strings catalog. Preview-only
# sample copy is allowed after the component preview annotation.
components_root='shared/src/commonMain/kotlin/com/crackmydroid/shared/presentation/components'
localization_violations=()
while IFS= read -r file; do
  matches=$(awk '
    /@[A-Za-z0-9_]*Preview/ { preview = 1 }
    !preview && ($0 !~ /[•✓✕⏱]/) && ($0 ~ /AppText[[:space:]]*\([[:space:]]*"/ ||
                 $0 ~ /text[[:space:]]*=[[:space:]]*"/ ||
                 $0 ~ /label[[:space:]]*=[[:space:]]*"/ ||
                 $0 ~ /contentDescription[[:space:]]*=[[:space:]]*"/) { print NR ":" $0 }
  ' "$file")
  if [[ -n "$matches" ]]; then
    localization_violations+=("$file\n$matches")
  fi
done < <(find "$components_root" -type f -name '*.kt' -print | sort)

if ((${#localization_violations[@]})); then
  printf 'UI localization guard failed: runtime component copy must use LocalStrings/Strings:\n%b\n' "${localization_violations[*]}" >&2
  exit 1
fi

# Direct Material 3 primitives are allowed only inside the design-system package or
# in this explicit migration baseline. Remove entries as each screen is migrated.
baseline=(
  'shared/src/commonMain/kotlin/com/crackmydroid/shared/ui/App.kt'
  'shared/src/commonMain/kotlin/com/crackmydroid/shared/ui/screens/ActivityListScreen.kt'
  'shared/src/commonMain/kotlin/com/crackmydroid/shared/ui/screens/InfoScreen.kt'
  'shared/src/commonMain/kotlin/com/crackmydroid/shared/ui/screens/InstalledAppsScreen.kt'
  'shared/src/commonMain/kotlin/com/crackmydroid/shared/ui/screens/LogScreen.kt'
  'shared/src/commonMain/kotlin/com/crackmydroid/shared/ui/screens/PenTestingScreen.kt'
  'shared/src/commonMain/kotlin/com/crackmydroid/shared/ui/screens/PermissionsScreen.kt'
  'shared/src/commonMain/kotlin/com/crackmydroid/shared/ui/screens/RootScreen.kt'
  'shared/src/commonMain/kotlin/com/crackmydroid/shared/ui/screens/TrickScreen.kt'
)

violations=()
while IFS= read -r file; do
  [[ "$file" == *'/presentation/components/'* ]] && continue
  skip=false
  for allowed in "${baseline[@]}"; do [[ "$file" == "$allowed" ]] && skip=true; done
  "$skip" || violations+=("$file")
done < <(rg -l 'androidx\.compose\.material3\.(AlertDialog|Button|ElevatedCard|OutlinedTextField|CircularProgressIndicator|LinearProgressIndicator)' shared/src/commonMain/kotlin/com/crackmydroid/shared/ui desktopApp/src/desktopMain --glob '*.kt' || true)

if ((${#violations[@]})); then
  printf 'Direct Material 3 primitives outside the approved migration baseline:\n%s\n' "${violations[*]}"
  exit 1
fi
