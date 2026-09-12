package com.lingubible.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.lingubible.app.core.navigation.LocalTopBarState
import com.lingubible.app.core.settings.AppLanguage
import com.lingubible.app.core.settings.AppSettingsManager
import com.lingubible.app.core.theme.*
import com.lingubible.app.ui.components.FrostedGlassHeader
import com.lingubible.app.ui.components.M3ButtonGroup
import com.lingubible.app.ui.components.M3ButtonGroupItem
import org.koin.compose.koinInject

/**
 * Academic Tools Screen (學業工具)
 *
 * Consolidates the 3 core academic tools for Lingnan students:
 * 1. Timetable Planner (選課排程)
 * 2. School Calendar (校歷日程)
 * 3. GPA & Honours (GPA 與榮譽)
 *
 * Controlled by a top Material 3 Expressive Connected Button Group with
 * spring stiffness shape morphing and dynamic language synchronization.
 */
@Composable
fun AcademicToolsScreen(
    initialTab: Int = 0,
    modifier: Modifier = Modifier,
    settingsManager: AppSettingsManager = koinInject()
) {
    val topBarState = LocalTopBarState.current
    val isDark = isAppDarkTheme()
    val appLanguage by settingsManager.appLanguage.collectAsState()
    val isZh = appLanguage == AppLanguage.ZH_TW

    var selectedTab by remember(initialTab) { mutableIntStateOf(initialTab.coerceIn(0, 2)) }
    var previousTab by remember { mutableIntStateOf(selectedTab) }

    LaunchedEffect(selectedTab) {
        topBarState.setAcademicSubTab(selectedTab)
    }

    val containerBg = MaterialTheme.colorScheme.background
    val headerBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = if (isDark) 0.35f else 0.5f)

    val tabItems = remember(isZh) {
        listOf(
            M3ButtonGroupItem(
                label = if (isZh) "選課排程" else "Planner",
                icon = Icons.Filled.CalendarMonth,
                onClick = {
                    selectedTab = 0
                    topBarState.setAcademicSubTab(0)
                }
            ),
            M3ButtonGroupItem(
                label = if (isZh) "校歷日程" else "Calendar",
                icon = Icons.Filled.DateRange,
                onClick = {
                    selectedTab = 1
                    topBarState.setAcademicSubTab(1)
                }
            ),
            M3ButtonGroupItem(
                label = if (isZh) "GPA 與榮譽" else "GPA & Hons",
                icon = Icons.Filled.Calculate,
                onClick = {
                    selectedTab = 2
                    topBarState.setAcademicSubTab(2)
                }
            )
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(containerBg)
    ) {
        // Top Expressive Button Group Bar linked to scroll hide/rebound
        AnimatedVisibility(
            visible = topBarState.isSubBarVisible,
            enter = expandVertically(
                expandFrom = Alignment.Top,
                animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
            ) + slideInVertically(
                initialOffsetY = { -it },
                animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
            ) + fadeIn(
                animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
            ),
            exit = shrinkVertically(
                shrinkTowards = Alignment.Top,
                animationSpec = spring(stiffness = Spring.StiffnessMedium)
            ) + slideOutVertically(
                targetOffsetY = { -it },
                animationSpec = spring(stiffness = Spring.StiffnessMedium)
            ) + fadeOut(
                animationSpec = spring(stiffness = Spring.StiffnessMedium)
            )
        ) {
            FrostedGlassHeader(
                modifier = Modifier.fillMaxWidth(),
                borderBottomOnly = true
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    M3ButtonGroup(
                        selectedIndex = selectedTab,
                        items = tabItems,
                        height = 36.dp,
                        activeCornerRadius = 18.dp,
                        inactiveCornerRadius = 8.dp,
                        showCheckmarkOnSelected = false
                    )
                }
            }
        }

        // Sub-tool content with directional spring animation
        AnimatedContent(
            targetState = selectedTab,
            transitionSpec = {
                val forward = targetState >= previousTab
                val direction = if (forward) 1 else -1
                (slideInHorizontally(
                    initialOffsetX = { (it * 0.25f * direction).toInt() },
                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                ) + fadeIn(animationSpec = tween(220))) togetherWith
                (slideOutHorizontally(
                    targetOffsetX = { (-it * 0.25f * direction).toInt() },
                    animationSpec = tween(180, easing = FastOutLinearInEasing)
                ) + fadeOut(animationSpec = tween(160)))
            },
            modifier = Modifier.weight(1f),
            label = "academicToolSubViewTransition"
        ) { tabIndex ->
            LaunchedEffect(tabIndex) {
                previousTab = tabIndex
            }
            when (tabIndex) {
                0 -> PlannerScreen()
                1 -> CalendarScreen()
                2 -> GpaHonsScreen()
                else -> PlannerScreen()
            }
        }
    }
}
