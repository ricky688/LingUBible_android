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
}
