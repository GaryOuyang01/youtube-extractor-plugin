package org.aikarasong.plugin.extractor

import java.util.UUID

/** Remembers where each search left off; the main app only ever sees the token. Keeps the last [capacity]. */
class CursorStore<T>(private val capacity: Int = 50) {
    private val map =
        object : LinkedHashMap<String, T>(capacity, LOAD_FACTOR, true) {
            override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, T>?) = size > capacity
        }

    private companion object {
        const val LOAD_FACTOR = 0.75f
    }

    @Synchronized
    fun put(value: T): String = UUID.randomUUID().toString().also { map[it] = value }

    @Synchronized
    fun get(token: String): T? = map[token]
}
