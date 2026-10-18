package com.lingubible.app.data.repository

import com.lingubible.app.data.remote.AppwriteClientProvider
import com.lingubible.app.domain.repository.AuthRepository
import io.appwrite.ID
import io.appwrite.Query
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.Instant

data class FavoriteKey(val type: String, val itemId: String)
data class FavoritesState(
    val userId: String? = null,
    val items: Map<FavoriteKey, String> = emptyMap(),
    val loading: Boolean = false,
    val error: String? = null
)

class AppwriteFavoritesRepository(
    private val provider: AppwriteClientProvider,
    private val auth: AuthRepository,
    scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
) {
    private val mutableState = MutableStateFlow(FavoritesState())
    val state = mutableState.asStateFlow()
    private val mutex = Mutex()

    init {
        scope.launch {
            auth.currentUser.map { it?.id }.distinctUntilChanged().collectLatest { userId ->
                mutableState.value = FavoritesState(userId = userId)
                if (userId != null) refresh()
            }
        }
    }

    suspend fun refresh(): Result<Unit> = mutex.withLock {
        val userId = auth.currentUser.value?.id ?: return@withLock Result.success(Unit)
        mutableState.update { FavoritesState(userId, if (it.userId == userId) it.items else emptyMap(), loading = true) }
        try {
            val db = requireNotNull(provider.tablesDB) { "收藏服務未就緒 · Favourites unavailable" }
            val items = mutableMapOf<FavoriteKey, String>()
            var cursor: String? = null
            do {
                val rows = db.listRows("favorites", "user_favorites", buildList {
                    add(Query.equal("user_id", userId)); add(Query.limit(100))
                    add(Query.orderAsc("\$id")); cursor?.let { add(Query.cursorAfter(it)) }
                }).rows
                rows.forEach { row ->
                    val type = row.data["type"] as? String
                    val item = row.data["item_id"] as? String
                    if (type in setOf("course", "instructor") && !item.isNullOrBlank()) {
                        items[FavoriteKey(requireNotNull(type), item)] = row.id
                    }
                }
                cursor = rows.lastOrNull()?.id
            } while (rows.size == 100)
            if (auth.currentUser.value?.id == userId) mutableState.value = FavoritesState(userId, items)
            Result.success(Unit)
        } catch (e: CancellationException) { throw e
        } catch (e: Exception) {
            if (auth.currentUser.value?.id == userId) mutableState.update { it.copy(loading = false, error = e.localizedMessage ?: "Could not load favourites") }
            Result.failure(e)
        }
    }

    suspend fun toggle(type: String, itemId: String): Result<Unit> = mutex.withLock {
        try {
            require(type in setOf("course", "instructor") && itemId.isNotBlank())
            val userId = requireNotNull(auth.currentUser.value?.id) { "請先登入以儲存收藏 · Sign in to save favourites" }
            check(state.value.userId == userId && !state.value.loading) { "收藏同步中，請稍後重試 · Favourites are syncing; try again shortly" }
            val db = requireNotNull(provider.tablesDB)
            check(provider.account?.get()?.id == userId) { "登入狀態已變更 · Please sign in again" }
            val key = FavoriteKey(type, itemId)
            val shouldAdd = key !in state.value.items
            val rows = db.listRows("favorites", "user_favorites", listOf(
                Query.equal("user_id", userId), Query.equal("type", type),
                Query.equal("item_id", itemId), Query.limit(100)
            )).rows
            var rowId: String? = if (shouldAdd) rows.firstOrNull()?.id else null
            if (shouldAdd && rows.isEmpty()) {
                rowId = db.createRow("favorites", "user_favorites", ID.unique(), mapOf(
                    "user_id" to userId, "type" to type, "item_id" to itemId,
                    "created_at" to Instant.now().toString()
                )).id
            } else if (!shouldAdd) {
                rows.forEach { db.deleteRow("favorites", "user_favorites", it.id) }
            }
            if (auth.currentUser.value?.id == userId) mutableState.update {
                it.copy(userId = userId, items = it.items.toMutableMap().apply {
                    if (rowId == null) remove(key) else put(key, rowId)
                }, error = null)
            }
            Result.success(Unit)
        } catch (e: CancellationException) { throw e
        } catch (e: Exception) { Result.failure(e) }
    }
}
