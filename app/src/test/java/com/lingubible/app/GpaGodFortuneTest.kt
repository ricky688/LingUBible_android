package com.lingubible.app

import com.lingubible.app.domain.calculator.GpaFortuneRepository
import org.junit.Assert.*
import org.junit.Test

class GpaGodFortuneTest {

    @Test
    fun `fortunes repository contains valid cantonese fortunes`() {
        val fortunes = GpaFortuneRepository.FORTUNES
        assertTrue("Fortunes repository must have at least 5 fortunes", fortunes.size >= 5)

        fortunes.forEach { fortune ->
            assertTrue("Fortune ID must be positive", fortune.id > 0)
            assertTrue("Fortune grade stamp must not be blank", fortune.grade.isNotBlank())
            assertTrue("Fortune title must not be blank", fortune.title.isNotBlank())
            assertTrue("Fortune poem must have at least 2 lines", fortune.poem.size >= 2)
            assertTrue("Fortune interpretation must not be blank", fortune.interpretation.isNotBlank())
            assertTrue("Fortune appropriate advice must not be blank", fortune.oracleAppropriate.isNotBlank())
            assertTrue("Fortune inappropriate advice must not be blank", fortune.oracleInappropriate.isNotBlank())
        }
    }

    @Test
    fun `four flat cgpa returns god tier fortune`() {
        val godFortune = GpaFortuneRepository.getRandomFortune(isFourFlat = true)
        assertTrue("God tier fortune must have isGodTier = true", godFortune.isGodTier)
        assertTrue("Title should mention 4.0", godFortune.title.contains("4.0"))
        assertTrue("Poem should praise 4.0 achievement", godFortune.poem.any { it.contains("四點零") || it.contains("4.0") })
    }

    @Test
    fun `getRandomFortune with excludeId selects different fortune`() {
        val first = GpaFortuneRepository.getRandomFortune(isFourFlat = false)
        val second = GpaFortuneRepository.getRandomFortune(isFourFlat = false, excludeId = first.id)
        assertNotEquals("Successive re-rolls should exclude previous fortune if pool allows", first.id, second.id)
    }

    @Test
    fun `cantonese vernacular terms are preserved in fortunes`() {
        val allPoemAndInterp = GpaFortuneRepository.FORTUNES.joinToString(" ") {
            it.title + " " + it.poem.joinToString(" ") + " " + it.interpretation
        }
        val expectedCantoneseKeywords = listOf("唔使", "靚 Grade", "天地線", "拉 Curve", "早八", "Free-rider")
        expectedCantoneseKeywords.forEach { keyword ->
            assertTrue("Fortunes should contain authentic student keyword '$keyword'", allPoemAndInterp.contains(keyword))
        }
    }
}
