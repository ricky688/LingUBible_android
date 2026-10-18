package com.lingubible.app

import com.lingubible.app.domain.model.*
import com.lingubible.app.domain.util.writeMaterialsZip
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.zip.ZipInputStream

class CourseParityTest {
    private val courses = listOf(
        Course("a", "ACT1110", "Accounting", teachingLanguages = listOf("E"), serviceLearningTypes = listOf("optional"), avgGpa = 3.0),
        Course("b", "BUS1102", "Business", teachingLanguages = listOf("C"), serviceLearningTypes = listOf("compulsory"), avgGpa = 0.0),
        Course("c", "CCC8001", "Core", avgGpa = -1.0)
    )
    @Test fun filtersCombineAndUseRealLanguageCodes() {
        val filter = CourseFilters(subjects = setOf("ACT", "BUS"), languages = setOf("E"), serviceLearning = setOf("optional"), terms = setOf("2026-27-T1"), hasMaterials = true)
        assertEquals(listOf("ACT1110"), filterCourses(courses, filter, setOf("ACT1110"), setOf("ACT1110")).map { it.code })
        assertTrue(filterCourses(courses, filter, emptySet(), setOf("ACT1110")).isEmpty())
    }
    @Test fun unknownGradesStayLastInEitherDirectionAndZeroIsValid() {
        assertEquals(listOf("BUS1102", "ACT1110", "CCC8001"), filterCourses(courses, CourseFilters(sort = CourseSort.GPA), null, null).map { it.code })
        assertEquals(listOf("ACT1110", "BUS1102", "CCC8001"), filterCourses(courses, CourseFilters(sort = CourseSort.GPA, descending = true), null, null).map { it.code })
    }
    @Test fun noServiceLearningAndEmptyFiltersWork() {
        assertEquals("CCC8001", filterCourses(courses, CourseFilters(serviceLearning = setOf("none")), null, null).single().code)
        assertEquals(3, filterCourses(courses, CourseFilters(), null, null).size)
    }
    @Test fun resourcesRequireExactCoursePrefixAndRejectMalformedData() {
        assertEquals("ACT1110", materialCourseCode("act1110_notes.pdf"))
        assertEquals("ACT11100", materialCourseCode("ACT11100_notes.pdf"))
        assertNull(materialCourseCode("notes_ACT1110.pdf"))
        assertNull(materialCourseCode("ACT1110extra.pdf"))
        assertEquals("notes", StudyMaterial("id", "ACT1110_notes.pdf", 12).displayName)
        assertTrue(parseCourseReadings("not-json").isEmpty())
        val readings = parseCourseReadings("""[{"TYPE":"essential","FULL_NAME":"Book","SOURCE":"A book title"},{"TYPE":"recommended","FULL_NAME":"Paper","SOURCE":"https://doi.org/10.1234/example"},{"FULL_NAME":"Unsafe","SOURCE":"javascript:alert(1)"}]""")
        assertTrue(readings[0].required); assertNull(readings[0].url)
        assertEquals("https://doi.org/10.1234/example", readings[1].url)
        assertNull(readings[2].url)
    }
    @Test fun doiPunctuationAndLibraryProxyAreHandled() {
        val raw = """[{"FULL_NAME":"Paper (https://doi.org/10.1000/example(1))."},{"SOURCE":"https://doi-org.lingnan.idm.oclc.org/10.1234/book"}]"""
        val readings = parseCourseReadings(raw)
        assertEquals("https://doi.org/10.1000/example(1)", readings[0].url)
        assertEquals("https://doi.org/10.1234/book", readings[1].url)
    }
    @Test fun zipUsesSafeDistinctNamesAndOriginalBytes() = runTest {
        val output = ByteArrayOutputStream()
        writeMaterialsZip(output, listOf(StudyMaterial("1", "../../notes.pdf", 1), StudyMaterial("2", "../../notes.pdf", 1))) { it.toByteArray() }
        val names = mutableListOf<String>(); val contents = mutableListOf<String>()
        ZipInputStream(ByteArrayInputStream(output.toByteArray())).use { zip ->
            while (true) {
                val entry = zip.nextEntry ?: break
                names += entry.name; contents += zip.readBytes().decodeToString()
            }
        }
        assertEquals(2, names.toSet().size); assertTrue(names.none { '/' in it || '\\' in it })
        assertEquals(listOf("1", "2"), contents)
    }
    @Test fun failedZipDownloadPropagatesFailure() = runTest {
        val error = runCatching { writeMaterialsZip(ByteArrayOutputStream(), listOf(StudyMaterial("1", "notes.pdf", 1))) { throw IllegalStateException("offline") } }.exceptionOrNull()
        assertEquals("offline", error?.message)
    }
}
