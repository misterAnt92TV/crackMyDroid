# CrackMyDroid

🌐 **Langues :** [English](README.md) · [Italiano](README.it.md) · [Deutsch](README.de.md) · [Español](README.es.md)

---

Kotlin Multiplatform (Android + Desktop + iOS) pour la sécurité mobile, le reverse engineering et le diagnostic de l'appareil — le tout dans une interface partagée construite avec Compose Multiplatform.

---

## Fonctionnalités principales

| Section | Description |
|---|---|
| **Accueil** | Aperçu rapide, objectif de l'application et FAQ déroulantes |
| **Infos appareil** | Matériel/logiciel, correctifs de sécurité, empreinte, radio, noyau, bootloader, batterie/réseau, indicateurs de débogage (ADB, options développeur, debuggable), statut root ; chaque ligne copiable, rapport exportable en TXT |
| **Root / Play Integrity** | Vérification root avec RootBeer, contrôles Play Integrity avec nonce configurable et détail des tests |
| **PenTesting** | Liste de contrôles de sécurité avec statut (queued/ok/error), compteurs, détail des erreurs, export/partage du rapport |
| **Activités** | Liste des activités par application avec recherche/historique, favoris, expansion par paquet, lancement direct et export |
| **Applications installées (APK)** | Recherche, filtres, favoris, icônes des apps, export APK vers le stockage et partage direct |
| **Permissions** | Aperçu des permissions des applications installées avec analyse des risques, recherche et détails |
| **Shell/Astuces** | Commandes ADB/shell préconfigurées (avec indicateur root-required), confirmation optionnelle, sortie et code de retour |
| **Journal** | Visualiseur de journaux applicatifs avec export |
| **Paramètres** | Thème (clair/sombre/système), verbosité des journaux, accessibilité (contraste élevé, animations réduites, grand texte), licences des bibliothèques, version de l'application |

---

## Application Android

Le module `:androidApp` est l'application Android native. Elle s'exécute **directement sur l'appareil** et dispose d'un accès complet à toutes les API système.

### Fonctionnement

```
CrackMyDroidApp (Application)
  └─ initKoin()                          // démarre l'injection de dépendances
  └─ AndroidDeviceSnapshotCoordinator    // collecte les données de l'appareil en arrière-plan
       └─ enregistre le snapshot JSON dans /sdcard/Android/data/.../snapshot.json

MainActivity
  └─ setContent { CrackMyDroidApp() }   // interface partagée du module :shared
```

1. Au démarrage, `CrackMyDroidApp` (Application) initialise **Koin** et lance `AndroidDeviceSnapshotCoordinator` en arrière-plan.
2. Le coordinator collecte en continu les informations de l'appareil (matériel, statut root, Play Integrity, paquets, permissions…) et les sérialise dans un **fichier JSON** sur le stockage partagé de l'appareil.
3. `MainActivity` monte l'interface partagée (`CrackMyDroidApp` Composable du module `:shared`), qui lit les données directement depuis les repositories Android natifs.

### Prérequis

- Android **API 24+** (Android 7.0)
- JDK 17, Android SDK avec `platform-tools`/`adb` dans le PATH

### Build & Installation

```bash
# Compiler l'APK de débogage
./gradlew :androidApp:assembleDebug

# Installer sur un appareil connecté
adb install -r androidApp/build/outputs/apk/debug/androidApp-debug.apk

# Ou installer directement
./gradlew :androidApp:installDebug
```

---

## Application Desktop (Compose Multiplatform)

Le module `:desktopApp` est une application **JVM/Desktop** construite avec Compose Multiplatform. Elle n'accède pas directement aux API Android : elle utilise **ADB comme pont** pour se connecter à un appareil Android physique ou émulé et en lire les données.

### Fonctionnement

```
Application Desktop
  └─ DevicePickerScreen                 // liste les appareils ADB disponibles
       └─ adb devices
  └─ DeviceSessionControllerDesktop     // gère la session ADB
       └─ RemoteDeviceSnapshotImporter  // adb pull du JSON de snapshot
            └─ snapshot.json → structures de données Kotlin
  └─ DesktopToolbar                     // infos appareil, rescan, changement d'appareil
  └─ CrackMyDroidApp()                  // même interface que le module :shared
```

1. Au démarrage, l'**écran de sélection d'appareil** est affiché.
2. Si `adb` n'est pas dans le PATH système, le chemin du binaire peut être configuré manuellement (enregistré dans DataStore, non demandé à nouveau lors des exécutions suivantes).
3. En cliquant sur **« Détecter l'appareil »**, `adb devices` est exécuté ; les appareils détectés apparaissent dans la liste.
4. En sélectionnant un appareil, l'application effectue un `adb pull` du fichier JSON de snapshot généré par l'app Android sur l'appareil.
5. Le JSON est désérialisé et injecté dans les mêmes repositories partagés : l'**interface `CrackMyDroidApp()`** démarre identiquement à l'application Android, mais affiche les données de l'appareil distant.
6. La **barre d'outils supérieure** affiche : modèle + numéro de série + statut ADB, source des données (live/snapshot + horodatage), boutons pour rescan, changement d'appareil et paramètres ADB.

### Prérequis

- JDK 17+
- macOS, Linux ou Windows
- `adb` installé (Android SDK platform-tools) et dans le PATH **ou** chemin configurable dans l'application

### Build & Lancement

```bash
# Lancer l'application desktop directement
./gradlew :desktopApp:run

# Créer un paquet distribuable (macOS .dmg, Linux .deb, Windows .msi)
./gradlew :desktopApp:packageDistributionForCurrentOS
```

---

## Utiliser Android + Desktop ensemble

L'application Android et l'application desktop se **complètent** : la première collecte les données sur l'appareil, la seconde les affiche sur un ordinateur connecté via ADB.

```
[PC/Mac]                          [Appareil Android]
  desktopApp                          androidApp
      │                                   │
      │  adb pull snapshot.json ◄─────────┤ AndroidDeviceSnapshotCoordinator
      │                                   │ (génère le JSON en arrière-plan)
      ▼                                   │
  Interface avec toutes                   │
  les données de l'appareil              │
```

### Déroulement étape par étape

1. **Installer l'application Android** sur l'appareil (voir ci-dessus).
2. **Lancer l'application Android** sur l'appareil et la laisser tourner quelques secondes — le coordinator génère le JSON de snapshot.
3. **Connecter l'appareil au PC** via USB avec le **débogage USB activé** (ou utiliser un émulateur déjà actif).
4. **Vérifier qu'ADB détecte l'appareil :**
   ```bash
   adb devices
   # attendu : <serial>   device
   ```
   Si le statut est `unauthorized`, autoriser l'ordinateur sur l'appareil.
5. **Lancer l'application desktop :**
   ```bash
   ./gradlew :desktopApp:run
   ```
6. Dans l'écran picker, cliquer sur **« Détecter l'appareil »** → sélectionner l'appareil.
7. L'application importe le snapshot et affiche l'interface complète avec les données de l'appareil.

> **Remarque :** si l'application Android n'est pas installée ou si le snapshot n'a pas encore été généré, l'application desktop signale *« Snapshot indisponible »* et affiche tout de même l'interface avec les informations de base récupérables directement via ADB (liste des paquets, commandes shell, etc.).

---

## Structure du projet

```
newCrackMyDroid/
├── androidApp/          # Application Android native (Activity, Application, BroadcastReceiver)
├── desktopApp/          # Application JVM Desktop (point d'entrée, picker, toolbar)
├── shared/              # Module KMP partagé
│   ├── commonMain/      # Compose UI, ViewModel, UseCases, modèles, DI
│   ├── androidMain/     # Repositories Android natifs (RootBeer, Play Integrity, DataStore…)
│   ├── desktopMain/     # Repositories desktop via ADB, driver SQLite, chemins
│   ├── iosMain/         # Stubs iOS (fonctionnalités Android non disponibles)
│   ├── jvmCommonTest/   # Tests JVM partagés (MockK, Android + Desktop)
│   └── desktopTest/     # Tests spécifiques au layer desktop/ADB
├── iosApp/              # Application iOS (wrapper SwiftUI)
└── gradle/
    └── libs.versions.toml  # Catalogue de versions centralisé
```

### Navigation UI (shared)

- **Barre d'onglets inférieure :** Accueil · Infos appareil · Paramètres
- **Menu latéral :** Applications installées · PenTest · Permissions · Activités · Root · Astuces · Journal

---

## Stack technique

| Couche | Bibliothèques |
|---|---|
| UI | Compose Multiplatform, Material3 |
| DI | Koin (core, Android, Compose) |
| Async | Kotlin Coroutines + Flow |
| Base de données locale | SQLDelight (driver Android / driver SQLite JVM) |
| Sécurité Android | RootBeer, Play Integrity API |
| Préférences | DataStore Preferences |
| Journalisation | Kermit |
| Tests | kotlin-test, MockK, kotlinx-coroutines-test |

---

## Notes iOS

Le module iOS est inclus pour partager l'interface et la logique. Les fonctionnalités dépendant des API Android (Root, ADB, Play Integrity, export APK) ne sont pas disponibles sur iOS et sont remplacées par des stubs.
