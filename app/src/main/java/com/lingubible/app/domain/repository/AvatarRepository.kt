package com.lingubible.app.domain.repository

import com.lingubible.app.domain.model.CustomAvatar
import kotlinx.coroutines.flow.StateFlow

interface AvatarRepository {
    val currentAvatar: StateFlow<CustomAvatar?>
    suspend fun getUserAvatar(userId: String): Result<CustomAvatar?>
    suspend fun saveUserAvatar(userId: String, animal: String, backgroundIndex: Int): Result<CustomAvatar>
    suspend fun deleteUserAvatar(userId: String): Result<Unit>
    fun getCachedAvatar(userId: String): CustomAvatar?
}
