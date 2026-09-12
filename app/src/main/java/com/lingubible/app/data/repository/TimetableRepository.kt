package com.lingubible.app.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.lingubible.app.domain.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader

class TimetableRepository(
    private val context: Context
) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("lingubible_timetable_prefs", Context.MODE_PRIVATE)

    // In-memory cache of parsed sections per term assetPath
    private val cache = mutableMapOf<String, List<TimetableSection>>()

    /**
     * Load and parse timetable sections from an asset CSV file.
     */
    suspend fun getSectionsForTerm(term: TimetableTerm): List<TimetableSection> = withContext(Dispatchers.IO) {
        cache[term.assetPath]?.let { return@withContext it }

        val sections = mutableMapOf<String, TimetableSection>()
        val colorIndexMap = mutableMapOf<String, Int>()

        try {
            val inputStream = context.assets.open(term.assetPath)
            val reader = BufferedReader(InputStreamReader(inputStream))
            val headerLine = reader.readLine() // Header line
            val headerCols = if (headerLine != null) {
                parseCsvLine(headerLine).map { it.trim().lowercase().removePrefix("\ufeff") }
            } else emptyList()

            val crnIdx = headerCols.indexOfFirst { it == "crn" }.takeIf { it >= 0 } ?: 0
            val codeIdx = headerCols.indexOfFirst { it == "course code" }.takeIf { it >= 0 } ?: 2
            val titleIdx = headerCols.indexOfFirst { it == "course title" }.takeIf { it >= 0 } ?: 3
            val sectIdx = headerCols.indexOfFirst { it == "sect" }.takeIf { it >= 0 } ?: 4
            val langIdx = headerCols.indexOfFirst { it == "lang" }.takeIf { it >= 0 } ?: 5
            val typeIdx = headerCols.indexOfFirst { it == "type" }.takeIf { it >= 0 } ?: 8
            val dayIdx = headerCols.indexOfFirst { it == "day" }.takeIf { it >= 0 } ?: 9
            val startIdx = headerCols.indexOfFirst { it == "start" }.takeIf { it >= 0 } ?: 10
            val endIdx = headerCols.indexOfFirst { it == "end" }.takeIf { it >= 0 } ?: 11
            val venueIdx = headerCols.indexOfFirst { it == "venue" }.takeIf { it >= 0 } ?: 12
            val instIdx = headerCols.indexOfFirst { it.contains("instructor name") }.takeIf { it >= 0 } ?: 13
            val emailIdx = headerCols.indexOfFirst { it.contains("instructor email") }.takeIf { it >= 0 } ?: 14

            var line: String? = null
            while (reader.readLine().also { line = it } != null) {
                val currentLine = line ?: continue
                if (currentLine.isBlank()) continue

                val cols = parseCsvLine(currentLine)
                if (cols.size < 10) continue

                val crn = cols.getOrNull(crnIdx)?.trim().orEmpty()
                val courseCode = cols.getOrNull(codeIdx)?.trim().orEmpty()
                val courseTitle = cols.getOrNull(titleIdx)?.trim().orEmpty()
                val sect = cols.getOrNull(sectIdx)?.trim().orEmpty()
                val lang = cols.getOrNull(langIdx)?.trim().orEmpty()
                val type = cols.getOrNull(typeIdx)?.trim().orEmpty().ifBlank { "LEC" }
                val dayRaw = cols.getOrNull(dayIdx)?.trim().orEmpty()
                val start = cols.getOrNull(startIdx)?.trim().orEmpty()
                val end = cols.getOrNull(endIdx)?.trim().orEmpty()
                val venue = cols.getOrNull(venueIdx)?.trim().orEmpty()
                val instructor = cols.getOrNull(instIdx)?.trim().orEmpty()
                val email = cols.getOrNull(emailIdx)?.trim().orEmpty()

                if (courseCode.isBlank()) continue

                val key = crn.ifBlank { "$courseCode-$sect" }
                val parsedDays = parseDays(dayRaw)
                val (startMin, endMin) = parseTimes(start, end)

                val meetings = parsedDays.map { day ->
                    TimetableMeeting(
                        day = day,
                        startMinutes = startMin,
                        endMinutes = endMin,
                        start = start,
                        end = end,
                        venue = venue,
                        type = type
                    )
                }

                val existing = sections[key]
                if (existing != null) {
                    val mergedMeetings = existing.meetings + meetings
                    val mergedInstructors = if (instructor.isNotBlank() && !existing.instructors.contains(instructor)) {
                        existing.instructors + instructor
                    } else existing.instructors
                    val mergedEmails = if (email.isNotBlank() && !existing.instructorEmails.contains(email)) {
                        existing.instructorEmails + email
                    } else existing.instructorEmails
                    val mergedTypes = if (!existing.types.contains(type)) {
                        existing.types + type
                    } else existing.types

                    sections[key] = existing.copy(
                        meetings = mergedMeetings,
                        instructors = mergedInstructors,
                        instructorEmails = mergedEmails,
                        types = mergedTypes
                    )
                } else {
                    val colorIdx = colorIndexMap.computeIfAbsent(courseCode) { colorIndexMap.size }
                    sections[key] = TimetableSection(
                        id = key,
                        crn = crn,
                        courseCode = courseCode,
                        courseTitle = courseTitle,
                        section = sect,
                        language = lang,
                        types = listOf(type),
                        instructors = if (instructor.isNotBlank()) listOf(instructor) else emptyList(),
                        instructorEmails = if (email.isNotBlank()) listOf(email) else emptyList(),
                        meetings = meetings,
                        colorHex = getCourseColor(courseCode, colorIdx)
                    )
                }
            }
            reader.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val result = sections.values.sortedWith(compareBy({ it.courseCode }, { it.section }))
        cache[term.assetPath] = result
        result
    }

    /**
     * Get saved selected section IDs for a term.
     */
    fun getSelectedSectionIds(termId: String): Set<String> {
        return prefs.getStringSet("selected_$termId", emptySet()) ?: emptySet()
    }

    /**
     * Save selected section IDs for a term.
     */
    fun saveSelectedSectionIds(termId: String, ids: Set<String>) {
        prefs.edit().putStringSet("selected_$termId", ids).apply()
    }

    /**
     * Find all time conflicts among a list of sections.
     */
    fun findConflicts(sections: List<TimetableSection>): List<TimeConflict> {
        val conflicts = mutableListOf<TimeConflict>()
        for (i in 0 until sections.size) {
            for (j in i + 1 until sections.size) {
                val secA = sections[i]
                val secB = sections[j]
                val clashingPairs = secA.conflictsWith(secB)
                for ((m1, m2) in clashingPairs) {
                    conflicts.add(
                        TimeConflict(
                            sectionA = secA,
                            sectionB = secB,
                            day = m1.day,
                            timeDescription = "${m1.start}-${m1.end} vs ${m2.start}-${m2.end}"
                        )
                    )
                }
            }
        }
        return conflicts
    }

    // ────────────────────────── Helpers ──────────────────────────

    private fun parseCsvLine(line: String): List<String> {
        val out = mutableListOf<String>()
        val sb = StringBuilder()
        var inQuotes = false

        for (ch in line) {
            when {
                ch == '"' -> inQuotes = !inQuotes
                ch == ',' && !inQuotes -> {
                    out.add(sb.toString().trim())
                    sb.clear()
                }
                else -> sb.append(ch)
            }
        }
        out.add(sb.toString().trim())
        return out
    }

    private val dayAliases = mapOf(
        "MON" to "MON", "MONDAY" to "MON",
        "TUE" to "TUE", "TUESDAY" to "TUE",
        "WED" to "WED", "WEDNESDAY" to "WED",
        "THU" to "THU", "THUR" to "THU", "THURSDAY" to "THU",
        "FRI" to "FRI", "FRIDAY" to "FRI",
        "SAT" to "SAT", "SATURDAY" to "SAT",
        "SUN" to "SUN", "SUNDAY" to "SUN"
    )

    private fun parseDays(raw: String): List<String> {
        if (raw.isBlank()) return emptyList()
        val days = mutableListOf<String>()
        val add = { d: String -> if (!days.contains(d)) days.add(d) }

        val parts = raw.uppercase().split(Regex("[\\s,/&;+|-]+")).filter { it.isNotBlank() }
        for (part in parts) {
            dayAliases[part]?.let { add(it); return@let }
            // Check concatenated 3-letter codes e.g. "TUETHU"
            if (part.length % 3 == 0 && part.isNotEmpty()) {
                var ok = true
                val chunks = mutableListOf<String>()
                for (i in part.indices step 3) {
                    val alias = dayAliases[part.substring(i, i + 3)]
                    if (alias != null) chunks.add(alias) else { ok = false; break }
                }
                if (ok) chunks.forEach(add)
            }
        }
        return days
    }

    private fun parseTimes(startStr: String, endStr: String): Pair<Int, Int> {
        val s = timeToMinutes(startStr)
        val e = timeToMinutes(endStr)
        return Pair(s, e)
    }

    private fun timeToMinutes(t: String): Int {
        val parts = t.split(":")
        if (parts.size != 2) return 0
        val h = parts[0].toIntOrNull() ?: 0
        val m = parts[1].toIntOrNull() ?: 0
        return h * 60 + m
    }
}
