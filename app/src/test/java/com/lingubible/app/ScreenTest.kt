package com.lingubible.app

import com.lingubible.app.core.navigation.BottomNavDestination
import com.lingubible.app.core.navigation.Screen
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ScreenTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `verify screen routes serialization roundtrip`() {
        // Test Home route
        val homeJson = json.encodeToString<Screen>(Screen.Home)
        val decodedHome = json.decodeFromString<Screen>(homeJson)
        assertEquals(Screen.Home, decodedHome)

        // Test Courses route
        val coursesJson = json.encodeToString<Screen>(Screen.Courses)
        val decodedCourses = json.decodeFromString<Screen>(coursesJson)
        assertEquals(Screen.Courses, decodedCourses)

        // Test CourseDetail with parameter
        val courseDetail = Screen.CourseDetail(courseCode = "CLC9001")
        val courseJson = json.encodeToString<Screen>(courseDetail)
        val decodedCourse = json.decodeFromString<Screen>(courseJson) as Screen.CourseDetail
        assertEquals("CLC9001", decodedCourse.courseCode)

        // Test Instructors route
        val instructorsJson = json.encodeToString<Screen>(Screen.Instructors)
        val decodedInstructors = json.decodeFromString<Screen>(instructorsJson)
        assertEquals(Screen.Instructors, decodedInstructors)

        // Test InstructorDetail with parameter
        val instructor = Screen.InstructorDetail(name = "Dr. Jane")
        val instructorJson = json.encodeToString<Screen>(instructor)
        val decodedInstructor = json.decodeFromString<Screen>(instructorJson) as Screen.InstructorDetail
        assertEquals("Dr. Jane", decodedInstructor.name)

        // Test Reviews route
        val reviewsJson = json.encodeToString<Screen>(Screen.Reviews)
        val decodedReviews = json.decodeFromString<Screen>(reviewsJson)
        assertEquals(Screen.Reviews, decodedReviews)

        // Test WriteReview with null and non-null course code
        val reviewWithCode = Screen.WriteReview(courseCode = "COMP3211")
        val reviewJson = json.encodeToString<Screen>(reviewWithCode)
        val decodedReview = json.decodeFromString<Screen>(reviewJson) as Screen.WriteReview
        assertEquals("COMP3211", decodedReview.courseCode)

        val reviewWithoutCode = Screen.WriteReview()
        val reviewEmptyJson = json.encodeToString<Screen>(reviewWithoutCode)
        val decodedEmptyReview = json.decodeFromString<Screen>(reviewEmptyJson) as Screen.WriteReview
        assertNull(decodedEmptyReview.courseCode)

        // Test Auth route with default and custom mode
        val authDefault = Screen.Auth()
        val authDefaultJson = json.encodeToString<Screen>(authDefault)
        val decodedAuthDefault = json.decodeFromString<Screen>(authDefaultJson) as Screen.Auth
        assertEquals("login", decodedAuthDefault.mode)

        val authRegister = Screen.Auth(mode = "register")
        val authRegisterJson = json.encodeToString<Screen>(authRegister)
        val decodedAuthRegister = json.decodeFromString<Screen>(authRegisterJson) as Screen.Auth
        assertEquals("register", decodedAuthRegister.mode)

        // Test Calendar route
        val calendarJson = json.encodeToString<Screen>(Screen.Calendar)
        val decodedCalendar = json.decodeFromString<Screen>(calendarJson)
        assertEquals(Screen.Calendar, decodedCalendar)

        // Test GpaHons route
        val gpaHonsJson = json.encodeToString<Screen>(Screen.GpaHons)
        val decodedGpaHons = json.decodeFromString<Screen>(gpaHonsJson)
        assertEquals(Screen.GpaHons, decodedGpaHons)

        // Test AcademicTools route with default and custom parameters
        val academicToolsDefault = Screen.AcademicTools()
        val academicToolsDefaultJson = json.encodeToString<Screen>(academicToolsDefault)
        val decodedAcademicToolsDefault = json.decodeFromString<Screen>(academicToolsDefaultJson) as Screen.AcademicTools
        assertEquals(0, decodedAcademicToolsDefault.initialTab)
        assertEquals(false, decodedAcademicToolsDefault.fromHome)

        val academicToolsFromHome = Screen.AcademicTools(initialTab = 1, fromHome = true)
        val academicToolsFromHomeJson = json.encodeToString<Screen>(academicToolsFromHome)
        val decodedAcademicToolsFromHome = json.decodeFromString<Screen>(academicToolsFromHomeJson) as Screen.AcademicTools
        assertEquals(1, decodedAcademicToolsFromHome.initialTab)
        assertEquals(true, decodedAcademicToolsFromHome.fromHome)

        // Test Profile route
        val profileJson = json.encodeToString<Screen>(Screen.Profile)
        val decodedProfile = json.decodeFromString<Screen>(profileJson)
        assertEquals(Screen.Profile, decodedProfile)

        // Test Settings route
        val settingsJson = json.encodeToString<Screen>(Screen.Settings)
        val decodedSettings = json.decodeFromString<Screen>(settingsJson)
        assertEquals(Screen.Settings, decodedSettings)
    }

    @Test
    fun `verify drawer navigation destinations include calendar and gpa and settings`() {
        val drawerDests = com.lingubible.app.core.navigation.DrawerNavDestination.entries
        val screens = drawerDests.map { it.screen }
        assertTrue(screens.contains(Screen.Planner))
        assertTrue(screens.contains(Screen.Calendar))
        assertTrue(screens.contains(Screen.GpaHons))
        assertTrue(screens.contains(Screen.Settings))
    }

    @Test
    fun `verify bottom navigation destination definitions`() {
        val destinations = BottomNavDestination.entries
        assertEquals(3, destinations.size)

        val screens = destinations.map { it.screen }
        assertEquals(3, screens.distinct().size)

        val zhLabels = destinations.map { it.titleZh }
        assertEquals(3, zhLabels.distinct().size)

        val enLabels = destinations.map { it.titleEn }
        assertEquals(3, enLabels.distinct().size)

        destinations.forEach { destination ->
            assertNotNull(destination.unselectedIcon)
            assertNotNull(destination.selectedIcon)
        }
    }
}
