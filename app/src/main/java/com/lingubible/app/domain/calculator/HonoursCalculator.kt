package com.lingubible.app.domain.calculator

import kotlinx.serialization.Serializable

object HonoursCalculator {
    const val MAX_GPA = 4.0
    const val MIN_TERM_GPA = 1.0

    val GPA_BEARING_GRADES = listOf(
        "A", "A-", "B+", "B", "B-", "C+", "C", "C-", "D+", "D", "F"
    )

    val NON_GPA_GRADES = listOf(
        "I", "M", "VS", "S", "U", "PASS", "FAIL"
    )

    val AWARD_DISQUALIFYING_GRADES = listOf(
        "F", "FAIL", "U", "I"
    )

    val GRADE_POINTS: Map<String, Double> = mapOf(
        "A" to 4.00,
        "A-" to 3.67,
        "B+" to 3.33,
        "B" to 3.00,
        "B-" to 2.67,
        "C+" to 2.33,
        "C" to 2.00,
        "C-" to 1.67,
        "D+" to 1.33,
        "D" to 1.00,
        "F" to 0.00
    )

    fun isGpaBearing(grade: String?): Boolean {
        if (grade.isNullOrBlank()) return false
        return GPA_BEARING_GRADES.contains(grade.trim().uppercase())
    }

    fun isAwardDisqualifying(grade: String?): Boolean {
        if (grade.isNullOrBlank()) return false
        return AWARD_DISQUALIFYING_GRADES.contains(grade.trim().uppercase())
    }

    fun getGradePoint(grade: String?): Double? {
        if (grade.isNullOrBlank()) return null
        return GRADE_POINTS[grade.trim().uppercase()]
    }

    enum class HonoursTier(val minCgpa: Double, val titleZh: String, val titleEn: String) {
        FIRST(3.50, "甲等榮譽", "First Class Honours"),
        UPPER_SECOND(3.00, "乙等一級榮譽", "Upper Second (2:1)"),
        LOWER_SECOND(2.50, "乙等二級榮譽", "Lower Second (2:2)"),
        THIRD(2.00, "丙等榮譽", "Third Class Honours"),
        PASS(1.67, "及格", "Pass")
    }

    enum class YearAward(val titleZh: String, val titleEn: String) {
        PRESIDENTS_LIST("校長榮譽榜", "President's List"),
        DEANS_LIST("院長榮譽榜", "Dean's List")
    }

    enum class RequiredAvgStatus {
        ACHIEVED,
        FEASIBLE,
        IMPOSSIBLE,
        NO_REMAINING
    }

    data class RequiredAvgResult(
        val required: Double,
        val status: RequiredAvgStatus,
        val projectedCgpa: Double
    )

    fun classifyHonours(cgpa: Double?): HonoursTier? {
        if (cgpa == null || cgpa.isNaN()) return null
        for (tier in HonoursTier.entries) {
            if (cgpa >= tier.minCgpa - 1e-9) return tier
        }
        return null
    }

    fun classifyYearAward(
        yearGpa: Double?,
        yearCredits: Double,
        maxTermCredits: Double,
        hasDisqualifyingGrade: Boolean
    ): YearAward? {
        if (yearGpa == null || yearGpa.isNaN() || hasDisqualifyingGrade) return null
        if (yearCredits < 24.0) return null
        if (maxTermCredits < 12.0) return null
        if (yearGpa >= 3.70 - 1e-9) return YearAward.PRESIDENTS_LIST
        if (yearGpa >= 3.30 - 1e-9) return YearAward.DEANS_LIST
        return null
    }

    fun calculateRequiredRemainingAvg(
        earnedPoints: Double,
        earnedCredits: Double,
        remainingCredits: Double,
        targetCgpa: Double
    ): RequiredAvgResult {
        if (remainingCredits <= 0.0) {
            val currentCgpa = if (earnedCredits > 0.0) earnedPoints / earnedCredits else 0.0
            return RequiredAvgResult(
                required = 0.0,
                status = if (currentCgpa >= targetCgpa - 1e-9) RequiredAvgStatus.ACHIEVED else RequiredAvgStatus.NO_REMAINING,
                projectedCgpa = currentCgpa
            )
        }

        val totalCredits = earnedCredits + remainingCredits
        val required = (targetCgpa * totalCredits - earnedPoints) / remainingCredits
        val guaranteedCgpa = (earnedPoints + MIN_TERM_GPA * remainingCredits) / totalCredits

        if (required <= MIN_TERM_GPA || guaranteedCgpa >= targetCgpa - 1e-9) {
            return RequiredAvgResult(
                required = maxOf(0.0, required),
                status = RequiredAvgStatus.ACHIEVED,
                projectedCgpa = guaranteedCgpa
            )
        }

        if (required > MAX_GPA + 1e-9) {
            return RequiredAvgResult(
                required = required,
                status = RequiredAvgStatus.IMPOSSIBLE,
                projectedCgpa = (earnedPoints + MAX_GPA * remainingCredits) / totalCredits
            )
        }

        return RequiredAvgResult(
            required = required,
            status = RequiredAvgStatus.FEASIBLE,
            projectedCgpa = targetCgpa
        )
    }
}
