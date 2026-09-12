package com.lingubible.app

import com.lingubible.app.di.AppConfig
import com.lingubible.app.di.appModule
import com.lingubible.app.ui.common.MainViewModel
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.test.KoinTest
import org.koin.test.get

class KoinModuleTest : KoinTest {

    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun `verify appModule resolves AppConfig and MainViewModel`() {
        startKoin {
            modules(appModule)
        }

        val appConfig = get<AppConfig>()
        assertNotNull(appConfig)
        assertEquals("https://appwrite.lingubible.com/v1", appConfig.endpoint)
        assertEquals("6a1097400037a55f6472", appConfig.projectId)
        assertEquals("lingubible", appConfig.databaseId)

        val mainViewModel = get<MainViewModel>()
        assertNotNull(mainViewModel)
        assertEquals("6a1097400037a55f6472", mainViewModel.uiState.value.appConfig.projectId)
    }
}
