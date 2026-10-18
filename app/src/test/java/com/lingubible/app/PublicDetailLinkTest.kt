package com.lingubible.app

import com.lingubible.app.domain.model.*
import org.junit.Assert.*
import org.junit.Test

class PublicDetailLinkTest {
    @Test fun publicLinksRoundTripEncodedNamesAndNormalizeCourseCodes() {
        for (name in listOf("CHUNG Yu Ho", "陳教授", "A+B", "O'Connor")) assertEquals(PublicDetailLink("instructors", name), parsePublicDetailLink(publicDetailUrl("instructors", name)))
        assertEquals(PublicDetailLink("courses", "ACT2200"), parsePublicDetailLink(publicDetailUrl("courses", "act2200")))
    }
    @Test fun knownWebTabLinksOpenTheirMatchingNativeSection() {
        assertEquals(1, parsePublicDetailLink("https://lingubible.com/courses/ACT2200/grades")!!.tab)
        assertEquals(3, parsePublicDetailLink("https://lingubible.com/courses/ACT2200/teaching")!!.tab)
        assertEquals(1, parsePublicDetailLink("https://lingubible.com/instructors/CHUNG%20Yu%20Ho/reviews")!!.tab)
        assertEquals(2, parsePublicDetailLink("https://lingubible.com/instructors/CHUNG%20Yu%20Ho/grades")!!.tab)
    }
    @Test fun linksRejectForeignOriginsUnexpectedPathsAndEncodedSeparators() {
        for (link in listOf("http://lingubible.com/courses/ACT2200", "https://lingubible.com.evil.test/courses/ACT2200", "https://a@lingubible.com/courses/ACT2200", "https://lingubible.com:444/courses/ACT2200", "https://lingubible.com/instructors/A%2FB", "https://lingubible.com/instructors/A%0AB", "https://lingubible.com/courses/ACT2200?token=secret", "https://lingubible.com/courses/ACT2200/unknown", "https://lingubible.com/instructors/")) assertNull(link, parsePublicDetailLink(link))
    }
    @Test fun recentQueriesAreBoundedOrderedTrimmedAndDeduplicated() {
        val history = (1..30).fold(emptyList<String>()) { values, n -> addRecentQuery(values, "Query $n") }
        assertEquals(20, history.size); assertEquals("Query 30", history.first())
        val reordered = addRecentQuery(history, " query 20 ")
        assertEquals(20, reordered.size); assertEquals("query 20", reordered.first())
        assertEquals(history, addRecentQuery(history, "  ")); assertEquals(history, addRecentQuery(history, "password\nvalue"))
    }
}
