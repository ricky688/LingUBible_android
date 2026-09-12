package com.lingubible.app

import com.lingubible.app.domain.calculator.HonoursCalculator
import org.junit.Assert.*
import org.junit.Test

class HonoursCalculatorTest {

    @Test
    fun testGradePoints() {
        assertEquals(4.00, HonoursCalculator.getGradePoint("A")!!, 0.001)
        assertEquals(3.67, HonoursCalculator.getGradePoint("A-")!!, 0.001)
        assertEquals(3.33, HonoursCalculator.getGradePoint("B+")!!, 0.001)
        assertEquals(3.00, HonoursCalculator.getGradePoint("B")!!, 0.001)
        assertEquals(2.67, HonoursCalculator.getGradePoint("B-")!!, 0.001)
        assertEquals(2.33, HonoursCalculator.getGradePoint("C+")!!, 0.001)
        assertEquals(2.00, HonoursCalculator.getGradePoint("C")!!, 0.001)
        assertEquals(1.67, HonoursCalculator.getGradePoint("C-")!!, 0.001)
        assertEquals(1.33, HonoursCalculator.getGradePoint("D+")!!, 0.001)
        assertEquals(1.00, HonoursCalculator.getGradePoint("D")!!, 0.001)
        assertEquals(0.00, HonoursCalculator.getGradePoint("F")!!, 0.001)
        assertNull(HonoursCalculator.getGradePoint("PASS"))
        assertNull(HonoursCalculator.getGradePoint("FAIL"))
        assertNull(HonoursCalculator.getGradePoint("I"))
    }

    @Test
    fun testHonoursClassification() {
        assertEquals(HonoursCalculator.HonoursTier.FIRST, HonoursCalculator.classifyHonours(3.85))
        assertEquals(HonoursCalculator.HonoursTier.FIRST, HonoursCalculator.classifyHonours(3.50))
        assertEquals(HonoursCalculator.HonoursTier.UPPER_SECOND, HonoursCalculator.classifyHonours(3.49))
        assertEquals(HonoursCalculator.HonoursTier.UPPER_SECOND, HonoursCalculator.classifyHonours(3.00))
        assertEquals(HonoursCalculator.HonoursTier.LOWER_SECOND, HonoursCalculator.classifyHonours(2.99))
        assertEquals(HonoursCalculator.HonoursTier.LOWER_SECOND, HonoursCalculator.classifyHonours(2.50))
        assertEquals(HonoursCalculator.HonoursTier.THIRD, HonoursCalculator.classifyHonours(2.49))
        assertEquals(HonoursCalculator.HonoursTier.THIRD, HonoursCalculator.classifyHonours(2.00))
        assertEquals(HonoursCalculator.HonoursTier.PASS, HonoursCalculator.classifyHonours(1.99))
        assertEquals(HonoursCalculator.HonoursTier.PASS, HonoursCalculator.classifyHonours(1.67))
        assertNull(HonoursCalculator.classifyHonours(1.66))
        assertNull(HonoursCalculator.classifyHonours(null))
    }

    @Test
    fun testYearAwardClassification() {
        // President's List: >= 3.70, >= 24 credits, max term >= 12, no disqualifying grades
        assertEquals(
            HonoursCalculator.YearAward.PRESIDENTS_LIST,
            HonoursCalculator.classifyYearAward(
                yearGpa = 3.75,
                yearCredits = 30.0,
                maxTermCredits = 15.0,
                hasDisqualifyingGrade = false
            )
        )

        // Dean's List: 3.30 - 3.69
        assertEquals(
            HonoursCalculator.YearAward.DEANS_LIST,
            HonoursCalculator.classifyYearAward(
                yearGpa = 3.45,
                yearCredits = 30.0,
                maxTermCredits = 15.0,
                hasDisqualifyingGrade = false
            )
        )

        // Disqualified if has F / FAIL / U / I
        assertNull(
            HonoursCalculator.classifyYearAward(
                yearGpa = 3.80,
                yearCredits = 30.0,
                maxTermCredits = 15.0,
                hasDisqualifyingGrade = true
            )
        )

        // Disqualified if year credits < 24
        assertNull(
            HonoursCalculator.classifyYearAward(
                yearGpa = 3.80,
                yearCredits = 21.0,
                maxTermCredits = 15.0,
                hasDisqualifyingGrade = false
            )
        )

        // Disqualified if max term credits < 12
        assertNull(
            HonoursCalculator.classifyYearAward(
                yearGpa = 3.80,
                yearCredits = 24.0,
                maxTermCredits = 9.0,
                hasDisqualifyingGrade = false
            )
        )
    }

    @Test
    fun testRequiredRemainingAvg() {
        // Feasible case: earned 60 credits at 3.4 (204 points), target 3.5 with 60 credits remaining
        // total credits = 120, total target points = 420. required = (420 - 204)/60 = 216/60 = 3.6
        val resultFeasible = HonoursCalculator.calculateRequiredRemainingAvg(
            earnedPoints = 204.0,
            earnedCredits = 60.0,
            remainingCredits = 60.0,
            targetCgpa = 3.50
        )
        assertEquals(HonoursCalculator.RequiredAvgStatus.FEASIBLE, resultFeasible.status)
        assertEquals(3.60, resultFeasible.required, 0.001)

        // Impossible case: earned 90 credits at 2.0 (180 points), target 3.8 with 30 credits remaining
        // required = (3.8 * 120 - 180)/30 = (456 - 180)/30 = 276/30 = 9.2 > 4.0
        val resultImpossible = HonoursCalculator.calculateRequiredRemainingAvg(
            earnedPoints = 180.0,
            earnedCredits = 90.0,
            remainingCredits = 30.0,
            targetCgpa = 3.80
        )
        assertEquals(HonoursCalculator.RequiredAvgStatus.IMPOSSIBLE, resultImpossible.status)

        // Achieved case: already secured target even with minimum 1.0 term gpa
        val resultAchieved = HonoursCalculator.calculateRequiredRemainingAvg(
            earnedPoints = 400.0,
            earnedCredits = 100.0,
            remainingCredits = 10.0,
            targetCgpa = 3.00
        )
        assertEquals(HonoursCalculator.RequiredAvgStatus.ACHIEVED, resultAchieved.status)
    }
}
