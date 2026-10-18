package com.lingubible.app.ui.screens

import androidx.compose.runtime.*
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lingubible.app.core.navigation.Screen
import com.lingubible.app.core.navigation.screenBottomPadding
import com.lingubible.app.core.settings.AppSettingsManager
import com.lingubible.app.ui.components.GettingStartedGuide
import org.koin.compose.koinInject

@Composable
fun GuideScreen(onClose: () -> Unit, onNavigate: (Screen) -> Unit) {
    val settings = koinInject<AppSettingsManager>()
    LaunchedEffect(Unit) { settings.markGuideSeen() }
    GettingStartedGuide(onClose, onNavigate, Modifier.fillMaxSize().screenBottomPadding(extra = 0.dp))
}
