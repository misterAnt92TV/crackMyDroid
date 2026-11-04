#!/usr/bin/env bash
set -euo pipefail

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
