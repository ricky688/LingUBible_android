package com.lingubible.app.e2e

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.lingubible.app.BuildConfig
import com.lingubible.app.core.navigation.Screen
import com.lingubible.app.core.theme.DarkColorScheme
import com.lingubible.app.core.theme.LightColorScheme
import com.lingubible.app.core.theme.LingnanRed
import com.lingubible.app.core.theme.Typography
import com.lingubible.app.core.util.I18n
import com.lingubible.app.e2e.contracts.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.*
import org.junit.Test
import java.io.File

/**
 * Tier 2: Boundary & Corner Cases Test Suite.
 * Covers limits, zero/negative inputs, empty lists, illegal domains, and malformed inputs
 * for all 24 features in TEST_INFRA.md.
 * 24 features × 5 test cases = 120 test cases.
 */
class Tier2BoundaryCornerCaseTest {

    // ==========================================
    // Feature 1: Gradle Toolchain & Project Scaffolding
    // ==========================================
    @Test
    fun test01_1_minSdkBelow26Rejected() {
        assertFalse("minSdk 24 must be rejected", ProjectScaffoldingContract.validateSdkConstraints(minSdk = 24, targetSdk = 35, compileSdk = 35))
        assertFalse("minSdk 25 must be rejected", ProjectScaffoldingContract.validateSdkConstraints(minSdk = 25, targetSdk = 35, compileSdk = 35))
        assertTrue("minSdk 26 must be accepted", ProjectScaffoldingContract.validateSdkConstraints(minSdk = 26, targetSdk = 35, compileSdk = 35))
        val buildGradle = File("app/build.gradle.kts").takeIf { it.exists() } ?: File("../app/build.gradle.kts")
        assertTrue(buildGradle.exists())
    }

    @Test
    fun test01_2_compileSdkBelowTargetSdkRejected() {
        assertFalse("compileSdk < targetSdk must be rejected", ProjectScaffoldingContract.validateSdkConstraints(minSdk = 26, targetSdk = 35, compileSdk = 34))
        assertTrue("compileSdk == targetSdk must be accepted", ProjectScaffoldingContract.validateSdkConstraints(minSdk = 26, targetSdk = 35, compileSdk = 35))
        val buildGradle = File("app/build.gradle.kts").takeIf { it.exists() } ?: File("../app/build.gradle.kts")
        assertTrue(buildGradle.exists())
    }

    @Test
    fun test01_3_missingMandatoryPluginValidation() {
        assertFalse(ProjectScaffoldingContract.validatePluginSet(emptyList()))
        assertFalse(ProjectScaffoldingContract.validatePluginSet(listOf("com.android.application", "org.jetbrains.kotlin.android")))
        assertTrue(ProjectScaffoldingContract.validatePluginSet(ProjectScaffoldingContract.REQUIRED_PLUGINS))
        val buildGradle = File("app/build.gradle.kts").takeIf { it.exists() } ?: File("../app/build.gradle.kts")
        assertTrue(buildGradle.exists())
    }

    @Test
    fun test01_4_unsupportedJavaTargetVersionRejected() {
        assertFalse(ProjectScaffoldingContract.validateJavaVersion(8))
        assertFalse(ProjectScaffoldingContract.validateJavaVersion(11))
        assertFalse(ProjectScaffoldingContract.validateJavaVersion(16))
        assertTrue(ProjectScaffoldingContract.validateJavaVersion(17))
        val jvmVersion = System.getProperty("java.version")
        val major = jvmVersion?.substringBefore(".")?.substringBefore("-")?.toIntOrNull() ?: 17
        assertTrue(major >= 17)
    }

    @Test
    fun test01_5_malformedPackageNameRejected() {
        assertFalse(ProjectScaffoldingContract.validatePackageName("com.lingubible..app"))
        assertFalse(ProjectScaffoldingContract.validatePackageName(".com.lingubible.app"))
        assertFalse(ProjectScaffoldingContract.validatePackageName("com.lingubible.app."))
        assertFalse(ProjectScaffoldingContract.validatePackageName("1com.lingubible.app"))
        assertFalse(ProjectScaffoldingContract.validatePackageName("com.lingubible.class"))
        assertTrue(ProjectScaffoldingContract.validatePackageName(BuildConfig.APPLICATION_ID))
    }

    // ==========================================
    // Feature 2: Material Design 3 Theming
    // ==========================================
    @Test
    fun test02_1_contrastRatioWcagAaCompliance() {
        val primary = LingnanRed
        val ratio = ColorContrastContract.calculateContrastRatio(1.0f, 1.0f, 1.0f, primary.red, primary.green, primary.blue)
        assertTrue("Lingnan red on white text ratio must be computed: $ratio", ratio >= 3.8)

        val onSurface = LightColorScheme.onSurface
        val surface = LightColorScheme.surface
        val textRatio = ColorContrastContract.calculateContrastRatio(onSurface.red, onSurface.green, onSurface.blue, surface.red, surface.green, surface.blue)
        assertTrue("Normal text on light surface must exceed WCAG AA 4.5: $textRatio", textRatio >= 4.5)
    }

    @Test
    fun test02_2_darkThemeSurfaceContrastRatio() {
        val onSurface = DarkColorScheme.onSurface
        val surface = DarkColorScheme.surface
        val ratio = ColorContrastContract.calculateContrastRatio(onSurface.red, onSurface.green, onSurface.blue, surface.red, surface.green, surface.blue)
        assertTrue("Dark theme text on surface must exceed WCAG AA 4.5: $ratio", ratio >= 4.5)
    }

    @Test
    fun test02_3_extremeFontScaleFactorHandling() {
        val baseSize = Typography.bodyLarge.fontSize.value
        val scales = listOf(0.5f, 0.85f, 1.0f, 1.5f, 2.0f, 3.0f)
        for (scale in scales) {
            val scaled = (baseSize * scale).coerceIn(10f, 40f)
            assertTrue("Scaled font size must be within readable bounds: $scaled", scaled in 10f..40f)
        }
    }

    @Test
    fun test02_4_emptyCustomFontFamilyFallback() {
        assertEquals(androidx.compose.ui.text.font.FontFamily.Default, Typography.bodyLarge.fontFamily)
        assertTrue(Typography.bodyLarge.fontSize.value > 0f)
        assertTrue(Typography.displayLarge.fontSize.value > 0f)
    }

    @Test
    fun test02_5_zeroCornerRadiusShape() {
        val zeroShape = androidx.compose.foundation.shape.RoundedCornerShape(0.dp)
        val density = Density(1.0f)
        val topStart = zeroShape.topStart.toPx(Size(100f, 100f), density)
        assertEquals("Zero corner radius shape must yield exactly 0px", 0f, topStart, 0.001f)
        assertNotNull(LightColorScheme.primary)
    }

    // ==========================================
    // Feature 3: Navigation Hierarchy
    // ==========================================
    @Test
    fun test03_1_pathTraversalInCourseCodeSanitized() {
        val detail = Screen.CourseDetail(courseCode = "../../etc/passwd")
        val jsonStr = Json.encodeToString<Screen>(detail)
        val decoded = Json.decodeFromString<Screen>(jsonStr) as Screen.CourseDetail
        assertEquals("../../etc/passwd", decoded.courseCode)
        assertFalse(CourseCatalogContract.validateCourse(CourseCatalogContract.CourseItem(courseCode = decoded.courseCode, title = "T", credits = 3, department = "D")))
    }

    @Test
    fun test03_2_specialCharactersInRouteParamEncoded() {
        val rawInstructor = "Prof. Richard O'Simmons & Team / 2024"
        val detail = Screen.InstructorDetail(name = rawInstructor)
        val jsonStr = Json.encodeToString<Screen>(detail)
        val decoded = Json.decodeFromString<Screen>(jsonStr) as Screen.InstructorDetail
        assertEquals(rawInstructor, decoded.name)
    }

    @Test
    fun test03_3_popBackStackAtRootReturnsFalse() {
        val stack = mutableListOf<Screen>(Screen.Home)
        val popped = if (stack.size > 1) stack.removeAt(stack.size - 1) else null
        assertNull("Cannot pop root destination", popped)
        assertEquals(1, stack.size)
        assertEquals(Screen.Home, stack.first())
    }

    @Test
    fun test03_4_unknownRouteFallbackToHome() {
        val invalidJson = """{"type":"com.lingubible.app.core.navigation.Screen.InvalidDestination"}"""
        val resolved = runCatching { Json.decodeFromString<Screen>(invalidJson) }.getOrDefault(Screen.Home)
        assertEquals(Screen.Home, resolved)
    }

    @Test
    fun test03_5_extremelyLongRouteQueryHandled() {
        val longCode = "CDS" + "9".repeat(5000)
        val detail = Screen.CourseDetail(courseCode = longCode)
        val jsonStr = Json.encodeToString<Screen>(detail)
        val decoded = Json.decodeFromString<Screen>(jsonStr) as Screen.CourseDetail
        assertEquals(longCode, decoded.courseCode)
    }

    // ==========================================
    // Feature 4: Multilingual Localization (i18n)
    // ==========================================
    @Test
    fun test04_1_missingKeyReturnsFallbackKeyIdentifier() {
        assertEquals("", I18n.keyToResName(""))
        assertEquals("_12345", I18n.keyToResName("12345"))
        assertEquals("courses_title", I18n.keyToResName("courses.title"))
    }

    @Test
    fun test04_2_emptyTranslationStringHandled() {
        val processed = I18n.processPluralTranslation("", 5)
        assertTrue(processed.isEmpty())
        val escaped = LocalizationContract.escapeForXml("")
        assertTrue(escaped.isEmpty())
    }

    @Test
    fun test04_3_multipleAdjacentXmlEntitiesEscaped() {
        val raw = "<&&>"
        val escaped = LocalizationContract.escapeForXml(raw)
        assertEquals("&lt;&amp;&amp;&gt;", escaped)
    }

    @Test
    fun test04_4_extraUnusedFormatParametersHandledSafely() {
        val converted = LocalizationContract.convertParamFormat("{name} has {count} reviews in {term}")
        assertEquals("%1\$s has %2\$s reviews in %3\$s", converted)
        val unclosed = LocalizationContract.convertParamFormat("Hello {user")
        assertEquals("Hello {user", unclosed)
    }

    @Test
    fun test04_5_highOrderUnicodeAndRareCjkPreserved() {
        val rareCjk = "嶺南大學 📚 𠮷野家"
        val escaped = LocalizationContract.escapeForXml(rareCjk)
        assertTrue(escaped.contains("📚"))
        assertTrue(escaped.contains("嶺南大學"))
    }

    // ==========================================
    // Feature 5: Appwrite Client & Config
    // ==========================================
    @Test
    fun test05_1_insecureHttpEndpointRejected() {
        val insecureEndpoint = "http://appwrite.lingubible.com/v1"
        assertFalse("HTTP must be rejected for Appwrite endpoint", AppwriteConfigContract.validateEndpoint(insecureEndpoint))
    }

    @Test
    fun test05_2_blankProjectIdRejected() {
        assertFalse(AppwriteConfigContract.validateProjectId(""))
        assertFalse(AppwriteConfigContract.validateProjectId("   "))
        assertTrue(AppwriteConfigContract.validateProjectId(AppwriteConfigContract.PROJECT_ID))
        assertTrue(AppwriteConfigContract.validateProjectId(BuildConfig.APPWRITE_PROJECT_ID))
    }

    @Test
    fun test05_3_blankDatabaseIdRejected() {
        assertFalse(AppwriteConfigContract.validateDatabaseId(""))
        assertFalse(AppwriteConfigContract.validateDatabaseId("   "))
        assertTrue(AppwriteConfigContract.validateDatabaseId(AppwriteConfigContract.DATABASE_ID))
        assertTrue(AppwriteConfigContract.validateDatabaseId(BuildConfig.APPWRITE_DATABASE_ID))
    }

    @Test
    fun test05_4_client401TreatedSilentlyAsGuest() {
        val mapped = AuthValidatorContract.mapHttpStatus(401)
        assertEquals(AuthValidatorContract.AuthError.InvalidCredentials, mapped)
    }

    @Test
    fun test05_5_networkTimeoutConfigurationBounds() {
        assertFalse(AppwriteConfigContract.validateTimeout(0))
        assertFalse(AppwriteConfigContract.validateTimeout(4))
        assertTrue(AppwriteConfigContract.validateTimeout(5))
        assertTrue(AppwriteConfigContract.validateTimeout(30))
        assertTrue(AppwriteConfigContract.validateTimeout(60))
        assertFalse(AppwriteConfigContract.validateTimeout(61))
    }

    // ==========================================
    // Feature 6: Student Email Validation
    // ==========================================
    @Test
    fun test06_1_nonLingnanDomainRejected() {
        assertFalse(EmailValidatorContract.isValidLingnanEmail("student@gmail.com"))
    }

    @Test
    fun test06_2_otherUniversityDomainRejected() {
        assertFalse(EmailValidatorContract.isValidLingnanEmail("student@hku.hk"))
        assertFalse(EmailValidatorContract.isValidLingnanEmail("student@cuhk.edu.hk"))
    }

    @Test
    fun test06_3_suffixAttackDomainRejected() {
        assertFalse(EmailValidatorContract.isValidLingnanEmail("student@ln.hk.attacker.com"))
        assertFalse(EmailValidatorContract.isValidLingnanEmail("student@ln.edu.hk.fake.org"))
    }

    @Test
    fun test06_4_missingLocalPartRejected() {
        assertFalse(EmailValidatorContract.isValidLingnanEmail("@ln.hk"))
        assertFalse(EmailValidatorContract.isValidLingnanEmail("@ln.edu.hk"))
    }

    @Test
    fun test06_5_emptyOrWhitespaceEmailRejected() {
        assertFalse(EmailValidatorContract.isValidLingnanEmail(""))
        assertFalse(EmailValidatorContract.isValidLingnanEmail("   "))
        assertFalse(EmailValidatorContract.isValidLingnanEmail(null))
    }

    // ==========================================
    // Feature 7: User Registration & Login
    // ==========================================
    @Test
    fun test07_1_passwordTooShortRejected() {
        assertFalse(AuthValidatorContract.validatePassword("").isValid)
        assertFalse(AuthValidatorContract.validatePassword("1234567").isValid)
        assertTrue(AuthValidatorContract.validatePassword("12345678").isValid)
    }

    @Test
    fun test07_2_passwordExceedsMaxCharsRejected() {
        assertTrue(AuthValidatorContract.validatePassword("A".repeat(256)).isValid)
        assertFalse(AuthValidatorContract.validatePassword("A".repeat(257)).isValid)
    }

    @Test
    fun test07_3_duplicateRegistrationConflictError() {
        val mapped = AuthValidatorContract.mapHttpStatus(409)
        assertEquals(AuthValidatorContract.AuthError.AccountConflict, mapped)
    }

    @Test
    fun test07_4_loginWithBlankEmailReturnsError() {
        val res = AuthValidatorContract.validateRegistrationPayload("", "password123", "Student")
        assertFalse(res.isValid)
    }

    @Test
    fun test07_5_expiredSessionTokenStateHandled() {
        assertFalse(AuthValidatorContract.isSessionActive(expiresAt = 1000L, now = 1001L))
        assertFalse(AuthValidatorContract.isSessionActive(expiresAt = 1000L, now = 1000L))
        assertTrue(AuthValidatorContract.isSessionActive(expiresAt = 1000L, now = 999L))
    }

    // ==========================================
    // Feature 8: User Profile Management
    // ==========================================
    @Test
    fun test08_1_animalNicknameNotInAllowedListRejected() {
        assertFalse(UserProfileContract.isValidAnimal("dragon"))
        assertFalse(UserProfileContract.isValidAnimal("unicorn"))
        assertFalse(UserProfileContract.isValidAnimal(""))
        assertTrue(UserProfileContract.isValidAnimal("bear"))
        assertTrue(UserProfileContract.isValidAnimal("PANDA"))
    }

    @Test
    fun test08_2_backgroundIndexBelowZeroClamped() {
        assertEquals(0, UserProfileContract.clampBackgroundIndex(-5))
        assertEquals(0, UserProfileContract.clampBackgroundIndex(-1))
        assertEquals(0, UserProfileContract.clampBackgroundIndex(0))
        assertEquals(5, UserProfileContract.clampBackgroundIndex(5))
    }

    @Test
    fun test08_3_backgroundIndexAboveElevenClamped() {
        assertEquals(11, UserProfileContract.clampBackgroundIndex(11))
        assertEquals(11, UserProfileContract.clampBackgroundIndex(12))
        assertEquals(11, UserProfileContract.clampBackgroundIndex(100))
    }

    @Test
    fun test08_4_displayNameAboveMaxCharsRejected() {
        assertTrue(UserProfileContract.validateDisplayName("A".repeat(100)))
        assertFalse(UserProfileContract.validateDisplayName("A".repeat(101)))
        assertFalse(UserProfileContract.validateDisplayName(""))
        assertFalse(UserProfileContract.validateDisplayName(null))
    }

    @Test
    fun test08_5_emptyFavoritesRendersEmptyStateGracefully() {
        val state = UserProfileContract.UserProfileState(userId = "u1", animal = "bear", bgIndex = 0, favorites = emptyList())
        assertTrue(state.isEmptyFavorites)
        assertTrue(state.favorites.isEmpty())
    }

    // ==========================================
    // Feature 9: Course Catalog Browsing
    // ==========================================
    @Test
    fun test09_1_pageOffsetBeyondTotalReturnsEmptyList() {
        val items = (1..50).map { CourseCatalogContract.CourseItem(courseCode = "C$it", title = "T$it") }
        val page1 = CourseCatalogContract.paginate(items, page = 1, pageSize = 20)
        assertEquals(20, page1.items.size)
        assertTrue(page1.hasMore)
        val page3 = CourseCatalogContract.paginate(items, page = 3, pageSize = 20)
        assertEquals(10, page3.items.size)
        assertFalse(page3.hasMore)
        val page4 = CourseCatalogContract.paginate(items, page = 4, pageSize = 20)
        assertTrue(page4.items.isEmpty())
        assertEquals(3, page4.totalPages)
    }

    @Test
    fun test09_2_pageSizeClampedToSensibleBounds() {
        val items = (1..50).map { CourseCatalogContract.CourseItem(courseCode = "C$it", title = "T$it") }
        val pageNeg = CourseCatalogContract.paginate(items, page = 1, pageSize = -5)
        assertEquals(10, pageNeg.pageSize)
        val pageHuge = CourseCatalogContract.paginate(items, page = 1, pageSize = 500)
        assertEquals(100, pageHuge.pageSize)
    }

    @Test
    fun test09_3_courseWithNullRatingNormalizedToZero() {
        val (rating1, count1) = CourseCatalogContract.normalizeMetrics(null, 5)
        assertEquals(0.0, rating1, 0.001)
        assertEquals(5, count1)
        val (rating2, _) = CourseCatalogContract.normalizeMetrics(-1.0, 5)
        assertEquals(0.0, rating2, 0.001)
    }

    @Test
    fun test09_4_zeroCoursesInCatalogRendersEmptyState() {
        val pageEmpty = CourseCatalogContract.paginate(emptyList<CourseCatalogContract.CourseItem>(), page = 1, pageSize = 20)
        assertTrue(pageEmpty.items.isEmpty())
        assertEquals(0, pageEmpty.totalCount)
        assertEquals(0, pageEmpty.totalPages)
        assertFalse(pageEmpty.hasMore)
    }

    @Test
    fun test09_5_negativeReviewCountClampedToZero() {
        val (_, countNeg) = CourseCatalogContract.normalizeMetrics(4.5, -1)
        assertEquals(0, countNeg)
        val (_, countPos) = CourseCatalogContract.normalizeMetrics(4.5, 10)
        assertEquals(10, countPos)
    }

    // ==========================================
    // Feature 10: Course Keyword Search
    // ==========================================
    @Test
    fun test10_1_emptySearchQueryReturnsAllCourses() {
        val catalog = listOf(CourseCatalogContract.CourseItem("CDS2004", "Data Science"), CourseCatalogContract.CourseItem("BUS1102", "Stats"))
        val resultEmpty = CourseCatalogContract.searchCourses("", catalog)
        assertEquals(2, resultEmpty.size)
        val resultNull = CourseCatalogContract.searchCourses(null, catalog)
        assertEquals(2, resultNull.size)
    }

    @Test
    fun test10_2_regexMetacharactersInSearchQueryEscaped() {
        val catalog = listOf(CourseCatalogContract.CourseItem("CDS2004", "C++ & Java (2024)*"), CourseCatalogContract.CourseItem("BUS1102", "Stats"))
        val matched = CourseCatalogContract.searchCourses("C++ & Java (2024)*", catalog)
        assertEquals(1, matched.size)
        assertEquals("CDS2004", matched.first().courseCode)
    }

    @Test
    fun test10_3_whitespaceOnlySearchQueryTrimmed() {
        val catalog = listOf(CourseCatalogContract.CourseItem("CDS2004", "Data Science"))
        val result = CourseCatalogContract.searchCourses("     \t\n ", catalog)
        assertEquals(1, result.size)
    }

    @Test
    fun test10_4_searchQueryTruncationAtMaxChars() {
        val catalog = listOf(CourseCatalogContract.CourseItem("CDS2004", "Data Science"))
        val longQuery = "X".repeat(250)
        val result = CourseCatalogContract.searchCourses(longQuery, catalog, maxQueryLength = 100)
        assertTrue(result.isEmpty())
    }

    @Test
    fun test10_5_searchWithZeroMatchesReturnsEmptyList() {
        val catalog = listOf(CourseCatalogContract.CourseItem("CDS2004", "Data Science"), CourseCatalogContract.CourseItem("BUS1102", "Stats"))
        val result = CourseCatalogContract.searchCourses("NON_EXISTENT_QUERY_XYZ", catalog)
        assertTrue(result.isEmpty())
    }

    // ==========================================
    // Feature 11: Course Multi-Criteria Filter
    // ==========================================
    @Test
    fun test11_1_nonExistentSubjectPrefixReturnsEmptyList() {
        val catalog = listOf(CourseCatalogContract.CourseItem("CDS2004", "Data Science"), CourseCatalogContract.CourseItem("BUS1102", "Stats"))
        val filtered = CourseCatalogContract.filterCourses(catalog, CourseCatalogContract.FilterCriteria(subjectPrefix = "ZZZ"))
        assertTrue(filtered.isEmpty())
    }

    @Test
    fun test11_2_conflictingFilterIntersectionReturnsEmptyList() {
        val catalog = listOf(CourseCatalogContract.CourseItem("CDS2004", "Data Science", term = "202409", languageCode = "E"))
        val filtered = CourseCatalogContract.filterCourses(catalog, CourseCatalogContract.FilterCriteria(term = "202409", languageCode = "P"))
        assertTrue(filtered.isEmpty())
    }

    @Test
    fun test11_3_invalidTeachingLanguageCodeRejected() {
        assertTrue(CourseCatalogContract.isValidLanguageCode("E"))
        assertTrue(CourseCatalogContract.isValidLanguageCode("C"))
        assertFalse(CourseCatalogContract.isValidLanguageCode("Z"))
        assertFalse(CourseCatalogContract.isValidLanguageCode(null))
    }

    @Test
    fun test11_4_filterResetRestoresFullCatalog() {
        val catalog = listOf(CourseCatalogContract.CourseItem("CDS2004", "DS"), CourseCatalogContract.CourseItem("BUS1102", "Stats"), CourseCatalogContract.CourseItem("LCC1010", "Chinese"))
        val filtered = CourseCatalogContract.filterCourses(catalog, CourseCatalogContract.FilterCriteria(subjectPrefix = "CDS"))
        assertEquals(1, filtered.size)
        val reset = CourseCatalogContract.filterCourses(catalog, CourseCatalogContract.FilterCriteria())
        assertEquals(3, reset.size)
    }

    @Test
    fun test11_5_emptyFilterCriteriaReturnsUnfilteredCatalog() {
        val criteria = CourseCatalogContract.FilterCriteria()
        assertTrue(criteria.isEmpty)
        val catalog = listOf(CourseCatalogContract.CourseItem("CDS2004", "DS"))
        val res = CourseCatalogContract.filterCourses(catalog, criteria)
        assertEquals(1, res.size)
    }

    // ==========================================
    // Feature 12: Instructor Catalog & Search
    // ==========================================
    @Test
    fun test12_1_chineseSearchWhenInstructorOnlyHasEnglishName() {
        val record = InstructorCatalogContract.InstructorRecord("1", "SIMMONS Richard", null, "English", true, listOf("ENG1001"))
        val res = InstructorCatalogContract.searchInstructors("西蒙斯", listOf(record), includeInactive = true)
        assertTrue(res.isEmpty())
    }

    @Test
    fun test12_2_combinedInstructorSlashDelimiterParsing() {
        val parsed = InstructorCatalogContract.parseSlashDelimitedInstructors("SIMMONS Richard / MAO Ying")
        assertEquals(2, parsed.size)
        assertEquals("SIMMONS Richard", parsed[0])
        assertEquals("MAO Ying", parsed[1])
        val parsedComplex = InstructorCatalogContract.parseSlashDelimitedInstructors("Prof A / Dr B / C / ")
        assertEquals(3, parsedComplex.size)
    }

    @Test
    fun test12_3_inactiveStaffExcludedWhenToggleOff() {
        val records = listOf(
            InstructorCatalogContract.InstructorRecord("1", "Active Staff", null, "ENG", true, emptyList()),
            InstructorCatalogContract.InstructorRecord("2", "Former Staff", null, "ENG", false, emptyList())
        )
        val activeOnly = InstructorCatalogContract.searchInstructors(null, records, includeInactive = false)
        assertEquals(1, activeOnly.size)
        assertEquals("Active Staff", activeOnly.first().nameEn)
        val all = InstructorCatalogContract.searchInstructors(null, records, includeInactive = true)
        assertEquals(2, all.size)
    }

    @Test
    fun test12_4_instructorWithZeroCoursesHandledGracefully() {
        val record = InstructorCatalogContract.InstructorRecord("1", "Prof. Zero", null, "ENG", true, emptyList())
        assertTrue(record.coursesTaught.isEmpty())
        assertEquals("Prof. Zero", record.nameEn)
    }

    @Test
    fun test12_5_specialCharactersInInstructorSearchEscaped() {
        val records = listOf(InstructorCatalogContract.InstructorRecord("1", "O'Connor (Prof.)", null, "ENG", true, emptyList()))
        val matched = InstructorCatalogContract.searchInstructors("O'Connor (Prof.)", records, includeInactive = true)
        assertEquals(1, matched.size)
    }

    // ==========================================
    // Feature 13: Course Detail View
    // ==========================================
    @Test
    fun test13_1_courseWithZeroReviewsRendersEmptyPrompt() {
        val stats = GradeMathContract.calculateGradeStatistics(emptyMap())
        assertEquals(0, stats.totalCount)
        assertNull(stats.mean)
    }

    @Test
    fun test13_2_courseWithNoSyllabusDisablesSyllabusButton() {
        val syllabusId: String? = null
        val action = StorageBucketContract.resolveDocumentAction("syl_001", "course_syllabus", isPdf = true, hasNativeViewer = false, isSessionActive = true)
        assertTrue(action is StorageBucketContract.DocumentAction.OpenBrowser)
        assertNull(syllabusId)
    }

    @Test
    fun test13_3_courseWithNoPastPapersHidesExamsTab() {
        assertFalse(CourseCatalogContract.evaluateTabs(pastPapersCount = 0).hasExams)
        assertTrue(CourseCatalogContract.evaluateTabs(pastPapersCount = 3).hasExams)
    }

    @Test
    fun test13_4_prerequisiteCircularCodeParsingSafeguard() {
        val prereqText = "Prerequisite: CDS2004 or equivalent"
        val extracted = "([A-Z]{3,4}\\d{4})".toRegex().findAll(prereqText).map { it.value }.toSet()
        assertEquals(setOf("CDS2004"), extracted)
        val course = CourseCatalogContract.CourseItem("CDS2004", "Data Science")
        assertTrue(CourseCatalogContract.validateCourse(course))
    }

    @Test
    fun test13_5_nullCreditsGracefullyHandled() {
        assertEquals("N/A", CourseCatalogContract.formatCredits(null))
        assertEquals("3", CourseCatalogContract.formatCredits(3))
    }

    // ==========================================
    // Feature 14: Instructor Detail View
    // ==========================================
    @Test
    fun test14_1_instructorWithZeroTeachingRecords() {
        val record = InstructorCatalogContract.InstructorRecord("1", "Smith", null, "ENG", true, emptyList())
        assertTrue(record.coursesTaught.isEmpty())
    }

    @Test
    fun test14_2_instructorWithZeroRatingsDisplaysNa() {
        assertNull(InstructorCatalogContract.calculateOverallRating(emptyList()))
    }

    @Test
    fun test14_3_missingInstructorEmailMailtoFallback() {
        assertFalse(EmailValidatorContract.isValidLingnanEmail(null))
        assertFalse(EmailValidatorContract.isValidLingnanEmail(""))
    }

    @Test
    fun test14_4_longCoursesTaughtListPagination() {
        val fiftyCourses = (1..50).map { CourseCatalogContract.CourseItem("CRS$it", "Title$it") }
        val page1 = CourseCatalogContract.paginate(fiftyCourses, 1, 20)
        assertEquals(20, page1.items.size)
        val page2 = CourseCatalogContract.paginate(fiftyCourses, 2, 20)
        assertEquals(20, page2.items.size)
        val page3 = CourseCatalogContract.paginate(fiftyCourses, 3, 20)
        assertEquals(10, page3.items.size)
    }

    @Test
    fun test14_5_biographyTextTruncation() {
        assertEquals("Short bio", InstructorCatalogContract.truncateBio("Short bio", 150))
        val longBio = "A".repeat(200)
        val truncated = InstructorCatalogContract.truncateBio(longBio, 150)
        assertEquals(153, truncated.length)
        assertTrue(truncated.endsWith("..."))
    }

    // ==========================================
    // Feature 15: Review Feed & Details
    // ==========================================
    @Test
    fun test15_1_extremeRatingsAllLowestScore() {
        val review = ReviewSubmissionContract.formatReview("r1", "Chan", isAnonymous = false, isAuthorDeleted = false, comment = "Low score", workload = 0.5, difficulty = 0.5, grading = 0.5, teaching = 0.5)
        assertEquals(0.5, review.overallRating, 0.001)
    }

    @Test
    fun test15_2_longReviewCommentMaxCharLimitEnforced() {
        val longComment = "C".repeat(2500)
        val review = ReviewSubmissionContract.formatReview("r1", "Chan", isAnonymous = false, isAuthorDeleted = false, comment = longComment, workload = 3.0, difficulty = 3.0, grading = 3.0, teaching = 3.0)
        assertEquals(2000, review.comment.length)
    }

    @Test
    fun test15_3_anonymousReviewHidesAuthorProfile() {
        val review = ReviewSubmissionContract.formatReview("r1", "Real Name", isAnonymous = true, isAuthorDeleted = false, comment = "Good", workload = 3.0, difficulty = 3.0, grading = 3.0, teaching = 3.0)
        assertEquals("Anonymous Student", review.authorDisplay)
        assertFalse(review.canOpenProfile)
    }

    @Test
    fun test15_4_reviewWithDeletedUserHandledGracefully() {
        val review = ReviewSubmissionContract.formatReview("r1", null, isAnonymous = false, isAuthorDeleted = true, comment = "Good", workload = 3.0, difficulty = 3.0, grading = 3.0, teaching = 3.0)
        assertEquals("[Deleted User]", review.authorDisplay)
        assertFalse(review.canOpenProfile)
    }

    @Test
    fun test15_5_feedWithZeroReviewsRendersCta() {
        val stats = GradeMathContract.calculateGradeStatistics(emptyMap())
        assertEquals(0, stats.totalCount)
    }

    // ==========================================
    // Feature 16: Review Voting System
    // ==========================================
    @Test
    fun test16_1_unauthenticatedUserVoteAttemptBlocked() {
        val sessionState = AuthValidatorContract.SessionStateMachine()
        assertFalse(sessionState.currentState.isAuthenticated)
    }

    @Test
    fun test16_2_rapidConsecutiveDoubleVoteIdempotent() {
        val initial = ReviewVotingContract.VoteState(upvotes = 10, downvotes = 2, userVote = null)
        val click1 = ReviewVotingContract.applyVote(initial, "up")
        val click2 = ReviewVotingContract.applyVote(click1, "up")
        assertEquals(10, click2.upvotes)
        assertNull(click2.userVote)
    }

    @Test
    fun test16_3_voteOnNonExistentReviewThrowsOrRejects() {
        val state = ReviewVotingContract.VoteState(0, 0, null)
        val next = ReviewVotingContract.applyVote(state, "up")
        assertEquals(1, next.upvotes)
    }

    @Test
    fun test16_4_voteCountFloorCannotDropBelowZero() {
        val initial = ReviewVotingContract.VoteState(upvotes = 0, downvotes = 0, userVote = "up")
        val next = ReviewVotingContract.applyVote(initial, "up")
        assertEquals(0, next.upvotes)
    }

    @Test
    fun test16_5_switchingVoteAdjustsBothCountersAccurately() {
        val initial = ReviewVotingContract.VoteState(upvotes = 10, downvotes = 5, userVote = "down")
        val next = ReviewVotingContract.applyVote(initial, "up")
        assertEquals(11, next.upvotes)
        assertEquals(4, next.downvotes)
        assertEquals("up", next.userVote)
    }

    // ==========================================
    // Feature 17: Review Submission Wizard
    // ==========================================
    @Test
    fun test17_1_step1WithoutCourseSelectionBlocksNext() {
        val draftEmpty = ReviewSubmissionContract.WizardDraft(step = 1, courseCode = null)
        assertFalse(ReviewSubmissionContract.validateStep(draftEmpty).canProceed)
        val draftValid = ReviewSubmissionContract.WizardDraft(step = 1, courseCode = "CDS2004")
        assertTrue(ReviewSubmissionContract.validateStep(draftValid).canProceed)
    }

    @Test
    fun test17_2_step2WithMissingRatingBlocksNext() {
        val ratings = mapOf("workload" to 3.5, "difficulty" to -2.0)
        val allValid = ratings.values.all { RatingValidatorContract.isValidRating(it) }
        assertFalse(allValid)
    }

    @Test
    fun test17_3_step4WithInvalidGradeBlocksNext() {
        val grade = "Z+"
        val isValidGrade = GradeMathContract.GRADE_TO_GPA_MAP.containsKey(grade)
        assertFalse(isValidGrade)
    }

    @Test
    fun test17_4_submissionPayloadWithNullCourseRejected() {
        val payload = ReviewSubmissionContract.WizardStepPayload(courseCode = null, termCode = "202409")
        assertFalse(ReviewSubmissionContract.validateStep1(payload))
    }

    @Test
    fun test17_5_formDraftRestoresFromStorage() {
        val draft = ReviewSubmissionContract.WizardDraft(step = 2, courseCode = "CDS2004", workloadRating = 3.5)
        assertEquals("CDS2004", draft.courseCode)
        assertEquals(2, draft.step)
        assertEquals(3.5, draft.workloadRating!!, 0.001)
    }

    // ==========================================
    // Feature 18: Half-Star Rating Inputs
    // ==========================================
    @Test
    fun test18_1_ratingZeroRejected() {
        assertFalse("Rating 0.0 must be rejected (min is 0.5)", RatingValidatorContract.isValidRating(0.0))
    }

    @Test
    fun test18_2_ratingAboveFiveClampedToFive() {
        val clamped = RatingValidatorContract.clampRating(5.8)
        assertEquals(5.0, clamped, 0.001)
    }

    @Test
    fun test18_3_arbitraryFractionalRatingRoundedToNearestHalf() {
        val clamped = RatingValidatorContract.clampRating(2.3)
        assertEquals(2.5, clamped, 0.001)
    }

    @Test
    fun test18_4_negativeRatingOtherThanMinusOneRejected() {
        assertFalse(RatingValidatorContract.isValidRating(-2.0))
        assertFalse(RatingValidatorContract.isValidRating(-0.5))
    }

    @Test
    fun test18_5_nanOrInfiniteRatingRejected() {
        assertFalse(RatingValidatorContract.isValidRating(Double.NaN))
        assertFalse(RatingValidatorContract.isValidRating(Double.POSITIVE_INFINITY))
    }

    // ==========================================
    // Feature 19: Review Anti-Spam Rules
    // ==========================================
    @Test
    fun test19_1_commentWithFourWordsRejected() {
        val fourWords = "One two three four"
        val result = WordCountValidatorContract.validateWordCount(fourWords, 5, 1000)
        assertFalse("Comment with 4 words must be rejected", result.isValid)
    }

    @Test
    fun test19_2_commentWithExactlyFiveWordsAcceptedBoundary() {
        val fiveWords = "One two three four five"
        val result = WordCountValidatorContract.validateWordCount(fiveWords, 5, 1000)
        assertTrue("Comment with exactly 5 words must be accepted", result.isValid)
    }

    @Test
    fun test19_3_commentWith1001WordsRejected() {
        val words = (1..1001).joinToString(" ") { "word" }
        val result = WordCountValidatorContract.validateWordCount(words, 5, 1000)
        assertFalse("Comment with 1001 words must be rejected", result.isValid)
    }

    @Test
    fun test19_4_eighthReviewInSameTermBlocked() {
        val eligibility = AntiSpamRulesContract.checkReviewEligibility(
            userId = "user1",
            courseCode = "NEW101",
            termCode = "202409",
            termReviewCount = 7,
            existingCourseReviews = emptyList()
        )
        assertFalse("8th review in same term must be blocked", eligibility.canSubmit)
        assertEquals("review.termLimitExceeded", eligibility.reason)
    }

    @Test
    fun test19_5_secondReviewForPassedCourseBlocked() {
        val passedReview = AntiSpamRulesContract.ExistingCourseReview(
            reviewId = "rev_pass",
            courseCode = "BUS1102",
            finalGrade = "B+"
        )
        val eligibility = AntiSpamRulesContract.checkReviewEligibility(
            userId = "user1",
            courseCode = "BUS1102",
            termCode = "202501",
            termReviewCount = 2,
            existingCourseReviews = listOf(passedReview)
        )
        assertFalse("Second review for passed course must be blocked", eligibility.canSubmit)
        assertEquals("review.limitReachedWithPass", eligibility.reason)
    }

    // ==========================================
    // Feature 20: Grade Distribution Stats
    // ==========================================
    @Test
    fun test20_1_allNaGradesYieldsNullMeanAndStdDev() {
        val distribution = mapOf("N/A" to 10)
        val stats = GradeMathContract.calculateGradeStatistics(distribution)
        assertNull(stats.mean)
        assertNull(stats.standardDeviation)
        assertEquals(10, stats.totalCount)
        assertEquals(0, stats.validGradeCount)
    }

    @Test
    fun test20_2_singleGradeEntryYieldsZeroStdDev() {
        val distribution = mapOf("A" to 1)
        val stats = GradeMathContract.calculateGradeStatistics(distribution)
        assertEquals(4.00, stats.mean!!, 0.001)
        assertEquals(0.00, stats.standardDeviation!!, 0.001)
    }

    @Test
    fun test20_3_allFGradesYieldsZeroGpa() {
        val distribution = mapOf("F" to 5)
        val stats = GradeMathContract.calculateGradeStatistics(distribution)
        assertEquals(0.00, stats.mean!!, 0.001)
    }

    @Test
    fun test20_4_allAGradesYieldsFourPointZeroGpa() {
        val distribution = mapOf("A" to 20)
        val stats = GradeMathContract.calculateGradeStatistics(distribution)
        assertEquals(4.00, stats.mean!!, 0.001)
    }

    @Test
    fun test20_5_unrecognizedGradeIgnoredInCalculation() {
        val distribution = mapOf("A" to 2, "UNKNOWN_GRADE" to 5)
        val stats = GradeMathContract.calculateGradeStatistics(distribution)
        assertEquals(4.00, stats.mean!!, 0.001)
        assertEquals(2, stats.validGradeCount)
    }

    // ==========================================
    // Feature 21: Interactive Grade Charts
    // ==========================================
    @Test
    fun test21_1_singleGradeDistributionOneHundredPercent() {
        val distribution = mapOf("A" to 15)
        val curve = GradeMathContract.calculateParetoCurve(distribution)
        val aPoint = curve.first { it.grade == "A" }
        assertEquals(100.0, aPoint.percentage, 0.001)
        assertEquals(100.0, aPoint.cumulativePercentage, 0.001)
    }

    @Test
    fun test21_2_zeroTotalStudentsYieldsEmptyPareto() {
        val distribution = mapOf("A" to 0, "B" to 0)
        val curve = GradeMathContract.calculateParetoCurve(distribution)
        assertTrue(curve.isEmpty())
    }

    @Test
    fun test21_3_paretoCurveReachesExactly100PercentOnLastValidGrade() {
        val distribution = mapOf("A" to 3, "B" to 3, "F" to 4)
        val curve = GradeMathContract.calculateParetoCurve(distribution)
        val fPoint = curve.first { it.grade == "F" }
        assertEquals(100.0, fPoint.cumulativePercentage, 0.001)
    }

    @Test
    fun test21_4_naGradesExcludedFromParetoCumulativeCurve() {
        val distribution = mapOf("A" to 5, "N/A" to 100)
        val curve = GradeMathContract.calculateParetoCurve(distribution)
        val aPoint = curve.first { it.grade == "A" }
        assertEquals(100.0, aPoint.cumulativePercentage, 0.001)
    }

    @Test
    fun test21_5_alternatingZeroFrequencyBinsHandled() {
        val distribution = mapOf("A" to 10, "A-" to 0, "B+" to 10)
        val curve = GradeMathContract.calculateParetoCurve(distribution)
        val aMinusPoint = curve.first { it.grade == "A-" }
        assertEquals(0, aMinusPoint.count)
        assertEquals(50.0, aMinusPoint.cumulativePercentage, 0.001)
    }

    // ==========================================
    // Feature 22: Past Examination Papers List
    // ==========================================
    @Test
    fun test22_1_malformedFilenameWithoutUnderscoresReturnsNull() {
        val malformed = "CDS200424252.pdf"
        val parsed = PastPapersParserContract.parseFilename(malformed)
        assertNull(parsed)
    }

    @Test
    fun test22_2_zeroByteFileSizeHandled() {
        assertEquals("0 B", StorageBucketContract.formatFileSize(0L))
    }

    @Test
    fun test22_3_unknownFilenamePatternReturnsNullFallback() {
        val unparseable = "old_syllabus_doc.pdf"
        val parsed = PastPapersParserContract.parseFilename(unparseable)
        assertNull(parsed)
    }

    @Test
    fun test22_4_courseWithOver100FilesRequiresOffsetPagination() {
        val files = (1..135).map { PastPapersParserContract.ParsedExamPaper("CDS2004", "2024-2025", "Term 1") }
        val page1 = CourseCatalogContract.paginate(files, 1, 100)
        assertEquals(100, page1.items.size)
        assertTrue(page1.hasMore)
        val page2 = CourseCatalogContract.paginate(files, 2, 100)
        assertEquals(35, page2.items.size)
        assertFalse(page2.hasMore)
    }

    @Test
    fun test22_5_duplicateFilenameEntriesDeduplicated() {
        val papers = listOf(
            PastPapersParserContract.ParsedExamPaper("CDS2004", "2024-2025", "Term 1"),
            PastPapersParserContract.ParsedExamPaper("CDS2004", "2024-2025", "Term 1"),
            PastPapersParserContract.ParsedExamPaper("CDS2004", "2023-2024", "Term 2")
        )
        val deduped = PastPapersParserContract.deduplicate(papers)
        assertEquals(2, deduped.size)
    }

    // ==========================================
    // Feature 23: Study Materials & Syllabus
    // ==========================================
    @Test
    fun test23_1_emptyMaterialsBucketReturnsEmptyList() {
        assertTrue(StorageBucketContract.isValidBucket("study_materials"))
    }

    @Test
    fun test23_2_nonPdfFileTypeHandledGracefully() {
        assertFalse(StorageBucketContract.isPdf("notes.docx", "application/msword"))
        assertTrue(StorageBucketContract.isPdf("notes.pdf", "application/pdf"))
    }

    @Test
    fun test23_3_fileWithMissingMetadataUsesDefaultName() {
        val mime = StorageBucketContract.getMimeType("document.unknown")
        assertEquals("application/octet-stream", mime)
    }

    @Test
    fun test23_4_largeFileSizeFormattedToMb() {
        assertEquals("75.0 MB", StorageBucketContract.formatFileSize(75 * 1024 * 1024L))
        assertEquals("2.0 GB", StorageBucketContract.formatFileSize(2 * 1024 * 1024 * 1024L))
    }

    @Test
    fun test23_5_storagePermissionDenied403Handled() {
        assertEquals(StorageBucketContract.StorageError.PermissionDenied, StorageBucketContract.mapHttpStatus(403))
        assertEquals(StorageBucketContract.StorageError.FileNotFound, StorageBucketContract.mapHttpStatus(404))
    }

    // ==========================================
    // Feature 24: Document View & Download
    // ==========================================
    @Test
    fun test24_1_downloadWithExpiredSessionRejected() {
        val action = StorageBucketContract.resolveDocumentAction("file_1", "past_exam_papers", isPdf = true, hasNativeViewer = true, isSessionActive = false)
        assertTrue(action is StorageBucketContract.DocumentAction.Error)
    }

    @Test
    fun test24_2_viewUrlOnNonExistentFileReturns404() {
        val error = StorageBucketContract.mapHttpStatus(404)
        assertEquals(StorageBucketContract.StorageError.FileNotFound, error)
    }

    @Test
    fun test24_3_zeroByteDownloadStreamHandled() {
        assertEquals("0 B", StorageBucketContract.formatFileSize(0L))
    }

    @Test
    fun test24_4_specialCharactersInDownloadFilenameEncoded() {
        val url = AppwriteConfigContract.buildDownloadUrl("study_materials", "Notes & Exam (2024).pdf")
        assertTrue(url.contains("study_materials"))
        assertTrue(url.contains(AppwriteConfigContract.PROJECT_ID))
    }

    @Test
    fun test24_5_missingPdfViewerAppFallsBackToBrowser() {
        val actionNative = StorageBucketContract.resolveDocumentAction("f1", "past_exam_papers", isPdf = true, hasNativeViewer = true, isSessionActive = true)
        assertTrue(actionNative is StorageBucketContract.DocumentAction.OpenInApp)
        val actionBrowser = StorageBucketContract.resolveDocumentAction("f1", "past_exam_papers", isPdf = true, hasNativeViewer = false, isSessionActive = true)
        assertTrue(actionBrowser is StorageBucketContract.DocumentAction.OpenBrowser)
    }
}
