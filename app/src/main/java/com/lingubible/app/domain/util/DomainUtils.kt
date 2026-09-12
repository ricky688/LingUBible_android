package com.lingubible.app.domain.util

import com.lingubible.app.domain.model.BoxPlotStatistics
import com.lingubible.app.domain.model.GradeStatistics
import com.lingubible.app.domain.model.ParetoPoint
import kotlin.math.floor
import kotlin.math.round
import kotlin.math.sqrt

object EmailValidator {
    private val EMAIL_REGEX = "^[A-Za-z0-9._%+-]+@(?:ln\\.hk|ln\\.edu\\.hk)$".toRegex(RegexOption.IGNORE_CASE)

    fun isValidLingnanEmail(email: String?): Boolean {
        if (email.isNullOrBlank()) return false
        return EMAIL_REGEX.matches(email.trim())
    }
}

object WordCountValidator {
    fun countWords(text: String?): Int {
        if (text.isNullOrBlank()) return 0
        return text.trim().split("\\s+".toRegex()).size
    }

    fun isValid(text: String?, minWords: Int = 5, maxWords: Int = 1000): Boolean {
        val count = countWords(text)
        return count in minWords..maxWords
    }
}

object RatingValidator {
    fun isValidRating(rating: Double): Boolean {
        if (rating == -1.0) return true
        if (rating < 0.5 || rating > 5.0) return false
        val multiplied = (rating * 2).toInt()
        return (rating * 2) == multiplied.toDouble()
    }

    fun clampRating(rating: Double): Double {
        if (rating == -1.0) return -1.0
        if (rating < 0.5) return 0.5
        if (rating > 5.0) return 5.0
        return round(rating * 2) / 2.0
    }
}

object GradeCalculator {
    val GRADE_TO_GPA_MAP: Map<String, Double> = mapOf(
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

    val GRADE_ORDER = listOf("A", "A-", "B+", "B", "B-", "C+", "C", "C-", "D+", "D", "F", "N/A")

    fun calculateGradeStatistics(distribution: Map<String, Int>): GradeStatistics {
        val totalCount = distribution.values.sum()
        if (totalCount == 0) {
            return GradeStatistics(null, null, 0, 0, distribution)
        }

        var totalGPA = 0.0
        var validGradeCount = 0

        distribution.forEach { (grade, count) ->
            if (grade != "N/A" && count > 0) {
                val gpa = GRADE_TO_GPA_MAP[grade]
                if (gpa != null) {
                    totalGPA += gpa * count
                    validGradeCount += count
                }
            }
        }

        if (validGradeCount == 0) {
            return GradeStatistics(null, null, totalCount, 0, distribution)
        }

        val mean = totalGPA / validGradeCount

        var varianceSum = 0.0
        distribution.forEach { (grade, count) ->
            if (grade != "N/A" && count > 0) {
                val gpa = GRADE_TO_GPA_MAP[grade]
                if (gpa != null) {
                    val dev = gpa - mean
                    varianceSum += (dev * dev) * count
                }
            }
        }

        val variance = varianceSum / validGradeCount
        val standardDeviation = sqrt(variance)

        return GradeStatistics(
            mean = mean,
            standardDeviation = standardDeviation,
            totalCount = totalCount,
            validGradeCount = validGradeCount,
            gradeDistribution = distribution
        )
    }

    fun calculateBoxPlotStatistics(distribution: Map<String, Int>): BoxPlotStatistics? {
        val gpaValues = mutableListOf<Double>()
        distribution.forEach { (grade, count) ->
            if (grade != "N/A" && count > 0) {
                val gpa = GRADE_TO_GPA_MAP[grade]
                if (gpa != null) {
                    repeat(count) { gpaValues.add(gpa) }
                }
            }
        }

        if (gpaValues.isEmpty()) return null
        val sorted = gpaValues.sorted()
        val n = sorted.size

        val q1Index = floor(n * 0.25).toInt()
        val medianIndex = floor(n * 0.5).toInt()
        val q3Index = floor(n * 0.75).toInt()

        val q1 = sorted[q1Index]
        val median = if (n % 2 == 0 && n > 1) {
            (sorted[medianIndex - 1] + sorted[medianIndex]) / 2.0
        } else {
            sorted[medianIndex]
        }
        val q3 = sorted[q3Index]

        val iqr = q3 - q1
        val lowerBound = q1 - 1.5 * iqr
        val upperBound = q3 + 1.5 * iqr

        val outliers = sorted.filter { it < lowerBound || it > upperBound }
        val nonOutliers = sorted.filter { it in lowerBound..upperBound }

        val min = if (nonOutliers.isNotEmpty()) nonOutliers.first() else sorted.first()
        val max = if (nonOutliers.isNotEmpty()) nonOutliers.last() else sorted.last()

        return BoxPlotStatistics(
            min = min,
            q1 = q1,
            median = median,
            q3 = q3,
            max = max,
            outliers = outliers,
            validCount = n
        )
    }

    fun calculateParetoCurve(distribution: Map<String, Int>): List<ParetoPoint> {
        val validGrades = listOf("A", "A-", "B+", "B", "B-", "C+", "C", "C-", "D+", "D", "F")
        val validTotal = validGrades.sumOf { distribution[it] ?: 0 }
        if (validTotal == 0) return emptyList()

        var runningCount = 0
        return validGrades.map { grade ->
            val count = distribution[grade] ?: 0
            runningCount += count
            val pct = (count.toDouble() / validTotal) * 100.0
            val cumPct = (runningCount.toDouble() / validTotal) * 100.0
            ParetoPoint(
                grade = grade,
                count = count,
                percentage = pct,
                cumulativePercentage = cumPct
            )
        }
    }
}

object PastPapersParser {
    private val PATTERN_A = "^([A-Z]{3,4}\\d{4})_(\\d{4})(\\d)(?:_(.+))?\\.pdf$".toRegex(RegexOption.IGNORE_CASE)
    private val PATTERN_B = "^([A-Z]{3,4}\\d{4})_(\\d{4}-\\d{4})_Term(\\d)(?:_(.+))?\\.pdf$".toRegex(RegexOption.IGNORE_CASE)

    data class ParsedInfo(
        val courseCode: String,
        val academicYear: String,
        val term: String,
        val instructor: String? = null
    )

    fun parseFilename(filename: String): ParsedInfo? {
        val matchA = PATTERN_A.matchEntire(filename)
        if (matchA != null) {
            val (code, yyRaw, termDigit, instructor) = matchA.destructured
            val year = "20${yyRaw.substring(0, 2)}-20${yyRaw.substring(2, 4)}"
            val term = "Term $termDigit"
            return ParsedInfo(code.uppercase(), year, term, instructor.ifBlank { null })
        }

        val matchB = PATTERN_B.matchEntire(filename)
        if (matchB != null) {
            val (code, year, termDigit, instructor) = matchB.destructured
            val term = "Term $termDigit"
            return ParsedInfo(code.uppercase(), year, term, instructor.ifBlank { null })
        }

        return null
    }
}
