package com.lingubible.app

import com.lingubible.app.data.remote.AppwriteClientProvider
import com.lingubible.app.data.repository.AppwriteInstructorRepository
import com.lingubible.app.domain.model.*
import com.lingubible.app.domain.repository.*
import com.lingubible.app.ui.viewmodels.InstructorsViewModel
import io.appwrite.Query
import io.appwrite.models.Document
import io.appwrite.models.DocumentList
import io.appwrite.services.Databases
import io.mockk.*
import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import org.junit.Assert.*
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class InstructorDirectoryTest {
    private fun doc(id: String, data: Map<String, Any>): Document<Map<String, Any>> = mockk<Document<Map<String, Any>>>().also {
        every { it.id } returns id; every { it.data } returns data
    }
    @Test fun directoryAndTeachingRecordsLoadBeyondFirstPage() = runTest {
        val provider = mockk<AppwriteClientProvider>(); val db = mockk<Databases>()
        every { provider.databases } returns db; every { provider.databaseId } returns "test"
        val pages = (1..100).map { doc("$it", mapOf("name" to "Name $it", "stats_teaching_score" to 0.0)) }
        val queries = mutableListOf<List<String>?>()
        coEvery { db.listDocuments("test", "instructors", captureNullable(queries)) } returnsMany listOf(DocumentList(101, pages), DocumentList(101, listOf(doc("101", mapOf("name" to "Last")))))
        val repo = AppwriteInstructorRepository(provider)
        val all = repo.getAllInstructors().getOrThrow()
        assertEquals(101, all.size); assertEquals(0.0, all.first().avgRating, 0.0)
        assertTrue(queries.last()!!.contains(Query.cursorAfter("100")))
        coEvery { db.listDocuments("test", "teaching_records", any()) } returnsMany listOf(
            DocumentList(101, (1..100).map { doc("$it", mapOf("instructor_name" to "Lecturer $it")) }),
            DocumentList(101, listOf(doc("101", mapOf("instructor_name" to "Last"))))
        )
        assertEquals(101, repo.getInstructorNamesForTerms(setOf("2025-T2")).getOrThrow().size)
    }
    @Test fun instructorDetailPaginatesHistoryAndPreservesLanguageAndSessions() = runTest {
        val provider = mockk<AppwriteClientProvider>(); val db = mockk<Databases>()
        every { provider.databases } returns db; every { provider.databaseId } returns "test"
        val queries = mutableListOf<List<String>?>()
        coEvery { db.listDocuments("test", "teaching_records", captureNullable(queries)) } returnsMany listOf(
            DocumentList(101, (1..100).map { doc("$it", mapOf("instructor_name" to "Teacher", "course_code" to "BUS1102", "term_code" to "2025-T1", "session_type" to "LEC", "teaching_language" to "E")) }),
            DocumentList(101, listOf(doc("101", mapOf("instructor_name" to "Teacher", "course_code" to "BUS1102", "term_code" to "2025-T2", "session_type" to "TUT", "teaching_language" to "C")))))
        val records = AppwriteInstructorRepository(provider).getInstructorTeachingRecords("Teacher").getOrThrow()
        assertEquals(101, records.size); assertEquals("C", records.last().teachingLanguage); assertEquals("TUT", records.last().section)
        assertTrue(queries.last()!!.contains(Query.cursorAfter("100")))
    }
    @Test fun sharedInstructorRowsAreMatchedByMemberRatherThanSubstring() = runTest {
        val provider = mockk<AppwriteClientProvider>(); val db = mockk<Databases>()
        every { provider.databases } returns db; every { provider.databaseId } returns "test"
        coEvery { db.listDocuments("test", "teaching_records", any()) } returns DocumentList(2, listOf(
            doc("a", mapOf("instructor_name" to "Ann / Bob", "course_code" to "BUS1102")),
            doc("b", mapOf("instructor_name" to "Annette", "course_code" to "BUS1102"))))
        val records = AppwriteInstructorRepository(provider).getInstructorTeachingRecords("Ann").getOrThrow()
        assertEquals(listOf("a"), records.map { it.id }); assertEquals("Ann", records.single().instructorName)
    }
    @Test fun unsupportedContainsUsesOneCachedFallbackButNetworkErrorsDoNotScanEverything() = runTest {
        val provider = mockk<AppwriteClientProvider>(); val db = mockk<Databases>()
        every { provider.databases } returns db; every { provider.databaseId } returns "test"
        coEvery { db.listDocuments("test", "teaching_records", match { Query.contains("instructor_name", "Ann") in it.orEmpty() }) } throws io.appwrite.exceptions.AppwriteException("Unsupported", 400)
        coEvery { db.listDocuments("test", "teaching_records", match { Query.contains("instructor_name", "Ann") !in it.orEmpty() }) } returns DocumentList(1, listOf(doc("a", mapOf("instructor_name" to "Ann / Bob"))))
        val repo = AppwriteInstructorRepository(provider)
        assertEquals(1, repo.getInstructorTeachingRecords("Ann").getOrThrow().size)
        assertEquals(1, repo.getInstructorTeachingRecords("Bob").getOrThrow().size)
        coVerify(exactly = 2) { db.listDocuments(any(), any(), any()) }
        clearMocks(db)
        coEvery { db.listDocuments(any(), any(), any()) } throws io.appwrite.exceptions.AppwriteException("Unavailable", 503)
        assertTrue(AppwriteInstructorRepository(provider).getInstructorTeachingRecords("Ann").isFailure)
        coVerify(exactly = 1) { db.listDocuments(any(), any(), any()) }
    }
    @Test fun termFilteringCachesNamesAndRetriesFailuresWithoutLosingDirectory() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val repo = mockk<InstructorRepository>(); val courses = mockk<CourseRepository>()
            coEvery { repo.getAllInstructors() } returns Result.success(listOf(Instructor("a", "Alice"), Instructor("b", "Bob")))
            coEvery { courses.getTerms() } returns Result.success(emptyList())
            coEvery { repo.getInstructorNamesForTerms(any()) } returnsMany listOf(Result.failure(IllegalStateException("offline")), Result.success(setOf("Alice")))
            val vm = InstructorsViewModel(repo, courses)
            advanceUntilIdle()
            vm.applyFilters(InstructorFilters(terms = setOf("2025-T2"))); advanceUntilIdle()
            assertEquals("offline", vm.uiState.value.errorMessage)
            assertEquals(2, vm.uiState.value.allInstructors.size)
            vm.applyFilters(vm.uiState.value.filters); advanceUntilIdle()
            assertEquals(listOf("Alice"), vm.uiState.value.instructors.map { it.name })
            vm.search("Ali"); advanceUntilIdle()
            coVerify(exactly = 2) { repo.getInstructorNamesForTerms(any()) }
            coVerify(exactly = 1) { repo.getAllInstructors() }
        } finally { Dispatchers.resetMain() }
    }
}
