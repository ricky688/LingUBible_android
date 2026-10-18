package com.lingubible.app

import com.lingubible.app.data.remote.AppwriteClientProvider
import com.lingubible.app.data.repository.AppwriteReviewRepository
import io.appwrite.services.Account
import io.appwrite.services.Databases
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class ReviewOwnershipTest {
    @Test fun otherUsersCannotLoadEditorOrDeleteReview() = runTest {
        val provider = mockk<AppwriteClientProvider>(); val db = mockk<Databases>(); val account = mockk<Account>()
        every { provider.databaseId } returns "lingubible"; every { provider.databases } returns db; every { provider.account } returns account
        coEvery { account.get() } returns mockk { every { id } returns "me" }
        coEvery { db.getDocument("lingubible", "reviews", "review") } returns mockk { every { data } returns mapOf("user_id" to "other") }
        val repository = AppwriteReviewRepository(provider)
        assertTrue(repository.getReviewEdit("review").isFailure)
        assertTrue(repository.deleteReview("review").isFailure)
        assertTrue(repository.updateReview("review", com.lingubible.app.domain.model.ReviewEdit.from(emptyMap())).isFailure)
        coVerify(exactly = 0) { db.updateDocument(any(), any(), any(), any()) }
        coVerify(exactly = 0) { db.deleteDocument(any(), any(), any()) }
        coVerify(exactly = 0) { db.listDocuments(any(), any(), any()) }
    }
}
