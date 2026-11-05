#!/usr/bin/env bash
set -euo pipefail

ui_root="shared/src/commonMain/kotlin/com/crackmydroid/shared/ui/screens"
ui_files=("$ui_root" "shared/src/commonMain/kotlin/com/crackmydroid/shared/ui/App.kt")
forbidden='KoinPlatform|OperationLogRepository|UseCase|Repository|viewModelScope|runCatching|ActivityIntentRiskAnalyzer|android\.content|android\.os|java\.io|mutableStateOf<[^>]*(Entry|Result|Execution|Screen)'

if rg -n "$forbidden" "${ui_files[@]}" --glob '*.kt'; then
  echo "UI architecture guard failed: application/data/platform logic found in screens." >&2
  exit 1
fi

presentation_root="shared/src/commonMain/kotlin/com/crackmydroid/shared/ui"
if rg -n 'import com\.crackmydroid\.shared\.ui\.screens\.' "$presentation_root" \
  --glob '*ViewModel.kt' --glob '*State.kt'; then
  echo "UI architecture guard failed: presentation state or ViewModel depends on a screen package." >&2
  exit 1
fi

visual_scope='\.launch[[:space:]]*\{[^}]*?(animateScrollToItem|animateScrollTo|showSnackbar|drawerState\.(open|close))'
warnings=$(rg -n '\.launch[[:space:]]*\{|async[[:space:]]*\{' "${ui_files[@]}" --glob '*.kt' | rg -v "$visual_scope" || true)
if [[ -n "$warnings" ]]; then
  echo "UI architecture guard warning: review non-visual coroutine usage:" >&2
  echo "$warnings" >&2
fi

echo "UI architecture guard passed."
