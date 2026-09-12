package com.lingubible.app

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hasRoute
import kotlin.math.abs
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.lingubible.app.core.navigation.BottomBarState
import com.lingubible.app.core.navigation.BottomNavDestination
import com.lingubible.app.core.navigation.LingUBibleBottomBar
import com.lingubible.app.core.navigation.LingUBibleNavHost
import com.lingubible.app.core.navigation.LingUBibleTopBar
import com.lingubible.app.core.navigation.LocalBottomBarState
import com.lingubible.app.core.navigation.LocalTopBarState
import com.lingubible.app.core.navigation.TopBarState
import com.lingubible.app.core.navigation.Screen
import com.lingubible.app.core.settings.AppSettingsManager
import com.lingubible.app.core.settings.ThemeMode
import com.lingubible.app.core.theme.LingUBibleTheme
import org.koin.android.ext.android.inject
import java.util.Locale

class MainActivity : ComponentActivity() {
    private val settingsManager: AppSettingsManager by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }
        enableEdgeToEdge()

        setContent {
            val themeMode by settingsManager.themeMode.collectAsStateWithLifecycle()
            val appLanguage by settingsManager.appLanguage.collectAsStateWithLifecycle()
            val isOledBlack by settingsManager.isOledBlack.collectAsStateWithLifecycle()
            val activeColorScheme by settingsManager.colorScheme.collectAsStateWithLifecycle()
            val paletteStyle by settingsManager.paletteStyle.collectAsStateWithLifecycle()
            val isInvertedColors by settingsManager.isInvertedColors.collectAsStateWithLifecycle()
            val contrastLevel by settingsManager.contrastLevel.collectAsStateWithLifecycle()
            val customColorHex by settingsManager.customColorHex.collectAsStateWithLifecycle()
            val isDynamicColor by settingsManager.isDynamicColor.collectAsStateWithLifecycle()
            val isHighContrast by settingsManager.isHighContrast.collectAsStateWithLifecycle()

            val isSystemDark = isSystemInDarkTheme()
            val isDark = when (themeMode) {
                ThemeMode.SYSTEM -> isSystemDark
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }

            // Provide dynamically localized Context and Configuration for in-app language and theme switching
            val context = LocalContext.current
            val localizedContext = remember(context, appLanguage, isDark) {
                val config = Configuration(context.resources.configuration)
                config.setLocale(appLanguage.locale)
                config.uiMode = (config.uiMode and Configuration.UI_MODE_NIGHT_MASK.inv()) or
                    (if (isDark) Configuration.UI_MODE_NIGHT_YES else Configuration.UI_MODE_NIGHT_NO)
                Locale.setDefault(appLanguage.locale)
                context.createConfigurationContext(config)
            }

            CompositionLocalProvider(
                LocalContext provides localizedContext,
                LocalConfiguration provides localizedContext.resources.configuration
            ) {
                LingUBibleTheme(
                    darkTheme = isDark,
                    oledBlack = isOledBlack,
                    appColorScheme = activeColorScheme,
                    paletteStyle = paletteStyle,
                    isInvertedColors = isInvertedColors,
                    contrastLevel = contrastLevel,
                    customColorHex = customColorHex,
                    dynamicColor = isDynamicColor,
                    highContrast = isHighContrast
                ) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        LingUBibleMainScreen()
                    }
                }
            }
        }
    }
}

@Composable
fun LingUBibleMainScreen() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    var isBottomBarVisible by remember { mutableStateOf(true) }
    var isTopBarVisible by remember { mutableStateOf(true) }
    var isTopBarExpanded by remember { mutableStateOf(true) }
    var isSubBarVisible by remember { mutableStateOf(true) }
    var academicSubTab by remember { mutableIntStateOf(0) }

    val shouldShowBottomBar = BottomNavDestination.entries.any { destination ->
        currentDestination?.hasRoute(destination.screen::class) == true
    } || currentDestination?.hasRoute<Screen.Planner>() == true
      || currentDestination?.hasRoute<Screen.Calendar>() == true
      || currentDestination?.hasRoute<Screen.GpaHons>() == true

    // Track accumulated downward scroll to prevent jitter from collapsing expanded title prematurely
    var accumulatedDownScroll by remember { mutableFloatStateOf(0f) }

    // Scroll-driven hide/reveal connection: hides when scrolling down, reveals when scrolling up
    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val dy = available.y
                val dx = available.x

                // Only react to direct user input when vertical scroll is dominant
                if (source == NestedScrollSource.UserInput && abs(dy) > abs(dx)) {
                    if (dy < 0f) {
                        // User scrolled DOWN
                        accumulatedDownScroll += abs(dy)

                        // When scrolling down intentionally, collapse large headline and sub-bar.
                        // The main bar (isTopBarVisible) is NEVER collapsed so it remains pinned at top.
                        if (accumulatedDownScroll > 24f) {
                            if (isTopBarExpanded) {
                                isTopBarExpanded = false
                            }
                            if (isSubBarVisible) {
                                isSubBarVisible = false
                            }
                            if (isBottomBarVisible) {
                                isBottomBarVisible = false
                            }
                        }
                    } else if (dy > 0f) {
                        // User scrolled UP
                        accumulatedDownScroll = 0f

                        // Reveal sub-bar and bottom bar immediately on upward scroll
                        if (dy > 10f) {
                            if (!isSubBarVisible) {
                                isSubBarVisible = true
                            }
                            if (!isBottomBarVisible) {
                                isBottomBarVisible = true
                            }
                        }
                    }
                }
                return Offset.Zero
            }

            override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
                // When available.y > 4f in onPostScroll, the list has reached the very top of the scrollable area
                if (available.y > 4f) {
                    accumulatedDownScroll = 0f
                    if (!isTopBarExpanded) {
                        isTopBarExpanded = true
                    }
                    if (!isSubBarVisible) {
                        isSubBarVisible = true
                    }
                    if (!isBottomBarVisible) {
                        isBottomBarVisible = true
                    }
                    if (!isTopBarVisible) {
                        isTopBarVisible = true
                    }
                }
                return Offset.Zero
            }
        }
    }

    // Always reveal all bars and expand top bar when navigating to a new destination or tab
    LaunchedEffect(currentDestination) {
        accumulatedDownScroll = 0f
        isBottomBarVisible = true
        isTopBarVisible = true
        isTopBarExpanded = true
        isSubBarVisible = true
    }

    val bottomBarState = remember(isBottomBarVisible) {
        BottomBarState(
            isVisible = isBottomBarVisible,
            onExpand = { isBottomBarVisible = true },
            setVisible = { isBottomBarVisible = it }
        )
    }

    val topBarState = remember(isTopBarVisible, isTopBarExpanded, isSubBarVisible, academicSubTab) {
        TopBarState(
            isVisible = isTopBarVisible,
            isExpanded = isTopBarExpanded,
            isSubBarVisible = isSubBarVisible,
            academicSubTab = academicSubTab,
            setVisible = { isTopBarVisible = it },
            setExpanded = { isTopBarExpanded = it },
            setSubBarVisible = { isSubBarVisible = it },
            setAcademicSubTab = { academicSubTab = it }
        )
    }

    CompositionLocalProvider(
        LocalBottomBarState provides bottomBarState,
        LocalTopBarState provides topBarState
    ) {
        Scaffold(
            modifier = Modifier.nestedScroll(nestedScrollConnection),
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                LingUBibleTopBar(
                    navController = navController,
                    isVisible = isTopBarVisible,
                    isExpanded = isTopBarExpanded,
                    academicSubTab = academicSubTab
                )
            },
            bottomBar = {
                if (shouldShowBottomBar) {
                    LingUBibleBottomBar(
                        navController = navController,
                        isVisible = isBottomBarVisible,
                        onExpand = { isBottomBarVisible = true }
                    )
                }
            }
        ) { innerPadding ->
            val topBarPadding = innerPadding.calculateTopPadding()
            val statusBarPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
            val effectiveTopPadding = maxOf(topBarPadding, statusBarPadding)
            LingUBibleNavHost(
                navController = navController,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        top = effectiveTopPadding,
                        bottom = if (shouldShowBottomBar) 0.dp else innerPadding.calculateBottomPadding()
                    )
                    .consumeWindowInsets(WindowInsets.statusBars)
            )
        }
    }
}
