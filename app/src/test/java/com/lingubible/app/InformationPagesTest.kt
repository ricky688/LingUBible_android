package com.lingubible.app

import com.lingubible.app.domain.model.*
import kotlinx.serialization.json.Json
import org.junit.Assert.*
import org.junit.Test
import java.io.File

class InformationPagesTest {
    @Test fun bundledPoliciesDecodeWithAllSectionsAndBothLanguages() {
        for ((name, expected) in listOf("terms" to 8, "privacy" to 9)) {
            val file = listOf(File("src/main/assets/information/$name.json"), File("app/src/main/assets/information/$name.json")).first { it.exists() }
            val document = Json.decodeFromString<InformationDocument>(file.readText())
            assertEquals(expected, document.sections.size)
            assertEquals(expected, document.sections.map { it.id }.distinct().size)
            val text = listOf(document.title, document.subtitle, document.updated) + document.sections.flatMap { listOf(it.title) + it.paragraphs }
            assertTrue(text.all { it.zh.isNotBlank() && it.en.isNotBlank() })
            assertTrue(document.updated.en.contains("2025"))
        }
    }
    @Test fun faqAndChangelogCoverNativeFlowsAndHaveNoWebReleaseHistory() {
        assertEquals(5,InformationPage.entries.size)
        assertEquals("Android Changelog",InformationPage.CHANGELOG.title(false))
        assertTrue(ANDROID_FAQ.any { it.id == "recovery" })
        assertTrue(ANDROID_FAQ.any { it.id == "backup" })
        val entries = ANDROID_CHANGELOG.flatMap { listOf(it.title) + it.paragraphs }
        assertTrue(entries.all { it.zh.isNotBlank() && it.en.isNotBlank() })
        assertTrue(entries.any { it.en.contains("native password recovery") })
        assertFalse(entries.any { it.en.contains("web release",true) || it.en.contains("Vite") })
    }
}
