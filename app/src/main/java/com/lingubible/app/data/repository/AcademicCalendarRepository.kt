package com.lingubible.app.data.repository

import android.content.Context
import com.lingubible.app.domain.model.AcademicEvent
import com.lingubible.app.domain.model.CalendarCategory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.InputStreamReader

class AcademicCalendarRepository(
    private val context: Context
) {
    private val json = Json { ignoreUnknownKeys = true }
    private var cachedEvents: List<AcademicEvent>? = null

    suspend fun getAcademicEvents(): List<AcademicEvent> = withContext(Dispatchers.IO) {
        cachedEvents?.let { return@withContext it }

        try {
            val inputStream = context.assets.open("data/academic_calendar.json")
            val content = InputStreamReader(inputStream).use { it.readText() }
            val events = json.decodeFromString<List<AcademicEvent>>(content)
            cachedEvents = events
            events
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun getEventsByCategory(category: CalendarCategory?): List<AcademicEvent> {
        val all = getAcademicEvents()
        if (category == null) return all
        return all.filter { it.category == category || it.category2 == category }
    }
}
