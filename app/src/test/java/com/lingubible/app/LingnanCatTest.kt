package com.lingubible.app

import com.lingubible.app.domain.model.LingnanCatRepository
import org.junit.Assert.*
import org.junit.Test

class LingnanCatTest {

    @Test
    fun `campus cats repository contains the three classic lingnan cats`() {
        val cats = LingnanCatRepository.CATS
        assertEquals("Campus cats roster must contain exactly 3 cats", 3, cats.size)

        val catIds = cats.map { it.id }
        assertTrue("Must contain Wing On Gingey", catIds.contains("gingey"))
        assertTrue("Must contain Chuang Tuxedo", catIds.contains("tuxedo"))
        assertTrue("Must contain Can Boy Butter", catIds.contains("butter"))
    }

    @Test
    fun `each cat has complete bilingual metadata and valid attributes`() {
        LingnanCatRepository.CATS.forEach { cat ->
            assertTrue("Cat ID must not be blank", cat.id.isNotBlank())
            assertTrue("Cat Chinese name must not be blank", cat.nameZh.isNotBlank())
            assertTrue("Cat English name must not be blank", cat.nameEn.isNotBlank())
            assertTrue("Cat emoji must not be blank", cat.emoji.isNotBlank())
            assertTrue("Cat Chinese role must not be blank", cat.roleZh.isNotBlank())
            assertTrue("Cat English role must not be blank", cat.roleEn.isNotBlank())
            assertTrue("Cat locationZh must not be blank", cat.locationZh.isNotBlank())
            assertTrue("Cat personalityZh must not be blank", cat.personalityZh.isNotBlank())
            assertTrue("Cat must have at least 4 quotes", cat.quotes.size >= 4)
            assertTrue("Cat Tuen Mun trivia must not be blank", cat.tuenMunTrivia.isNotBlank())
        }
    }

    @Test
    fun `campus cats incorporate authentic lingnan campus locations and tuen mun culture`() {
        val allQuotesAndTrivia = LingnanCatRepository.CATS.flatMap { it.quotes + listOf(it.tuenMunTrivia, it.locationZh) }.joinToString(" ")

        // Lingnan campus landmarks
        val expectedLandmarks = listOf("永安廣場", "莊月明", "Can 仔")
        expectedLandmarks.forEach { landmark ->
            assertTrue("Cats content must feature Lingnan landmark '$landmark'", allQuotesAndTrivia.contains(landmark))
        }

        // Tuen Mun specialties & cultural keywords
        val expectedTuenMunCulture = listOf("屯門", "輕鐵", "紅橋", "置樂")
        expectedTuenMunCulture.forEach { keyword ->
            assertTrue("Cats content must feature Tuen Mun specialty keyword '$keyword'", allQuotesAndTrivia.contains(keyword))
        }

        // University student vernacular
        val expectedStudentTerms = listOf("過三", "assignment", "essay", "present", "溫書")
        expectedStudentTerms.forEach { term ->
            assertTrue("Cats content must feature university student vernacular '$term'", allQuotesAndTrivia.contains(term))
        }
    }

    @Test
    fun `getCatById returns correct cat or fallback`() {
        val gingey = LingnanCatRepository.getCatById("gingey")
        assertEquals("gingey", gingey.id)

        val tuxedo = LingnanCatRepository.getCatById("tuxedo")
        assertEquals("tuxedo", tuxedo.id)

        val butter = LingnanCatRepository.getCatById("butter")
        assertEquals("butter", butter.id)

        val unknownFallback = LingnanCatRepository.getCatById("non_existent")
        assertEquals("Fallback should be the first cat (gingey)", "gingey", unknownFallback.id)
    }
}
