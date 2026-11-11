package com.crackmydroid.ui.strings

import androidx.compose.runtime.staticCompositionLocalOf

data class UiStrings(
    val confirm: String,
    val expanded: String,
    val collapsed: String,
    val scrollToTop: String,
    val help: String,
    val back: String,
    val menu: String,
    val exportDetailsTitle: String,
    val exportFile: (String) -> String,
    val exportFormat: (String) -> String,
    val exportPath: (String) -> String,
    val exportDirectory: (String) -> String,
)

val LocalUiStrings = staticCompositionLocalOf {
    UiStrings(
        confirm = "Confirm",
        expanded = "Expanded",
        collapsed = "Collapsed",
        scrollToTop = "Scroll to top",
        help = "Help",
        back = "Back",
        menu = "Menu",
        exportDetailsTitle = "Export details",
        exportFile = { "File: $it" },
        exportFormat = { "Format: $it" },
        exportPath = { "Path: $it" },
        exportDirectory = { "Directory: $it" },
    )
}
