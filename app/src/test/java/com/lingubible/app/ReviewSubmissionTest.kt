package com.lingubible.app

import com.lingubible.app.domain.model.*
import kotlinx.serialization.json.*
import org.junit.Assert.*
import org.junit.Test

class ReviewSubmissionTest {
    private val evaluation = InstructorEvaluation("Lecturer", "Lecture", 0.0, -1.0,
        "The lecturer explained the topics clearly", setOf("has_final"))
    private val form = ReviewForm("BUS1102", "202510", "A", 0.0, -1.0, 4.5,
        "This course helped me understand the subject", instructors = listOf(evaluation))

    @Test fun `payload preserves multiple session evaluations and explicit zero or NA`() {
        val f = form.copy(anonymous = false, language = "zh-TW", instructors = listOf(evaluation,
            evaluation.copy(session = "Tutorial", notAttended = true, teaching = null,
                serviceLearning = "compulsory", serviceDescription = "We worked with a local community group")))
        val data = f.documentData("owner", "Student", "2026-10-16T00:00:00Z")
        assertEquals(false, data["is_anon"]); assertEquals("zh-TW", data["review_language"])
        assertEquals(0.0, data["course_workload"]); assertEquals(-1.0, data["course_difficulties"])
        assertEquals(4.5, data["course_usefulness"])
        val raw = Json.parseToJsonElement(data["instructor_details"] as String).jsonArray
        assertEquals(2, raw.size)
        assertEquals("Tutorial", raw[1].jsonObject["session_type"]?.jsonPrimitive?.content)
        assertEquals(-1.0, raw[1].jsonObject["teaching"]?.jsonPrimitive?.double)
        assertTrue(raw[1].jsonObject["has_service_learning"]!!.jsonPrimitive.boolean)
        assertEquals("compulsory", raw[1].jsonObject["service_learning_type"]?.jsonPrimitive?.content)
        assertEquals(f.instructors.map { it.key }, InstructorEvaluation.fromWire(raw.toString()).map { it.key })
    }
    @Test fun `unanswered ratings and invalid scores cannot be published`() {
        listOf<Double?>(null, 5.5, -0.5, 0.25, Double.NaN, Double.POSITIVE_INFINITY).forEach { rating ->
            assertTrue(runCatching { form.copy(workload = rating).validate() }.isFailure)
        }
        listOf(-1.0, 0.0, 0.5, 5.0).forEach { form.copy(workload = it).validate() }
    }
    @Test fun `each instructor needs assessment ratings and own comment`() {
        listOf(evaluation.copy(requirements = emptySet()), evaluation.copy(requirements = setOf("unknown")),
            evaluation.copy(grading = null), evaluation.copy(teaching = null), evaluation.copy(comment = "Too short"))
            .forEach { assertTrue(runCatching { form.copy(instructors = listOf(it)).validate() }.isFailure) }
        assertTrue(runCatching { form.copy(instructors = listOf(evaluation, evaluation)).validate() }.isFailure)
        assertTrue(runCatching { form.copy(instructors = emptyList()).validate() }.isFailure)
    }
    @Test fun `compulsory service learning requires description while optional may be empty`() {
        assertTrue(runCatching { form.copy(instructors = listOf(evaluation.copy(serviceLearning = "compulsory"))).validate() }.isFailure)
        form.copy(instructors = listOf(evaluation.copy(serviceLearning = "optional"))).validate()
        assertTrue(runCatching { form.copy(instructors = listOf(evaluation.copy(serviceLearning = "invalid"))).validate() }.isFailure)
    }
    @Test fun `course term grade language and signed in owner are required`() {
        listOf(form.copy(courseCode = ""), form.copy(term = ""), form.copy(grade = ""), form.copy(language = "bad"), form.copy(comment = "Too short"))
            .forEach { assertTrue(runCatching { it.validate() }.isFailure) }
        assertTrue(runCatching { form.documentData("", "Guest", "now") }.isFailure)
    }
    @Test fun `legacy instructor fields and new flags are read without inventing ratings`() {
        val parsed = InstructorEvaluation.fromWire("""[{"instructor_name":"Name","session_type":"Lab","teaching":0,"grading":-1,"comments":"Good","has_quiz":true}]""")
        assertEquals(0.0, parsed.single().teaching!!, 0.0)
        assertEquals(setOf("has_quiz"), parsed.single().requirements)
        assertEquals("none", parsed.single().serviceLearning)
        assertTrue(InstructorEvaluation.fromWire("broken").isEmpty())
    }
    @Test fun `prefer not to disclose grade uses the existing web sentinel`() {
        val data = form.copy(grade = "-1").documentData("owner", "Student", "now")
        assertEquals("-1", data["course_final_grade"])
    }
    @Test fun `legacy unescaped newlines do not hide instructor evaluations`() {
        val raw = """[{"instructor_name":"Name","session_type":"Lecture","comments":"First line
Second line"}]"""
        assertEquals("First line\nSecond line", InstructorEvaluation.fromWire(raw).single().comment)
    }

}
