package com.crackmydroid.shared.data.adb

import com.crackmydroid.shared.domain.model.ActivityEntry
import com.crackmydroid.shared.domain.model.ConnectedDevice
import com.crackmydroid.shared.domain.model.InstalledAppEntry

internal object AdbParsers {
    private val deviceKeyValue = Regex("""([A-Za-z_]+):([^\s]+)""")
    private val propLine = Regex("""\[(.+?)\]: \[(.*)]""")
    private val componentPattern = Regex("""([A-Za-z0-9_.]+)/(?:([A-Za-z0-9_$.]+))""")
    private val permissionPattern = Regex("""[A-Za-z0-9_.]+(?:permission|PERMISSION)[A-Za-z0-9_.]*""")

    fun parseAdbDevices(output: String): List<ConnectedDevice> {
        return output.lineSequence()
            .dropWhile { !it.startsWith("List of devices attached") }
            .drop(1)
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .mapNotNull { line ->
                val parts = line.split(Regex("\\s+"))
                if (parts.size < 2) return@mapNotNull null
                val extras = deviceKeyValue.findAll(line).associate { it.groupValues[1] to it.groupValues[2] }
                ConnectedDevice(
                    serial = parts[0],
                    state = parts[1],
                    model = extras["model"],
                    product = extras["product"],
                    device = extras["device"],
                    transportId = extras["transport_id"]
                )
            }
            .toList()
    }

    fun parseGetProp(output: String): Map<String, String> =
        output.lineSequence()
            .mapNotNull { line ->
                propLine.matchEntire(line.trim())?.let { match ->
                    match.groupValues[1] to match.groupValues[2]
                }
            }
            .toMap()

    fun parseInstalledApps(output: String): List<InstalledAppEntry> {
        return output.lineSequence()
            .mapNotNull { line ->
                val trimmed = line.trim()
                if (!trimmed.startsWith("package:")) return@mapNotNull null
                val body = trimmed.removePrefix("package:")
                val separatorIndex = body.lastIndexOf('=')
                if (separatorIndex <= 0 || separatorIndex == body.lastIndex) return@mapNotNull null
                val sourcePath = body.substring(0, separatorIndex)
                val packageName = body.substring(separatorIndex + 1)
                InstalledAppEntry(
                    appLabel = labelFromPackage(packageName),
                    packageName = packageName,
                    sourcePath = sourcePath
                )
            }
            .sortedBy { it.appLabel.lowercase() }
            .toList()
    }

    fun parsePmPath(output: String): List<String> =
        output.lineSequence()
            .map { it.trim() }
            .filter { it.startsWith("package:") }
            .map { it.removePrefix("package:") }
            .map { it.substringBefore("=") }
            .filter { it.isNotBlank() }
            .toList()

    fun parseBatteryLevel(output: String): Int? =
        output.lineSequence()
            .map { it.trim() }
            .firstOrNull { it.startsWith("level:") }
            ?.substringAfter("level:")
            ?.trim()
            ?.toIntOrNull()

    fun parseWmSize(output: String): String? =
        output.lineSequence()
            .map { it.trim() }
            .firstOrNull { it.startsWith("Physical size:") || it.startsWith("Override size:") }
            ?.substringAfter(":")
            ?.trim()

    fun parseDensity(output: String): Int? =
        output.lineSequence()
            .map { it.trim() }
            .firstOrNull { it.startsWith("Physical density:") || it.startsWith("Override density:") }
            ?.substringAfter(":")
            ?.trim()
            ?.substringBefore("dpi")
            ?.trim()
            ?.toIntOrNull()

    fun parseMemInfo(output: String): Pair<Long?, Long?> {
        val values = output.lineSequence()
            .map { it.trim() }
            .mapNotNull { line ->
                val key = line.substringBefore(":").trim()
                val amount = line.substringAfter(":", "").trim().substringBefore(" ").toLongOrNull()
                if (amount != null) key to amount * 1024 else null
            }
            .toMap()
        return values["MemTotal"] to values["MemAvailable"]
    }

    fun parseRequestedPermissions(output: String): List<String> {
        val section = extractSection(output, "requested permissions:")
        if (section.isBlank()) return emptyList()
        return permissionPattern.findAll(section)
            .map { it.value.trim() }
            .distinct()
            .sorted()
            .toList()
    }

    fun parseActivities(packageName: String, appLabel: String, output: String): List<ActivityEntry> {
        val resolverSpecs = parseResolverActivitySpecs(
            extractSections(
                output,
                headers = setOf("Activity Resolver Table:", "Activity Intent Resolver Table:")
            ),
            packageName = packageName
        )
        val resolverActivities = parseActivityComponents(
            extractSections(
                output,
                headers = setOf("Activity Resolver Table:", "Activity Intent Resolver Table:")
            ),
            packageName = packageName
        )
        val declaredActivities = parseActivityComponents(
            extractSections(
                output,
                headers = setOf("Activities:")
            ),
            packageName = packageName
        )

        val allActivities = linkedSetOf<String>().apply {
            addAll(resolverActivities)
            addAll(declaredActivities)
        }

        return allActivities
            .map { activityName ->
                val resolverSpec = resolverSpecs[activityName]
                val visibleInResolver = resolverActivities.contains(activityName)
                val requiresIntentContext = resolverSpec?.requiresIntentContext() == true
                val launchable = visibleInResolver && !requiresIntentContext
                ActivityEntry(
                    label = activityName.substringAfterLast('$').substringAfterLast('.'),
                    appLabel = appLabel,
                    packageName = packageName,
                    activityName = activityName,
                    launchableViaShell = launchable,
                    launchabilityReason = when {
                        launchable -> null
                        requiresIntentContext -> {
                            val hint = resolverSpec?.toHint()
                            if (hint.isNullOrBlank()) {
                                "Activity richiede un intent contestualizzato; usa il launcher guidato"
                            } else {
                                "Activity richiede un intent contestualizzato ($hint); usa il launcher guidato"
                            }
                        }
                        else -> {
                        "Activity rilevata nel dump ma non esposta nel resolver table del package"
                        }
                    },
                    launchContextHint = resolverSpec?.toHint(),
                    launchIntentAction = resolverSpec?.defaultAction(),
                    launchIntentData = resolverSpec?.defaultDataUri(),
                    launchIntentMimeType = resolverSpec?.defaultMimeType()
                )
            }
    }

    fun labelFromPackage(packageName: String): String =
        packageName.substringAfterLast('.')
            .replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }

    private fun extractSection(output: String, header: String): String {
        val lines = output.lines()
        val startIndex = lines.indexOfFirst { it.trim() == header }
        if (startIndex == -1) return ""
        val startIndent = lines[startIndex].indexOfFirst { !it.isWhitespace() }.coerceAtLeast(0)
        val collected = mutableListOf<String>()
        for (index in (startIndex + 1) until lines.size) {
            val line = lines[index]
            val trimmed = line.trim()
            if (trimmed.isBlank()) continue
            val indent = line.indexOfFirst { !it.isWhitespace() }.coerceAtLeast(0)
            if (indent <= startIndent && trimmed.endsWith(":")) break
            collected += line
        }
        return collected.joinToString("\n")
    }

    private fun extractSections(output: String, headers: Set<String>): String {
        val lines = output.lines()
        val builder = StringBuilder()
        var collecting = false
        var sectionIndent = -1

        for (line in lines) {
            val trimmed = line.trim()
            val indent = line.indexOfFirst { !it.isWhitespace() }.coerceAtLeast(0)

            if (trimmed in headers) {
                collecting = true
                sectionIndent = indent
                builder.appendLine(line)
                continue
            }

            if (collecting && trimmed.endsWith(":") && indent <= sectionIndent) {
                collecting = false
                sectionIndent = -1
            }

            if (collecting) {
                builder.appendLine(line)
            }
        }

        return builder.toString()
    }

    private fun parseActivityComponents(output: String, packageName: String): LinkedHashSet<String> {
        val matches = linkedSetOf<String>()
        componentPattern.findAll(output).forEach { match ->
            if (match.groupValues[1] != packageName) return@forEach
            val raw = match.groupValues[2]
            val activityName = if (raw.startsWith(".")) "$packageName$raw" else raw
            matches += activityName
        }
        return matches
    }

    private fun parseResolverActivitySpecs(output: String, packageName: String): Map<String, ResolverActivitySpec> {
        val specs = linkedMapOf<String, ResolverActivitySpec>()
        var currentComponent: String? = null

        output.lineSequence().forEach { line ->
            val trimmed = line.trim()
            val componentMatch = componentPattern.find(trimmed)
            if (componentMatch != null && componentMatch.groupValues[1] == packageName) {
                val raw = componentMatch.groupValues[2]
                val normalized = if (raw.startsWith(".")) "$packageName$raw" else raw
                currentComponent = normalized
                specs.putIfAbsent(normalized, ResolverActivitySpec())
                return@forEach
            }

            if (trimmed.endsWith(":") &&
                !trimmed.startsWith("Action:") &&
                !trimmed.startsWith("Scheme:") &&
                !trimmed.startsWith("Authority:") &&
                !trimmed.startsWith("StaticType:") &&
                !trimmed.startsWith("Path:")
            ) {
                currentComponent = null
            }

            val component = currentComponent ?: return@forEach
            val spec = specs.getOrPut(component) { ResolverActivitySpec() }
            when {
                trimmed.startsWith("Action:") -> spec.actions += trimmed.substringAfter('"').substringBeforeLast('"')
                trimmed.startsWith("Scheme:") -> spec.schemes += trimmed.substringAfter('"').substringBeforeLast('"')
                trimmed.startsWith("Authority:") -> spec.authorities += trimmed.substringAfter('"').substringBefore('"')
                trimmed.startsWith("StaticType:") -> spec.staticTypes += trimmed.substringAfter('"').substringBeforeLast('"')
                trimmed.startsWith("Path:") -> {
                    spec.hasPath = true
                    extractPathHint(trimmed)?.let { spec.paths += it }
                }
            }
        }

        return specs
    }

    private fun extractPathHint(line: String): String? {
        val quoted = line.substringAfter('"', "").substringBeforeLast('"').trim()
        if (quoted.isNotBlank()) {
            if (quoted.startsWith("/")) return quoted
            val matcherPath = quoted.substringAfter(": ", "")
            if (matcherPath.startsWith("/")) return matcherPath
        }
        val fallback = line.substringAfter("Path:", "").trim()
        return fallback.takeIf { it.startsWith("/") }
    }

    private data class ResolverActivitySpec(
        val actions: LinkedHashSet<String> = linkedSetOf(),
        val schemes: LinkedHashSet<String> = linkedSetOf(),
        val authorities: LinkedHashSet<String> = linkedSetOf(),
        val staticTypes: LinkedHashSet<String> = linkedSetOf(),
        val paths: LinkedHashSet<String> = linkedSetOf(),
        var hasPath: Boolean = false
    ) {
        fun requiresIntentContext(): Boolean =
            schemes.isNotEmpty() ||
                authorities.isNotEmpty() ||
                staticTypes.isNotEmpty() ||
                hasPath

        fun defaultAction(): String? = actions.firstOrNull()

        fun defaultMimeType(): String? = staticTypes.firstOrNull()

        fun defaultDataUri(): String? {
            val scheme = schemes.firstOrNull()
            val authority = authorities.firstOrNull()
            val path = paths.firstOrNull()
            if (scheme == null && authority == null) return null

            val resolvedScheme = scheme ?: "content"
            return buildString {
                append(resolvedScheme)
                append("://")
                authority?.let { append(it) }
                when {
                    !path.isNullOrBlank() -> append(if (path.startsWith("/")) path else "/$path")
                    hasPath -> append("/...")
                }
            }
        }

        fun toHint(): String? {
            val parts = buildList {
                actions.firstOrNull()?.let { add("action $it") }
                schemes.firstOrNull()?.let { add("scheme $it") }
                authorities.firstOrNull()?.let { add("authority $it") }
                staticTypes.firstOrNull()?.let { add("type $it") }
                val pathHint = paths.firstOrNull()
                if (pathHint != null) {
                    add("path $pathHint")
                } else if (hasPath) {
                    add("path specifico")
                }
            }
            return parts.takeIf { it.isNotEmpty() }?.joinToString(" • ")
        }
    }
}
