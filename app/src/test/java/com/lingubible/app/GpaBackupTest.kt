package com.lingubible.app

import com.lingubible.app.core.util.GpaBackup
import com.lingubible.app.core.util.GpaPlannerSnapshot
import com.lingubible.app.ui.viewmodels.GpaCourseEntry
import com.lingubible.app.ui.viewmodels.GpaDocument
import com.lingubible.app.ui.viewmodels.GpaHonsUiState
import com.lingubible.app.ui.viewmodels.GpaTerm
import com.lingubible.app.ui.viewmodels.TermPart
import org.junit.Assert.*
import org.junit.Test

/** Data integrity checks following ARTEMIS exploration of file export and restore. */
class GpaBackupTest {
    private val document = GpaDocument(
        terms = listOf(
            GpaTerm(id = "term-one", year = 1, courses = listOf(
                GpaCourseEntry(id = "course-one", code = "BUS1102", title = "商業統計, \"Statistics\"", credits = "3", grade = "A"),
                GpaCourseEntry(id = "course-two", code = "Draft", credits = "", grade = "")
            )),
            GpaTerm(id = "summer", year = 2, part = TermPart.SUMMER, courses = listOf(
                GpaCourseEntry(id = "pass-course", credits = "1.5", grade = "PASS")
            ))
        ), yearAcademic = mapOf(1 to "2024-2025", 2 to "2025-2026")
    )
    private val snapshot = GpaPlannerSnapshot(document, "3.70", "24")

    @Test fun backupPreservesAllEntriesDraftsAndGoals() {
        assertEquals(snapshot, GpaBackup.decode(GpaBackup.encode(snapshot)))
        val blank = GpaPlannerSnapshot(GpaDocument(), "3.50", "")
        assertEquals(blank, GpaBackup.decode(GpaBackup.encode(blank)))
        assertEquals(snapshot, GpaBackup.decode("\uFEFF" + GpaBackup.encode(snapshot)))
        val draftGoals = snapshot.copy(targetCgpaInput = ".", remainingCreditsInput = "100000")
        assertEquals(draftGoals, GpaBackup.decode(GpaBackup.encode(draftGoals)))
    }

    @Test fun rejectsUnrelatedMalformedAndFutureFiles() {
        listOf("{}", "not json", GpaBackup.encode(snapshot).replace("\"version\": 1", "\"version\": 2"),
            GpaBackup.encode(snapshot).replace("lingubible-gpa", "other-app"),
            GpaBackup.encode(snapshot).replace("\"credits\": \"3\",", "")).forEach { raw ->
            assertTrue(runCatching { GpaBackup.decode(raw) }.isFailure)
        }
    }

    @Test fun rejectsDuplicateIdsInvalidGradesAndNonFiniteCredits() {
        val badDocuments = listOf(
            document.copy(terms = document.terms + document.terms.first()),
            document.copy(terms = listOf(document.terms.first().copy(year = 9))),
            document.copy(terms = listOf(GpaTerm(courses = listOf(GpaCourseEntry(grade = "A+++"))))),
            document.copy(terms = listOf(GpaTerm(courses = listOf(GpaCourseEntry(credits = "NaN"))))),
            document.copy(terms = listOf(GpaTerm(courses = listOf(GpaCourseEntry(credits = "-3")))))
        )
        badDocuments.forEach { bad ->
            assertTrue(runCatching { GpaBackup.decode(GpaBackup.encode(snapshot.copy(document = bad))) }.isFailure)
        }
        assertTrue(runCatching { GpaBackup.decode(" ".repeat(GpaBackup.MAX_BYTES + 1)) }.isFailure)
    }

    @Test fun csvPreservesBilingualTextAndEscapesSpreadsheetFormulas() {
        val csv = GpaBackup.csv(GpaHonsUiState(document = document, cgpa = 4.0))
        assertTrue(csv.startsWith("\uFEFF"))
        assertTrue(csv.contains("\"商業統計, \"\"Statistics\"\"\""))
        assertTrue(csv.contains("\"BUS1102\",\"商業統計"))
        assertTrue(csv.contains("\"PASS\""))
        val formulaDoc = GpaDocument(terms = listOf(GpaTerm(courses = listOf(GpaCourseEntry(code = "=1+1", title = "@SUM(A1)")))))
        val safeCsv = GpaBackup.csv(GpaHonsUiState(document = formulaDoc))
        assertTrue(safeCsv.contains("\"'=1+1\""))
        assertTrue(safeCsv.contains("\"'@SUM(A1)\""))
    }
}
