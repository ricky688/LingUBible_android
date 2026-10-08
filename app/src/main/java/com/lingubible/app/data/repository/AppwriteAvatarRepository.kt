package com.lingubible.app.data.repository

import android.content.Context
import android.util.Log
import com.lingubible.app.data.remote.AppwriteClientProvider
import com.lingubible.app.domain.model.AvatarPresets
import com.lingubible.app.domain.model.CustomAvatar
import com.lingubible.app.domain.repository.AvatarRepository
import io.appwrite.ID
import io.appwrite.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class AppwriteAvatarRepository(
    private val context: Context,
    private val clientProvider: AppwriteClientProvider
) : AvatarRepository {

    private val prefs = context.getSharedPreferences("lingubible_avatar_prefs", Context.MODE_PRIVATE)
    private val dbId = clientProvider.databaseId
    private val collectionId = "user_avatars"
    private val TAG = "AvatarRepo"

    private val _currentAvatar = MutableStateFlow<CustomAvatar?>(null)
    override val currentAvatar: StateFlow<CustomAvatar?> = _currentAvatar.asStateFlow()

    init {
        val cachedGuest = getCachedAvatar("guest")
        if (cachedGuest != null) {
            _currentAvatar.value = cachedGuest
        }
    }

    private fun getCurrentIsoTime(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        return sdf.format(Date())
    }

    override fun getCachedAvatar(userId: String): CustomAvatar? {
        if (userId.isBlank()) return null
        val animal = prefs.getString("avatar_${userId}_animal", null) ?: return null
        val bgIndex = prefs.getInt("avatar_${userId}_bg", -1)
        if (bgIndex == -1) return null
        return CustomAvatar(animal = animal, backgroundIndex = bgIndex)
    }

    private fun saveToLocalCache(userId: String, avatar: CustomAvatar?) {
        if (userId.isBlank()) return
        prefs.edit().apply {
            if (avatar != null) {
                putString("avatar_${userId}_animal", avatar.animal)
                putInt("avatar_${userId}_bg", avatar.backgroundIndex)
            } else {
                remove("avatar_${userId}_animal")
                remove("avatar_${userId}_bg")
            }
            apply()
        }
    }

    override suspend fun getUserAvatar(userId: String): Result<CustomAvatar?> {
        if (userId.isBlank()) return Result.success(null)

        // 1. Immediately yield local cache if present
        val cached = getCachedAvatar(userId)
        if (cached != null) {
            _currentAvatar.value = cached
        }

        // For guest user, local cache is authoritative
        if (userId == "guest") {
            val guestAvatar = cached ?: AvatarPresets.getDefaultAvatar("guest")
            _currentAvatar.value = guestAvatar
            return Result.success(guestAvatar)
        }

        val databases = clientProvider.databases
            ?: return Result.success(cached)

        return try {
            val response = databases.listDocuments(
                databaseId = dbId,
                collectionId = collectionId,
                queries = listOf(
                    Query.equal("userId", userId),
                    Query.limit(1)
                )
            )

            if (response.documents.isNotEmpty()) {
                val doc = response.documents[0]
                val animal = doc.data["animal"] as? String ?: "🐢"
                val bgIndex = (doc.data["backgroundIndex"] as? Number)?.toInt() ?: 0
                val createdAt = doc.createdAt
                val avatar = CustomAvatar(animal = animal, backgroundIndex = bgIndex, createdAt = createdAt)

                saveToLocalCache(userId, avatar)
                _currentAvatar.value = avatar
                Result.success(avatar)
            } else {
                saveToLocalCache(userId, null)
                // If user has no custom avatar, default avatar is computed from deterministic hash
                val defaultAvatar = AvatarPresets.getDefaultAvatar(userId)
                _currentAvatar.value = defaultAvatar
                Result.success(defaultAvatar)
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to fetch avatar from Appwrite: ${e.message}")
            // Fallback to cache or deterministic default
            val fallback = cached ?: AvatarPresets.getDefaultAvatar(userId)
            _currentAvatar.value = fallback
            Result.success(fallback)
        }
    }

    override suspend fun saveUserAvatar(userId: String, animal: String, backgroundIndex: Int): Result<CustomAvatar> {
        if (userId.isBlank()) return Result.failure(IllegalArgumentException("User ID cannot be empty"))

        val now = getCurrentIsoTime()
        val avatar = CustomAvatar(animal = animal, backgroundIndex = backgroundIndex, createdAt = now)

        saveToLocalCache(userId, avatar)
        _currentAvatar.value = avatar

        if (userId == "guest") {
            return Result.success(avatar)
        }

        val databases = clientProvider.databases ?: return Result.success(avatar)

        return try {
            // Check if document already exists
            val existing = databases.listDocuments(
                databaseId = dbId,
                collectionId = collectionId,
                queries = listOf(
                    Query.equal("userId", userId),
                    Query.limit(1)
                )
            )

            if (existing.documents.isNotEmpty()) {
                val docId = existing.documents[0].id
                databases.updateDocument(
                    databaseId = dbId,
                    collectionId = collectionId,
                    documentId = docId,
                    data = mapOf(
                        "animal" to animal,
                        "backgroundIndex" to backgroundIndex,
                        "updatedAt" to now
                    )
                )
            } else {
                databases.createDocument(
                    databaseId = dbId,
                    collectionId = collectionId,
                    documentId = ID.unique(),
                    data = mapOf(
                        "userId" to userId,
                        "animal" to animal,
                        "backgroundIndex" to backgroundIndex,
                        "createdAt" to now,
                        "updatedAt" to now
                    )
                )
            }

            Result.success(avatar)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save user avatar remotely: ${e.message}", e)
            Result.success(avatar)
        }
    }

    override suspend fun deleteUserAvatar(userId: String): Result<Unit> {
        if (userId.isBlank()) return Result.success(Unit)

        saveToLocalCache(userId, null)
        _currentAvatar.value = null

        if (userId == "guest") {
            return Result.success(Unit)
        }

        val databases = clientProvider.databases ?: return Result.success(Unit)

        return try {
            val existing = databases.listDocuments(
                databaseId = dbId,
                collectionId = collectionId,
                queries = listOf(
                    Query.equal("userId", userId),
                    Query.limit(1)
                )
            )

            if (existing.documents.isNotEmpty()) {
                val docId = existing.documents[0].id
                databases.deleteDocument(
                    databaseId = dbId,
                    collectionId = collectionId,
                    documentId = docId
                )
            }

            saveToLocalCache(userId, null)
            val defaultAvatar = AvatarPresets.getDefaultAvatar(userId)
            _currentAvatar.value = defaultAvatar
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete user avatar: ${e.message}", e)
            Result.failure(e)
        }
    }
}
