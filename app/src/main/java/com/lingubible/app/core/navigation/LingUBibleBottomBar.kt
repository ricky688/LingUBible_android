package com.lingubible.app.core.navigation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.lingubible.app.core.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

import com.lingubible.app.core.settings.AppSettingsManager
import org.koin.compose.koinInject

data class BottomBarState(
    val isVisible: Boolean = true,
    val onExpand: () -> Unit = {},
    val setVisible: (Boolean) -> Unit = {}
)

val LocalBottomBarState = compositionLocalOf { BottomBarState() }

@Composable
fun LingUBibleBottomBar(
    navController: NavHostController,
    isVisible: Boolean = true,
    onExpand: () -> Unit = {},
    modifier: Modifier = Modifier,
    settingsManager: AppSettingsManager = koinInject()
) {
    val isDark = com.lingubible.app.core.theme.isAppDarkTheme()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val appLanguage by settingsManager.appLanguage.collectAsState()

    // Frosted glass styling colors & gradient palette aligned with M3
    val surfaceColor = MaterialTheme.colorScheme.surface
    val frostedScrimBase = MaterialTheme.colorScheme.surface
    val targetGlassHighlightColor = if (isDark) MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f) else Color(0xFFCBD5E1).copy(alpha = 0.85f)
    val glassHighlightColor by animateColorAsState(
        targetValue = targetGlassHighlightColor,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "bottomBarGlassHighlight"
    )

    val targetGlassSheenColor = if (isDark) Color.White.copy(alpha = 0.05f) else Color.White.copy(alpha = 0.35f)
    val glassSheenColor by animateColorAsState(
        targetValue = targetGlassSheenColor,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "bottomBarGlassSheen"
    )

    // Spring physics for slide down / up with pure spring stiffness
    val barTranslationY by animateDpAsState(
        targetValue = if (isVisible) 0.dp else 200.dp,
        animationSpec = if (isVisible) {
            spring(stiffness = Spring.StiffnessMediumLow)
        } else {
            spring(stiffness = Spring.StiffnessMedium)
        },
        label = "bottomBarTranslationY"
    )

    // Fade animation on items
    val itemsAlpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "itemsAlpha"
    )

    // Trigger sequence key for left-to-right staggered entrance animation
    var sequenceKey by remember { mutableIntStateOf(1) }
    var wasVisible by remember { mutableStateOf(isVisible) }

    LaunchedEffect(isVisible) {
        if (isVisible && !wasVisible) {
            sequenceKey++
        }
        wasVisible = isVisible
    }

    val destinations = remember { BottomNavDestination.entries }
    val itemAnimatables = remember {
        List(destinations.size) { Animatable(0f) }
    }

    // Launch staggered pop-up and focus animation from left to right
    LaunchedEffect(sequenceKey) {
        if (isVisible) {
            itemAnimatables.forEach { it.snapTo(0f) }
            itemAnimatables.forEachIndexed { index, animatable ->
                launch {
                    delay(index * 45L) // Staggered delay: 0ms, 45ms, 90ms
                    animatable.animateTo(
                        targetValue = 1f,
                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                    )
                }
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                translationY = barTranslationY.toPx()
                alpha = itemsAlpha
            }
    ) {
        // 1. Frosted Glass Top Gradient Scrim extending ABOVE the navigation bar at the bottom of the app
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .graphicsLayer {
                    alpha = itemsAlpha
                }
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            frostedScrimBase.copy(alpha = 0.08f),
                            frostedScrimBase.copy(alpha = 0.25f),
                            frostedScrimBase.copy(alpha = 0.50f),
                            frostedScrimBase.copy(alpha = 0.78f),
                            frostedScrimBase.copy(alpha = 0.95f),
                            surfaceColor
                        )
                    )
                )
                .drawBehind {
                    // Subtle top specular edge highlight
                    drawLine(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color.Transparent,
                                glassHighlightColor.copy(alpha = 0.20f),
                                glassHighlightColor.copy(alpha = 0.40f),
                                glassHighlightColor.copy(alpha = 0.20f),
                                Color.Transparent
                            )
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(size.width, 0f),
                        strokeWidth = 1.dp.toPx()
                    )
                }
        )

        // 2. Navigation Bar with Solid Opaque Surface (zero text collisions) and Specular Boundary Line
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .drawBehind {
                    // 100% Solid opaque background for navigation bar
                    drawRect(color = surfaceColor)

                    // Crisp specular divider line between the frosted scrim and navigation bar
                    drawLine(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                glassHighlightColor.copy(alpha = 0.25f),
                                glassHighlightColor,
                                glassHighlightColor.copy(alpha = 0.25f)
                            )
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(size.width, 0f),
                        strokeWidth = 1.2.dp.toPx()
                    )

                    // Subtle top sheen
                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                glassSheenColor,
                                Color.Transparent
                            ),
                            startY = 0f,
                            endY = 24.dp.toPx()
                        )
                    )
                }
        ) {
            NavigationBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer {
                        alpha = itemsAlpha
                    },
                containerColor = Color.Transparent,
                tonalElevation = 0.dp
            ) {
                destinations.forEachIndexed { index, destination ->
                    val selected = when (destination) {
                        BottomNavDestination.EXPLORE -> currentDestination?.hasRoute<Screen.Home>() == true
                        BottomNavDestination.ACADEMIC_TOOLS -> currentDestination?.hasRoute<Screen.AcademicTools>() == true ||
                            currentDestination?.hasRoute<Screen.Planner>() == true ||
                            currentDestination?.hasRoute<Screen.Calendar>() == true ||
                            currentDestination?.hasRoute<Screen.GpaHons>() == true
                        BottomNavDestination.ACCOUNT -> currentDestination?.hasRoute<Screen.Profile>() == true
                    }

                    val itemProgress = itemAnimatables.getOrNull(index)?.value ?: 1f
                    // Pop-up and focus scale: starts at 0.25f, overshoots to ~1.18f, settles at 1.0f
                    val iconScale = (0.25f + 0.75f * itemProgress).coerceAtLeast(0f)
                    val iconTranslationY = ((1f - itemProgress) * 28f).coerceAtLeast(-10f)
                    val itemAlpha = itemProgress.coerceIn(0f, 1f)
                    val itemLabel = destination.label(appLanguage)

                    NavigationBarItem(
                        selected = selected,
                        enabled = isVisible,
                        modifier = Modifier.pointerHoverIcon(PointerIcon.Hand),
                        onClick = {
                            if (isVisible && !selected) {
                                navController.navigate(destination.screen) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = {
                            Box(
                                modifier = Modifier.graphicsLayer {
                                    scaleX = iconScale
                                    scaleY = iconScale
                                    translationY = iconTranslationY
                                    alpha = itemAlpha
                                },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (selected) destination.selectedIcon else destination.unselectedIcon,
                                    contentDescription = itemLabel
                                )
                            }
                        },
                        label = {
                            Text(
                                text = itemLabel,
                                modifier = Modifier.graphicsLayer {
                                    alpha = itemAlpha
                                    translationY = iconTranslationY * 0.4f
                                },
                                maxLines = 1,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = if (isDark) 0.6f else 0.45f),
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
        }
    }
}
