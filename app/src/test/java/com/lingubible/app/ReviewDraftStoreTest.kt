package com.lingubible.app

import android.content.SharedPreferences
import com.lingubible.app.data.repository.ReviewDraftStore
import com.lingubible.app.domain.model.ReviewForm
import io.mockk.*
import org.junit.Assert.*
import org.junit.Test

class ReviewDraftStoreTest {
    private val data = mutableMapOf<String, String>()
    private val prefs = mockk<SharedPreferences>()
    private val editor = mockk<SharedPreferences.Editor>()
    private var time = 10L
    private val store: ReviewDraftStore
    init {
        every { prefs.getString(any(), null) } answers { data[firstArg()] }
        every { prefs.edit() } returns editor
        every { editor.putString(any(), any()) } answers { data[firstArg()] = secondArg(); editor }
        every { editor.remove(any()) } answers { data.remove(firstArg<String>()); editor }
        every { editor.apply() } just Runs
        store = ReviewDraftStore(prefs) { time }
    }
    @Test fun `drafts survive store recreation and remain scoped to their owner`() {
        val form = ReviewForm(courseCode = "BUS1102", comment = "My private draft", anonymous = false)
        store.save("a", form).getOrThrow()
        assertEquals(form, ReviewDraftStore(prefs) { time }.load("a").getOrThrow())
        assertNull(store.load("b").getOrThrow())
        store.save("b", ReviewForm(courseCode = "ACT1100"))
        store.clear("a").getOrThrow()
        assertNull(store.load("a").getOrThrow()); assertEquals("ACT1100", store.load("b").getOrThrow()!!.courseCode)
    }
    @Test fun `expired malformed or unsupported drafts are cleared`() {
        store.save("expired", ReviewForm()); time += 7L * 24 * 60 * 60 * 1000 + 1
        assertNull(store.load("expired").getOrThrow()); assertFalse(data.containsKey("new:expired"))
        data["new:broken"] = "bad JSON"; assertNull(store.load("broken").getOrThrow())
        data["new:version"] = """{"version":99,"savedAt":$time,"form":{}}"""
        assertNull(store.load("version").getOrThrow()); assertFalse(data.containsKey("new:version"))
    }
    @Test fun `storage errors are reported and blank accounts cannot store drafts`() {
        assertTrue(store.save("", ReviewForm()).isFailure)
        every { prefs.edit() } throws IllegalStateException("storage unavailable")
        assertTrue(store.save("a", ReviewForm()).isFailure)
        assertTrue(store.clear("a").isFailure)
    }
}
