package com.lingubible.app

import android.content.Context
import android.content.SharedPreferences
import com.lingubible.app.data.repository.SearchHistoryStore
import io.mockk.*
import org.junit.Assert.*
import org.junit.Test

class SearchHistoryStoreTest {
    @Test fun searchesSurviveRecreationAndClearOnlyTheirOwnList() {
        val data = mutableMapOf<String, String>()
        val context = mockk<Context>(); val prefs = mockk<SharedPreferences>(); val editor = mockk<SharedPreferences.Editor>()
        every { context.applicationContext } returns context
        every { context.getSharedPreferences("lingubible_recent_searches", Context.MODE_PRIVATE) } returns prefs
        every { prefs.getString(any(), any()) } answers { data[firstArg()] ?: secondArg() }
        every { prefs.edit() } returns editor
        every { editor.putString(any(), any()) } answers { data[firstArg()] = secondArg(); editor }
        every { editor.apply() } just Runs
        val store = SearchHistoryStore(context)
        store.add("courses", "ACT2200"); store.add("instructors", "Teacher")
        assertEquals(listOf("ACT2200"), SearchHistoryStore(context).read("courses"))
        store.remove("courses", "ACT2200"); assertTrue(store.read("courses").isEmpty())
        store.add("courses", "BUS1102"); store.clear("courses")
        assertEquals(listOf("Teacher"), store.read("instructors"))
        data["courses"] = "broken json"; assertTrue(store.read("courses").isEmpty())
    }
}
