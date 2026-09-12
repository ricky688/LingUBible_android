package com.lingubible.app

import com.lingubible.app.core.navigation.BottomNavDestination
import com.lingubible.app.core.navigation.Screen
import com.lingubible.app.core.util.I18n
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.*
import org.junit.Test

class AdversarialNavigationTest {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    @Test
    fun `adversarial test course detail with edge case course codes`() {
        val edgeCases = listOf(
            "BUS1102",
            "GEA/1000",
            "LANG 101",
            "COMP-3211",
            "中文課程",
            "COMP@101",
            "A".repeat(500),
            "",
            "course?query=1&sort=desc#anchor"
        )

        for (code in edgeCases) {
            val screen = Screen.CourseDetail(courseCode = code)
            val encoded = json.encodeToString<Screen>(screen)
            val decoded = json.decodeFromString<Screen>(encoded) as Screen.CourseDetail
            assertEquals("Roundtrip failed for code: $code", code, decoded.courseCode)
        }
    }

    @Test
    fun `adversarial test instructor detail with edge case names`() {
        val edgeCases = listOf(
            "Prof. John Doe",
            "Dr. O'Connor",
            "Prof. A/B Testing",
            "陳教授",
            "Dr. Jane & Team",
            "Name with emoji \uD83D\uDC31",
            "",
            "Dr. Unknown (Visiting Scholar, 2024-2025)"
        )

        for (name in edgeCases) {
            val screen = Screen.InstructorDetail(name = name)
            val encoded = json.encodeToString<Screen>(screen)
            val decoded = json.decodeFromString<Screen>(encoded) as Screen.InstructorDetail
            assertEquals("Roundtrip failed for name: $name", name, decoded.name)
        }
    }

    @Test
    fun `adversarial test write review with null and edge case course codes`() {
        // null courseCode
        val nullScreen = Screen.WriteReview(null)
        val nullEncoded = json.encodeToString<Screen>(nullScreen)
        val nullDecoded = json.decodeFromString<Screen>(nullEncoded) as Screen.WriteReview
        assertNull(nullDecoded.courseCode)

        // default parameter
        val defaultScreen = Screen.WriteReview()
        val defaultEncoded = json.encodeToString<Screen>(defaultScreen)
        val defaultDecoded = json.decodeFromString<Screen>(defaultEncoded) as Screen.WriteReview
        assertNull(defaultDecoded.courseCode)

        // edge case codes
        val codeScreen = Screen.WriteReview("CLC9001/A")
        val codeEncoded = json.encodeToString<Screen>(codeScreen)
        val codeDecoded = json.decodeFromString<Screen>(codeEncoded) as Screen.WriteReview
        assertEquals("CLC9001/A", codeDecoded.courseCode)
    }

    @Test
    fun `adversarial test auth route with multiple modes`() {
        val modes = listOf("login", "register", "forgot_password", "", "custom_mode_123")
        for (mode in modes) {
            val screen = Screen.Auth(mode = mode)
            val encoded = json.encodeToString<Screen>(screen)
            val decoded = json.decodeFromString<Screen>(encoded) as Screen.Auth
            assertEquals(mode, decoded.mode)
        }
    }

    @Test
    fun `adversarial test I18n key sanitization stress harness`() {
        val testCases = mapOf(
            "simpleKey" to "simpleKey",
            "auth.login" to "auth_login",
            "multi.part.nested.key" to "multi_part_nested_key",
            "404.title" to "_404_title",
            "3d.view" to "_3d_view",
            "kebab-case-key" to "kebab_case_key",
            "animal.🐱" to "animal_u1f431",
            "emoji.🎉" to "emoji_u1f389",
            "already_valid_name" to "already_valid_name"
        )

        for ((input, expected) in testCases) {
            val actual = I18n.keyToResName(input)
            assertEquals("Failed for key: $input", expected, actual)
            // Verify resulting name matches Android resource identifier rules
            assertTrue("Identifier invalid: $actual", actual.matches(Regex("^[a-zA-Z_][a-zA-Z0-9_]*$")))
        }
    }

    @Test
    fun `adversarial test I18n plural processing stress harness`() {
        val template = "Found %1\$s course|courses"

        // Count = 1 (singular)
        val singular = I18n.processPluralTranslation(template, 1)
        assertEquals("Found %1\$s course", singular)

        // Count = 0 (plural in English)
        val plural0 = I18n.processPluralTranslation(template, 0)
        assertEquals("Found %1\$s courses", plural0)

        // Count = 2 (plural)
        val plural2 = I18n.processPluralTranslation(template, 2)
        assertEquals("Found %1\$s courses", plural2)

        // Count = 1.0 (Double singular)
        val singularDouble = I18n.processPluralTranslation(template, 1.0)
        assertEquals("Found %1\$s course", singularDouble)

        // Count = 1.5 (Double plural)
        val pluralDouble = I18n.processPluralTranslation(template, 1.5)
        assertEquals("Found %1\$s courses", pluralDouble)

        // Text with no plural pattern
        val noPlural = "Welcome to LingUBible"
        assertEquals(noPlural, I18n.processPluralTranslation(noPlural, 5))
    }

    @Test
    fun `adversarial test bottom navigation destination coverage`() {
        val destinations = BottomNavDestination.entries
        assertEquals(3, destinations.size)

        // Ensure unique screens and unique labels
        val screens = destinations.map { it.screen::class }
        assertEquals(3, screens.toSet().size)

        val zhLabels = destinations.map { it.titleZh }
        assertEquals(3, zhLabels.toSet().size)

        val enLabels = destinations.map { it.titleEn }
        assertEquals(3, enLabels.toSet().size)
    }
}
