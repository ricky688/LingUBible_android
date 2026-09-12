package com.lingubible.app.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.lingubible.app.core.theme.*
import com.lingubible.app.ui.components.FrostedGlassHeader
import com.lingubible.app.ui.components.SchoolCalendarView
import com.lingubible.app.ui.viewmodels.AcademicCalendarViewModel
import org.koin.androidx.compose.koinViewModel

/**
 * Dedicated School Calendar (校歷日程) Screen.
 *
 * Provides full academic calendar functionality:
 * - Month chips and category filters (Semester, Add/Drop, Exams, Holidays, Graduation)
 * - Single event ICS download and Android Calendar Provider Intent integration
 * - Batch ICS export for the entire academic calendar
 * - Mouse scrollbar and responsive layout
 */
@Composable
fun CalendarScreen(
    calendarViewModel: AcademicCalendarViewModel = koinViewModel(),
    modifier: Modifier = Modifier
) {
    val topBarState = LocalTopBarState.current
    val context = LocalContext.current
    val isDark = com.lingubible.app.core.theme.isAppDarkTheme()
    val calendarState by calendarViewModel.uiState.collectAsState()

    val containerBg = MaterialTheme.colorScheme.background
    val cardBg = MaterialTheme.colorScheme.surfaceContainer
    val borderCol = MaterialTheme.colorScheme.outlineVariant
    val textMuted = MaterialTheme.colorScheme.onSurfaceVariant

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(containerBg)
    ) {
        // Calendar Header Action Bar linked to scroll hide/rebound
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
                FrostedGlassHeader(
                    modifier = Modifier.fillMaxWidth(),
                    borderBottomOnly = true
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "嶺大校曆",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "2026-27 學年校曆",
                                style = MaterialTheme.typography.labelSmall,
                                color = textMuted
                            )
                        }

                        // Batch Export Button
                        FilledTonalButton(
                            onClick = { calendarViewModel.exportAllEventsToIcs(context) },
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                                contentColor = MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier.pointerHoverIcon(PointerIcon.Hand)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.FileDownload,
                                contentDescription = "Export All Events",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "匯出全校校歷",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }

        // School Calendar Content matching web app parity
        SchoolCalendarView(
            allEvents = calendarState.allEvents,
            visibleEvents = calendarState.visibleEvents,
            selectedDayEvents = calendarState.selectedDayEvents,
            selectedMonthKey = calendarState.selectedMonthKey,
            selectedDate = calendarState.selectedDate ?: java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE),
            hiddenCategories = calendarState.hiddenCategories,
            onSelectMonth = { calendarViewModel.setMonth(it) },
            onSelectDate = { calendarViewModel.setDate(it) },
            onToggleCategory = { calendarViewModel.toggleCategory(it) },
            onShowAllCategories = { calendarViewModel.showAllCategories() },
            onJumpTermStart = { calendarViewModel.jumpToTermStart() },
            onAddToCalendar = { calendarViewModel.addToSystemCalendar(context, it) },
            onExportIcs = { calendarViewModel.exportSingleEventIcs(context, it) },
            modifier = Modifier.fillMaxSize()
        )
    }
}
