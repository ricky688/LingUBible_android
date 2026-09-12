package com.lingubible.app.domain.model

data class PlatformStats(
    val verifiedStudentsCount: Int = 0,
    val verifiedStudentsLast30Days: Int = 0,
    val reviewsCount: Int = 0,
    val reviewsLast30Days: Int = 0,
    val coursesCount: Int = 0,
    val coursesLast30Days: Int = 0,
    val instructorsCount: Int = 0,
    val instructorsLast30Days: Int = 0
)
