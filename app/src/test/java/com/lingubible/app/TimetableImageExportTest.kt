package com.lingubible.app

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import com.lingubible.app.core.util.TimetableImageExport
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.*
import org.junit.Test
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException

class TimetableImageExportTest {
    private val resolver = mockk<ContentResolver>()
    private val context = mockk<Context> { every { contentResolver } returns resolver }
    private val uri = mockk<Uri>()

    @Test fun documentExportCopiesAllBytesClosesOutputAndKeepsSource() {
        val source = File.createTempFile("timetable-test", ".png")
        var closed = false
        val output = object : ByteArrayOutputStream() {
            override fun close() { closed = true; super.close() }
        }
        val bytes = ByteArray(100_000) { (it % 251).toByte() }
        try {
            source.writeBytes(bytes)
            every { resolver.openOutputStream(uri, "wt") } returns output
            TimetableImageExport.saveToDocument(context, source, uri)
            assertArrayEquals(bytes, output.toByteArray())
            assertArrayEquals(bytes, source.readBytes())
            assertTrue(closed)
        } finally { source.delete() }
    }

    @Test fun unavailableOrFailingDocumentProviderReportsFailureWithoutChangingSource() {
        val source = File.createTempFile("timetable-test", ".png")
        try {
            source.writeText("unchanged-image")
            every { resolver.openOutputStream(uri, "wt") } returns null
            assertThrows(IllegalStateException::class.java) { TimetableImageExport.saveToDocument(context, source, uri) }
            every { resolver.openOutputStream(uri, "wt") } throws IOException("Provider offline")
            assertThrows(IOException::class.java) { TimetableImageExport.saveToDocument(context, source, uri) }
            assertEquals("unchanged-image", source.readText())
        } finally { source.delete() }
    }

    @Test fun writeFailureClosesTheProviderStreamAndKeepsTheImage() {
        val source = File.createTempFile("timetable-test", ".png")
        var closed = false
        val output = object : java.io.OutputStream() {
            override fun write(value: Int) { throw IOException("Storage full") }
            override fun close() { closed = true }
        }
        try {
            source.writeText("image-to-export")
            every { resolver.openOutputStream(uri, "wt") } returns output
            assertThrows(IOException::class.java) { TimetableImageExport.saveToDocument(context, source, uri) }
            assertTrue(closed)
            assertEquals("image-to-export", source.readText())
        } finally { source.delete() }
    }

    @Test fun landscapeExportDimensionsAreInWidescreenLandscapeMode() {
        assertEquals(2400, TimetableImageExport.EXPORT_WIDTH)
        assertEquals(1400, TimetableImageExport.EXPORT_HEIGHT)
        assertTrue("Exported image must be landscape (width > height)",
            TimetableImageExport.EXPORT_WIDTH > TimetableImageExport.EXPORT_HEIGHT)
        val aspectRatio = TimetableImageExport.EXPORT_WIDTH.toFloat() / TimetableImageExport.EXPORT_HEIGHT.toFloat()
        assertTrue("Aspect ratio should be widescreen (> 1.5)", aspectRatio > 1.5f)
    }

    @Test fun textTruncationHandlesShortAndLongStringsSafely() {
        val paint = mockk<android.graphics.Paint>()
        every { paint.measureText(any<String>()) } answers {
            it.invocation.args[0].toString().length * 10f
        }
        // Short text returns directly (70f <= 200f)
        assertEquals("CDS1001", TimetableImageExport.truncateText("CDS1001", paint, 200f))
        assertEquals("", TimetableImageExport.truncateText("", paint, 200f))

        // Long text truncation contains ellipsis (660f > 150f)
        val longText = "Very Long Course Title That Exceeds Normal Card Width In Timetable"
        val truncated = TimetableImageExport.truncateText(longText, paint, 150f)
        assertTrue("Truncated text should end with ...", truncated.endsWith("..."))
        assertTrue("Truncated text should be shorter than original", truncated.length < longText.length)
    }

    @Test fun crnAndSectionFormattingVerification() {
        fun formatCrn(sec: String, crn: String): String = when {
            sec.isNotBlank() && crn.isNotBlank() -> "Sect $sec • CRN $crn"
            crn.isNotBlank() -> "CRN $crn"
            sec.isNotBlank() -> "Section $sec"
            else -> ""
        }
        assertEquals("Sect 1 • CRN 5403", formatCrn("1", "5403"))
        assertEquals("CRN 5403", formatCrn("", "5403"))
        assertEquals("Section 2", formatCrn("2", ""))
        assertEquals("", formatCrn("", ""))
    }

    @Test fun portraitExportDimensionsMatchStandardPhoneRatio() {
        assertEquals(1440, TimetableImageExport.PORTRAIT_WIDTH)
        assertEquals(2560, TimetableImageExport.PORTRAIT_HEIGHT)
        assertTrue("Exported image must be portrait (height > width)",
            TimetableImageExport.PORTRAIT_HEIGHT > TimetableImageExport.PORTRAIT_WIDTH)
        val aspectRatio = TimetableImageExport.PORTRAIT_HEIGHT.toFloat() / TimetableImageExport.PORTRAIT_WIDTH.toFloat()
        // 2560 / 1440 = 16 / 9 = 1.7777778f
        assertEquals(16f / 9f, aspectRatio, 0.001f)
    }

    @Test fun orientationEnumProvidesCorrectLabels() {
        val landscape = com.lingubible.app.core.util.TimetableExportOrientation.LANDSCAPE
        assertEquals("橫向寬螢幕", landscape.titleZh)
        assertEquals("Landscape", landscape.titleEn)
        assertTrue(landscape.resolutionLabel.contains("2400 × 1400"))

        val portrait = com.lingubible.app.core.util.TimetableExportOrientation.PORTRAIT
        assertEquals("直向標準手機", portrait.titleZh)
        assertEquals("Portrait", portrait.titleEn)
        assertTrue(portrait.resolutionLabel.contains("1440 × 2560"))
    }

    @Test fun dynamicHourRangeLeavesBreathingRoomAtTopAndBottom() {
        // Scenario 1: Empty schedule defaults to 08:30 - 18:30
        val emptyRange = TimetableImageExport.computeDynamicHourRange(emptyList())
        assertEquals(8, emptyRange.startHour) // 08:30
        assertEquals(18, emptyRange.endHour)  // 18:30

        // Scenario 2: User's exact prompt example:
        // Course ending at 14:30 (870 min) and starting at 10:30 (630 min)
        // Must show 15:30 at bottom to prevent crowding, and leave space at top
        val afternoonSection = com.lingubible.app.domain.model.TimetableSection(
            id = "s1",
            crn = "1001",
            courseCode = "TEST1001",
            courseTitle = "Test Course",
            section = "1",
            colorHex = "#3B82F6",
            meetings = listOf(
                com.lingubible.app.domain.model.TimetableMeeting(
                    day = "WED",
                    start = "10:30",
                    end = "14:30",
                    startMinutes = 10 * 60 + 30, // 630 min
                    endMinutes = 14 * 60 + 30,   // 870 min
                    venue = "LKK201",
                    type = "LEC"
                )
            )
        )
        val range2 = TimetableImageExport.computeDynamicHourRange(listOf(afternoonSection))
        // Top should leave at least 1 hour of space before 10:30 -> starts at 09:30 (startHour = 9)
        assertEquals(9, range2.startHour)
        // Bottom should extend to 15:30 to leave room after 14:30 -> ends at 15:30 (endHour = 15)
        assertEquals(15, range2.endHour)

        // Scenario 3: Evening class ending at 20:50 (1250 min)
        val eveningSection = com.lingubible.app.domain.model.TimetableSection(
            id = "s2",
            crn = "2001",
            courseCode = "EVNG2001",
            courseTitle = "Evening Course",
            section = "1",
            colorHex = "#EA580C",
            meetings = listOf(
                com.lingubible.app.domain.model.TimetableMeeting(
                    day = "TUE",
                    start = "18:00",
                    end = "20:50",
                    startMinutes = 18 * 60,       // 1080 min
                    endMinutes = 20 * 60 + 50,   // 1250 min
                    venue = "MBG01",
                    type = "LEC"
                )
            )
        )
        val range3 = TimetableImageExport.computeDynamicHourRange(listOf(eveningSection))
        // Bottom must extend to 22:30 (endHour = 22) so 20:50 doesn't crash into bottom border
        assertEquals(22, range3.endHour)
    }

    @Test fun showGridLinesDefaultsToTrue() {
        // Verify default showGridLines parameter is optional and present across methods
        val defaultLandscapeSig = TimetableImageExport::generateLandscapeBitmap.parameters.find { it.name == "showGridLines" }
        assertNotNull(defaultLandscapeSig)
        assertTrue(defaultLandscapeSig!!.isOptional)

        val defaultPortraitSig = TimetableImageExport::generatePortraitBitmap.parameters.find { it.name == "showGridLines" }
        assertNotNull(defaultPortraitSig)
        assertTrue(defaultPortraitSig!!.isOptional)

        val defaultGenerateBitmapSig = TimetableImageExport::generateBitmap.parameters.find { it.name == "showGridLines" }
        assertNotNull(defaultGenerateBitmapSig)
        assertTrue(defaultGenerateBitmapSig!!.isOptional)
    }

    @Test fun computeSummaryTextDeduplicatesMultiSectionCourses() {
        val sections = listOf(
            com.lingubible.app.domain.model.TimetableSection("1", "811", "LUE4002", "English", section = "4"),
            com.lingubible.app.domain.model.TimetableSection("2", "401", "CLD9031", "Chinese", section = "1"),
            com.lingubible.app.domain.model.TimetableSection("3", "405", "CLE9008", "Ethics LEC", section = "1"),
            com.lingubible.app.domain.model.TimetableSection("4", "406", "cle9008", "Ethics TUT", section = "2"),
            com.lingubible.app.domain.model.TimetableSection("5", "234", "CDS3004", "Data Science", section = "1"),
            com.lingubible.app.domain.model.TimetableSection("6", "233", "CDS3001", "Algorithms", section = "1"),
            com.lingubible.app.domain.model.TimetableSection("7", "381", "CLC9022", "Culture", section = "1")
        )
        // 7 sections belonging to 6 distinct courses, each 3 credits
        val summary = TimetableImageExport.computeSummaryText(sections)
        assertEquals("6 科課程 • 共 18 學分", summary)

        // Empty list
        val emptySummary = TimetableImageExport.computeSummaryText(emptyList())
        assertEquals("0 科課程 • 共 0 學分", emptySummary)
    }
}

