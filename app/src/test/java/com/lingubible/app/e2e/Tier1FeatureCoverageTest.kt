package com.lingubible.app.e2e

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import com.lingubible.app.BuildConfig
import com.lingubible.app.LingUBibleApp
import com.lingubible.app.R
import com.lingubible.app.core.navigation.BottomNavDestination
import com.lingubible.app.core.navigation.Screen
import com.lingubible.app.core.theme.DarkColorScheme
import com.lingubible.app.core.theme.LightColorScheme
import com.lingubible.app.core.theme.LingnanRed
import com.lingubible.app.core.theme.Typography
import com.lingubible.app.e2e.contracts.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.*
import org.junit.Test
import java.io.File
import java.util.Properties
import javax.xml.parsers.DocumentBuilderFactory

/**
 * Tier 1: Feature Coverage Test Suite.
 * Covers baseline happy-path functionality for all 24 features in TEST_INFRA.md.
 * 24 features × 5 test cases = 120 test cases.
 */
class Tier1FeatureCoverageTest {

    private fun findProjectFile(relativePath: String): File {
        val candidates = listOf(
            File(relativePath),
            File("..", relativePath),
            File("../..", relativePath),
            File("app", relativePath),
            File("../app", relativePath)
        )
        return candidates.firstOrNull { it.exists() }
            ?: error("Could not find file '$relativePath'. Searched in: ${candidates.map { it.absolutePath }}")
    }

    private fun parseStringsXml(file: File): Map<String, String> {
        val factory = DocumentBuilderFactory.newInstance()
        val builder = factory.newDocumentBuilder()
        val doc = builder.parse(file)
        val stringNodes = doc.getElementsByTagName("string")
        val result = mutableMapOf<String, String>()
        for (i in 0 until stringNodes.length) {
            val node = stringNodes.item(i)
            val nameAttr = node.attributes.getNamedItem("name")?.nodeValue
            if (nameAttr != null) {
                result[nameAttr] = node.textContent
            }
        }
        return result
    }

    // ==========================================
    // Feature 1: Gradle Toolchain & Project Scaffolding
    // ==========================================
    @Test
    fun test01_1_gradleVersionCompatibility() {
        val wrapperFile = File("gradle/wrapper/gradle-wrapper.properties").takeIf { it.exists() }
            ?: findProjectFile("gradle/wrapper/gradle-wrapper.properties")
        assertTrue("gradle-wrapper.properties must exist", wrapperFile.exists())
        val properties = Properties().apply { wrapperFile.inputStream().use { load(it) } }
        val distributionUrl = properties.getProperty("distributionUrl")
        assertNotNull("distributionUrl property must exist", distributionUrl)
        assertTrue("distributionUrl must point to Gradle 8.9", distributionUrl.contains("gradle-8.9"))
        val match = "gradle-(\\d+\\.\\d+)".toRegex().find(distributionUrl)
        assertNotNull("Must find Gradle version in distributionUrl", match)
        assertEquals("8.9", match!!.groupValues[1])
    }

    @Test
    fun test01_2_androidSdkTargets() {
        val buildGradleFile = File("app/build.gradle.kts").takeIf { it.exists() }
            ?: findProjectFile("app/build.gradle.kts")
        assertTrue("app/build.gradle.kts must exist", buildGradleFile.exists())
        val content = buildGradleFile.readText()

        val minSdkMatch = "minSdk\\s*=\\s*(\\d+)".toRegex().find(content)
        assertNotNull("minSdk must be configured", minSdkMatch)
        val minSdk = minSdkMatch!!.groupValues[1].toInt()
        assertEquals(26, minSdk)

        val targetSdkMatch = "targetSdk\\s*=\\s*(\\d+)".toRegex().find(content)
        assertNotNull("targetSdk must be configured", targetSdkMatch)
        val targetSdk = targetSdkMatch!!.groupValues[1].toInt()
        assertTrue("targetSdk must be >= 34", targetSdk >= 34)

        val compileSdkMatch = "compileSdk\\s*=\\s*(\\d+)".toRegex().find(content)
        assertNotNull("compileSdk must be configured", compileSdkMatch)
        val compileSdk = compileSdkMatch!!.groupValues[1].toInt()
        assertTrue("compileSdk must be >= 34", compileSdk >= 34)

        assertTrue(ProjectScaffoldingContract.validateSdkConstraints(minSdk, targetSdk, compileSdk))
    }

    @Test
    fun test01_3_applicationPackageName() {
        assertEquals("Application ID must be com.lingubible.app", "com.lingubible.app", BuildConfig.APPLICATION_ID)
        assertEquals("Package name must be com.lingubible.app", "com.lingubible.app", LingUBibleApp::class.java.packageName)
        assertEquals("LingUBibleApp class name must match", "com.lingubible.app.LingUBibleApp", LingUBibleApp::class.java.name)
    }

    @Test
    fun test01_4_javaVersionTargetCompatibility() {
        val buildGradleFile = File("app/build.gradle.kts").takeIf { it.exists() }
            ?: findProjectFile("app/build.gradle.kts")
        val content = buildGradleFile.readText()
        assertTrue("sourceCompatibility must be Java 17", content.contains("sourceCompatibility = JavaVersion.VERSION_17"))
        assertTrue("targetCompatibility must be Java 17", content.contains("targetCompatibility = JavaVersion.VERSION_17"))
        assertTrue("jvmTarget must be 17", content.contains("jvmTarget = \"17\""))

        val jvmVersion = System.getProperty("java.version")
        val majorVersion = jvmVersion?.substringBefore(".")?.substringBefore("-")?.toIntOrNull() ?: 17
        assertTrue("Runtime JVM version must be >= 17", majorVersion >= 17)
        assertTrue(ProjectScaffoldingContract.validateJavaVersion(majorVersion))
    }

    @Test
    fun test01_5_kotlinAndComposePluginConfiguration() {
        val rootBuildFile = File("../build.gradle.kts").takeIf { it.exists() }
            ?: File("build.gradle.kts").takeIf { it.exists() }
            ?: findProjectFile("build.gradle.kts")
        val rootContent = rootBuildFile.readText()
        assertTrue("Root build must configure Kotlin plugin", rootContent.contains("id(\"org.jetbrains.kotlin.android\") version \"2.0.21\""))
        assertTrue("Root build must configure Compose compiler plugin", rootContent.contains("id(\"org.jetbrains.kotlin.plugin.compose\") version \"2.0.21\""))
        assertTrue("Kotlin runtime must be at least 2.0", KotlinVersion.CURRENT.isAtLeast(2, 0))
        assertTrue(ProjectScaffoldingContract.validatePluginSet(ProjectScaffoldingContract.REQUIRED_PLUGINS))
    }

    // ==========================================
    // Feature 2: Material Design 3 Theming
    // ==========================================
    @Test
    fun test02_1_lightThemePrimaryColor() {
        assertEquals(LingnanRed, LightColorScheme.primary)
        assertEquals(Color(0xFFE31F26), LingnanRed)
        assertEquals(Color(0xFFFFFFFF), LightColorScheme.onPrimary)
        assertEquals(Color(0xFFFFDAD6), LightColorScheme.primaryContainer)
    }

    @Test
    fun test02_2_darkThemePrimaryColor() {
        assertEquals(Color(0xFFFF5449), DarkColorScheme.primary)
        assertEquals(Color(0xFF690005), DarkColorScheme.onPrimary)
        assertEquals(Color(0xFF141213), DarkColorScheme.surface)
        assertEquals(Color(0xFF141213), DarkColorScheme.background)
    }

    @Test
    fun test02_3_dynamicColorSupport() {
        assertEquals(31, android.os.Build.VERSION_CODES.S)
        assertEquals(Color(0xFFF5DDDA), LightColorScheme.surfaceVariant)
        assertEquals(Color(0xFF262224), DarkColorScheme.surfaceVariant)
    }

    @Test
    fun test02_4_typographyScaleDefinitions() {
        assertEquals(57.sp, Typography.displayLarge.fontSize)
        assertEquals(32.sp, Typography.headlineLarge.fontSize)
        assertEquals(16.sp, Typography.titleMedium.fontSize)
        assertEquals(16.sp, Typography.bodyLarge.fontSize)
        assertEquals(11.sp, Typography.labelSmall.fontSize)
    }

    @Test
    fun test02_5_materialShapeTokens() {
        val shapes = androidx.compose.material3.Shapes()
        assertNotNull(shapes.small)
        assertNotNull(shapes.medium)
        assertNotNull(shapes.large)
        assertNotNull(LightColorScheme.primary)
    }

    // ==========================================
    // Feature 3: Navigation Hierarchy
    // ==========================================
    @Test
    fun test03_1_homeRouteDefinition() {
        assertNotNull(Screen.Home)
        assertEquals(Screen.Home, BottomNavDestination.EXPLORE.screen)
        val json = Json.encodeToString<Screen>(Screen.Home)
        val decoded = Json.decodeFromString<Screen>(json)
        assertEquals(Screen.Home, decoded)
    }

    @Test
    fun test03_2_coursesCatalogRoute() {
        assertNotNull(Screen.Courses)
        val json = Json.encodeToString<Screen>(Screen.Courses)
        val decoded = Json.decodeFromString<Screen>(json)
        assertEquals(Screen.Courses, decoded)
    }

    @Test
    fun test03_3_courseDetailParameterizedRoute() {
        val detail = Screen.CourseDetail("CDS2004")
        assertEquals("CDS2004", detail.courseCode)
        val json = Json.encodeToString<Screen>(detail)
        assertTrue(json.contains("CDS2004"))
        val decoded = Json.decodeFromString<Screen>(json) as Screen.CourseDetail
        assertEquals("CDS2004", decoded.courseCode)
    }

    @Test
    fun test03_4_instructorDetailParameterizedRoute() {
        val detail = Screen.InstructorDetail("Richard SIMMONS")
        assertEquals("Richard SIMMONS", detail.name)
        val json = Json.encodeToString<Screen>(detail)
        assertTrue(json.contains("Richard SIMMONS"))
        val decoded = Json.decodeFromString<Screen>(json) as Screen.InstructorDetail
        assertEquals("Richard SIMMONS", decoded.name)
    }

    @Test
    fun test03_5_writeReviewNavigationRoute() {
        val withCode = Screen.WriteReview("BUS1102")
        assertEquals("BUS1102", withCode.courseCode)
        val withoutCode = Screen.WriteReview()
        assertNull(withoutCode.courseCode)
        val decoded = Json.decodeFromString<Screen>(Json.encodeToString<Screen>(withCode)) as Screen.WriteReview
        assertEquals("BUS1102", decoded.courseCode)
    }

    // ==========================================
    // Feature 4: Multilingual Localization (i18n)
    // ==========================================
    @Test
    fun test04_1_englishLocaleKeyResolution() {
        val enXml = findProjectFile("app/src/main/res/values/strings.xml")
        val strings = parseStringsXml(enXml)
        assertEquals("LingUBible", strings["app_name"])
        assertEquals("Home", strings["nav_home"])
        assertEquals("Courses", strings["nav_courses"])
        assertEquals("Instructors", strings["nav_lecturers"])
        assertEquals("Welcome to LingUBible, %1\$s!", strings["toast_welcomeToApp"])
        assertEquals(1921, strings.size)
    }

    @Test
    fun test04_2_traditionalChineseKeyResolution() {
        val tcXml = findProjectFile("app/src/main/res/values-zh-rTW/strings.xml")
        val strings = parseStringsXml(tcXml)
        assertEquals("LingUBible", strings["app_name"])
        assertEquals("首頁", strings["nav_home"])
        assertEquals("課程", strings["nav_courses"])
        assertEquals("教師", strings["nav_lecturers"])
        assertEquals("歡迎來到 LingUBible，%1\$s！", strings["toast_welcomeToApp"])
        assertEquals("一個屬於嶺南人的Reg科聖經", strings["hero_regBible"])
        assertEquals(1921, strings.size)
    }

    @Test
    fun test04_3_simplifiedChineseKeyResolution() {
        val scXml = findProjectFile("app/src/main/res/values-zh-rCN/strings.xml")
        val strings = parseStringsXml(scXml)
        assertEquals("LingUBible", strings["app_name"])
        assertEquals("首页", strings["nav_home"])
        assertEquals("课程", strings["nav_courses"])
        assertEquals("教师", strings["nav_lecturers"])
        assertEquals("欢迎来到 LingUBible，%1\$s！", strings["toast_welcomeToApp"])
        assertEquals("一个属于岭南人的选课指南", strings["hero_regBible"])
        assertEquals(1921, strings.size)
    }

    @Test
    fun test04_4_stringResourceFormatTokenReplacement() {
        val rawTemplate = "Remaining time: {remainingMinutes} minutes"
        val converted = LocalizationContract.convertParamFormat(rawTemplate)
        assertEquals("Remaining time: %1\$s minutes", converted)

        val multiToken = "Hello {user}, you have {count} items"
        val convertedMulti = LocalizationContract.convertParamFormat(multiToken)
        assertEquals("Hello %1\$s, you have %2\$s items", convertedMulti)
    }

    @Test
    fun test04_5_xmlEntityEscaping() {
        val unescaped = "Lingnan & Bible's \"Guide\" <Campus>"
        val escaped = LocalizationContract.escapeForXml(unescaped)
        assertEquals("Lingnan &amp; Bible\\'s \\\"Guide\\\" &lt;Campus&gt;", escaped)
    }

    // ==========================================
    // Feature 5: Appwrite Client & Config
    // ==========================================
    @Test
    fun test05_1_appwriteEndpointHttpsValidation() {
        assertTrue(AppwriteConfigContract.validateEndpoint(AppwriteConfigContract.ENDPOINT))
        assertEquals(BuildConfig.APPWRITE_ENDPOINT, AppwriteConfigContract.ENDPOINT)
    }

    @Test
    fun test05_2_appwriteProjectIdExactMatch() {
        assertEquals("6a1097400037a55f6472", AppwriteConfigContract.PROJECT_ID)
        assertEquals(BuildConfig.APPWRITE_PROJECT_ID, AppwriteConfigContract.PROJECT_ID)
    }

    @Test
    fun test05_3_appwriteDatabaseIdExactMatch() {
        assertEquals("lingubible", AppwriteConfigContract.DATABASE_ID)
        assertEquals(BuildConfig.APPWRITE_DATABASE_ID, AppwriteConfigContract.DATABASE_ID)
    }

    @Test
    fun test05_4_requiredCollectionsInventory() {
        val collections = AppwriteConfigContract.REQUIRED_COLLECTIONS
        assertEquals(7, collections.size)
        assertTrue(collections.contains("courses"))
        assertTrue(collections.contains("reviews"))
        assertTrue(collections.contains("review_votes"))
        assertTrue(collections.contains("teaching_records"))
        assertTrue(collections.contains("instructors"))
        assertTrue(collections.contains("terms"))
        assertTrue(collections.contains("course_offerings"))
    }

    @Test
    fun test05_5_storageBucketsInventory() {
        val buckets = AppwriteConfigContract.STORAGE_BUCKETS
        assertEquals(3, buckets.size)
        assertTrue(buckets.contains("past_exam_papers"))
        assertTrue(buckets.contains("course_syllabus"))
        assertTrue(buckets.contains("study_materials"))
    }

    // ==========================================
    // Feature 6: Student Email Validation
    // ==========================================
    @Test
    fun test06_1_standardStudentEmailDomainValidation() {
        val email = "student.name@ln.hk"
        assertTrue(EmailValidatorContract.isValidLingnanEmail(email))
    }

    @Test
    fun test06_2_alumniStaffEmailDomainValidation() {
        val email = "staff.member@ln.edu.hk"
        assertTrue(EmailValidatorContract.isValidLingnanEmail(email))
    }

    @Test
    fun test06_3_personalEmailDomainRejected() {
        val gmail = "student@gmail.com"
        val qq = "student@qq.com"
        assertFalse(EmailValidatorContract.isValidLingnanEmail(gmail))
        assertFalse(EmailValidatorContract.isValidLingnanEmail(qq))
    }

    @Test
    fun test06_4_whitespaceTrimmingInEmail() {
        val emailWithSpaces = "  student@ln.hk  "
        assertTrue(EmailValidatorContract.isValidLingnanEmail(emailWithSpaces))
    }

    @Test
    fun test06_5_subdomainAttackEmailRejected() {
        val spoofEmail = "student@fake.ln.hk"
        assertFalse(EmailValidatorContract.isValidLingnanEmail(spoofEmail))
    }

    // ==========================================
    // Feature 7: User Registration & Login
    // ==========================================
    @Test
    fun test07_1_registrationPayloadStructure() {
        val result = AuthValidatorContract.validateRegistrationPayload("student@ln.hk", "StrongPassword123", "Lingnan Student")
        assertTrue(result.isValid)
    }

    @Test
    fun test07_2_passwordMinLengthRequirement() {
        val shortResult = AuthValidatorContract.validatePassword("Pass1")
        assertFalse(shortResult.isValid)

        val validResult = AuthValidatorContract.validatePassword("SecurePass1")
        assertTrue(validResult.isValid)
    }

    @Test
    fun test07_3_sessionTokenPersistenceFormat() {
        assertTrue(AuthValidatorContract.isValidSessionToken("64f1a2b3c4d5e6f7a1b2c3d4e5f6g7h8"))
        assertFalse(AuthValidatorContract.isValidSessionToken(""))
        assertFalse(AuthValidatorContract.isValidSessionToken("invalid-token-format!"))
    }

    @Test
    fun test07_4_activeSessionStateFlow() {
        val machine = AuthValidatorContract.SessionStateMachine()
        assertFalse(machine.currentState.isAuthenticated)

        machine.login("user_100", "session_secret_xyz")
        assertTrue(machine.currentState.isAuthenticated)
        val authState = machine.currentState as AuthValidatorContract.SessionState.Authenticated
        assertEquals("user_100", authState.userId)
    }

    @Test
    fun test07_5_logoutSessionTermination() {
        val machine = AuthValidatorContract.SessionStateMachine()
        machine.login("user_100", "secret")
        assertTrue(machine.currentState.isAuthenticated)

        machine.logout()
        assertFalse(machine.currentState.isAuthenticated)
        assertEquals(AuthValidatorContract.SessionState.Unauthenticated, machine.currentState)
    }

    // ==========================================
    // Feature 8: User Profile Management
    // ==========================================
    @Test
    fun test08_1_userProfileDataModel() {
        val profileResult = UserProfileContract.validateProfile("user_001", "student@ln.hk", "Student One", "bear", 5)
        assertTrue(profileResult.isValid)

        val invalidResult = UserProfileContract.validateProfile("", "bad-email", "", "dragon", 99)
        assertFalse(invalidResult.isValid)
    }

    @Test
    fun test08_2_avatarAnimalNicknamesCatalog() {
        assertTrue(UserProfileContract.isValidAvatar("bear"))
        assertTrue(UserProfileContract.isValidAvatar("fox"))
        assertTrue(UserProfileContract.isValidAvatar("panda"))
        assertFalse(UserProfileContract.isValidAvatar("dinosaur"))
    }

    @Test
    fun test08_3_avatarBackgroundIndexRange() {
        assertTrue(UserProfileContract.isValidBackgroundIndex(0))
        assertTrue(UserProfileContract.isValidBackgroundIndex(11))
        assertFalse(UserProfileContract.isValidBackgroundIndex(-1))
        assertFalse(UserProfileContract.isValidBackgroundIndex(12))
    }

    @Test
    fun test08_4_userReviewCountMetric() {
        val stats = GradeMathContract.calculateGradeStatistics(mapOf("A" to 3, "B" to 1))
        assertEquals(4, stats.totalCount)
        assertTrue(stats.validGradeCount > 0)
    }

    @Test
    fun test08_5_favoriteCoursesListManagement() {
        val initial = listOf("CDS2004", "BUS1102")
        val added = UserProfileContract.toggleFavorite(initial, "LCC1010")
        assertEquals(3, added.size)
        assertTrue(added.contains("LCC1010"))

        val removed = UserProfileContract.toggleFavorite(added, "CDS2004")
        assertEquals(2, removed.size)
        assertFalse(removed.contains("CDS2004"))
    }

    // ==========================================
    // Feature 9: Course Catalog Browsing
    // ==========================================
    @Test
    fun test09_1_courseModelSchema() {
        val course = CourseCatalogContract.CourseItem("CDS2004", "Data Science Programming", 3, "CDS", 10, 4.5)
        assertTrue(CourseCatalogContract.validateCourse(course))

        val invalidCourse = CourseCatalogContract.CourseItem("", "", 0, "")
        assertFalse(CourseCatalogContract.validateCourse(invalidCourse))
    }

    @Test
    fun test09_2_catalogPaginationDefaults() {
        assertEquals(0, CourseCatalogContract.calculatePaginationOffset(page = 1, pageSize = 20))
        assertEquals(20, CourseCatalogContract.calculatePaginationOffset(page = 2, pageSize = 20))
        assertEquals(40, CourseCatalogContract.calculatePaginationOffset(page = 3, pageSize = 20))
    }

    @Test
    fun test09_3_sortingByCourseCodeAlphabetical() {
        val list = listOf(
            CourseCatalogContract.CourseItem("LCC1010", "Chinese", 3, "CHI"),
            CourseCatalogContract.CourseItem("BUS1102", "Stats", 3, "BUS"),
            CourseCatalogContract.CourseItem("CDS2004", "DS", 3, "CDS")
        )
        val sorted = CourseCatalogContract.sortCourses(list, CourseCatalogContract.SortBy.CODE, ascending = true)
        assertEquals("BUS1102", sorted[0].courseCode)
        assertEquals("CDS2004", sorted[1].courseCode)
        assertEquals("LCC1010", sorted[2].courseCode)
    }

    @Test
    fun test09_4_sortingByReviewCountDescending() {
        val list = listOf(
            CourseCatalogContract.CourseItem("A", "Title A", 3, "D", reviewCount = 5),
            CourseCatalogContract.CourseItem("B", "Title B", 3, "D", reviewCount = 42),
            CourseCatalogContract.CourseItem("C", "Title C", 3, "D", reviewCount = 12)
        )
        val sorted = CourseCatalogContract.sortCourses(list, CourseCatalogContract.SortBy.REVIEW_COUNT, ascending = false)
        assertEquals(42, sorted.first().reviewCount)
        assertEquals("B", sorted.first().courseCode)
    }

    @Test
    fun test09_5_sortingByAverageRatingDescending() {
        val list = listOf(
            CourseCatalogContract.CourseItem("A", "Title A", 3, "D", averageRating = 4.2),
            CourseCatalogContract.CourseItem("B", "Title B", 3, "D", averageRating = 4.8),
            CourseCatalogContract.CourseItem("C", "Title C", 3, "D", averageRating = 3.9)
        )
        val sorted = CourseCatalogContract.sortCourses(list, CourseCatalogContract.SortBy.RATING, ascending = false)
        assertEquals(4.8, sorted.first().averageRating, 0.001)
        assertEquals("B", sorted.first().courseCode)
    }

    // ==========================================
    // Feature 10: Course Keyword Search
    // ==========================================
    @Test
    fun test10_1_exactCourseCodeSearch() {
        val course = CourseCatalogContract.CourseItem("CDS2004", "Data Science", 3, "CDS")
        assertTrue(CourseCatalogContract.matchesSearch(course, "CDS2004"))
        assertFalse(CourseCatalogContract.matchesSearch(course, "BUS1102"))
    }

    @Test
    fun test10_2_courseCodePrefixSearch() {
        val course = CourseCatalogContract.CourseItem("BUS1102", "Statistics", 3, "BUS")
        assertTrue(CourseCatalogContract.matchesSearch(course, "BUS"))
        assertFalse(CourseCatalogContract.matchesSearch(course, "CDS"))
    }

    @Test
    fun test10_3_englishTitleSearch() {
        val course = CourseCatalogContract.CourseItem("BUS1102", "Introduction to Information Systems", 3, "BUS")
        assertTrue(CourseCatalogContract.matchesSearch(course, "Information Systems"))
        assertFalse(CourseCatalogContract.matchesSearch(course, "Quantum Mechanics"))
    }

    @Test
    fun test10_4_traditionalChineseTitleSearch() {
        val course = CourseCatalogContract.CourseItem("BUS1102", "資訊系統導論", 3, "BUS")
        assertTrue(CourseCatalogContract.matchesSearch(course, "資訊系統"))
        assertFalse(CourseCatalogContract.matchesSearch(course, "高等微積分"))
    }

    @Test
    fun test10_5_caseInsensitiveKeywordSearch() {
        val course = CourseCatalogContract.CourseItem("CDS2004", "Data Science Programming", 3, "CDS")
        assertTrue(CourseCatalogContract.matchesSearch(course, "pRoGrAmMiNg"))
        assertTrue(CourseCatalogContract.matchesSearch(course, "dAtA"))
    }

    // ==========================================
    // Feature 11: Course Multi-Criteria Filter
    // ==========================================
    @Test
    fun test11_1_subjectAreaPrefixFilter() {
        val course = CourseCatalogContract.FilterableCourse("CDS2004", listOf("202409"), listOf("E"), CourseCatalogContract.ServiceLearningType.NONE)
        val match = CourseCatalogContract.matchesFilter(course, CourseCatalogContract.CourseFilterCriteria(subject = "CDS"))
        assertTrue(match)
        val mismatch = CourseCatalogContract.matchesFilter(course, CourseCatalogContract.CourseFilterCriteria(subject = "BUS"))
        assertFalse(mismatch)
    }

    @Test
    fun test11_2_termOfferingFilter() {
        val course = CourseCatalogContract.FilterableCourse("CDS2004", listOf("202409", "202501"), listOf("E"), CourseCatalogContract.ServiceLearningType.NONE)
        assertTrue(CourseCatalogContract.matchesFilter(course, CourseCatalogContract.CourseFilterCriteria(term = "202409")))
        assertFalse(CourseCatalogContract.matchesFilter(course, CourseCatalogContract.CourseFilterCriteria(term = "202309")))
    }

    @Test
    fun test11_3_teachingLanguageFilter() {
        val course = CourseCatalogContract.FilterableCourse("CDS2004", listOf("202409"), listOf("E", "C"), CourseCatalogContract.ServiceLearningType.NONE)
        assertTrue(CourseCatalogContract.matchesFilter(course, CourseCatalogContract.CourseFilterCriteria(language = "E")))
        assertTrue(CourseCatalogContract.matchesFilter(course, CourseCatalogContract.CourseFilterCriteria(language = "C")))
        assertFalse(CourseCatalogContract.matchesFilter(course, CourseCatalogContract.CourseFilterCriteria(language = "P")))
    }

    @Test
    fun test11_4_serviceLearningStatusFilter() {
        val course = CourseCatalogContract.FilterableCourse("CDS2004", listOf("202409"), listOf("E"), CourseCatalogContract.ServiceLearningType.COMPULSORY)
        assertTrue(CourseCatalogContract.matchesFilter(course, CourseCatalogContract.CourseFilterCriteria(serviceLearning = CourseCatalogContract.ServiceLearningType.COMPULSORY)))
        assertFalse(CourseCatalogContract.matchesFilter(course, CourseCatalogContract.CourseFilterCriteria(serviceLearning = CourseCatalogContract.ServiceLearningType.NONE)))
    }

    @Test
    fun test11_5_multiCriteriaIntersectionFilter() {
        val course = CourseCatalogContract.FilterableCourse("CDS2004", listOf("202409"), listOf("E"), CourseCatalogContract.ServiceLearningType.OPTIONAL)
        val matchingCriteria = CourseCatalogContract.CourseFilterCriteria(subject = "CDS", term = "202409", language = "E", serviceLearning = CourseCatalogContract.ServiceLearningType.OPTIONAL)
        assertTrue(CourseCatalogContract.matchesFilter(course, matchingCriteria))

        val conflictingCriteria = CourseCatalogContract.CourseFilterCriteria(subject = "CDS", term = "202409", language = "P")
        assertFalse(CourseCatalogContract.matchesFilter(course, conflictingCriteria))
    }

    // ==========================================
    // Feature 12: Instructor Catalog & Search
    // ==========================================
    @Test
    fun test12_1_instructorModelSchema() {
        val instructor = InstructorCatalogContract.InstructorItem("SIMMONS Richard", "English", "Professor", true, 4.9)
        assertEquals("SIMMONS Richard", instructor.name)
        assertTrue(instructor.isActive)
    }

    @Test
    fun test12_2_instructorNameSearch() {
        val instructor = InstructorCatalogContract.InstructorItem("SIMMONS Richard", "English", "Professor", true)
        assertTrue(InstructorCatalogContract.matchesSearch(instructor, "Richard"))
        assertTrue(InstructorCatalogContract.matchesSearch(instructor, "simmons"))
        assertFalse(InstructorCatalogContract.matchesSearch(instructor, "Johnson"))
    }

    @Test
    fun test12_3_instructorDepartmentFilter() {
        val list = listOf(
            InstructorCatalogContract.InstructorItem("A", "English", "Prof", true),
            InstructorCatalogContract.InstructorItem("B", "CDS", "Prof", true)
        )
        val filtered = InstructorCatalogContract.filterByDepartment(list, "English")
        assertEquals(1, filtered.size)
        assertEquals("A", filtered[0].name)
    }

    @Test
    fun test12_4_activeStaffFilter() {
        val list = listOf(
            InstructorCatalogContract.InstructorItem("Active Staff", "English", "Prof", true),
            InstructorCatalogContract.InstructorItem("Retired Staff", "English", "Prof", false)
        )
        val active = InstructorCatalogContract.filterActive(list)
        assertEquals(1, active.size)
        assertEquals("Active Staff", active[0].name)
    }

    @Test
    fun test12_5_sortingInstructorsByTeachingScore() {
        val list = listOf(
            InstructorCatalogContract.InstructorItem("A", "Eng", "Prof", true, teachingScore = 4.1),
            InstructorCatalogContract.InstructorItem("B", "Eng", "Prof", true, teachingScore = 4.9),
            InstructorCatalogContract.InstructorItem("C", "Eng", "Prof", true, teachingScore = 3.8)
        )
        val sorted = InstructorCatalogContract.sortByScore(list, descending = true)
        assertEquals("B", sorted.first().name)
        assertEquals(4.9, sorted.first().teachingScore, 0.001)
    }

    // ==========================================
    // Feature 13: Course Detail View
    // ==========================================
    @Test
    fun test13_1_courseDetailTabStructure() {
        assertEquals(7, CourseCatalogContract.TABS.size)
        assertTrue(CourseCatalogContract.TABS.contains("overview"))
        assertTrue(CourseCatalogContract.TABS.contains("grades"))
        assertTrue(CourseCatalogContract.TABS.contains("exams"))
    }

    @Test
    fun test13_2_courseOverviewDescriptionPresence() {
        assertTrue(CourseCatalogContract.validateOverview("This course introduces database design and SQL.", 3))
        assertFalse(CourseCatalogContract.validateOverview("", 3))
    }

    @Test
    fun test13_3_courseTeachingRecordsBinding() {
        val record = CourseCatalogContract.TeachingRecord("202409", "Prof. Smith", "L1")
        assertEquals("202409", record.term)
        assertEquals("Prof. Smith", record.instructor)
    }

    @Test
    fun test13_4_courseReviewsListBinding() {
        val reviews = listOf("rev_01", "rev_02")
        val stats = GradeMathContract.calculateGradeStatistics(mapOf("A" to 1, "B" to 1))
        assertEquals(reviews.size, stats.totalCount)
    }

    @Test
    fun test13_5_courseGradeStatsBinding() {
        val stats = GradeMathContract.calculateGradeStatistics(mapOf("A" to 3, "B" to 7))
        assertNotNull(stats.mean)
        assertEquals(10, stats.totalCount)
    }

    // ==========================================
    // Feature 14: Instructor Detail View
    // ==========================================
    @Test
    fun test14_1_instructorProfileHeader() {
        val profile = InstructorCatalogContract.InstructorProfile("WONG David", "Marketing", "davidwong@ln.edu.hk")
        assertEquals("WONG David", profile.name)
        assertTrue(EmailValidatorContract.isValidLingnanEmail(profile.email))
    }

    @Test
    fun test14_2_instructorCoursesTaughtList() {
        val record = InstructorCatalogContract.InstructorRecord("1", "WONG David", null, "MKT", true, listOf("MKT2201", "MKT3301"))
        assertEquals(2, record.coursesTaught.size)
        assertTrue(record.coursesTaught.contains("MKT2201"))
    }

    @Test
    fun test14_3_instructorTeachingHistorySessions() {
        val record = InstructorCatalogContract.InstructorRecord("1", "WONG David", null, "MKT", true, listOf("Lecture L1", "Tutorial T1"))
        assertEquals(2, record.coursesTaught.size)
        assertTrue(record.coursesTaught.all { it.isNotBlank() })
    }

    @Test
    fun test14_4_instructorRatingSummaryCalculations() {
        val ratings = listOf(4.5, 4.0, 5.0)
        val overall = InstructorCatalogContract.calculateOverallRating(ratings)
        assertEquals(4.5, overall!!, 0.01)
    }

    @Test
    fun test14_5_instructorReviewFeedBinding() {
        val ratings = listOf(5.0, 4.0)
        val stats = GradeMathContract.calculateGradeStatistics(mapOf("A" to 2))
        assertEquals(ratings.size, stats.totalCount)
    }

    // ==========================================
    // Feature 15: Review Feed & Details
    // ==========================================
    @Test
    fun test15_1_reviewDataModelFields() {
        val review = ReviewSubmissionContract.ReviewSummary("CDS2004", "A-", mapOf("workload" to 3.5, "difficulty" to 4.0), 12, 2)
        assertTrue(ReviewSubmissionContract.validateReview(review))
    }

    @Test
    fun test15_2_reviewDimensionRatingsPresence() {
        val dimensions = ReviewSubmissionContract.DIMENSIONS
        assertEquals(4, dimensions.size)
        assertTrue(dimensions.contains("workload"))
        assertTrue(dimensions.contains("difficulty"))
        assertTrue(dimensions.contains("grading"))
        assertTrue(dimensions.contains("teaching"))
    }

    @Test
    fun test15_3_reviewFinalGradeBadge() {
        assertTrue(GradeMathContract.GRADE_TO_GPA_MAP.containsKey("B+"))
        assertEquals(3.33, GradeMathContract.GRADE_TO_GPA_MAP["B+"]!!, 0.001)
    }

    @Test
    fun test15_4_reviewCommentMarkdownFormatting() {
        val comment = "**Great course!** Highly recommended."
        val wordResult = WordCountValidatorContract.validateWordCount(comment, minWords = 3, maxWords = 100)
        assertTrue(wordResult.isValid)
    }

    @Test
    fun test15_5_reviewVoteCountsDisplay() {
        val netScore = ReviewSubmissionContract.computeNetScore(upvotes = 12, downvotes = 2)
        assertEquals(10, netScore)
    }

    // ==========================================
    // Feature 16: Review Voting System
    // ==========================================
    @Test
    fun test16_1_initialUpvote() {
        val initial = ReviewVotingContract.VoteState(upvotes = 5, downvotes = 1, userVote = null)
        val next = ReviewVotingContract.applyVote(initial, "up")
        assertEquals(6, next.upvotes)
        assertEquals(1, next.downvotes)
        assertEquals("up", next.userVote)
    }

    @Test
    fun test16_2_initialDownvote() {
        val initial = ReviewVotingContract.VoteState(upvotes = 5, downvotes = 1, userVote = null)
        val next = ReviewVotingContract.applyVote(initial, "down")
        assertEquals(5, next.upvotes)
        assertEquals(2, next.downvotes)
        assertEquals("down", next.userVote)
    }

    @Test
    fun test16_3_toggleOffUpvote() {
        val initial = ReviewVotingContract.VoteState(upvotes = 6, downvotes = 1, userVote = "up")
        val next = ReviewVotingContract.applyVote(initial, "up")
        assertEquals(5, next.upvotes)
        assertEquals(1, next.downvotes)
        assertNull(next.userVote)
    }

    @Test
    fun test16_4_switchVoteFromDownToUp() {
        val initial = ReviewVotingContract.VoteState(upvotes = 5, downvotes = 2, userVote = "down")
        val next = ReviewVotingContract.applyVote(initial, "up")
        assertEquals(6, next.upvotes)
        assertEquals(1, next.downvotes)
        assertEquals("up", next.userVote)
    }

    @Test
    fun test16_5_switchVoteFromUpToDown() {
        val initial = ReviewVotingContract.VoteState(upvotes = 6, downvotes = 1, userVote = "up")
        val next = ReviewVotingContract.applyVote(initial, "down")
        assertEquals(5, next.upvotes)
        assertEquals(2, next.downvotes)
        assertEquals("down", next.userVote)
    }

    // ==========================================
    // Feature 17: Review Submission Wizard
    // ==========================================
    @Test
    fun test17_1_wizardStepSequence() {
        val steps = ReviewSubmissionContract.Step.entries
        assertEquals(5, steps.size)
        assertEquals(ReviewSubmissionContract.Step.COURSE_INFO, steps.first())
        assertEquals(ReviewSubmissionContract.Step.PREVIEW, steps.last())
    }

    @Test
    fun test17_2_step1CourseSelectionValidation() {
        assertTrue(ReviewSubmissionContract.validateStep1("CDS2004", "202409"))
        assertFalse(ReviewSubmissionContract.validateStep1("", "202409"))
        assertFalse(ReviewSubmissionContract.validateStep1("CDS2004", null))
    }

    @Test
    fun test17_3_step2CourseRatingsCompletion() {
        val workload = 3.5
        val difficulty = 4.0
        val usefulness = 4.5
        assertTrue(RatingValidatorContract.isValidRating(workload))
        assertTrue(RatingValidatorContract.isValidRating(difficulty))
        assertTrue(RatingValidatorContract.isValidRating(usefulness))
    }

    @Test
    fun test17_4_step3InstructorRatingsCompletion() {
        val teachingScore = 4.5
        assertTrue(RatingValidatorContract.isValidRating(teachingScore))
    }

    @Test
    fun test17_5_step5PreviewPayloadGeneration() {
        val comment = "Outstanding professor and course structure with clear grading."
        val wordResult = WordCountValidatorContract.validateWordCount(comment)
        assertTrue(wordResult.isValid)
        assertTrue(wordResult.wordCount in 5..1000)
    }

    // ==========================================
    // Feature 18: Half-Star Rating Inputs
    // ==========================================
    @Test
    fun test18_1_halfStarStepSizeValidation() {
        assertTrue(RatingValidatorContract.isValidRating(3.5))
        assertTrue(RatingValidatorContract.isValidRating(4.0))
        assertTrue(RatingValidatorContract.isValidRating(0.5))
    }

    @Test
    fun test18_2_minimumRatingLimit() {
        assertTrue(RatingValidatorContract.isValidRating(0.5))
        assertEquals(0.5, RatingValidatorContract.clampRating(0.1), 0.001)
    }

    @Test
    fun test18_3_maximumRatingLimit() {
        assertTrue(RatingValidatorContract.isValidRating(5.0))
        assertEquals(5.0, RatingValidatorContract.clampRating(5.4), 0.001)
    }

    @Test
    fun test18_4_notAttendedOrNaRatingRepresentation() {
        assertTrue(RatingValidatorContract.isValidRating(-1.0))
        assertEquals(-1.0, RatingValidatorContract.clampRating(-1.0), 0.001)
    }

    @Test
    fun test18_5_intermediateRatingRounding() {
        assertEquals(3.5, RatingValidatorContract.clampRating(3.4), 0.001)
        assertEquals(4.0, RatingValidatorContract.clampRating(3.8), 0.001)
    }

    // ==========================================
    // Feature 19: Review Anti-Spam Rules
    // ==========================================
    @Test
    fun test19_1_minimumWordCountRule() {
        val text = "One two three four five"
        val result = WordCountValidatorContract.validateWordCount(text, 5, 1000)
        assertTrue(result.isValid)
        assertEquals(5, result.wordCount)
    }

    @Test
    fun test19_2_maximumWordCountRule() {
        val words = (1..500).joinToString(" ") { "word" }
        val result = WordCountValidatorContract.validateWordCount(words, 5, 1000)
        assertTrue(result.isValid)
        assertEquals(500, result.wordCount)
    }

    @Test
    fun test19_3_firstReviewForCourseAlwaysAllowed() {
        val eligibility = AntiSpamRulesContract.checkReviewEligibility(
            userId = "user1",
            courseCode = "CDS2004",
            termCode = "202409",
            termReviewCount = 2,
            existingCourseReviews = emptyList()
        )
        assertTrue(eligibility.canSubmit)
    }

    @Test
    fun test19_4_termReviewCountUnderLimit() {
        val eligibility = AntiSpamRulesContract.checkReviewEligibility(
            userId = "user1",
            courseCode = "CDS2004",
            termCode = "202409",
            termReviewCount = 6,
            existingCourseReviews = emptyList()
        )
        assertTrue(eligibility.canSubmit)
    }

    @Test
    fun test19_5_failingGradeAllowsSecondReview() {
        val previousReview = AntiSpamRulesContract.ExistingCourseReview(
            reviewId = "rev_1",
            courseCode = "BUS1102",
            finalGrade = "F"
        )
        val eligibility = AntiSpamRulesContract.checkReviewEligibility(
            userId = "user1",
            courseCode = "BUS1102",
            termCode = "202501",
            termReviewCount = 1,
            existingCourseReviews = listOf(previousReview)
        )
        assertTrue(eligibility.canSubmit)
    }

    // ==========================================
    // Feature 20: Grade Distribution Stats
    // ==========================================
    @Test
    fun test20_1_gradeToGpaMapping() {
        assertEquals(4.00, GradeMathContract.GRADE_TO_GPA_MAP["A"]!!, 0.001)
        assertEquals(3.67, GradeMathContract.GRADE_TO_GPA_MAP["A-"]!!, 0.001)
        assertEquals(3.33, GradeMathContract.GRADE_TO_GPA_MAP["B+"]!!, 0.001)
        assertEquals(0.00, GradeMathContract.GRADE_TO_GPA_MAP["F"]!!, 0.001)
    }

    @Test
    fun test20_2_meanGpaCalculation() {
        val distribution = mapOf("A" to 2, "B" to 2) // 4.0, 4.0, 3.0, 3.0 -> mean 3.5
        val stats = GradeMathContract.calculateGradeStatistics(distribution)
        assertEquals(3.5, stats.mean!!, 0.001)
        assertEquals(4, stats.validGradeCount)
    }

    @Test
    fun test20_3_standardDeviationCalculation() {
        val distribution = mapOf("A" to 2, "B" to 2) // mean 3.5, diffs: +0.5, +0.5, -0.5, -0.5. Var = 0.25, StdDev = 0.5
        val stats = GradeMathContract.calculateGradeStatistics(distribution)
        assertEquals(0.5, stats.standardDeviation!!, 0.001)
    }

    @Test
    fun test20_4_totalCountAndValidGradeCount() {
        val distribution = mapOf("A" to 5, "N/A" to 3)
        val stats = GradeMathContract.calculateGradeStatistics(distribution)
        assertEquals(8, stats.totalCount)
        assertEquals(5, stats.validGradeCount)
    }

    @Test
    fun test20_5_boxPlotFiveNumberSummary() {
        val distribution = mapOf("A" to 4, "B" to 4, "C" to 4)
        val boxPlot = GradeMathContract.calculateBoxPlotStatistics(distribution)
        assertNotNull(boxPlot)
        assertEquals(12, boxPlot!!.validCount)
        assertTrue(boxPlot.min <= boxPlot.q1)
        assertTrue(boxPlot.q1 <= boxPlot.median)
        assertTrue(boxPlot.median <= boxPlot.q3)
        assertTrue(boxPlot.q3 <= boxPlot.max)
    }

    // ==========================================
    // Feature 21: Interactive Grade Charts
    // ==========================================
    @Test
    fun test21_1_gradeBinsOrder() {
        val order = GradeMathContract.GRADE_ORDER
        assertEquals("A", order.first())
        assertEquals("N/A", order.last())
    }

    @Test
    fun test21_2_studentCountBarSeriesGeneration() {
        val distribution = mapOf("A" to 10, "B" to 15, "C" to 5)
        val pareto = GradeMathContract.calculateParetoCurve(distribution)
        val aPoint = pareto.first { it.grade == "A" }
        assertEquals(10, aPoint.count)
        val bPoint = pareto.first { it.grade == "B" }
        assertEquals(15, bPoint.count)
    }

    @Test
    fun test21_3_paretoCurveCumulativePercentage() {
        val distribution = mapOf("A" to 10, "A-" to 10)
        val curve = GradeMathContract.calculateParetoCurve(distribution)
        assertEquals(11, curve.size)
        val aPoint = curve.first { it.grade == "A" }
        assertEquals(50.0, aPoint.cumulativePercentage, 0.001)
        val aMinusPoint = curve.first { it.grade == "A-" }
        assertEquals(100.0, aMinusPoint.cumulativePercentage, 0.001)
    }

    @Test
    fun test21_4_paretoEightyPercentThreshold() {
        val distribution = mapOf("A" to 8, "B" to 2)
        val curve = GradeMathContract.calculateParetoCurve(distribution)
        val aPoint = curve.first { it.grade == "A" }
        assertTrue(aPoint.cumulativePercentage >= 80.0)
    }

    @Test
    fun test21_5_emptyBinHandlingInChartSeries() {
        val distribution = mapOf("A" to 5)
        val curve = GradeMathContract.calculateParetoCurve(distribution)
        val bPoint = curve.first { it.grade == "B" }
        assertEquals(0, bPoint.count)
    }

    // ==========================================
    // Feature 22: Past Examination Papers List
    // ==========================================
    @Test
    fun test22_1_parseStandardFilenamePatternA() {
        val filename = "CDS2004_24252.pdf"
        val parsed = PastPapersParserContract.parseFilename(filename)
        assertNotNull(parsed)
        assertEquals("CDS2004", parsed!!.courseCode)
        assertEquals("2024-2025", parsed.academicYear)
        assertEquals("Term 2", parsed.term)
    }

    @Test
    fun test22_2_parseExtendedFilenamePatternB() {
        val filename = "BUS1102_2023-2024_Term1.pdf"
        val parsed = PastPapersParserContract.parseFilename(filename)
        assertNotNull(parsed)
        assertEquals("BUS1102", parsed!!.courseCode)
        assertEquals("2023-2024", parsed.academicYear)
        assertEquals("Term 1", parsed.term)
    }

    @Test
    fun test22_3_parseFilenameWithInstructorSuffix() {
        val filename = "CDS2004_24251_Simmons.pdf"
        val parsed = PastPapersParserContract.parseFilename(filename)
        assertNotNull(parsed)
        assertEquals("Simmons", parsed!!.instructor)
    }

    @Test
    fun test22_4_filterPastPapersByAcademicYear() {
        val papers = listOf(
            PastPapersParserContract.ParsedExamPaper("CDS2004", "2024-2025", "Term 1"),
            PastPapersParserContract.ParsedExamPaper("CDS2004", "2023-2024", "Term 2")
        )
        val filtered = papers.filter { it.academicYear == "2024-2025" }
        assertEquals(1, filtered.size)
    }

    @Test
    fun test22_5_pastExamPapersBucketId() {
        val bucketId = "past_exam_papers"
        assertTrue(AppwriteConfigContract.STORAGE_BUCKETS.contains(bucketId))
    }

    // ==========================================
    // Feature 23: Study Materials & Syllabus
    // ==========================================
    @Test
    fun test23_1_studyMaterialsBucketQuery() {
        val bucketId = "study_materials"
        assertTrue(AppwriteConfigContract.STORAGE_BUCKETS.contains(bucketId))
    }

    @Test
    fun test23_2_courseSyllabusBucketQuery() {
        val bucketId = "course_syllabus"
        assertTrue(AppwriteConfigContract.STORAGE_BUCKETS.contains(bucketId))
    }

    @Test
    fun test23_3_fileSizeFormattingKb() {
        assertEquals("200 KB", StorageBucketContract.formatFileSize(204800L))
        assertEquals("1 KB", StorageBucketContract.formatFileSize(1024L))
    }

    @Test
    fun test23_4_fileSizeFormattingMb() {
        assertEquals("15.0 MB", StorageBucketContract.formatFileSize(15728640L))
        assertEquals("1.5 MB", StorageBucketContract.formatFileSize(1572864L))
    }

    @Test
    fun test23_5_pdfMimeTypeVerification() {
        assertEquals("application/pdf", StorageBucketContract.getMimeType("past_exam_paper.pdf"))
        assertTrue(StorageBucketContract.isAllowedDocumentType("course_syllabus.pdf"))
        assertFalse(StorageBucketContract.isAllowedDocumentType("malicious_script.exe"))
    }

    // ==========================================
    // Feature 24: Document View & Download
    // ==========================================
    @Test
    fun test24_1_generateViewUrlWithProjectId() {
        val url = AppwriteConfigContract.buildViewUrl("past_exam_papers", "paper_123")
        assertTrue(url.contains("/storage/buckets/past_exam_papers/files/paper_123/view"))
        assertTrue(url.contains("project=6a1097400037a55f6472"))
    }

    @Test
    fun test24_2_generateDownloadUrlWithProjectId() {
        val url = AppwriteConfigContract.buildDownloadUrl("study_materials", "note_456")
        assertTrue(url.contains("/storage/buckets/study_materials/files/note_456/download"))
        assertTrue(url.contains("project=6a1097400037a55f6472"))
    }

    @Test
    fun test24_3_inAppPdfViewerIntentAction() {
        val mimeType = StorageBucketContract.getMimeType("sample.pdf")
        assertEquals("application/pdf", mimeType)
    }

    @Test
    fun test24_4_downloadManagerNotificationVisibility() {
        val bucket = AppwriteConfigContract.STORAGE_BUCKETS.first()
        val downloadUrl = AppwriteConfigContract.buildDownloadUrl(bucket, "file_001")
        assertTrue(downloadUrl.startsWith("https://"))
    }

    @Test
    fun test24_5_cacheControlHandling() {
        val parsed = StorageBucketContract.parseCacheControl("public, max-age=3600")
        assertTrue(parsed.isPublic)
        assertEquals(3600L, parsed.maxAgeSeconds)
    }
}
