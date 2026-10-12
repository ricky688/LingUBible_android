package com.lingubible.app.core.util

import com.lingubible.app.domain.calculator.HonoursCalculator
import com.lingubible.app.ui.viewmodels.GpaDocument
import com.lingubible.app.ui.viewmodels.GpaHonsUiState
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonArray

@Serializable
data class GpaPlannerSnapshot(
    val document: GpaDocument,
    val targetCgpaInput: String,
    val remainingCreditsInput: String
)

@Serializable
private data class GpaBackupFile(
    val format: String,
    val version: Int,
    val planner: GpaPlannerSnapshot
)

/** Versioned, lossless backups. Calculated GPAs are recomputed on restore. */
object GpaBackup {
    const val MAX_BYTES = 1_048_576
    private val json = Json { encodeDefaults = true; prettyPrint = true; ignoreUnknownKeys = true }

    fun encode(snapshot: GpaPlannerSnapshot): String =
        json.encodeToString(GpaBackupFile("lingubible-gpa", 1, snapshot))

    fun decode(raw: String): GpaPlannerSnapshot {
        require(raw.toByteArray(Charsets.UTF_8).size <= MAX_BYTES)
        val contents = raw.removePrefix("\uFEFF")
        // Reject incomplete records instead of silently supplying model defaults.
        val document = json.parseToJsonElement(contents).jsonObject.getValue("planner")
            .jsonObject.getValue("document").jsonObject
        require(document.keys.containsAll(listOf("terms", "yearAcademic")))
        document.getValue("terms").jsonArray.forEach { element ->
            val term = element.jsonObject
            require(term.keys.containsAll(listOf("id", "year", "part", "courses")))
            term.getValue("courses").jsonArray.forEach { course ->
                require(course.jsonObject.keys.containsAll(listOf("id", "code", "title", "credits", "grade")))
            }
        }
        val backup = json.decodeFromString<GpaBackupFile>(contents)
        require(backup.format == "lingubible-gpa" && backup.version == 1)
        val snapshot = backup.planner
        val doc = snapshot.document
        require(doc.terms.size <= 24)
        require(doc.terms.map { it.id }.distinct().size == doc.terms.size)
        require(doc.terms.map { it.year to it.part }.distinct().size == doc.terms.size)
        val courseIds = doc.terms.flatMap { it.courses }.map { it.id }
        require(courseIds.distinct().size == courseIds.size)
        val grades = HonoursCalculator.GPA_BEARING_GRADES + HonoursCalculator.NON_GPA_GRADES + ""
        doc.terms.forEach { term ->
            require(term.year in 1..8 && term.id.isNotBlank() && term.id.length <= 128)
            require(term.courses.size <= term.part.maxCourses)
            term.courses.forEach { course ->
                require(course.id.isNotBlank() && course.id.length <= 128)
                require(course.grade in grades)
                require(validNumber(course.credits, 10_000.0))
            }
        }
        require(doc.yearAcademic.all { (year, academic) ->
            year in 1..8 && Regex("[0-9]{4}-[0-9]{4}").matches(academic)
        })
        require(validNumber(snapshot.targetCgpaInput, 4.0))
        require(snapshot.remainingCreditsInput.all { it in '0'..'9' })
        return snapshot
    }

    private fun validNumber(value: String, maximum: Double): Boolean =
        value.length <= 32 && (value.isBlank() || value == "." ||
            value.toDoubleOrNull()?.let { it.isFinite() && it in 0.0..maximum } == true)

    /** UTF-8 BOM and CRLF support spreadsheet readers; neutralize text formulas. */
    fun csv(state: GpaHonsUiState): String {
        fun cell(value: String): String {
            val safe = if (value.trimStart().firstOrNull() in listOf('=', '+', '-', '@')) "'$value" else value
            return "\"${safe.replace("\"", "\"\"")}\""
        }
        val rows = mutableListOf(listOf(
            "學年 Year", "年度 Academic year", "學期 Term", "課程 Course", "名稱 Title",
            "學分 Credits", "成績 Grade", "績點 Grade point", "學期 GPA Term GPA", "累積 GPA CGPA"
        ))
        state.document.terms.sortedWith(compareBy({ it.year }, { it.part.ordinal })).forEach { term ->
            term.courses.forEach { course ->
                rows += listOf(
                    term.year.toString(), state.document.yearAcademic[term.year] ?: "${2021 + term.year}-${2022 + term.year}",
                    "${term.part.titleZh} ${term.part.titleEn}", course.code, course.title,
                    course.credits, course.grade, HonoursCalculator.getGradePoint(course.grade)?.toString().orEmpty(),
                    state.termStats[term.id]?.termGpa?.toString().orEmpty(), state.cgpa?.toString().orEmpty()
                )
            }
        }
        return "\uFEFF" + rows.joinToString("\r\n", postfix = "\r\n") { row -> row.joinToString(",") { cell(it) } }
    }
}
