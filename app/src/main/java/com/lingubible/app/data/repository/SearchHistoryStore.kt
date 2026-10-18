package com.lingubible.app.data.repository

import android.content.Context
import com.lingubible.app.domain.model.addRecentQuery
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class SearchHistoryStore(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences("lingubible_recent_searches", Context.MODE_PRIVATE)
    fun read(kind: String): List<String> = runCatching { Json.decodeFromString<List<String>>(prefs.getString(kind, "[]").orEmpty()) }.getOrDefault(emptyList())
    fun add(kind: String, query: String) = write(kind, addRecentQuery(read(kind), query))
    fun remove(kind: String, query: String) = write(kind, read(kind).filterNot { it == query })
    fun clear(kind: String) = write(kind, emptyList())
    private fun write(kind: String, entries: List<String>) { prefs.edit().putString(kind, Json.encodeToString(entries)).apply() }
}
