# CrackMyDroid

🌐 **Idiomas:** [English](README.md) · [Italiano](README.it.md) · [Deutsch](README.de.md) · [Français](README.fr.md)

---

Kotlin Multiplatform (Android + Desktop + iOS) para seguridad móvil, ingeniería inversa y diagnóstico del dispositivo — todo en una única interfaz compartida construida con Compose Multiplatform.

---

## Funcionalidades principales

| Sección | Descripción |
|---|---|
| **Inicio** | Resumen rápido, propósito de la app y preguntas frecuentes expandibles |
| **Info del dispositivo** | Hardware/software, parches de seguridad, huella digital, radio, kernel, bootloader, batería/red, indicadores de depuración (ADB, opciones de desarrollador, debuggable), estado root; cada fila copiable, informe exportable en TXT |
| **Root / Play Integrity** | Verificación root con RootBeer, comprobaciones Play Integrity con nonce configurable y detalle de pruebas |
| **PenTesting** | Lista de comprobaciones de seguridad con estado (queued/ok/error), contadores, detalle de errores, exportar/compartir informe |
| **Actividades** | Lista de actividades por app con búsqueda/historial, favoritos, expansión por paquete, lanzamiento directo y exportación |
| **Apps instaladas (APK)** | Búsqueda, filtros, favoritos, iconos de app, exportar APK al almacenamiento y compartir directamente |
| **Permisos** | Resumen de permisos de apps instaladas con análisis de riesgo, búsqueda y detalles |
| **Shell/Trucos** | Comandos ADB/shell preconfigurados (con indicador root-required), confirmación opcional, salida y código de retorno |
| **Registro** | Visor de registros de la aplicación con exportación |
| **Ajustes** | Tema (claro/oscuro/sistema), verbosidad de registros, accesibilidad (alto contraste, animaciones reducidas, texto grande), licencias de bibliotecas, versión de la app |

---

## Aplicación Android

El módulo `:androidApp` es la aplicación Android nativa. Se ejecuta **directamente en el dispositivo** y tiene acceso completo a todas las API del sistema.

### Cómo funciona

```
CrackMyDroidApp (Application)
  └─ initKoin()                          // inicia la inyección de dependencias
  └─ AndroidDeviceSnapshotCoordinator    // recopila datos del dispositivo en segundo plano
       └─ guarda snapshot JSON en /sdcard/Android/data/.../snapshot.json

MainActivity
  └─ setContent { CrackMyDroidApp() }   // interfaz compartida del módulo :shared
```

1. Al inicio, `CrackMyDroidApp` (Application) inicializa **Koin** y lanza `AndroidDeviceSnapshotCoordinator` en segundo plano.
2. El coordinator recopila de forma continua la información del dispositivo (hardware, estado root, Play Integrity, paquetes, permisos…) y la serializa en un **archivo JSON** en el almacenamiento compartido del dispositivo.
3. `MainActivity` monta la interfaz compartida (`CrackMyDroidApp` Composable del módulo `:shared`), que lee los datos directamente de los repositorios Android nativos.

### Requisitos

- Android **API 24+** (Android 7.0)
- JDK 17, Android SDK con `platform-tools`/`adb` en el PATH

### Build e Instalación

```bash
# Compilar APK de depuración
./gradlew :androidApp:assembleDebug

# Instalar en el dispositivo conectado
adb install -r androidApp/build/outputs/apk/debug/androidApp-debug.apk

# O instalar directamente
./gradlew :androidApp:installDebug
```

---

## Aplicación Desktop (Compose Multiplatform)

El módulo `:desktopApp` es una aplicación **JVM/Desktop** construida con Compose Multiplatform. No accede directamente a las API de Android: utiliza **ADB como puente** para conectarse a un dispositivo Android físico o emulado y leer sus datos.

### Cómo funciona

```
Aplicación Desktop
  └─ DevicePickerScreen                 // lista los dispositivos ADB disponibles
       └─ adb devices
  └─ DeviceSessionControllerDesktop     // gestiona la sesión ADB
       └─ RemoteDeviceSnapshotImporter  // adb pull del JSON de snapshot
            └─ snapshot.json → estructuras de datos Kotlin
  └─ DesktopToolbar                     // info dispositivo, rescan, cambio de dispositivo
  └─ CrackMyDroidApp()                  // misma interfaz que el módulo :shared
```

1. Al inicio se muestra la **pantalla de selección de dispositivo**.
2. Si `adb` no está en el PATH del sistema, se puede configurar manualmente la ruta del binario (guardada en DataStore, no se vuelve a solicitar en ejecuciones posteriores).
3. Al hacer clic en **«Detectar dispositivo»** se ejecuta `adb devices`; los dispositivos detectados aparecen en la lista.
4. Al seleccionar un dispositivo, la app realiza un `adb pull` del archivo JSON de snapshot generado por la app Android en el dispositivo.
5. El JSON se deserializa y se inyecta en los mismos repositorios compartidos: la **interfaz `CrackMyDroidApp()`** se inicia de forma idéntica a la app Android, pero muestra los datos del dispositivo remoto.
6. La **barra de herramientas superior** muestra: modelo + número de serie + estado ADB, fuente de datos (live/snapshot + marca de tiempo), botones para rescan, cambio de dispositivo y ajustes de ADB.

### Requisitos

- JDK 17+
- macOS, Linux o Windows
- `adb` instalado (Android SDK platform-tools) y en el PATH **o** ruta configurable en la app

### Build y Ejecución

```bash
# Ejecutar la app desktop directamente
./gradlew :desktopApp:run

# Crear un paquete distribuible (macOS .dmg, Linux .deb, Windows .msi)
./gradlew :desktopApp:packageDistributionForCurrentOS
```

---

## Usar Android + Desktop juntos

La app Android y la app desktop se **complementan**: la primera recopila los datos en el dispositivo, la segunda los muestra en un ordenador conectado vía ADB.

```
[PC/Mac]                          [Dispositivo Android]
  desktopApp                          androidApp
      │                                   │
      │  adb pull snapshot.json ◄─────────┤ AndroidDeviceSnapshotCoordinator
      │                                   │ (genera el JSON en segundo plano)
      ▼                                   │
  Interfaz con todos los datos            │
  del dispositivo remoto                  │
```

### Flujo paso a paso

1. **Instalar la app Android** en el dispositivo (ver arriba).
2. **Lanzar la app Android** en el dispositivo y dejarla correr unos segundos — el coordinator genera el JSON de snapshot.
3. **Conectar el dispositivo al PC** vía USB con la **depuración USB activada** (o usar un emulador ya activo).
4. **Verificar que ADB detecta el dispositivo:**
   ```bash
   adb devices
   # esperado: <serial>   device
   ```
   Si el estado es `unauthorized`, autorizar el ordenador en el dispositivo.
5. **Lanzar la app desktop:**
   ```bash
   ./gradlew :desktopApp:run
   ```
6. En la pantalla picker, hacer clic en **«Detectar dispositivo»** → seleccionar el dispositivo.
7. La app importa el snapshot y muestra la interfaz completa con los datos del dispositivo.

> **Nota:** si la app Android no está instalada o el snapshot aún no se ha generado, la app desktop indica *«Snapshot no disponible»* y muestra de todos modos la interfaz con la información básica obtenible directamente vía ADB (lista de paquetes, comandos shell, etc.).

---

## Estructura del proyecto

```
newCrackMyDroid/
├── androidApp/          # App Android nativa (Activity, Application, BroadcastReceiver)
├── desktopApp/          # App JVM Desktop (punto de entrada, device picker, toolbar)
├── shared/              # Módulo KMP compartido
│   ├── commonMain/      # Compose UI, ViewModel, UseCases, modelos, DI
│   ├── androidMain/     # Repositorios Android nativos (RootBeer, Play Integrity, DataStore…)
│   ├── desktopMain/     # Repositorios desktop vía ADB, driver SQLite, rutas
│   ├── iosMain/         # Stubs iOS (funcionalidades Android no disponibles)
│   ├── jvmCommonTest/   # Tests JVM compartidos (MockK, Android + Desktop)
│   └── desktopTest/     # Tests específicos del layer desktop/ADB
├── iosApp/              # Aplicación iOS (wrapper SwiftUI)
└── gradle/
    └── libs.versions.toml  # Catálogo de versiones centralizado
```

### Navegación UI (shared)

- **Barra de pestañas inferior:** Inicio · Info dispositivo · Ajustes
- **Menú lateral:** Apps instaladas · PenTest · Permisos · Actividades · Root · Trucos · Registro

---

## Stack tecnológico

| Capa | Bibliotecas |
|---|---|
| UI | Compose Multiplatform, Material3 |
| DI | Koin (core, Android, Compose) |
| Async | Kotlin Coroutines + Flow |
| Base de datos local | SQLDelight (driver Android / driver SQLite JVM) |
| Seguridad Android | RootBeer, Play Integrity API |
| Preferencias | DataStore Preferences |
| Registro | Kermit |
| Tests | kotlin-test, MockK, kotlinx-coroutines-test |

---

## Notas iOS

El módulo iOS está incluido para compartir interfaz y lógica. Las funcionalidades que dependen de API Android (Root, ADB, Play Integrity, exportar APK) no están disponibles en iOS y son reemplazadas por stubs.
