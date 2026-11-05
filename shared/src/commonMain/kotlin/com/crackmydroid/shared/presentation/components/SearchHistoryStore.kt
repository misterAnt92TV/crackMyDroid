package com.crackmydroid.shared.presentation.components

private const val MAX_HISTORY = 8
private const val MAX_VISIBLE_SUGGESTIONS = 6

object SearchHistoryStore {
    private val histories = mutableMapOf<String, ArrayDeque<String>>()
    private var enabled: Boolean = true
    private val multiSpaceRegex = Regex("\\s+")

    fun setEnabled(value: Boolean) {
        enabled = value
    }

    fun add(key: String, query: String) {
        if (!enabled) return
        val normalized = normalizeQuery(query)
        if (normalized.isEmpty()) return
        val deque = histories.getOrPut(key) { ArrayDeque() }
        val existing = deque.firstOrNull { it.equals(normalized, ignoreCase = true) }
        if (existing != null) deque.remove(existing)
        deque.addFirst(normalized)
        while (deque.size > MAX_HISTORY) deque.removeLast()
    }

    fun remove(key: String, query: String) {
        val normalized = normalizeQuery(query)
        histories[key]?.removeAll { it.equals(normalized, ignoreCase = true) }
    }

    fun clear(key: String) {
        histories[key]?.clear()
    }

    fun suggestions(key: String, prefix: String = ""): List<String> {
        if (!enabled) return emptyList()
        val deque = histories[key] ?: return emptyList()
        val normalizedPrefix = normalizeQuery(prefix)
        if (normalizedPrefix.isBlank()) return deque.take(MAX_VISIBLE_SUGGESTIONS)
        return deque.asSequence()
            .filter { it.contains(normalizedPrefix, ignoreCase = true) }
            .filterNot { it.equals(normalizedPrefix, ignoreCase = true) }
            .sortedBy { if (it.startsWith(normalizedPrefix, ignoreCase = true)) 0 else 1 }
            .take(MAX_VISIBLE_SUGGESTIONS)
            .toList()
    }

    fun clearAll() {
        histories.clear()
    }

    private fun normalizeQuery(query: String): String = query
        .trim()
        .replace(multiSpaceRegex, " ")
}
