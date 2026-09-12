package com.lingubible.app.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.TableView
import androidx.compose.material.icons.outlined.ViewAgenda
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import com.lingubible.app.core.navigation.LocalTopBarState
import com.lingubible.app.core.settings.AppLanguage
import com.lingubible.app.core.settings.AppSettingsManager
import com.lingubible.app.core.theme.*
import com.lingubible.app.domain.model.TimetableTerm
import com.lingubible.app.ui.components.CourseSectionSelector
import com.lingubible.app.ui.components.FrostedGlassHeader
import com.lingubible.app.ui.components.M3ButtonGroup
import com.lingubible.app.ui.components.Material3TermSelector
import com.lingubible.app.ui.components.TimeConflictAlertCard
import com.lingubible.app.ui.components.TimetableGrid
import com.lingubible.app.ui.viewmodels.TimetableViewModel
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

enum class TimetableSubView {
    GRID,
    SELECTOR
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlannerScreen(
    timetableViewModel: TimetableViewModel = koinViewModel(),
    modifier: Modifier = Modifier
) {
    val topBarState = LocalTopBarState.current
    val context = LocalContext.current
    val isDark = isAppDarkTheme()
    val settingsManager: AppSettingsManager = koinInject()
    val appLanguage by settingsManager.appLanguage.collectAsState()

    val isZh = appLanguage == AppLanguage.ZH_TW
    val gridText = if (isZh) "課表" else "Grid"
    val browseText = if (isZh) "+ 加選" else "+ Browse"

    val timetableState by timetableViewModel.uiState.collectAsState()

    var activeSubView by remember { mutableStateOf(TimetableSubView.GRID) }

    val containerBg = MaterialTheme.colorScheme.background
    val cardBg = MaterialTheme.colorScheme.surfaceContainer
    val cardBorder = MaterialTheme.colorScheme.outlineVariant
    val textMuted = MaterialTheme.colorScheme.onSurfaceVariant

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(containerBg)
    ) {
        // Timetable Header Action Bars linked to scroll hide/rebound
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
            Column(modifier = Modifier.fillMaxWidth()) {
                // Timetable Header: Term Selector & View Switcher Bar
                FrostedGlassHeader(
                    modifier = Modifier.fillMaxWidth(),
                    borderBottomOnly = true
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                // Material 3 Expressive Term Selector Dropdown Menu
                Material3TermSelector(
                    selectedTerm = timetableState.selectedTerm,
                    availableTerms = timetableState.availableTerms,
                    onSelectTerm = { term ->
                        timetableViewModel.selectTerm(term)
                    }
                )

                // Material 3 Expressive Button Group for View Toggle (Grid vs Course Browser)
                M3ButtonGroup(
                    selectedIndex = if (activeSubView == TimetableSubView.GRID) 0 else 1,
                    onLeadingClick = { activeSubView = TimetableSubView.GRID },
                    onTrailingClick = { activeSubView = TimetableSubView.SELECTOR },
                    leadingText = gridText,
                    trailingText = browseText
                )
            }
        }

        // Summary Pill & Action Bar (Export to ICS, Total Credits, Course Count)
        FrostedGlassHeader(
            modifier = Modifier.fillMaxWidth(),
            borderBottomOnly = true
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Credits Chip
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "已選 ${timetableState.totalCredits} 學分",
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    // Courses Count Chip
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "${timetableState.selectedSections.size} 門課程",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium,
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                // ICS Export Button
                FilledTonalButton(
                    onClick = { timetableViewModel.exportTimetableIcs(context) },
                    enabled = timetableState.selectedSections.isNotEmpty(),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.pointerHoverIcon(PointerIcon.Hand)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.FileDownload,
                        contentDescription = "Export ICS",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "匯出日曆",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

        // Material 3 Expressive Collision Warning Alert Card
        TimeConflictAlertCard(
            conflicts = timetableState.conflicts,
            onSelectConflict = {
                activeSubView = TimetableSubView.GRID
            },
            onResolveClick = {
                activeSubView = TimetableSubView.SELECTOR
            }
        )

        // Main Content: Smooth Transition between Timetable Weekly Grid AND Adding Course Selector
        AnimatedContent(
            targetState = activeSubView,
            transitionSpec = {
                val isForward = targetState.ordinal > initialState.ordinal
                val slideOffset = 0.18f
                if (isForward) {
                    (slideInHorizontally(
                        initialOffsetX = { (it * slideOffset).toInt() },
                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                    ) + fadeIn(
                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                    )).togetherWith(
                        slideOutHorizontally(
                            targetOffsetX = { (-it * slideOffset).toInt() },
                            animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                        ) + fadeOut(
                            animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                        )
                    )
                } else {
                    (slideInHorizontally(
                        initialOffsetX = { (-it * slideOffset).toInt() },
                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                    ) + fadeIn(
                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                    )).togetherWith(
                        slideOutHorizontally(
                            targetOffsetX = { (it * slideOffset).toInt() },
                            animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                        ) + fadeOut(
                            animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                        )
                    )
                }.using(SizeTransform(clip = true) { _, _ -> spring(stiffness = Spring.StiffnessMediumLow) })
            },
            label = "timetableSubViewTransition",
            modifier = Modifier.fillMaxSize()
        ) { subView ->
            when (subView) {
                TimetableSubView.GRID -> {
                    TimetableGrid(
                        selectedSections = timetableState.selectedSections,
                        conflicts = timetableState.conflicts,
                        onRemoveSection = { timetableViewModel.removeSection(it) },
                        onNavigateToAddCourse = { activeSubView = TimetableSubView.SELECTOR },
                        modifier = Modifier.fillMaxSize()
                    )
                }
                TimetableSubView.SELECTOR -> {
                    CourseSectionSelector(
                        sections = timetableState.filteredSections,
                        selectedSectionIds = timetableState.selectedSectionIds,
                        selectedSections = timetableState.selectedSections,
                        searchQuery = timetableState.searchQuery,
                        selectedDepartment = timetableState.selectedDepartment,
                        onSearchQueryChange = { timetableViewModel.setSearchQuery(it) },
                        onDepartmentSelect = { timetableViewModel.setSelectedDepartment(it) },
                        onToggleSection = { timetableViewModel.toggleSection(it) },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}
