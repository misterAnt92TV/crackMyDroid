package com.crackmydroid.shared.data.device

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ApplicationInfo
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.BatteryManager
import android.os.Build
import android.provider.Settings
import android.telephony.TelephonyManager
import com.crackmydroid.database.CacheDatabase
import com.crackmydroid.shared.domain.model.DeviceInfo
import com.crackmydroid.shared.domain.repository.DeviceInfoRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class DeviceInfoRepositoryAndroid(
    private val context: Context,
    private val db: CacheDatabase
) : DeviceInfoRepository {
    override suspend fun getDeviceInfo(): DeviceInfo = withContext(Dispatchers.Default) {
        loadCached()?.let { return@withContext it }
        val batteryPct = readBattery()
        val (totalMem, availMem) = readMemory()
        val (width, height, density) = readDisplay()
        val (systemPropSource, systemPropDump) = readSystemPropDump()
        val info = DeviceInfo(
            manufacturer = Build.MANUFACTURER,
            model = Build.MODEL,
            brand = Build.BRAND,
            device = Build.DEVICE,
            product = Build.PRODUCT,
            androidVersion = Build.VERSION.RELEASE ?: "unknown",
            securityPatch = Build.VERSION.SECURITY_PATCH ?: "unknown",
            buildId = Build.ID,
            buildDisplay = Build.DISPLAY,
            buildFingerprint = Build.FINGERPRINT,
            radio = Build.getRadioVersion(),
            kernelVersion = System.getProperty("os.version"),
            hardware = Build.HARDWARE ?: "n/a",
            bootloader = Build.BOOTLOADER ?: "n/a",
            batteryLevel = batteryPct,
            networkType = readNetworkType(),
            totalMemory = totalMem,
            freeMemory = availMem,
            screenResolution = width?.let { w -> height?.let { h -> "${w}x$h" } },
            densityDpi = density,
            adbEnabled = isAdbEnabled(),
            developerOptions = isDeveloperOptionsEnabled(),
            appDebuggable = isAppDebuggable(),
            imei = readImei(),
            serial = readSerial(),
            systemPropSource = systemPropSource,
            systemPropDump = systemPropDump
        )

        cacheInfo(info)
        info
    }

    private fun cacheInfo(info: DeviceInfo) {
        db.cacheDatabaseQueries.transaction {
            db.cacheDatabaseQueries.clearDeviceInfo()
            fun put(key: String, value: String?) {
                value?.let { db.cacheDatabaseQueries.setInfoField(key, it) }
            }
            put("manufacturer", info.manufacturer)
            put("model", info.model)
            put("brand", info.brand)
            put("device", info.device)
            put("product", info.product)
            put("androidVersion", info.androidVersion)
            put("securityPatch", info.securityPatch)
            put("buildId", info.buildId)
            put("buildDisplay", info.buildDisplay)
            put("buildFingerprint", info.buildFingerprint)
            put("radio", info.radio)
            put("kernelVersion", info.kernelVersion)
            put("hardware", info.hardware)
            put("bootloader", info.bootloader)
            put("batteryLevel", info.batteryLevel?.toString())
            put("networkType", info.networkType)
            put("totalMemory", info.totalMemory?.toString())
            put("freeMemory", info.freeMemory?.toString())
            put("screenResolution", info.screenResolution)
            put("densityDpi", info.densityDpi?.toString())
            put("adbEnabled", info.adbEnabled.toString())
            put("developerOptions", info.developerOptions.toString())
            put("appDebuggable", info.appDebuggable.toString())
            put("imei", info.imei)
            put("serial", info.serial)
            put("systemPropSource", info.systemPropSource)
            put("systemPropDump", info.systemPropDump)
        }
    }

    private fun loadCached(): DeviceInfo? {
        val rows = db.cacheDatabaseQueries.getAllInfo().executeAsList()
        if (rows.isEmpty()) return null
        fun value(key: String) = rows.firstOrNull { it.key == key }?.value_
        return DeviceInfo(
            manufacturer = value("manufacturer") ?: return null,
            model = value("model") ?: return null,
            brand = value("brand"),
            device = value("device"),
            product = value("product"),
            androidVersion = value("androidVersion") ?: "unknown",
            securityPatch = value("securityPatch") ?: "unknown",
            buildId = value("buildId"),
            buildDisplay = value("buildDisplay"),
            buildFingerprint = value("buildFingerprint"),
            radio = value("radio"),
            kernelVersion = value("kernelVersion"),
            hardware = value("hardware") ?: "n/a",
            bootloader = value("bootloader") ?: "n/a",
            batteryLevel = value("batteryLevel")?.toIntOrNull(),
            networkType = value("networkType") ?: "unknown",
            totalMemory = value("totalMemory")?.toLongOrNull(),
            freeMemory = value("freeMemory")?.toLongOrNull(),
            screenResolution = value("screenResolution"),
            densityDpi = value("densityDpi")?.toIntOrNull(),
            adbEnabled = value("adbEnabled")?.toBoolean() ?: false,
            developerOptions = value("developerOptions")?.toBoolean() ?: false,
            appDebuggable = value("appDebuggable")?.toBoolean() ?: false,
            imei = value("imei"),
            serial = value("serial"),
            systemPropSource = value("systemPropSource"),
            systemPropDump = value("systemPropDump")
        )
    }

    private fun readBattery(): Int? {
        val ifilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val batteryStatus = context.registerReceiver(null, ifilter) ?: return null
        val level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        return if (level >= 0 && scale > 0) (level * 100 / scale.toFloat()).toInt() else null
    }

    private fun readMemory(): Pair<Long?, Long?> {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager ?: return null to null
        val memInfo = ActivityManager.MemoryInfo()
        am.getMemoryInfo(memInfo)
        return memInfo.totalMem to memInfo.availMem
    }

    private fun readNetworkType(): String {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return "unknown"
        val network = cm.activeNetwork ?: return "offline"
        val caps = cm.getNetworkCapabilities(network) ?: return "offline"
        return when {
            caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "Wi-Fi"
            caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "Cell"
            caps.hasTransport(NetworkCapabilities.TRANSPORT_VPN) -> "VPN"
            else -> "other"
        }
    }

    private fun readDisplay(): Triple<Int?, Int?, Int?> {
        val metrics = context.resources.displayMetrics
        return Triple(metrics.widthPixels, metrics.heightPixels, metrics.densityDpi)
    }

    private fun isAdbEnabled(): Boolean =
        Settings.Global.getInt(context.contentResolver, Settings.Global.ADB_ENABLED, 0) == 1

    private fun isDeveloperOptionsEnabled(): Boolean =
        Settings.Global.getInt(context.contentResolver, Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, 0) == 1

    private fun isAppDebuggable(): Boolean =
        (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0

    @Suppress("DEPRECATION")
    private fun readSerial(): String? =
        runCatching { Build.getSerial() }.getOrNull()

    @Suppress("MissingPermission")
    private fun readImei(): String? {
        val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager ?: return null
        return runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                tm.imei ?: tm.meid
            } else {
                @Suppress("DEPRECATION") tm.deviceId
            }
        }.getOrNull()
    }

    private fun readSystemPropDump(): Pair<String?, String?> {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) return null to null

        val propFiles = listOf(
            "/default.prop",
            "/system/build.prop",
            "/vendor/build.prop",
            "/product/build.prop",
            "/system_ext/build.prop",
            "/odm/etc/build.prop"
        )

        val sections = mutableListOf<String>()
        val sources = mutableListOf<String>()

        propFiles.forEach { path ->
            val content = readPropFile(path)
            if (!content.isNullOrBlank()) {
                sources += path
                sections += "[$path]\n$content"
            }
        }

        if (sections.isEmpty()) {
            val getpropDump = normalizePropText(runShellText("getprop | sort"))
            if (!getpropDump.isNullOrBlank()) {
                sources += "getprop"
                sections += "[getprop]\n$getpropDump"
            }
        }

        if (sections.isEmpty()) {
            return "unavailable" to "Nessuna proprietà di sistema leggibile (default.prop/build.prop/getprop)."
        }

        return sources.joinToString(", ") to sections.joinToString("\n\n")
    }

    private fun readPropFile(path: String): String? {
        val fromFile = runCatching {
            val file = File(path)
            if (!file.exists() || !file.canRead()) return@runCatching null
            file.readText()
        }.getOrNull()
        normalizePropText(fromFile)?.let { return it }

        return normalizePropText(runShellText("cat $path"))
    }

    private fun runShellText(command: String): String? {
        return runCatching {
            val process = Runtime.getRuntime().exec(arrayOf("sh", "-c", command))
            val stdout = process.inputStream.bufferedReader().use { it.readText() }
            process.waitFor()
            stdout
        }.getOrNull()
    }

    private fun normalizePropText(raw: String?): String? {
        return raw
            ?.lineSequence()
            ?.map { it.trimEnd() }
            ?.filter { it.isNotBlank() && !it.startsWith("#") }
            ?.joinToString("\n")
            ?.takeIf { it.isNotBlank() }
    }
}
