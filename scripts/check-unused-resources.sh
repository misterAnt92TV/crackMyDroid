#!/usr/bin/env bash
set -euo pipefail

project_root="$(cd "$(dirname "$0")/.." && pwd)"
cd "$project_root"

unused=0

check_shared_strings() {
    local string_name occurrences

    while read -r string_name; do
        occurrences=$(rg -w "$string_name" shared desktopApp androidApp --glob '*.kt' | wc -l | tr -d ' ')
        # One declaration plus one named argument in each of the five supported locales.
        if [ "$occurrences" -le 6 ]; then
            printf 'Unused shared string: %s\n' "$string_name"
            unused=1
        fi
    done < <(
        awk '/^data class Strings\(/,/^\)/ { if ($1 == "val") { gsub(/:/, "", $2); print $2 } }' \
            shared/src/commonMain/kotlin/com/crackmydroid/shared/i18n/Strings.kt
    )
}

check_android_strings() {
    local string_name references

    while read -r string_name; do
        references=$(rg -n "R\\.string\\.$string_name|@string/$string_name" androidApp shared \
            --glob '*.{kt,xml}' | wc -l | tr -d ' ')
        if [ "$references" -eq 0 ]; then
            printf 'Unused Android string: %s\n' "$string_name"
            unused=1
        fi
    done < <(
        rg -o '<string name="[^"]+' androidApp/src/main/res/values*/strings.xml | \
            sed -E 's/.*<string name="//'
    )
}

check_shared_strings
check_android_strings
exit "$unused"
