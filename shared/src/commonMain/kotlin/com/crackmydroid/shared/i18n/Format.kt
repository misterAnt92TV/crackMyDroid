package com.crackmydroid.shared.i18n

fun String.format(vararg args: Any?): String = buildString {
    var argumentIndex = 0
    var index = 0

    while (index < this@format.length) {
        val character = this@format[index]
        if (character != '%') {
            append(character)
            index++
            continue
        }

        require(index + 1 < this@format.length) { "Unterminated format placeholder in \"$this@format\"." }
        when (val placeholder = this@format[index + 1]) {
            '%' -> append('%')

            's' -> {
                require(argumentIndex < args.size) { "Missing argument for %s in \"$this@format\"." }
                append(args[argumentIndex++]?.toString() ?: "null")
            }

            'd' -> {
                require(argumentIndex < args.size) { "Missing argument for %d in \"$this@format\"." }
                when (val argument = args[argumentIndex++]) {
                    is Byte, is Short, is Int, is Long -> append(argument)

                    else -> throw IllegalArgumentException(
                        "Expected an integer argument for %d in \"$this@format\", got $argument.",
                    )
                }
            }

            else -> throw IllegalArgumentException(
                "Unsupported format placeholder %$placeholder in \"$this@format\".",
            )
        }
        index += 2
    }
}
