package com.lingubible.app.domain.repository

import com.lingubible.app.domain.model.PlatformStats

interface StatsRepository {
    suspend fun getPlatformStats(): Result<PlatformStats>
}
