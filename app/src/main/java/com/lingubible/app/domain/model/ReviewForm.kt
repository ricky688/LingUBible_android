package com.lingubible.app.domain.model

import com.lingubible.app.domain.util.WordCountValidator
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*

val REVIEW_REQUIREMENTS = linkedMapOf(
    "has_midterm" to "期中試 Midterm", "has_final" to "期末試 Final",
    "has_quiz" to "小測 Quiz", "has_group_project" to "小組專題 Group project",
    "has_individual_assignment" to "個人作業 Individual assignment",
    "has_presentation" to "匯報 Presentation", "has_reading" to "閱讀 Reading",
    "has_attendance_requirement" to "出席要求 Attendance"
)
val REVIEW_GRADES = listOf("A", "A-", "B+", "B", "B-", "C+", "C", "C-", "D+", "D", "F", "-1")
fun validReviewRating(value: Double?): Boolean = value != null && value.isFinite() &&
    (value == -1.0 || (value in 0.0..5.0 && value * 2 == kotlin.math.floor(value * 2)))

@Serializable
data class InstructorEvaluation(
    val name: String = "", val session: String = "",
    val teaching: Double? = null, val grading: Double? = null,
    val comment: String = "", val requirements: Set<String> = emptySet(),
    val serviceLearning: String = "none", val serviceDescription: String = "",
    val notAttended: Boolean = false
) {
    val key: String get() = "$name|$session"
    fun wire(): JsonObject = buildJsonObject {
        put("instructor_name", name); put("session_type", session)
        put("teaching", if (notAttended) -1.0 else teaching); put("grading", grading)
        put("not_attended", notAttended); put("comments", comment.trim())
        REVIEW_REQUIREMENTS.keys.forEach { put(it, it in requirements) }
        put("has_service_learning", serviceLearning != "none")
        put("service_learning_type", if (serviceLearning == "none") "optional" else serviceLearning)
        put("service_learning_description", if (serviceLearning == "none") "" else serviceDescription.trim())
    }
    companion object {
        fun fromWire(raw: String?): List<InstructorEvaluation> = runCatching {
            if (raw.isNullOrBlank()) return emptyList()
            val parsed = runCatching { Json.parseToJsonElement(raw) }.getOrElse {
                // Older imported comments can contain unescaped newlines inside JSON strings.
                var quoted = false
                var escaped = false
                val repaired = buildString {
                    raw.forEach { char ->
                        if (quoted && char.code < 32) append("\\u" + char.code.toString(16).padStart(4, '0')) else append(char)
                        if (!escaped && char == '"') quoted = !quoted
                        escaped = !escaped && char == '\\'
                    }
                }
                Json.parseToJsonElement(repaired)
            }
            parsed.jsonArray.map { item ->
                val v = item.jsonObject
                fun text(key: String) = v[key]?.jsonPrimitive?.contentOrNull.orEmpty()
                fun flag(key: String) = v[key]?.jsonPrimitive?.booleanOrNull == true
                InstructorEvaluation(text("instructor_name"), text("session_type"),
                    v["teaching"]?.jsonPrimitive?.doubleOrNull, v["grading"]?.jsonPrimitive?.doubleOrNull,
                    text("comments"), REVIEW_REQUIREMENTS.keys.filter(::flag).toSet(),
                    if (flag("has_service_learning")) text("service_learning_type").ifBlank { "optional" } else "none",
                    text("service_learning_description"), flag("not_attended"))
            }
        }.getOrDefault(emptyList())
    }
}

@Serializable
data class ReviewForm(
    val courseCode: String = "", val term: String = "", val grade: String = "",
    val workload: Double? = null, val difficulty: Double? = null, val usefulness: Double? = null,
    val comment: String = "", val anonymous: Boolean = true, val language: String = "en",
    val instructors: List<InstructorEvaluation> = emptyList()
) {
    fun validate() {
        require(courseCode.isNotBlank() && term.isNotBlank()) { "請選擇課程及學期 · Select a course and term" }
        require(grade in REVIEW_GRADES) { "請選擇成績 · Select your grade" }
        require(language in setOf("en", "zh-TW", "zh-CN")) { "請選擇評價語言 · Select review language" }
        require(listOf(workload, difficulty, usefulness).all(::validReviewRating)) { "請完成課程評分 · Complete course ratings" }
        require(WordCountValidator.isValid(comment, 5, 1000)) { "課程評價需 5–1000 字 · Course comments: 5–1000 words" }
        require(instructors.isNotEmpty() && instructors.map { it.key }.distinct().size == instructors.size) { "請選擇講師及課節 · Select instructors and sessions" }
        instructors.forEach { i ->
            val label = "${i.name} · ${i.session}"
            require(i.name.isNotBlank() && i.session.isNotBlank()) { "Invalid instructor/session" }
            require(validReviewRating(i.grading) && (i.notAttended || validReviewRating(i.teaching))) { "$label: 請完成評分 · Complete ratings" }
            require(i.requirements.isNotEmpty() && i.requirements.all { it in REVIEW_REQUIREMENTS }) { "$label: 請選擇至少一項課程要求 · Select at least one assessment requirement" }
            require(WordCountValidator.isValid(i.comment, 5, 1000)) { "$label: 講師評價需 5–1000 字 · Instructor comments: 5–1000 words" }
            require(i.serviceLearning in setOf("none", "optional", "compulsory")) { "Invalid service-learning type" }
            if (i.serviceLearning != "none") require(WordCountValidator.isValid(i.serviceDescription, if (i.serviceLearning == "compulsory") 5 else 0, 1000)) { "$label: 服務學習描述需 ${if (i.serviceLearning == "compulsory") "5–1000" else "0–1000"} 字 · Check service-learning description" }
        }
    }
    fun documentData(userId: String, username: String, submittedAt: String): Map<String, Any> {
        validate()
        require(userId.isNotBlank()) { "Sign in required" }
        return mapOf("course_code" to courseCode, "term_code" to term, "course_final_grade" to grade,
            "course_workload" to requireNotNull(workload), "course_difficulties" to requireNotNull(difficulty),
            "course_usefulness" to requireNotNull(usefulness), "course_comments" to comment.trim(),
            "instructor_details" to JsonArray(instructors.map { it.wire() }).toString(),
            "user_id" to userId, "username" to username, "is_anon" to anonymous,
            "submitted_at" to submittedAt, "review_language" to language)
    }
}
data class ReviewSubmission(val userId: String, val form: ReviewForm)

fun reviewRatingAverage(values: List<Double?>): Double = values.filterNotNull().filter { it >= 0 && it.isFinite() }.let { if (it.isEmpty()) -1.0 else it.average() }
