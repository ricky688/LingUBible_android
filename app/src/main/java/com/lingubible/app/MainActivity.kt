package com.lingubible.app

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.LocalActivityResultRegistryOwner
import androidx.activity.enableEdgeToEdge
import androidx.activity.SystemBarStyle
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.graphics.luminance
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
import com.lingubible.app.core.navigation.LocalScreenBottomClearance
import com.lingubible.app.core.navigation.LocalTopBarState
import com.lingubible.app.core.navigation.TopBarState
import com.lingubible.app.core.navigation.Screen
import com.lingubible.app.core.settings.AppSettingsManager
import com.lingubible.app.core.settings.ThemeMode
import com.lingubible.app.ui.components.CatFootstepsOverlay
import com.lingubible.app.ui.components.DiscoverTopBarGlass
import com.lingubible.app.ui.components.rememberCatWalkState
import com.lingubible.app.core.theme.LingUBibleTheme
import org.koin.android.ext.android.inject
import java.util.Locale

val LocalComponentActivity = staticCompositionLocalOf<ComponentActivity?> { null }

class MainActivity : ComponentActivity() {
    private val settingsManager: AppSettingsManager by inject()
    private var systemBarIcons: Pair<Boolean, Boolean>? = null

    private fun applySystemBarAppearance() {
        val (lightStatusIcons, lightNavigationIcons) = systemBarIcons ?: return
        val transparent = android.graphics.Color.TRANSPARENT
        enableEdgeToEdge(
            statusBarStyle = if (lightStatusIcons) SystemBarStyle.dark(transparent)
                else SystemBarStyle.light(transparent, transparent),
            navigationBarStyle = if (lightNavigationIcons) SystemBarStyle.dark(transparent)
                else SystemBarStyle.light(transparent, transparent)
        )
        // enableEdgeToEdge configures this flag; apply our policy afterward.
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        applySystemBarAppearance()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        // Dialogs and external activities can restore platform defaults on return.
        if (hasFocus) applySystemBarAppearance()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }

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
            val configuration = LocalConfiguration.current
            val localizedContext = remember(context, configuration, appLanguage, isDark) {
                val config = Configuration(configuration)
                config.setLocale(appLanguage.locale)
                config.uiMode = (config.uiMode and Configuration.UI_MODE_NIGHT_MASK.inv()) or
                    (if (isDark) Configuration.UI_MODE_NIGHT_YES else Configuration.UI_MODE_NIGHT_NO)
                Locale.setDefault(appLanguage.locale)
                context.createConfigurationContext(config)
            }

            CompositionLocalProvider(
                LocalComponentActivity provides this@MainActivity,
                LocalActivityResultRegistryOwner provides this@MainActivity,
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
                    // Use the rendered colors, including inverted/dynamic/OLED themes.
                    val lightStatusIcons = MaterialTheme.colorScheme.background.luminance() < 0.5f
                    val lightNavigationIcons = MaterialTheme.colorScheme.surface.luminance() < 0.5f
                    DisposableEffect(lightStatusIcons, lightNavigationIcons) {
                        systemBarIcons = lightStatusIcons to lightNavigationIcons
                        applySystemBarAppearance()
                        onDispose { }
                    }
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
    val density = LocalDensity.current
    val imeVisible = WindowInsets.ime.getBottom(density) > 0
    val collapseThreshold = with(density) { 24.dp.toPx() }
    val revealThreshold = with(density) { 10.dp.toPx() }
    val overscrollThreshold = with(density) { 4.dp.toPx() }
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
    val nestedScrollConnection = remember(density) {
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
                        if (accumulatedDownScroll > collapseThreshold) {
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
                        if (dy > revealThreshold) {
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
                // Positive unconsumed scroll indicates the list has reached its top.
                if (available.y > overscrollThreshold) {
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

    val catWalkState = rememberCatWalkState()
    val haptic = LocalHapticFeedback.current

    CompositionLocalProvider(
        LocalBottomBarState provides bottomBarState,
        LocalTopBarState provides topBarState
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val density = LocalDensity.current
            val screenWidthPx = with(density) { maxWidth.toPx() }
            val screenHeightPx = with(density) { maxHeight.toPx() }

            val systemBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
            val isBottomBarTargetVisible = shouldShowBottomBar && isBottomBarVisible && !imeVisible
            // Standard NavigationBar height is 80.dp + systemBottom. Scrim extends 48.dp above the bar.
            // Full hide translation ensures the entire bar and its top decorative scrim are pushed below screen.
            val hideTranslation = 80.dp + systemBottom + 48.dp

            val bottomBarTranslationY by animateDpAsState(
                targetValue = if (isBottomBarTargetVisible) 0.dp else hideTranslation,
                animationSpec = if (isBottomBarTargetVisible) {
                    spring(stiffness = Spring.StiffnessMediumLow)
                } else {
                    spring(stiffness = Spring.StiffnessMedium)
                },
                label = "bottomBarTranslationY"
            )

            Scaffold(
                modifier = Modifier.nestedScroll(nestedScrollConnection),
                contentWindowInsets = WindowInsets(0, 0, 0, 0),
                containerColor = MaterialTheme.colorScheme.background,
                topBar = {
                    LingUBibleTopBar(
                        navController = navController,
                        isVisible = isTopBarVisible,
                        isExpanded = isTopBarExpanded,
                        academicSubTab = academicSubTab,
                        onLogoClick = {
                            catWalkState.onLogoTap(screenWidthPx, screenHeightPx) { hapticType ->
                                haptic.performHapticFeedback(hapticType)
                            }
                        }
                    )
                },
                bottomBar = {
                    if (shouldShowBottomBar) {
                        LingUBibleBottomBar(
                            navController = navController,
                            isVisible = isBottomBarTargetVisible,
                            barOffset = bottomBarTranslationY,
                            onExpand = { isBottomBarVisible = true }
                        )
                    }
                }
            ) { innerPadding ->
                val topBarPadding = innerPadding.calculateTopPadding()
                val statusBarPadding = WindowInsets.safeDrawing.asPaddingValues().calculateTopPadding()
                val effectiveTopPadding = maxOf(topBarPadding, statusBarPadding)
                val fullBarClearance = if (shouldShowBottomBar) maxOf(innerPadding.calculateBottomPadding(), systemBottom) else systemBottom
                // The visible clearance strictly tracks the physical top edge of the rising bottom navigation bar.
                // When the bar is off-screen, clearance is pinned at systemBottom.
                // As the bar rises onto the screen, clearance increases in exact 1:1 frame-perfect lockstep with the bar.
                val visibleBottomClearance = when {
                    imeVisible -> 0.dp // imePadding below owns the entire keyboard inset.
                    !shouldShowBottomBar -> systemBottom
                    else -> (fullBarClearance - bottomBarTranslationY).coerceAtLeast(systemBottom)
                }
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = effectiveTopPadding)
                        .consumeWindowInsets(PaddingValues(top = effectiveTopPadding))
                        .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal))
                        .imePadding()
                ) {
                    CompositionLocalProvider(LocalScreenBottomClearance provides visibleBottomClearance) {
                        LingUBibleNavHost(
                            navController = navController,
                            modifier = Modifier.fillMaxSize()
                        )
                        // Draw over the list, immediately below the measured top bar.
                        // This decorative layer neither reserves space nor intercepts touches.
                        if (currentDestination?.hasRoute<Screen.Home>() == true && isTopBarVisible) {
                            DiscoverTopBarGlass()
                        }
                    }
                }
            }

            // 🐱 嶺南「校園貓咪 / 屯門特產」文化彩蛋覆蓋層
            CatFootstepsOverlay(state = catWalkState)
        }
    }
}
