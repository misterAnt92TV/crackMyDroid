package com.crackmydroid.shared.data.cache

object PaginationHelper {
    fun <T> paginate(items: List<T>, offset: Int, limit: Int): List<T> {
        val startIndex = offset.coerceAtLeast(0)
        val endIndex = (startIndex + limit.coerceAtLeast(0)).coerceAtMost(items.size)
        return if (startIndex >= items.size) emptyList() else items.subList(startIndex, endIndex)
    }

    fun countPages(totalItems: Int, pageSize: Int): Int =
        if (pageSize <= 0) 0 else (totalItems.coerceAtLeast(0) + pageSize - 1) / pageSize
}
