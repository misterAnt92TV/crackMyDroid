package com.crackmydroid.shared.i18n

import com.crackmydroid.ui.strings.UiStrings

fun Strings.toUiStrings(): UiStrings = UiStrings(
    confirm = confirm,
    expanded = expandedStateDescription,
    collapsed = collapsedStateDescription,
    scrollToTop = scrollToTopContentDescription,
    help = helpContentDescription,
    back = backContentDescription,
    menu = menuContentDescription,
    search = searchContentDescription,
    exportDetailsTitle = exportDetailsTitle,
    exportFile = { exportFile.format(it) },
    exportFormat = { exportFormat.format(it) },
    exportPath = { exportPath.format(it) },
    exportDirectory = { exportDirectory.format(it) },
)
