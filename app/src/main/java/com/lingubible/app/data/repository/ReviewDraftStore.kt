package com.lingubible.app.data.repository

import android.content.SharedPreferences
import com.lingubible.app.domain.model.ReviewForm
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
internal data class SavedReviewDraft(val version: Int = 1, val savedAt: Long, val form: ReviewForm)

class ReviewDraftStore(private val prefs: SharedPreferences, private val now: () -> Long = System::currentTimeMillis) {
    private val json = Json { ignoreUnknownKeys = true }
    private fun key(userId: String): String { require(userId.isNotBlank()); return "new:$userId" }
    fun load(userId: String): Result<ReviewForm?> = runCatching {
        val raw = prefs.getString(key(userId), null) ?: return@runCatching null
        val saved = runCatching { json.decodeFromString<SavedReviewDraft>(raw) }.getOrNull()
        if (saved == null || saved.version != 1 || now() - saved.savedAt !in 0..7L * 24 * 60 * 60 * 1000) {
            clear(userId).getOrThrow(); null
        } else saved.form
    }
    // SharedPreferences applies in memory immediately and completes pending disk writes at lifecycle boundaries.
    fun save(userId: String, form: ReviewForm): Result<Unit> = runCatching {
        prefs.edit().putString(key(userId), json.encodeToString(SavedReviewDraft(savedAt = now(), form = form))).apply()
    }
    fun clear(userId: String): Result<Unit> = runCatching { prefs.edit().remove(key(userId)).apply() }
}
