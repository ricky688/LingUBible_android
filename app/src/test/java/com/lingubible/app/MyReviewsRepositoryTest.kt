package com.lingubible.app

import android.util.Log
import com.lingubible.app.data.remote.AppwriteClientProvider
import com.lingubible.app.data.repository.AppwriteReviewRepository
import io.appwrite.Query
import io.appwrite.models.Document
import io.appwrite.models.DocumentList
import io.appwrite.services.Databases
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class MyReviewsRepositoryTest {
    private val provider = mockk<AppwriteClientProvider>()
    private val database = mockk<Databases>()
    private lateinit var repository: AppwriteReviewRepository

    @Before fun setup() {
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        every { Log.e(any(), any(), any()) } returns 0
        every { provider.databaseId } returns "lingubible"
        every { provider.databases } returns database
        coEvery { database.listDocuments("lingubible", "review_votes", any()) } returns DocumentList(0, emptyList())
        repository = AppwriteReviewRepository(provider)
    }
    @After fun cleanup() { unmockkStatic(Log::class) }

    private fun document(id: String): Document<Map<String, Any>> = mockk {
        every { this@mockk.id } returns id
        every { data } returns mapOf("user_id" to "owner", "course_code" to "BUS1102")
        every { createdAt } returns "2026-10-12T00:00:00Z"
    }
    @Test fun `personal query keeps account filter on every page and retrieves older reviews`() = runTest {
        val queries = mutableListOf<List<String>?>()
        coEvery { database.listDocuments(databaseId = "lingubible", collectionId = "reviews", queries = captureNullable(queries)) } returnsMany listOf(
            DocumentList(101, (1..100).map { document("r$it") }),
            DocumentList(101, listOf(document("older")))
        )
        val result = repository.getReviewsForUser("owner").getOrThrow()
        assertEquals(101, result.size)
        assertEquals("older", result.last().id)
        assertEquals(2, queries.size)
        queries.forEach { page ->
            assertTrue(page!!.contains(Query.equal("user_id", "owner")))
            assertTrue(page.contains(Query.orderDesc("\$createdAt")))
            assertTrue(page.contains(Query.limit(100)))
        }
        assertTrue(queries.last()!!.contains(Query.cursorAfter("r100")))
    }
    @Test fun `blank user cannot request an unfiltered review list`() = runTest {
        try {
            repository.getReviewsForUser(" ")
            fail("Blank account must be rejected")
        } catch (_: IllegalArgumentException) { }
        coVerify(exactly = 0) { database.listDocuments(any(), any(), any(), any()) }
    }
}
