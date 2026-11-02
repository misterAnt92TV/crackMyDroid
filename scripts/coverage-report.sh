#!/usr/bin/env sh
set -u

ROOT_DIR="$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)"
cd "$ROOT_DIR"

if [ ! -x "./gradlew" ]; then
  echo "Errore: gradlew non trovato in $ROOT_DIR"
  exit 1
fi

usage() {
  cat <<USAGE
Usage:
  scripts/coverage-report.sh [all|module1,module2,...] [--skip-tests]

Moduli supportati:
  shared      -> esegue :shared:desktopTest :shared:testDebugUnitTest
  androidApp  -> esegue :androidApp:testDebugUnitTest
  desktopApp  -> esegue :desktopApp:desktopTest

Esempi:
  scripts/coverage-report.sh all
  scripts/coverage-report.sh shared
  scripts/coverage-report.sh shared,desktopApp
  scripts/coverage-report.sh shared --skip-tests

Output principali:
  - Report test per task: <modulo>/build/reports/tests/<task>/index.html
  - Report coverage Kover: build/reports/kover/html/index.html e build/reports/kover/report.xml
  - Report summary: build/reports/coverage/coverage-summary.txt
USAGE
}

MODULES_ARG="${1:-all}"
SKIP_TESTS="false"

if [ "$MODULES_ARG" = "-h" ] || [ "$MODULES_ARG" = "--help" ]; then
  usage
  exit 0
fi

if [ "${2:-}" = "--skip-tests" ] || [ "$MODULES_ARG" = "--skip-tests" ]; then
  SKIP_TESTS="true"
  if [ "$MODULES_ARG" = "--skip-tests" ]; then
    MODULES_ARG="all"
  fi
fi

if [ "$MODULES_ARG" = "all" ]; then
  MODULES="shared androidApp desktopApp"
else
  MODULES="$(echo "$MODULES_ARG" | tr ',' ' ')"
fi

is_valid_module() {
  case "$1" in
    shared|androidApp|desktopApp) return 0 ;;
    *) return 1 ;;
  esac
}

TEST_TASKS=""
EXPECTED_REPORTS=""
for module in $MODULES; do
  if ! is_valid_module "$module"; then
    echo "Modulo non supportato: $module"
    usage
    exit 1
  fi

  case "$module" in
    shared)
      TEST_TASKS="$TEST_TASKS :shared:desktopTest :shared:testDebugUnitTest"
      EXPECTED_REPORTS="$EXPECTED_REPORTS shared/build/reports/tests/desktopTest/index.html shared/build/reports/tests/testDebugUnitTest/index.html"
      ;;
    androidApp)
      TEST_TASKS="$TEST_TASKS :androidApp:testDebugUnitTest"
      EXPECTED_REPORTS="$EXPECTED_REPORTS androidApp/build/reports/tests/testDebugUnitTest/index.html"
      ;;
    desktopApp)
      TEST_TASKS="$TEST_TASKS :desktopApp:desktopTest"
      EXPECTED_REPORTS="$EXPECTED_REPORTS desktopApp/build/reports/tests/desktopTest/index.html"
      ;;
  esac
done

KOVER_TASKS=":koverXmlReport :koverHtmlReport"

SUMMARY_DIR="build/reports/coverage"
SUMMARY_FILE="$SUMMARY_DIR/coverage-summary.txt"
mkdir -p "$SUMMARY_DIR"

{
  echo "Coverage Summary"
  echo "Generated at: $(date '+%Y-%m-%d %H:%M:%S %z')"
  echo "Root: $ROOT_DIR"
  echo "Modules: $MODULES"
  echo ""
  echo "[configured test tasks]"
  for task in $TEST_TASKS; do
    echo "  - $task"
  done
  echo ""
} > "$SUMMARY_FILE"

TEST_EXIT=0
KOVER_EXIT=0

if [ "$SKIP_TESTS" = "false" ]; then
  echo "==> Running tests for modules: $MODULES"
  ./gradlew $TEST_TASKS --continue --console=plain --no-daemon || TEST_EXIT=$?
  if [ "$TEST_EXIT" -ne 0 ]; then
    echo "ATTENZIONE: alcuni test sono falliti (exit=$TEST_EXIT), continuo con la generazione coverage."
  fi
else
  echo "==> Skipping test execution (--skip-tests)"
fi

echo "==> Generating Kover reports"
./gradlew $KOVER_TASKS --continue --console=plain --no-daemon || KOVER_EXIT=$?

{
  echo "[test reports expected]"
  for report in $EXPECTED_REPORTS; do
    if [ -f "$report" ]; then
      echo "  - OK   $report"
    else
      echo "  - MISS $report"
    fi
  done
  echo ""
} >> "$SUMMARY_FILE"

append_if_dir_exists() {
  label="$1"
  dir="$2"
  if [ -d "$dir" ]; then
    # shellcheck disable=SC2129
    echo "[$label] $dir" >> "$SUMMARY_FILE"
    find "$dir" -type f | sed 's#^#  - #' >> "$SUMMARY_FILE"
    echo "" >> "$SUMMARY_FILE"
  fi
}

for module in $MODULES; do
  append_if_dir_exists "$module tests" "$module/build/reports/tests"
done

append_if_dir_exists "root kover" "build/reports/kover"

if [ -f "build/reports/kover/report.xml" ]; then
  COVERED="$(grep -m1 -o 'covered="[0-9]*"' build/reports/kover/report.xml | cut -d'"' -f2)"
  MISSED="$(grep -m1 -o 'missed="[0-9]*"' build/reports/kover/report.xml | cut -d'"' -f2)"
  if [ -n "$COVERED" ] && [ -n "$MISSED" ]; then
    TOTAL=$((COVERED + MISSED))
    if [ "$TOTAL" -gt 0 ]; then
      PCT=$(awk "BEGIN { printf \"%.2f\", ($COVERED/$TOTAL)*100 }")
      {
        echo "[coverage]"
        echo "  - covered instructions: $COVERED"
        echo "  - missed instructions: $MISSED"
        echo "  - instruction coverage: ${PCT}%"
        echo ""
      } >> "$SUMMARY_FILE"
      echo "==> Coverage instructions: ${PCT}% (covered=$COVERED missed=$MISSED)"
    fi
  fi
fi

echo "==> Summary generated: $SUMMARY_FILE"

if [ "$KOVER_EXIT" -ne 0 ]; then
  echo "ERRORE: generazione report Kover fallita (exit=$KOVER_EXIT)"
  exit "$KOVER_EXIT"
fi

if [ "$TEST_EXIT" -ne 0 ]; then
  echo "Completato con test falliti (exit test=$TEST_EXIT), report coverage comunque generato."
  exit "$TEST_EXIT"
fi

echo "Done."
