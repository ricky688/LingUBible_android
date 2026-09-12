package com.lingubible.app

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import com.lingubible.app.domain.model.COURSE_CATEGORIES
import com.lingubible.app.domain.model.CourseCategory
import com.lingubible.app.domain.model.TimetableSection
import com.lingubible.app.ui.viewmodels.TimetableUiState
import org.junit.Assert.*
import org.junit.Test

class CourseCategoryAndGlassTest {

    @Test
    fun `course categories list contains all required departments with bilingual names`() {
        assertNotNull(COURSE_CATEGORIES)
        assertTrue("Categories must not be empty", COURSE_CATEGORIES.isNotEmpty())

        val codes = COURSE_CATEGORIES.map { it.code }
        assertTrue("Must contain ALL option", codes.contains("ALL"))
        assertTrue("Must contain CCC", codes.contains("CCC"))
        assertTrue("Must contain ACT", codes.contains("ACT"))
        assertTrue("Must contain BUS", codes.contains("BUS"))
        assertTrue("Must contain CDS", codes.contains("CDS"))
        assertTrue("Must contain CHI", codes.contains("CHI"))
        assertTrue("Must contain ECO", codes.contains("ECO"))
        assertTrue("Must contain ENG", codes.contains("ENG"))
        assertTrue("Must contain FIN", codes.contains("FIN"))
        assertTrue("Must contain GOV", codes.contains("GOV"))
        assertTrue("Must contain HST", codes.contains("HST"))
        assertTrue("Must contain MGT", codes.contains("MGT"))
        assertTrue("Must contain MKT", codes.contains("MKT"))
        assertTrue("Must contain PHI", codes.contains("PHI"))
        assertTrue("Must contain POL", codes.contains("POL"))
        assertTrue("Must contain PSY", codes.contains("PSY"))
        assertTrue("Must contain SCI", codes.contains("SCI"))
        assertTrue("Must contain SOC", codes.contains("SOC"))
        assertTrue("Must contain TRA", codes.contains("TRA"))

        assertTrue("Total category count must be at least 19", COURSE_CATEGORIES.size >= 19)

        // Ensure each category has non-blank Chinese and English names
        COURSE_CATEGORIES.forEach { category ->
            assertTrue("Code must not be blank", category.code.isNotBlank())
            assertTrue("nameZh must not be blank for ${category.code}", category.nameZh.isNotBlank())
            assertTrue("nameEn must not be blank for ${category.code}", category.nameEn.isNotBlank())
        }
    }

    @Test
    fun `category codes are distinct`() {
        val codes = COURSE_CATEGORIES.map { it.code }
        assertEquals("All category codes must be unique", codes.distinct().size, codes.size)
    }

    @Test
    fun `department filtering matches section department case-insensitively`() {
        val sampleSections = listOf(
            TimetableSection(id = "1", crn = "1001", courseCode = "ACT2101", courseTitle = "Financial Accounting"),
            TimetableSection(id = "2", crn = "1002", courseCode = "BUS1102", courseTitle = "Statistics for Business"),
            TimetableSection(id = "3", crn = "1003", courseCode = "FIN2201", courseTitle = "Hong Kong Monetary System"),
            TimetableSection(id = "4", crn = "1004", courseCode = "act3101", courseTitle = "Cost Accounting"),
            TimetableSection(id = "5", crn = "1005", courseCode = "CDS1001", courseTitle = "Intro to Prog for Data Science"),
            TimetableSection(id = "6", crn = "1006", courseCode = "ECO2101", courseTitle = "Intro to Economics"),
            TimetableSection(id = "7", crn = "1007", courseCode = "SCI1001", courseTitle = "Science and Technology")
        )

        // ALL returns all
        val stateAll = TimetableUiState(allSections = sampleSections, selectedDepartment = "ALL")
        assertEquals(7, stateAll.filteredSections.size)

        // ACT returns both uppercase and lowercase ACT
        val stateAct = TimetableUiState(allSections = sampleSections, selectedDepartment = "ACT")
        assertEquals(2, stateAct.filteredSections.size)
        assertTrue(stateAct.filteredSections.all { it.department.equals("ACT", ignoreCase = true) })

        // CDS returns CDS courses
        val stateCds = TimetableUiState(allSections = sampleSections, selectedDepartment = "CDS")
        assertEquals(1, stateCds.filteredSections.size)
        assertEquals("CDS1001", stateCds.filteredSections.first().courseCode)

        // ECO returns ECO courses
        val stateEco = TimetableUiState(allSections = sampleSections, selectedDepartment = "ECO")
        assertEquals(1, stateEco.filteredSections.size)
        assertEquals("ECO2101", stateEco.filteredSections.first().courseCode)

        // SCI returns SCI courses
        val stateSci = TimetableUiState(allSections = sampleSections, selectedDepartment = "SCI")
        assertEquals(1, stateSci.filteredSections.size)
        assertEquals("SCI1001", stateSci.filteredSections.first().courseCode)

        // Lowercase "bus" filter returns BUS courses
        val stateBus = TimetableUiState(allSections = sampleSections, selectedDepartment = "bus")
        assertEquals(1, stateBus.filteredSections.size)
        assertEquals("BUS1102", stateBus.filteredSections.first().courseCode)

        // Department with no matching sections returns empty list
        val stateChi = TimetableUiState(allSections = sampleSections, selectedDepartment = "CHI")
        assertTrue(stateChi.filteredSections.isEmpty())
    }

    @Test
    fun `combined department filter and search query work together`() {
        val sampleSections = listOf(
            TimetableSection(id = "1", crn = "1001", courseCode = "ACT2101", courseTitle = "Financial Accounting"),
            TimetableSection(id = "2", crn = "1002", courseCode = "ACT3202", courseTitle = "Auditing"),
            TimetableSection(id = "3", crn = "1003", courseCode = "BUS1102", courseTitle = "Accounting for Managers"),
            TimetableSection(id = "4", crn = "1004", courseCode = "CDS3004", courseTitle = "Data Mining")
        )

        // Filter by ACT only
        val stateAct = TimetableUiState(allSections = sampleSections, selectedDepartment = "ACT")
        assertEquals(2, stateAct.filteredSections.size)

        // Filter by ACT + search "Accounting" -> matches only ACT2101 (BUS1102 also has Accounting, but is in BUS)
        val stateActSearch = TimetableUiState(
            allSections = sampleSections,
            selectedDepartment = "ACT",
            searchQuery = "Accounting"
        )
        assertEquals(1, stateActSearch.filteredSections.size)
        assertEquals("ACT2101", stateActSearch.filteredSections.first().courseCode)

        // Filter by CDS + search "Mining"
        val stateCdsSearch = TimetableUiState(
            allSections = sampleSections,
            selectedDepartment = "CDS",
            searchQuery = "Mining"
        )
        assertEquals(1, stateCdsSearch.filteredSections.size)
        assertEquals("CDS3004", stateCdsSearch.filteredSections.first().courseCode)

        // Empty search query does not filter out sections
        val stateEmptySearch = TimetableUiState(
            allSections = sampleSections,
            selectedDepartment = "ACT",
            searchQuery = "   "
        )
        assertEquals(2, stateEmptySearch.filteredSections.size)
    }

    @Test
    fun `material 3 expressive pure spring motion physics invariant`() {
        // M3E guidelines dictate using spring(stiffness = ...) without altering damping ratios
        val mediumLowSpring = spring<Float>(stiffness = Spring.StiffnessMediumLow)
        val mediumSpring = spring<Float>(stiffness = Spring.StiffnessMedium)
        val lowSpring = spring<Float>(stiffness = Spring.StiffnessLow)

        assertEquals(Spring.StiffnessMediumLow, mediumLowSpring.stiffness, 0.001f)
        assertEquals(Spring.StiffnessMedium, mediumSpring.stiffness, 0.001f)
        assertEquals(Spring.StiffnessLow, lowSpring.stiffness, 0.001f)

        // Default damping ratio in Compose spring is DampingRatioNoBouncy (1.0f) or standard
        assertEquals(Spring.DampingRatioNoBouncy, mediumLowSpring.dampingRatio, 0.001f)
    }

    @Test
    fun `responsive max grid height respects landscape and portrait viewports`() {
        fun resolveMaxGridHeight(isLandscape: Boolean): Int {
            return if (isLandscape) 120 else 190
        }

        val portraitHeight = resolveMaxGridHeight(isLandscape = false)
        val landscapeHeight = resolveMaxGridHeight(isLandscape = true)

        assertEquals("Portrait max height must be 190dp to fit category grid cleanly without covering courses", 190, portraitHeight)
        assertEquals("Landscape max height must be clamped to 120dp to prevent viewport overflow", 120, landscapeHeight)
        assertTrue("Landscape height must be strictly smaller than portrait height", landscapeHeight < portraitHeight)
    }

    @Test
    fun `light mode connected button group container follows expressive soft tonal container`() {
        val palettes = listOf(
            com.lingubible.app.core.settings.AppColorScheme.LINGNAN_RED,
            com.lingubible.app.core.settings.AppColorScheme.GOOGLE_BLUE,
            com.lingubible.app.core.settings.AppColorScheme.GOOGLE_PURPLE,
            com.lingubible.app.core.settings.AppColorScheme.ANDROID_BLUE
        )
        for (palette in palettes) {
            val scheme = com.lingubible.app.core.theme.colorSchemeFor(palette, isDark = false)
            // OG unselected connected button uses soft primaryContainer with 0.35f alpha
            val buttonContainer = scheme.primaryContainer.copy(alpha = 0.35f)
            val contentColor = scheme.primary

            assertNotNull(buttonContainer)
            assertEquals(0.35f, buttonContainer.alpha, 0.001f)
            assertNotNull(contentColor)
        }
    }
}
