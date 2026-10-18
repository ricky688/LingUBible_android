package com.lingubible.app

import com.lingubible.app.data.remote.AppwriteClientProvider
import com.lingubible.app.data.repository.*
import com.lingubible.app.domain.model.User
import com.lingubible.app.domain.repository.AuthRepository
import io.appwrite.services.Account
import io.appwrite.services.TablesDB
import io.mockk.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.*
import org.junit.Assert.*
import org.junit.Test

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class FavoritesRepositoryTest {
    @Test fun guestCannotWriteFavorites() = runTest {
        val provider = mockk<AppwriteClientProvider>()
        val auth = mockk<AuthRepository>(); every { auth.currentUser } returns MutableStateFlow(null)
        val repository = AppwriteFavoritesRepository(provider, auth, backgroundScope)
        assertTrue(repository.toggle("course", "ACT1110").isFailure)
        assertTrue(repository.state.value.items.isEmpty())
        verify { provider wasNot Called }
    }
    @Test fun successfulSavePersistsAndLogoutClearsAccountData() = runTest {
        val provider = mockk<AppwriteClientProvider>(); val db = mockk<TablesDB>(); val account = mockk<Account>()
        every { provider.tablesDB } returns db; every { provider.account } returns account
        coEvery { account.get() } returns mockk { every { id } returns "u1" }
        coEvery { db.listRows("favorites", "user_favorites", any()) } returns mockk { every { rows } returns emptyList() }
        coEvery { db.createRow("favorites", "user_favorites", any(), any()) } returns mockk { every { id } returns "row1" }
        val users = MutableStateFlow<User?>(User("u1", "Test", "test@ln.hk"))
        val auth = mockk<AuthRepository>(); every { auth.currentUser } returns users
        val repository = AppwriteFavoritesRepository(provider, auth, backgroundScope)
        runCurrent()
        assertTrue(repository.toggle("course", "ACT1110").isSuccess)
        assertEquals("row1", repository.state.value.items[FavoriteKey("course", "ACT1110")])
        users.value = null; runCurrent()
        assertTrue(repository.state.value.items.isEmpty()); assertNull(repository.state.value.userId)
    }
    @Test fun failedSaveDoesNotPretendItemWasSaved() = runTest {
        val provider = mockk<AppwriteClientProvider>(); val db = mockk<TablesDB>(); val account = mockk<Account>()
        every { provider.tablesDB } returns db; every { provider.account } returns account
        coEvery { account.get() } returns mockk { every { id } returns "u1" }
        coEvery { db.listRows("favorites", "user_favorites", any()) } returns mockk { every { rows } returns emptyList() }
        coEvery { db.createRow("favorites", "user_favorites", any(), any()) } throws IllegalStateException("offline")
        val auth = mockk<AuthRepository>(); every { auth.currentUser } returns MutableStateFlow(User("u1", "Test", "test@ln.hk"))
        val repository = AppwriteFavoritesRepository(provider, auth, backgroundScope); runCurrent()
        assertTrue(repository.toggle("course", "ACT1110").isFailure)
        assertTrue(repository.state.value.items.isEmpty())
    }
    @Test fun accountChangeCannotMutateUsingPreviousAccountsState() = runTest {
        val provider = mockk<AppwriteClientProvider>(); val db = mockk<TablesDB>(); val account = mockk<Account>()
        every { provider.tablesDB } returns db; every { provider.account } returns account
        coEvery { db.listRows("favorites", "user_favorites", any()) } returns mockk { every { rows } returns emptyList() }
        val users = MutableStateFlow<User?>(User("u1", "Test", "test@ln.hk"))
        val auth = mockk<AuthRepository>(); every { auth.currentUser } returns users
        val repository = AppwriteFavoritesRepository(provider, auth, backgroundScope); runCurrent()
        users.value = User("u2", "Other", "other@ln.hk")
        assertTrue(repository.toggle("course", "ACT1110").isFailure)
        coVerify(exactly = 0) { account.get() }
        coVerify(exactly = 0) { db.createRow(any(), any(), any(), any()) }
    }
    @Test fun remoteAlreadySavedItemIsNotAccidentallyRemoved() = runTest {
        val provider = mockk<AppwriteClientProvider>(); val db = mockk<TablesDB>(); val account = mockk<Account>()
        every { provider.tablesDB } returns db; every { provider.account } returns account
        coEvery { account.get() } returns mockk { every { id } returns "u1" }
        coEvery { db.listRows("favorites", "user_favorites", any()) } returns mockk { every { rows } returns emptyList() }
        val auth = mockk<AuthRepository>(); every { auth.currentUser } returns MutableStateFlow(User("u1", "Test", "test@ln.hk"))
        val repository = AppwriteFavoritesRepository(provider, auth, backgroundScope); runCurrent()
        coEvery { db.listRows("favorites", "user_favorites", any()) } returns mockk {
            every { rows } returns listOf(mockk { every { id } returns "remote-row" })
        }
        assertTrue(repository.toggle("course", "ACT1110").isSuccess)
        assertEquals("remote-row", repository.state.value.items[FavoriteKey("course", "ACT1110")])
        coVerify(exactly = 0) { db.deleteRow(any(), any(), any()) }
        coVerify(exactly = 0) { db.createRow(any(), any(), any(), any()) }
    }

}
