# Android runtime screenshots

This gallery records the English runtime appearance of CrackMyDroid from the clean
`Small_Phone_-_720x1280` AVD. The images are authoritative for runtime layout and are also imported
as non-editable references beside the native boards in the hosted Penpot `Screens` page.

## Reproduce the capture

1. Start the authorized AVD and confirm that `adb devices -l` reports `emulator-5554`.
2. Build and install with `./gradlew :androidApp:installDebug`, then launch
   `com.crackmydroid.android` on the emulator serial (never select a physical device).
3. Dismiss the first-run guidance with the accessible `Got it` action. Open `Settings`, scroll to
   `Language`, and select `English`.
4. Navigate with the accessibility UI driver using the bottom navigation and `Menu` drawer labels.
   Wait for the target label to be visible before each capture.
5. Capture only stable screens with `adb -s emulator-5554 exec-out screencap -p`.

The complete machine-readable record
is [the screenshot manifest](design/exports/screenshots/manifest.json). All PNGs are 720×1280 RGBA
images. The emulator inventory is used as-is; no production data or temporary fixture APK is
included.

## Gallery

| Screen                | Runtime state                | Image                                                                                 |
|-----------------------|------------------------------|---------------------------------------------------------------------------------------|
| Home                  | Recent operations populated  | [home.png](design/exports/screenshots/android/home.png)                               |
| Device Info           | Default device summary       | [device-info.png](design/exports/screenshots/android/device-info.png)                 |
| Settings              | English selected             | [settings.png](design/exports/screenshots/android/settings.png)                       |
| Installed Apps        | First-entry APK guidance     | [installed-apps.png](design/exports/screenshots/android/installed-apps.png)           |
| PenTesting            | Step 1/4 wizard              | [pentesting.png](design/exports/screenshots/android/pentesting.png)                   |
| Permissions           | Installed-app inventory      | [permissions.png](design/exports/screenshots/android/permissions.png)                 |
| Activities            | Installed activity inventory | [activities.png](design/exports/screenshots/android/activities.png)                   |
| Root / Play Integrity | Checks ready                 | [root-play-integrity.png](design/exports/screenshots/android/root-play-integrity.png) |
| Shell / Trick         | Command cards ready          | [shell-trick.png](design/exports/screenshots/android/shell-trick.png)                 |
| Log                   | Filtered log view            | [log.png](design/exports/screenshots/android/log.png)                                 |
| Help                  | English feature map          | [help.png](design/exports/screenshots/android/help.png)                               |

### Additional states

First-entry guidance and contextual help are preserved as separate
references: [PenTesting tip](design/exports/screenshots/android/pentesting-tip.png), [Permissions tip](design/exports/screenshots/android/permissions-tip.png), [Activities tip](design/exports/screenshots/android/activities-tip.png), [Root / Play Integrity tip](design/exports/screenshots/android/root-play-integrity-tip.png), [Shell / Trick tip](design/exports/screenshots/android/shell-trick-tip.png),
and [Help dialog](design/exports/screenshots/android/help-dialog.png).

Some legacy strings remain Italian in the current Compose implementation (for example drawer
descriptions and action subtitles) even after selecting English. They are intentionally documented
as runtime limitations rather than edited in the screenshot artifacts.
