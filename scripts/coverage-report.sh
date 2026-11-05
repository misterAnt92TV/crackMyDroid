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
  shared      -> esegue test JVM/Desktop e Android Debug
  androidApp  -> esegue Android unit test

Esempi:
  scripts/coverage-report.sh all
  scripts/coverage-report.sh shared
  scripts/coverage-report.sh shared,androidApp
  scripts/coverage-report.sh shared --skip-tests

Output principali:
  - Report test per task: <modulo>/build/reports/tests/<task>/index.html
  - Report coverage Kover aggregato: build/reports/kover/html/index.html
  - Report coverage XML aggregato: build/reports/kover/report.xml
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
  MODULES="shared androidApp"
else
  MODULES="$(echo "$MODULES_ARG" | tr ',' ' ')"
fi

is_valid_module() {
  case "$1" in
    shared|androidApp) return 0 ;;
    *) return 1 ;;
  esac
}

TEST_TASKS=""
EXPECTED_REPORTS=""
KOVER_TASKS=""
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
  esac
done

KOVER_TASKS=":koverXmlReportCoverage :koverHtmlReportCoverage"

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

apply_kover_html_theme() {
  report="$1"
  tmp_file="${report}.tmp.$$"
  if awk '
    BEGIN {
      theme = "<style id=\"kover-report-theme\">:root{color-scheme:light;--kover-gutter:24px;--kover-surface:#ffffff;--kover-canvas:#f4f7fb;--kover-ink:#172033;--kover-muted:#5f6b82;--kover-border:#dce3ee;--kover-primary:#175cd3;--kover-primary-dark:#123e88;--kover-covered:#166534;--kover-partial:#9a6700;--kover-uncovered:#b42318}*{box-sizing:border-box}body{margin:0;padding:var(--kover-gutter);min-width:320px;background:var(--kover-canvas);color:var(--kover-ink);font:14px/1.5 Inter,ui-sans-serif,system-ui,-apple-system,BlinkMacSystemFont,\"Segoe UI\",sans-serif}.content{width:100%;min-width:0;overflow-x:auto;padding:clamp(18px,3vw,32px);background:var(--kover-surface);border:1px solid var(--kover-border);border-radius:16px;box-shadow:0 12px 30px rgba(23,32,51,.08)}.breadCrumbs{margin:0 0 20px;color:var(--kover-muted);font-size:12px;font-weight:600;letter-spacing:.01em}.separator{padding:0 8px;color:#9aa6ba}h1,h2{color:var(--kover-primary-dark);font-weight:700;line-height:1.25}h1{margin:0 0 20px;font-size:clamp(22px,3vw,30px)}h2{margin:28px 0 12px;font-size:18px}a{color:var(--kover-primary);font-weight:600;text-decoration:none}a:hover,a:focus-visible{text-decoration:underline}a:focus-visible{outline:2px solid var(--kover-primary);outline-offset:2px;border-radius:3px}table.coverageStats,table.overallStats{width:100%;border-collapse:separate;border-spacing:0;border:1px solid var(--kover-border);border-radius:12px;overflow:hidden;background:#fff}table.coverageStats th,table.coverageStats td{padding:12px 14px;border:0;border-bottom:1px solid var(--kover-border);vertical-align:middle}table.coverageStats th{background:#edf3ff;color:var(--kover-primary-dark);font-size:11px;font-weight:750;letter-spacing:.06em;text-transform:uppercase}table.coverageStats th a{color:inherit}table.coverageStats tr:last-child td{border-bottom:0}table.coverageStats tbody tr:hover td{background:#f7faff}.percent{font-size:14px;font-weight:750}.absValue{color:var(--kover-muted);font-size:12px}.percent .green,.absValue .green{color:var(--kover-covered)}.percent .red,.absValue .red{color:var(--kover-uncovered)}.percent .totalDiff{color:var(--kover-muted)}.kover-module-toolbar{display:flex;align-items:end;justify-content:space-between;gap:16px;margin:0 0 14px;padding:14px 16px;border:1px solid var(--kover-border);border-radius:12px;background:#f8faff}.kover-module-toolbar__control{display:grid;gap:6px;min-width:min(280px,100%)}.kover-module-toolbar label{color:var(--kover-primary-dark);font-size:12px;font-weight:750;letter-spacing:.04em;text-transform:uppercase}.kover-module-toolbar select{min-height:40px;padding:8px 36px 8px 12px;border:1px solid #b8c7df;border-radius:8px;background:#fff;color:var(--kover-ink);font:inherit;font-weight:600}.kover-module-toolbar select:focus-visible{outline:2px solid var(--kover-primary);outline-offset:2px}.kover-module-toolbar output{color:var(--kover-muted);font-size:13px;font-weight:600;white-space:nowrap}tr[hidden]{display:none}code.sourceCode{display:block;width:100%;overflow:auto;padding:16px;border:1px solid var(--kover-border);border-radius:10px;background:#f8fafc;font:12px/1.55 \"SFMono-Regular\",Consolas,\"Liberation Mono\",monospace}code.sourceCode .fc{background:#dcfce7}code.sourceCode .pc{background:#fef3c7}code.sourceCode .nc{background:#fee2e2}div.footer{margin:28px 0 0;color:var(--kover-muted)}@media (max-width:640px){:root{--kover-gutter:12px}.content{padding:16px;border-radius:12px}table.coverageStats th,table.coverageStats td{padding:10px 9px}h1{font-size:22px}.kover-module-toolbar{align-items:stretch;flex-direction:column}.kover-module-toolbar output{white-space:normal}}</style>"
      filter = "<script id=\"kover-report-module-filter\">(function(){function setup(){var overview=Array.prototype.some.call(document.querySelectorAll(\"h1\"),function(node){return node.textContent.indexOf(\"Overall Coverage Summary\")!==-1});if(!overview){return}var heading=Array.prototype.find.call(document.querySelectorAll(\"h2\"),function(node){return node.textContent.trim()===\"Coverage Breakdown\"});if(!heading){return}var table=heading.nextElementSibling;if(!table||!table.classList.contains(\"coverageStats\")){return}var rows=Array.prototype.filter.call(table.querySelectorAll(\"tr\"),function(row){return row.querySelector(\"td.name a\")});if(rows.length<2){return}function groupFor(name){name=name.trim();if(name===\"com.crackmydroid.shared\"){return \"core\"}if(name.indexOf(\"com.crackmydroid.database\")===0){return \"database\"}var prefix=\"com.crackmydroid.shared.\";if(name.indexOf(prefix)===0){return name.slice(prefix.length).split(\".\")[0]||\"core\"}return \"other\"}var labels={core:\"Core\",database:\"Database\",data:\"Data\",di:\"DI\",domain:\"Domain\",i18n:\"I18n\",presentation:\"Presentation\",ui:\"UI\",util:\"Util\",other:\"Other\"};var groups=[];rows.forEach(function(row){var group=groupFor(row.querySelector(\"td.name a\").textContent);row.dataset.koverGroup=group;if(groups.indexOf(group)===-1){groups.push(group)}});if(groups.length<2){return}groups.sort(function(left,right){return labels[left].localeCompare(labels[right])});var toolbar=document.createElement(\"section\");toolbar.className=\"kover-module-toolbar\";toolbar.setAttribute(\"aria-label\",\"Filtro moduli coverage\");var control=document.createElement(\"div\");control.className=\"kover-module-toolbar__control\";var label=document.createElement(\"label\");label.htmlFor=\"kover-module-filter\";label.textContent=\"Mostra moduli\";var select=document.createElement(\"select\");select.id=\"kover-module-filter\";select.name=\"kover-module-filter\";select.add(new Option(\"Tutti i moduli\",\"all\",true,true));groups.forEach(function(group){select.add(new Option(labels[group]||group,group))});control.appendChild(label);control.appendChild(select);var count=document.createElement(\"output\");count.setAttribute(\"aria-live\",\"polite\");function update(){var visible=0;rows.forEach(function(row){var show=select.value===\"all\"||row.dataset.koverGroup===select.value;row.hidden=!show;if(show){visible+=1}});count.textContent=visible+\" di \"+rows.length+\" package visibili\"}select.addEventListener(\"change\",update);toolbar.appendChild(control);toolbar.appendChild(count);heading.insertAdjacentElement(\"afterend\",toolbar);update()}if(document.readyState===\"loading\"){document.addEventListener(\"DOMContentLoaded\",setup)}else{setup()}})();</script>"
      enhancement = theme filter
    }
    {
      if (skipping) {
        if (index($0, skip_end)) {
          skipping = 0
        }
        next
      }

      if (index($0, "id=\"kover-report-theme\"") || index($0, "id=\"kover-report-padding\"")) {
        if (!inserted) {
          print enhancement
          inserted = 1
        }
        if (!index($0, "</style>")) {
          skipping = 1
          skip_end = "</style>"
        }
        next
      }

      if (index($0, "id=\"kover-report-module-filter\"")) {
        if (!inserted) {
          print enhancement
          inserted = 1
        }
        if (!index($0, "</script>")) {
          skipping = 1
          skip_end = "</script>"
        }
        next
      }

      head_end = index($0, "</head>")
      if (head_end && !inserted) {
        print substr($0, 1, head_end - 1)
        print enhancement
        print substr($0, head_end)
        inserted = 1
      } else {
        print
      }
    }
    END {
      if (!inserted) exit 1
    }
  ' "$report" > "$tmp_file"; then
    if ! mv "$tmp_file" "$report"; then
      rm -f "$tmp_file"
      echo "ERRORE: impossibile aggiornare il report HTML Kover: $report" >&2
      return 1
    fi
  else
    rm -f "$tmp_file"
    echo "ERRORE: tag </head> non trovato nel report HTML Kover: $report" >&2
    return 1
  fi
}

for report in $(find "build/reports/kover/html" -type f -name '*.html' 2>/dev/null); do
  if ! apply_kover_html_theme "$report"; then
    KOVER_EXIT=1
  fi
done

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

KOVER_HTML_REPORT="build/reports/kover/html/index.html"
KOVER_XML_REPORT="build/reports/kover/report.xml"

{
  echo "[kover aggregated]"
  if [ -f "$KOVER_HTML_REPORT" ]; then
    echo "  - OK   $KOVER_HTML_REPORT"
  else
    echo "  - MISS $KOVER_HTML_REPORT"
  fi
  if [ -f "$KOVER_XML_REPORT" ]; then
    echo "  - OK   $KOVER_XML_REPORT"
  else
    echo "  - MISS $KOVER_XML_REPORT"
  fi
  echo ""
} >> "$SUMMARY_FILE"

COVERED_TOTAL="$(grep -o 'covered="[0-9]*"' "$KOVER_XML_REPORT" 2>/dev/null | tail -n 1 | cut -d'"' -f2)"
MISSED_TOTAL="$(grep -o 'missed="[0-9]*"' "$KOVER_XML_REPORT" 2>/dev/null | tail -n 1 | cut -d'"' -f2)"

if [ -z "$COVERED_TOTAL" ] || [ -z "$MISSED_TOTAL" ]; then
  echo "ERRORE: report Kover aggregato senza istruzioni rilevate: $KOVER_XML_REPORT" >&2
  KOVER_EXIT=1
else
  TOTAL=$((COVERED_TOTAL + MISSED_TOTAL))
  if [ "$TOTAL" -eq 0 ]; then
    echo "ERRORE: report Kover aggregato senza istruzioni rilevate: $KOVER_XML_REPORT" >&2
    KOVER_EXIT=1
    exit "$KOVER_EXIT"
  fi
  PCT=$(awk "BEGIN { printf \"%.2f\", ($COVERED_TOTAL/$TOTAL)*100 }")
  {
    echo ""
    echo "[coverage]"
    echo "  - source: $KOVER_XML_REPORT"
    echo "  - covered instructions: $COVERED_TOTAL"
    echo "  - missed instructions: $MISSED_TOTAL"
    echo "  - instruction coverage: ${PCT}%"
    echo ""
  } >> "$SUMMARY_FILE"
  echo "==> Coverage instructions: ${PCT}% (covered=$COVERED_TOTAL missed=$MISSED_TOTAL)"
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
