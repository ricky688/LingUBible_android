package com.lingubible.app

import android.app.Application
import com.lingubible.app.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class LingUBibleApp : Application() {
    override fun onCreate() {
        super.onCreate()

        // Apply dark/light theme mode globally before window creation for opening animation parity
        val prefs = getSharedPreferences("app_settings", MODE_PRIVATE)
        val themeCode = prefs.getString("theme_mode", "system")
        val nightMode = when (themeCode) {
            "light" -> androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
            "dark" -> androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
            else -> androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }
        androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(nightMode)

        startKoin {
            androidLogger(if (BuildConfig.DEBUG) Level.DEBUG else Level.NONE)
            androidContext(this@LingUBibleApp)
            modules(appModule)
        }
    }
}
