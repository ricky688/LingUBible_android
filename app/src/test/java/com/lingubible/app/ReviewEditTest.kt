package com.lingubible.app

import com.lingubible.app.domain.model.ReviewEdit
import kotlinx.serialization.json.*
import org.junit.Assert.*
import org.junit.Test

class ReviewEditTest {
    private fun draft() = ReviewEdit.from(mapOf(
        "course_comments" to "A useful course with practical examples", "course_final_grade" to "A",
        "review_language" to "en", "course_workload" to -1,
        "instructor_details" to """[{"instructor_name":"One","session_type":"Tutorial","teaching":4,"grading":-1,"comments":"Clear explanations with helpful practical examples","service_learning_type":"optional"},{"instructor_name":"Two","comments":"Helpful and clear explanations every week"}]"""
    ))
    @Test fun editingPreservesMultipleInstructorsAndExtraFields() {
        val edit = draft().copy(comment = "Updated course comments with more examples")
        val patch = edit.patch()
        val instructors = Json.parseToJsonElement(patch["instructor_details"] as String).jsonArray
        assertEquals(2, instructors.size)
        assertEquals("optional", instructors[0].jsonObject["service_learning_type"]!!.jsonPrimitive.content)
        assertEquals(-1.0, patch["course_workload"])
        assertFalse(patch.containsKey("user_id")); assertFalse(patch.containsKey("course_code"))
    }
    @Test(expected = IllegalArgumentException::class) fun rejectsInvalidRatings() { draft().copy(workload = 5.5).patch() }
    @Test(expected = IllegalArgumentException::class) fun rejectsTooShortComment() { draft().copy(comment = "too short").patch() }
    @Test(expected = IllegalArgumentException::class) fun rejectsInvalidGrade() { draft().copy(grade = "anything").patch() }
    @Test(expected = IllegalArgumentException::class) fun refusesMalformedInstructorData() { ReviewEdit.from(mapOf("instructor_details" to "not-json")) }
}
