package com.crackmydroid.shared.data.shell

import android.os.Build
import com.crackmydroid.shared.AppLogger
import com.crackmydroid.shared.domain.model.ShellCommand
import com.crackmydroid.shared.domain.model.ShellCommandResult
import com.crackmydroid.shared.domain.repository.ShellCommandRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.util.Locale

class ShellCommandRepositoryAndroid : ShellCommandRepository {

    private val baseCommands = listOf(
        ShellCommand("Riavvia", "reboot", requiresRoot = true, confirmation = "Riavviare il dispositivo?"),
        ShellCommand("Force stop SystemUI", "am force-stop com.android.systemui", requiresRoot = true, confirmation = "Forzare l'arresto di SystemUI?"),
        ShellCommand("Toggle animazioni 0", "settings put global window_animation_scale 0; settings put global transition_animation_scale 0; settings put global animator_duration_scale 0", true),
        ShellCommand("Toggle animazioni 1", "settings put global window_animation_scale 1; settings put global transition_animation_scale 1; settings put global animator_duration_scale 1", true),
        ShellCommand("Attiva debug USB", "settings put global adb_enabled 1", true),
        ShellCommand("Disattiva debug USB", "settings put global adb_enabled 0", true),
        ShellCommand("Mostra top process", "top -n 1", false),
        ShellCommand("Elenco pacchetti", "pm list packages", false),
        ShellCommand("Riavvia SystemUI", "pkill -TERM com.android.systemui", true, "Riavviare SystemUI?"),
        ShellCommand("Forza luminosità 50%", "settings put system screen_brightness 128", true),
        ShellCommand("Audio muto", "service call audio 7 i32 3 i32 0", true),
        ShellCommand(
            "Installa toolbox (toybox)",
            "mkdir -p /data/local/tmp/toolbox && cp /apex/com.android.runtime/bin/toybox /data/local/tmp/toolbox/ && cd /data/local/tmp/toolbox && chmod 755 toybox && ./toybox --install .",
            requiresRoot = true,
            confirmation = "Creare toolbox in /data/local/tmp e installare symlink?"
        ),
        ShellCommand(
            "Rimuovi toolbox",
            "rm -rf /data/local/tmp/toolbox",
            requiresRoot = true,
            confirmation = "Rimuovere toolbox installata in /data/local/tmp?"
        )
    )

    private val brandCommands: Map<DeviceBrand, List<ShellCommand>> = mapOf(
        DeviceBrand.OnePlus to listOf(
            packageProbeCommand(
                title = "OnePlus: pacchetti OEM",
                regex = "oneplus|oplus|oxygen",
                emptyMessage = "Nessun pacchetto OnePlus trovato"
            ),
            propertyProbeCommand(
                title = "OnePlus: proprietà sistema",
                regex = "oneplus|oplus|oxygen|ro\\.rom\\.version",
                emptyMessage = "Nessuna proprietà OnePlus trovata"
            )
        ),
        DeviceBrand.Samsung to listOf(
            packageProbeCommand(
                title = "Samsung: pacchetti OEM",
                regex = "samsung|knox|sec",
                emptyMessage = "Nessun pacchetto Samsung trovato"
            ),
            propertyProbeCommand(
                title = "Samsung: proprietà CSC/Knox",
                regex = "samsung|knox|ro\\.csc|ril\\.sales_code|omc",
                emptyMessage = "Nessuna proprietà Samsung trovata"
            )
        ),
        DeviceBrand.Huawei to listOf(
            packageProbeCommand(
                title = "Huawei: pacchetti OEM",
                regex = "huawei|honor|hms",
                emptyMessage = "Nessun pacchetto Huawei trovato"
            ),
            propertyProbeCommand(
                title = "Huawei: proprietà EMUI",
                regex = "huawei|emui|ro\\.config\\.hw|ro\\.build\\.version\\.emui",
                emptyMessage = "Nessuna proprietà Huawei trovata"
            )
        ),
        DeviceBrand.Honor to listOf(
            packageProbeCommand(
                title = "Honor: pacchetti OEM",
                regex = "honor|hms|magicui",
                emptyMessage = "Nessun pacchetto Honor trovato"
            ),
            propertyProbeCommand(
                title = "Honor: proprietà MagicUI",
                regex = "honor|magicui|ro\\.build\\.version\\.magic|ro\\.product\\.brand",
                emptyMessage = "Nessuna proprietà Honor trovata"
            )
        ),
        DeviceBrand.Oppo to listOf(
            packageProbeCommand(
                title = "Oppo: pacchetti OEM",
                regex = "oppo|oplus|coloros|heytap",
                emptyMessage = "Nessun pacchetto Oppo trovato"
            ),
            propertyProbeCommand(
                title = "Oppo: proprietà ColorOS",
                regex = "oppo|oplus|coloros|ro\\.build\\.version\\.opporom",
                emptyMessage = "Nessuna proprietà Oppo trovata"
            )
        ),
        DeviceBrand.Realme to listOf(
            packageProbeCommand(
                title = "Realme: pacchetti OEM",
                regex = "realme|heytap|oplus|coloros",
                emptyMessage = "Nessun pacchetto Realme trovato"
            ),
            propertyProbeCommand(
                title = "Realme: proprietà RealmeUI",
                regex = "realme|ro\\.build\\.realmeui|ro\\.build\\.version\\.opporom|ro\\.product\\.brand",
                emptyMessage = "Nessuna proprietà Realme trovata"
            )
        ),
        DeviceBrand.Vivo to listOf(
            packageProbeCommand(
                title = "Vivo: pacchetti OEM",
                regex = "vivo|iqoo|funtouch|originos",
                emptyMessage = "Nessun pacchetto Vivo trovato"
            ),
            propertyProbeCommand(
                title = "Vivo: proprietà Funtouch/OriginOS",
                regex = "vivo|iqoo|funtouch|originos|ro\\.vivo|ro\\.product\\.brand",
                emptyMessage = "Nessuna proprietà Vivo trovata"
            )
        ),
        DeviceBrand.Motorola to listOf(
            packageProbeCommand(
                title = "Motorola: pacchetti OEM",
                regex = "motorola|moto|myui",
                emptyMessage = "Nessun pacchetto Motorola trovato"
            ),
            propertyProbeCommand(
                title = "Motorola: proprietà sistema",
                regex = "motorola|moto|myui|ro\\.mot",
                emptyMessage = "Nessuna proprietà Motorola trovata"
            )
        ),
        DeviceBrand.Xiaomi to listOf(
            packageProbeCommand(
                title = "Xiaomi: pacchetti OEM",
                regex = "xiaomi|miui|hyperos|poco",
                emptyMessage = "Nessun pacchetto Xiaomi trovato"
            ),
            propertyProbeCommand(
                title = "Xiaomi: proprietà MIUI/HyperOS",
                regex = "xiaomi|miui|hyperos|ro\\.miui|ro\\.build\\.version\\.incremental",
                emptyMessage = "Nessuna proprietà Xiaomi trovata"
            )
        ),
        DeviceBrand.Redmi to listOf(
            packageProbeCommand(
                title = "Redmi: pacchetti OEM",
                regex = "redmi|xiaomi|miui",
                emptyMessage = "Nessun pacchetto Redmi trovato"
            ),
            propertyProbeCommand(
                title = "Redmi: proprietà dispositivo",
                regex = "redmi|miui|ro\\.product\\.marketname|ro\\.product\\.brand",
                emptyMessage = "Nessuna proprietà Redmi trovata"
            )
        ),
        DeviceBrand.Asus to listOf(
            packageProbeCommand(
                title = "ASUS: pacchetti OEM",
                regex = "asus|zenui|rog",
                emptyMessage = "Nessun pacchetto ASUS trovato"
            ),
            propertyProbeCommand(
                title = "ASUS: proprietà ZenUI/ROG",
                regex = "asus|zenui|rog|ro\\.asus|ro\\.product\\.brand",
                emptyMessage = "Nessuna proprietà ASUS trovata"
            )
        ),
        DeviceBrand.Sony to listOf(
            packageProbeCommand(
                title = "Sony: pacchetti OEM",
                regex = "sony|xperia|semc",
                emptyMessage = "Nessun pacchetto Sony trovato"
            ),
            propertyProbeCommand(
                title = "Sony: proprietà Xperia",
                regex = "sony|xperia|ro\\.sony|ro\\.product\\.brand",
                emptyMessage = "Nessuna proprietà Sony trovata"
            )
        ),
        DeviceBrand.Nokia to listOf(
            packageProbeCommand(
                title = "Nokia/HMD: pacchetti OEM",
                regex = "nokia|hmd",
                emptyMessage = "Nessun pacchetto Nokia/HMD trovato"
            ),
            propertyProbeCommand(
                title = "Nokia/HMD: proprietà sistema",
                regex = "nokia|hmd|ro\\.product\\.brand|ro\\.vendor\\.build\\.fingerprint",
                emptyMessage = "Nessuna proprietà Nokia/HMD trovata"
            )
        ),
        DeviceBrand.Nothing to listOf(
            packageProbeCommand(
                title = "Nothing/CMF: pacchetti OEM",
                regex = "nothing|cmf",
                emptyMessage = "Nessun pacchetto Nothing/CMF trovato"
            ),
            propertyProbeCommand(
                title = "Nothing/CMF: proprietà sistema",
                regex = "nothing|cmf|ro\\.product\\.brand|ro\\.build\\.fingerprint",
                emptyMessage = "Nessuna proprietà Nothing/CMF trovata"
            )
        ),
        DeviceBrand.Zte to listOf(
            packageProbeCommand(
                title = "ZTE/Nubia: pacchetti OEM",
                regex = "zte|nubia|redmagic",
                emptyMessage = "Nessun pacchetto ZTE/Nubia trovato"
            ),
            propertyProbeCommand(
                title = "ZTE/Nubia: proprietà sistema",
                regex = "zte|nubia|redmagic|ro\\.product\\.brand",
                emptyMessage = "Nessuna proprietà ZTE/Nubia trovata"
            )
        ),
        DeviceBrand.Lenovo to listOf(
            packageProbeCommand(
                title = "Lenovo: pacchetti OEM",
                regex = "lenovo|zuk",
                emptyMessage = "Nessun pacchetto Lenovo trovato"
            ),
            propertyProbeCommand(
                title = "Lenovo: proprietà sistema",
                regex = "lenovo|zuk|ro\\.lenovo|ro\\.product\\.brand",
                emptyMessage = "Nessuna proprietà Lenovo trovata"
            )
        ),
        DeviceBrand.Google to listOf(
            packageProbeCommand(
                title = "Google: pacchetti OEM",
                regex = "google|pixel|gms",
                emptyMessage = "Nessun pacchetto Google trovato"
            ),
            propertyProbeCommand(
                title = "Google: proprietà Pixel",
                regex = "google|pixel|ro\\.build\\.fingerprint|ro\\.boot\\.hardware\\.sku",
                emptyMessage = "Nessuna proprietà Google trovata"
            )
        )
    )

    override suspend fun listCommands(): List<ShellCommand> {
        val matchedBrands = resolveMatchedBrands()
        val oemCommands = matchedBrands.flatMap { brand -> brandCommands[brand].orEmpty() }
        return baseCommands + oemCommands
    }

    override suspend fun execute(command: ShellCommand): ShellCommandResult = withContext(Dispatchers.IO) {
        runCommand(command.command, command.requiresRoot)
    }

    private fun runCommand(cmd: String, root: Boolean): ShellCommandResult {
        return try {
            val prefix = if (root) arrayOf("su", "-c", cmd) else arrayOf("sh", "-c", cmd)
            val process = Runtime.getRuntime().exec(prefix)
            val stdout = process.inputStream.bufferedReader().use(BufferedReader::readText)
            val stderr = process.errorStream.bufferedReader().use(BufferedReader::readText)
            val code = process.waitFor()
            AppLogger.logger.i { "cmd=$cmd code=$code" }
            ShellCommandResult(code == 0, stdout = stdout, stderr = stderr, code = code)
        } catch (t: Throwable) {
            AppLogger.logger.e(t) { "shell failed for $cmd" }
            ShellCommandResult(false, stdout = "", stderr = t.message ?: "errore", code = -1)
        }
    }

    private fun resolveMatchedBrands(): List<DeviceBrand> {
        val signature = listOfNotNull(
            Build.BRAND,
            Build.MANUFACTURER,
            Build.MODEL,
            Build.PRODUCT,
            Build.DEVICE
        ).joinToString(" ")
            .lowercase(Locale.ROOT)

        return DeviceBrand.values().filter { brand ->
            brand.aliases.any { alias -> signature.contains(alias) }
        }
    }

    private fun packageProbeCommand(
        title: String,
        regex: String,
        emptyMessage: String
    ): ShellCommand = ShellCommand(
        title = title,
        command = "pm list packages | grep -Ei \"$regex\" || echo \"$emptyMessage\"",
        requiresRoot = false
    )

    private fun propertyProbeCommand(
        title: String,
        regex: String,
        emptyMessage: String
    ): ShellCommand = ShellCommand(
        title = title,
        command = "getprop | grep -Ei \"$regex\" || echo \"$emptyMessage\"",
        requiresRoot = false
    )

    private enum class DeviceBrand(val aliases: List<String>) {
        OnePlus(listOf("oneplus")),
        Samsung(listOf("samsung")),
        Huawei(listOf("huawei")),
        Honor(listOf("honor")),
        Oppo(listOf("oppo", "oplus")),
        Realme(listOf("realme")),
        Vivo(listOf("vivo", "iqoo")),
        Motorola(listOf("motorola", "moto")),
        Xiaomi(listOf("xiaomi", "miui", "poco")),
        Redmi(listOf("redmi")),
        Asus(listOf("asus", "rog")),
        Sony(listOf("sony", "xperia")),
        Nokia(listOf("nokia", "hmd")),
        Nothing(listOf("nothing", "cmf")),
        Zte(listOf("zte", "nubia", "redmagic")),
        Lenovo(listOf("lenovo", "zuk")),
        Google(listOf("google", "pixel"))
    }
}
