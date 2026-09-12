package com.lingubible.app.domain.model

data class User(
    val id: String,
    val name: String,
    val email: String,
    val emailVerification: Boolean = false,
    val status: Boolean = true,
    val registrationDate: String = ""
)

data class Session(
    val id: String,
    val userId: String,
    val expire: String = "",
    val provider: String = "email"
)

data class Course(
    val id: String,
    val code: String,
    val titleEn: String,
    val titleZh: String = "",
    val department: String = "",
    val credits: Int = 3,
    val description: String = "",
    val reviewCount: Int = 0,
    val avgRating: Double = 0.0,
    val avgWorkload: Double = 0.0,
    val avgDifficulty: Double = 0.0,
    val avgGrade: String = "N/A"
)

data class Instructor(
    val id: String,
    val name: String,
    val nameZh: String = "",
    val department: String = "",
    val title: String = "",
    val reviewCount: Int = 0,
    val avgRating: Double = 0.0,
    val teachingSession: String = "Lecture"
) {
    val displayName: String
        get() {
            if (title.isBlank()) return name
            val cleanTitle = title.trim().removeSuffix(".")
            val enTitle = "$cleanTitle."
            return if (!name.startsWith(enTitle, ignoreCase = true)) {
                "$enTitle $name"
            } else {
                name
            }
        }

    val displayNameZh: String
        get() {
            if (nameZh.isBlank()) return ""
            val cleanTitle = title.trim().removeSuffix(".")
            val zhTitle = when (cleanTitle.lowercase()) {
                "dr" -> "博士"
                "prof", "professor" -> "教授"
                "ms" -> "女士"
                "mr" -> "先生"
                "mrs" -> "女士"
                "miss" -> "小姐"
                else -> ""
            }
            return if (zhTitle.isNotBlank() && !nameZh.endsWith(zhTitle)) {
                "$nameZh$zhTitle"
            } else {
                nameZh
            }
        }
}

data class TeachingRecord(
    val id: String,
    val courseCode: String,
    val instructorName: String,
    val instructorNameZh: String = "",
    val term: String = "",
    val academicYear: String = "",
    val section: String = ""
)

data class Review(
    val id: String,
    val courseCode: String,
    val courseTitle: String = "",
    val courseTitleZh: String = "",
    val instructorName: String = "",
    val instructorNameZh: String = "",
    val term: String = "",
    val academicYear: String = "",
    val grade: String = "N/A",
    val workloadRating: Double = 3.0,
    val difficultyRating: Double = 3.0,
    val gradingRating: Double = 3.0,
    val teachingRating: Double = 3.0,
    val comment: String = "",
    val upvotes: Int = 0,
    val downvotes: Int = 0,
    val userVote: String? = null,
    val createdAt: String = "",
    val userId: String = "",
    val isAnonymous: Boolean = true
)

data class ReviewSubmission(
    val courseCode: String,
    val courseTitle: String,
    val instructorName: String,
    val term: String,
    val academicYear: String,
    val grade: String,
    val workloadRating: Double,
    val difficultyRating: Double,
    val gradingRating: Double,
    val teachingRating: Double,
    val comment: String,
    val isAnonymous: Boolean = true
)

data class VoteState(
    val upvotes: Int,
    val downvotes: Int,
    val userVote: String?
)

data class EligibilityResult(
    val canSubmit: Boolean,
    val reasonKey: String? = null
)

data class GradeStatistics(
    val mean: Double?,
    val standardDeviation: Double?,
    val totalCount: Int,
    val validGradeCount: Int,
    val gradeDistribution: Map<String, Int>
)

data class BoxPlotStatistics(
    val min: Double,
    val q1: Double,
    val median: Double,
    val q3: Double,
    val max: Double,
    val outliers: List<Double>,
    val validCount: Int
)

data class ParetoPoint(
    val grade: String,
    val count: Int,
    val percentage: Double,
    val cumulativePercentage: Double
)

data class PastPaper(
    val id: String,
    val courseCode: String,
    val academicYear: String,
    val term: String,
    val instructorName: String? = null,
    val fileId: String,
    val fileName: String,
    val fileSize: Long = 0L,
    val downloadUrl: String = "",
    val viewUrl: String = ""
)
