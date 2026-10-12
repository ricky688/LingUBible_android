package com.lingubible.app

import android.content.Context
import android.content.ContentResolver
import android.content.SharedPreferences
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.util.Log
import com.lingubible.app.core.util.GpaBackup
import com.lingubible.app.core.util.GpaPlannerSnapshot
import com.lingubible.app.domain.repository.CourseRepository
import com.lingubible.app.ui.viewmodels.*
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import java.io.File
import java.io.FileInputStream
import java.io.ByteArrayOutputStream

class GpaBackupViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val context = mockk<Context>(relaxed = true)
    private val resolver = mockk<ContentResolver>()
    private val prefs = mockk<SharedPreferences>()
    private val editor = mockk<SharedPreferences.Editor>(relaxed = true)
    private val repository = mockk<CourseRepository>()
    private val uri = mockk<Uri>()
    private val saved = mutableMapOf<String, String?>()
    private val backup = GpaPlannerSnapshot(
        GpaDocument(terms = listOf(GpaTerm(id = "imported-term", courses = listOf(
            GpaCourseEntry(id = "imported-course", code = "BUS1102", credits = "3", grade = "A")
        ))), yearAcademic = mapOf(1 to "2025-2026")), "3.70", "30"
    )

    @Before fun setUp() {
        Dispatchers.setMain(dispatcher)
        every { context.getSharedPreferences(any(), any()) } returns prefs
        every { context.contentResolver } returns resolver
        every { prefs.getString(any(), any()) } answers { saved[firstArg()] ?: secondArg() }
        every { prefs.edit() } returns editor
        every { editor.putString(any(), any()) } answers { saved[firstArg()] = secondArg(); editor }
        coEvery { repository.getCourses(any(), any(), any()) } returns Result.success(emptyList())
        mockkStatic(Log::class)
        every { Log.w(any(), any<String>()) } returns 0
    }

    @After fun tearDown() {
        Dispatchers.resetMain()
        unmockkStatic(Log::class)
    }

    private fun withBackupFile(raw: String, providerReadBug: Boolean = false, block: () -> Unit) {
        val file = File.createTempFile("gpa-backup", ".json")
        try {
            file.writeText(raw)
            FileInputStream(file).use { stream ->
                val descriptor = mockk<ParcelFileDescriptor>(relaxed = true)
                every { descriptor.fileDescriptor } returns stream.fd
                if (providerReadBug) {
                    every { resolver.openFileDescriptor(uri, "r") } throws NullPointerException()
                    every { resolver.openFileDescriptor(uri, "rw") } returns descriptor
                } else {
                    every { resolver.openFileDescriptor(uri, "r") } returns descriptor
                }
                block()
            }
            // Both descriptor paths must leave the original file byte-for-byte intact.
            assertEquals(raw, file.readText())
        } finally { file.delete() }
    }

    @Test fun previewCancelRestoreUndoRedoAndRestartPreserveData() = runTest(dispatcher) {
        val vm = GpaHonsViewModel(context, repository, dispatcher)
        advanceUntilIdle()
        val original = vm.uiState.value.document
        withBackupFile(GpaBackup.encode(backup)) {
            vm.readCalculatorBackup(uri)
            advanceUntilIdle()
        }
        assertEquals(original, vm.uiState.value.document)
        assertEquals(backup, vm.uiState.value.pendingRestore)
        vm.dismissCalculatorRestore()
        assertNull(vm.uiState.value.pendingRestore)
        assertEquals(original, vm.uiState.value.document)

        withBackupFile(GpaBackup.encode(backup), providerReadBug = true) {
            vm.readCalculatorBackup(uri)
            advanceUntilIdle()
        }
        vm.confirmCalculatorRestore()
        advanceUntilIdle()
        assertEquals(backup.document, vm.uiState.value.document)
        assertEquals(4.0, vm.uiState.value.cgpa!!, 0.0001)
        assertEquals("3.70", vm.uiState.value.targetCgpaInput)
        assertEquals("30", vm.uiState.value.remainingCreditsInput)
        vm.undo()
        advanceUntilIdle()
        assertEquals(original, vm.uiState.value.document)
        assertEquals("3.50", vm.uiState.value.targetCgpaInput)
        vm.redo()
        advanceUntilIdle()
        val restarted = GpaHonsViewModel(context, repository, dispatcher)
        advanceUntilIdle()
        assertEquals(backup.document, restarted.uiState.value.document)
        assertEquals("3.70", restarted.uiState.value.targetCgpaInput)
        assertEquals("30", restarted.uiState.value.remainingCreditsInput)
    }

    @Test fun invalidOrUnreadableImportDoesNotChangeExistingEntries() = runTest(dispatcher) {
        val vm = GpaHonsViewModel(context, repository, dispatcher)
        advanceUntilIdle()
        val original = vm.uiState.value.document
        withBackupFile("{}") { vm.readCalculatorBackup(uri); advanceUntilIdle() }
        assertEquals(original, vm.uiState.value.document)
        assertNull(vm.uiState.value.pendingRestore)
        assertNotNull(vm.uiState.value.backupMessage)
        every { resolver.openFileDescriptor(uri, "r") } returns null
        vm.readCalculatorBackup(uri)
        advanceUntilIdle()
        assertEquals(original, vm.uiState.value.document)
        assertFalse(vm.uiState.value.backupBusy)
    }

    @Test fun exportedJsonIsRestorableAndFailedWriteReportsFailure() = runTest(dispatcher) {
        val vm = GpaHonsViewModel(context, repository, dispatcher)
        advanceUntilIdle()
        val output = ByteArrayOutputStream()
        every { resolver.openOutputStream(uri, "wt") } returns output
        vm.exportCalculator(uri, false)
        advanceUntilIdle()
        assertEquals(vm.uiState.value.document, GpaBackup.decode(output.toString("UTF-8")).document)
        every { resolver.openOutputStream(uri, "wt") } returns null
        vm.exportCalculator(uri, false)
        advanceUntilIdle()
        assertTrue(vm.uiState.value.backupMessage!!.contains("Export failed"))
        assertFalse(vm.uiState.value.backupBusy)
    }
}
