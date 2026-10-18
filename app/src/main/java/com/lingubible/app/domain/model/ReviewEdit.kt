package com.lingubible.app.domain.model

import com.lingubible.app.domain.util.RatingValidator
import com.lingubible.app.domain.util.WordCountValidator
import kotlinx.serialization.json.*

data class ReviewEdit(
    val comment: String,
    val grade: String,
    val workload: Double,
    val difficulty: Double,
    val usefulness: Double,
    val anonymous: Boolean,
    val language: String,
    val instructors: JsonArray
) {
    fun patch(): Map<String, Any> {
        require(WordCountValidator.isValid(comment, 5, 1000)) { "評價需 5–1000 字 · Review must contain 5–1000 words" }
        require(grade in GRADES) { "請選擇有效成績 · Select a valid grade" }
        require(language in setOf("en", "zh-TW", "zh-CN")) { "Invalid review language" }
        require(listOf(workload, difficulty, usefulness).all(::validReviewRating)) { "Invalid course rating" }
        instructors.forEach {
            val detail = it.jsonObject
            listOf("grading", "teaching").forEach { key ->
                val rating = detail[key]?.jsonPrimitive?.doubleOrNull ?: -1.0
                require(validReviewRating(rating)) { "Invalid instructor rating" }
            }
            require(WordCountValidator.isValid(detail["comments"]?.jsonPrimitive?.contentOrNull, 5, 1000)) { "講師評價需 5–1000 字 · Instructor comments must contain 5–1000 words" }
        }
        return mapOf(
            "course_comments" to comment.trim(), "course_final_grade" to grade,
            "course_workload" to workload, "course_difficulties" to difficulty,
            "course_usefulness" to usefulness, "is_anon" to anonymous,
            "review_language" to language, "instructor_details" to instructors.toString()
        )
    }

    companion object {
        val GRADES = listOf("A", "A-", "B+", "B", "B-", "C+", "C", "C-", "D+", "D", "F", "PASS", "FAIL", "S", "U", "I", "N/A", "-1")
        fun from(data: Map<String, Any>): ReviewEdit = ReviewEdit(
            data["course_comments"] as? String ?: "",
            data["course_final_grade"] as? String ?: "N/A",
            (data["course_workload"] as? Number)?.toDouble() ?: -1.0,
            (data["course_difficulties"] as? Number)?.toDouble() ?: -1.0,
            (data["course_usefulness"] as? Number)?.toDouble() ?: -1.0,
            data["is_anon"] as? Boolean ?: true,
            data["review_language"] as? String ?: "en",
            (data["instructor_details"] as? String)?.takeIf { it.isNotBlank() }?.let { Json.parseToJsonElement(it).jsonArray } ?: JsonArray(emptyList())
        )
    }
}
