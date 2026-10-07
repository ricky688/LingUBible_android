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

object FileUtils {
    fun formatFileSize(bytes: Long): String {
        if (bytes <= 0L) return "0 B"
        val kb = 1024L
        val mb = kb * 1024L
        val gb = mb * 1024L
        return when {
            bytes >= gb -> String.format(java.util.Locale.US, "%.1f GB", bytes.toDouble() / gb)
            bytes >= mb -> String.format(java.util.Locale.US, "%.1f MB", bytes.toDouble() / mb)
            bytes >= kb -> {
                if (bytes % kb == 0L) {
                    "${bytes / kb} KB"
                } else {
                    String.format(java.util.Locale.US, "%.1f KB", bytes.toDouble() / kb)
                }
            }
            else -> "$bytes B"
        }
    }
}

object PastPapersParser {
    // 1. Compact short format: e.g. CDS2004_24252.pdf, CDS2004_24251_Simmons.pdf, CDS2004_24251-Simmons.pdf, CDS2004_24251(Simmons).pdf, CDS2004 - 24251.pdf
    private val PATTERN_SHORT = "^([A-Za-z]{3,4}\\d{4}[A-Za-z]?)[ _\\-]+(\\d{2})(\\d{2})([0-9Ss])(?:[ _\\-(]+(.+?)\\)?)?\\s*\\.pdf$".toRegex(RegexOption.IGNORE_CASE)

    // 2. Compact 4-digit year with separated term: e.g. CDS2004_2425_Term1.pdf, CDS2004_2425_Sem2.pdf, CDS2004_2425_T1.pdf, CDS2004_2425_Summer.pdf, CDS2004_2425_1.pdf
    private val PATTERN_COMPACT_YEAR_TERM = "^([A-Za-z]{3,4}\\d{4}[A-Za-z]?)[ _\\-]+(\\d{2})(\\d{2})[ _\\-]+(?:(?:Term|Sem|Semester)[ _\\-]?([0-9Ss]|Summer)|T[ _\\-]?([0-9Ss])|([0-9Ss])|(Summer(?:[ _\\-]?Term)?))(?:[ _\\-(]+(.+?)\\)?)?\\s*\\.pdf$".toRegex(RegexOption.IGNORE_CASE)

    // 3. Expanded/delimited, single 4-digit, or 8-digit years: e.g. BUS1102_2023-2024_Term1.pdf, BUS1102_2023_2024_Term1.pdf, BUS1102_20242025_Term1.pdf, BUS1102_23-24_Term1.pdf, BUS1102_2024_Term1.pdf
    private val PATTERN_EXPANDED = "^([A-Za-z]{3,4}\\d{4}[A-Za-z]?)[ _\\-]+(?:(\\d{4})(\\d{4})|(\\d{2}|\\d{4})[-/_](\\d{2,4})|(\\d{4}))[ _\\-]+(?:(?:Term|Sem|Semester)[ _\\-]?([0-9Ss]|Summer)|T[ _\\-]?([0-9Ss])|([0-9Ss])|(Summer(?:[ _\\-]?Term)?))(?:[ _\\-(]+(.+?)\\)?)?\\s*\\.pdf$".toRegex(RegexOption.IGNORE_CASE)

    private val EXAM_TYPE_WORDS = setOf(
        "final", "midterm", "exam", "final exam", "midterm exam",
        "solution", "solutions", "mock", "paper", "quiz", "test", "answers", "ans"
    )

    data class ParsedInfo(
        val courseCode: String,
        val academicYear: String,
        val term: String,
        val instructor: String? = null
    )

    private fun cleanInstructor(raw: String?): String? {
        if (raw.isNullOrBlank()) return null
        var trimmed = raw.trim().trim('(', ')', '-', '_', ' ')
        if (trimmed.isBlank()) return null
        if (trimmed.lowercase() in EXAM_TYPE_WORDS) return null
        for (word in listOf("final", "midterm", "exam", "solution", "solutions", "mock")) {
            if (trimmed.lowercase().startsWith("$word ") ||
                trimmed.lowercase().startsWith("$word-") ||
                trimmed.lowercase().startsWith("${word}_")
            ) {
                trimmed = trimmed.substring(word.length).trim('(', ')', '-', '_', ' ')
            }
            if (trimmed.lowercase().endsWith(" $word") ||
                trimmed.lowercase().endsWith("-$word") ||
                trimmed.lowercase().endsWith("_$word")
            ) {
                trimmed = trimmed.substring(0, trimmed.length - word.length).trim('(', ')', '-', '_', ' ')
            }
        }
        return trimmed.ifBlank { null }
    }

    private fun normalizeTerm(rawTerm: String): String {
        val upper = rawTerm.trim().uppercase()
        return when {
            upper == "1" || upper == "T1" -> "Term 1"
            upper == "2" || upper == "T2" -> "Term 2"
            upper in listOf("0", "3", "S", "SUMMER") || upper.startsWith("SUMMER") -> "Summer Term"
            else -> "Term $upper"
        }
    }

    fun parseFilename(filename: String): ParsedInfo? {
        val trimmed = filename.trim()

        val matchShort = PATTERN_SHORT.matchEntire(trimmed)
        if (matchShort != null) {
            val code = matchShort.groupValues[1].uppercase()
            val yy1 = matchShort.groupValues[2]
            val yy2 = matchShort.groupValues[3]
            val termDigit = matchShort.groupValues[4]
            val instructorRaw = matchShort.groupValues[5]
            val year = "20$yy1-20$yy2"
            val term = normalizeTerm(termDigit)
            val instructor = cleanInstructor(instructorRaw)
            return ParsedInfo(code, year, term, instructor)
        }

        val matchCompact = PATTERN_COMPACT_YEAR_TERM.matchEntire(trimmed)
        if (matchCompact != null) {
            val code = matchCompact.groupValues[1].uppercase()
            val yy1 = matchCompact.groupValues[2]
            val yy2 = matchCompact.groupValues[3]

            val t1 = matchCompact.groupValues[4]
            val t2 = matchCompact.groupValues[5]
            val t3 = matchCompact.groupValues[6]
            val t4 = matchCompact.groupValues[7]
            val rawTerm = listOf(t1, t2, t3, t4).firstOrNull { it.isNotBlank() } ?: "1"
            val term = normalizeTerm(rawTerm)

            val y1Num = yy1.toIntOrNull() ?: 0
            val y2Num = yy2.toIntOrNull() ?: 0
            val year = if ((y1Num + 1) % 100 == y2Num) {
                "20$yy1-20$yy2"
            } else if (yy1 == "20") {
                val calYear = 2000 + y2Num
                if (term == "Term 1") "$calYear-${calYear + 1}" else "${calYear - 1}-$calYear"
            } else {
                "20$yy1-20$yy2"
            }

            val instructorRaw = matchCompact.groupValues[8]
            val instructor = cleanInstructor(instructorRaw)
            return ParsedInfo(code, year, term, instructor)
        }

        val matchExp = PATTERN_EXPANDED.matchEntire(trimmed)
        if (matchExp != null) {
            val code = matchExp.groupValues[1].uppercase()
            val raw8Y1 = matchExp.groupValues[2]
            val raw8Y2 = matchExp.groupValues[3]
            val rawDelimY1 = matchExp.groupValues[4]
            val rawDelimY2 = matchExp.groupValues[5]
            val rawSingleYear = matchExp.groupValues[6]

            val t1 = matchExp.groupValues[7]
            val t2 = matchExp.groupValues[8]
            val t3 = matchExp.groupValues[9]
            val t4 = matchExp.groupValues[10]
            val rawTerm = listOf(t1, t2, t3, t4).firstOrNull { it.isNotBlank() } ?: "1"
            val term = normalizeTerm(rawTerm)

            val academicYear = when {
                raw8Y1.isNotBlank() && raw8Y2.isNotBlank() -> "$raw8Y1-$raw8Y2"
                rawSingleYear.isNotBlank() -> {
                    val sYear = rawSingleYear.toIntOrNull() ?: 2024
                    if (term == "Term 1") "$sYear-${sYear + 1}" else "${sYear - 1}-$sYear"
                }
                else -> {
                    val startYear = if (rawDelimY1.length == 2) "20$rawDelimY1" else rawDelimY1
                    val century = startYear.take(2)
                    val endYear = if (rawDelimY2.length == 2) "$century$rawDelimY2" else rawDelimY2
                    "$startYear-$endYear"
                }
            }

            val instructorRaw = matchExp.groupValues[11]
            val instructor = cleanInstructor(instructorRaw)
            return ParsedInfo(code, academicYear, term, instructor)
        }

        return null
    }

    fun getTermSortKey(academicYear: String, term: String): Int {
        val startYear = academicYear.substringBefore("-").toIntOrNull() ?: 0
        val termRank = when (term) {
            "Term 1" -> 1
            "Term 2" -> 2
            "Summer Term" -> 3
            else -> 0
        }
        return startYear * 10 + termRank
    }

    fun formatFileSize(bytes: Long): String = FileUtils.formatFileSize(bytes)
}
