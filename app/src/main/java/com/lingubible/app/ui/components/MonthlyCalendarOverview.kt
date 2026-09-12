package com.lingubible.app.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.ViewWeek
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lingubible.app.core.theme.LingnanRed
import com.lingubible.app.domain.model.AcademicEvent
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

enum class CalendarViewMode {
    DAY3,
    WEEK,
    MONTH
}

/**
 * Positioned event within a week / strip of dates.
 * Follows the Google Calendar / Outlook layout algorithm (layoutStrip in web app).
 */
data class PositionedEvent(
    val event: AcademicEvent,
    val lane: Int,
    val startCol: Int,
    val endCol: Int,
    val clippedLeft: Boolean,
    val clippedRight: Boolean
)

/**
 * Assigns overlapping multi-day events to stacked lanes within a strip of days.
 * Directly ported from src/pages/Calendar.tsx layoutStrip().
 */
fun layoutStrip(
    days: List<LocalDate>,
    events: List<AcademicEvent>,
    maxLanes: Int = 2
): List<PositionedEvent> {
    if (days.isEmpty()) return emptyList()
    val first = days.first()
    val last = days.last()
    val firstDateStr = first.format(DateTimeFormatter.ISO_LOCAL_DATE)
    val lastDateStr = last.format(DateTimeFormatter.ISO_LOCAL_DATE)

    val overlapping = events.filter { e ->
        val endStr = e.end ?: e.start
        endStr >= firstDateStr && e.start <= lastDateStr
    }.map { e ->
        val sDate = try { LocalDate.parse(e.start) } catch (ex: Exception) { first }
        val enDate = try { LocalDate.parse(e.end ?: e.start) } catch (ex: Exception) { sDate }

        val startCol = if (sDate.isBefore(first)) 0 else ChronoUnit.DAYS.between(first, sDate).toInt().coerceIn(0, days.size - 1)
        val endCol = if (enDate.isAfter(last)) days.size - 1 else ChronoUnit.DAYS.between(first, enDate).toInt().coerceIn(0, days.size - 1)

        val clippedLeft = sDate.isBefore(first)
        val clippedRight = enDate.isAfter(last)

        PositionedEvent(
            event = e,
            lane = 0,
            startCol = startCol,
            endCol = endCol,
            clippedLeft = clippedLeft,
            clippedRight = clippedRight
        )
    }.sortedWith(
        compareBy<PositionedEvent> { it.startCol }
            .thenByDescending { it.endCol - it.startCol }
            .thenBy { it.event.category.ordinal }
    )

    val laneEnds = mutableListOf<Int>()
    val positioned = mutableListOf<PositionedEvent>()

    for (item in overlapping) {
        var lane = 0
        while (lane < laneEnds.size && laneEnds[lane] >= item.startCol) {
            lane++
        }
        if (lane < laneEnds.size) {
            laneEnds[lane] = item.endCol
        } else {
            laneEnds.add(item.endCol)
        }
        positioned.add(item.copy(lane = lane))
    }

    return positioned
}

/**
 * Overview Monthly Calendar with Google Calendar Multi-Day Spanning Bars and Frosted Glass Effect.
 */
@Composable
fun MonthlyCalendarOverview(
    events: List<AcademicEvent>,
    selectedMonthKey: String?,
    selectedDate: String?,
    onSelectDate: (String?) -> Unit,
    onMonthChanged: (String) -> Unit,
    onSelectEvent: (AcademicEvent) -> Unit = {},
    onJumpTermStart: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val isDark = com.lingubible.app.core.theme.isAppDarkTheme()
    var isExpanded by remember { mutableStateOf(true) }
    var viewMode by remember { mutableStateOf(CalendarViewMode.MONTH) }

    // Resolve current YearMonth (defaults to today's month, e.g. 2026-09)
    val currentYearMonth = remember(selectedMonthKey, selectedDate) {
        if (!selectedMonthKey.isNullOrBlank()) {
            try {
                YearMonth.parse(selectedMonthKey)
            } catch (e: Exception) {
                YearMonth.from(LocalDate.now())
            }
        } else if (!selectedDate.isNullOrBlank()) {
            try {
                YearMonth.parse(selectedDate.take(7))
            } catch (e: Exception) {
                YearMonth.from(LocalDate.now())
            }
        } else {
            YearMonth.from(LocalDate.now())
        }
    }

    val today = remember { LocalDate.now() }
    val todayString = remember { today.format(DateTimeFormatter.ISO_LOCAL_DATE) }

    // Anchor Date for Week / 3-Day view
    val anchorDate = remember(selectedDate, currentYearMonth) {
        if (!selectedDate.isNullOrBlank()) {
            try { LocalDate.parse(selectedDate) } catch (e: Exception) { currentYearMonth.atDay(1) }
        } else {
            currentYearMonth.atDay(1)
        }
    }

    // Frosted Glass Palette
    val glassBg = if (isDark) Color(0xFF1E293B).copy(alpha = 0.70f) else Color.White.copy(alpha = 0.75f)
    val glassBorder = Brush.linearGradient(
        colors = if (isDark) listOf(
            Color.White.copy(alpha = 0.28f),
            Color(0xFF475569).copy(alpha = 0.35f),
            Color.White.copy(alpha = 0.08f)
        ) else listOf(
            Color.White.copy(alpha = 0.95f),
            Color(0xFFCBD5E1).copy(alpha = 0.50f),
            Color.White.copy(alpha = 0.30f)
        )
    )
    val glassSheen = Brush.verticalGradient(
        colors = listOf(
            Color.White.copy(alpha = if (isDark) 0.12f else 0.40f),
            Color.Transparent
        )
    )
    val textPrimary = if (isDark) Color.White else Color(0xFF0F172A)
    val textMuted = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)

    // Events in this YearMonth
    val ymString = currentYearMonth.toString()
    val monthEventsCount = remember(events, ymString) {
        events.count { it.start.startsWith(ymString) || (it.end?.startsWith(ymString) == true) }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        // Frosted Glass Card Container with clean flat elevation
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    BorderStroke(1.dp, glassBorder),
                    RoundedCornerShape(20.dp)
                ),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = glassBg),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp)
            ) {
                // 1. Navigation Action Buttons & Month Title Header Row (matching web app)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left: "今天", "學期開始", and navigation arrows (< >)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        // "今天" Quick Jump
                        OutlinedButton(
                            onClick = {
                                val thisMonth = YearMonth.now()
                                onMonthChanged(thisMonth.toString())
                                onSelectDate(todayString)
                            },
                            contentPadding = PaddingValues(horizontal = 9.dp, vertical = 2.dp),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, if (isDark) Color(0xFF3F3F46) else Color(0xFFCBD5E1)),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = textPrimary
                            ),
                            modifier = Modifier.height(30.dp)
                        ) {
                            Text(
                                text = "今天",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        // "學期開始" Quick Jump
                        OutlinedButton(
                            onClick = onJumpTermStart,
                            contentPadding = PaddingValues(horizontal = 9.dp, vertical = 2.dp),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, if (isDark) Color(0xFF3F3F46) else Color(0xFFCBD5E1)),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = textPrimary
                            ),
                            modifier = Modifier.height(30.dp)
                        ) {
                            Text(
                                text = "學期開始",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        // Previous Button (<)
                        IconButton(
                            onClick = {
                                when (viewMode) {
                                    CalendarViewMode.MONTH -> {
                                        val prevMonth = currentYearMonth.minusMonths(1)
                                        onMonthChanged(prevMonth.toString())
                                    }
                                    CalendarViewMode.WEEK -> {
                                        val newAnchor = anchorDate.minusWeeks(1)
                                        onMonthChanged(YearMonth.from(newAnchor).toString())
                                        onSelectDate(newAnchor.format(DateTimeFormatter.ISO_LOCAL_DATE))
                                    }
                                    CalendarViewMode.DAY3 -> {
                                        val newAnchor = anchorDate.minusDays(3)
                                        onMonthChanged(YearMonth.from(newAnchor).toString())
                                        onSelectDate(newAnchor.format(DateTimeFormatter.ISO_LOCAL_DATE))
                                    }
                                }
                            },
                            modifier = Modifier
                                .size(28.dp)
                                .pointerHoverIcon(PointerIcon.Hand)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBackIos,
                                contentDescription = "Previous",
                                modifier = Modifier.size(13.dp),
                                tint = textPrimary
                            )
                        }

                        // Next Button (>)
                        IconButton(
                            onClick = {
                                when (viewMode) {
                                    CalendarViewMode.MONTH -> {
                                        val nextMonth = currentYearMonth.plusMonths(1)
                                        onMonthChanged(nextMonth.toString())
                                    }
                                    CalendarViewMode.WEEK -> {
                                        val newAnchor = anchorDate.plusWeeks(1)
                                        onMonthChanged(YearMonth.from(newAnchor).toString())
                                        onSelectDate(newAnchor.format(DateTimeFormatter.ISO_LOCAL_DATE))
                                    }
                                    CalendarViewMode.DAY3 -> {
                                        val newAnchor = anchorDate.plusDays(3)
                                        onMonthChanged(YearMonth.from(newAnchor).toString())
                                        onSelectDate(newAnchor.format(DateTimeFormatter.ISO_LOCAL_DATE))
                                    }
                                }
                            },
                            modifier = Modifier
                                .size(28.dp)
                                .pointerHoverIcon(PointerIcon.Hand)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                                contentDescription = "Next",
                                modifier = Modifier.size(13.dp),
                                tint = textPrimary
                            )
                        }
                    }

                    // Right: Title (e.g. "2026年9月") + Expand/Collapse Chevron
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End
                    ) {
                        val headerTitle = when (viewMode) {
                            CalendarViewMode.MONTH -> "${currentYearMonth.year}年${currentYearMonth.monthValue}月"
                            CalendarViewMode.WEEK -> {
                                val dayOfWeekVal = anchorDate.dayOfWeek.value % 7
                                val sunday = anchorDate.minusDays(dayOfWeekVal.toLong())
                                val saturday = sunday.plusDays(6)
                                if (sunday.monthValue == saturday.monthValue) {
                                    "${sunday.monthValue}月${sunday.dayOfMonth}日 – ${saturday.dayOfMonth}日"
                                } else {
                                    "${sunday.monthValue}月${sunday.dayOfMonth}日 – ${saturday.monthValue}月${saturday.dayOfMonth}日"
                                }
                            }
                            CalendarViewMode.DAY3 -> {
                                val end = anchorDate.plusDays(2)
                                if (anchorDate.monthValue == end.monthValue) {
                                    "${anchorDate.monthValue}月${anchorDate.dayOfMonth}日 – ${end.dayOfMonth}日"
                                } else {
                                    "${anchorDate.monthValue}月${anchorDate.dayOfMonth}日 – ${end.monthValue}月${end.dayOfMonth}日"
                                }
                            }
                        }

                        AnimatedContent(
                            targetState = headerTitle,
                            transitionSpec = {
                                (fadeIn(animationSpec = tween(220)) + slideInVertically(
                                    initialOffsetY = { it / 3 },
                                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                                )) togetherWith (fadeOut(animationSpec = tween(150)) + slideOutVertically(
                                    targetOffsetY = { -it / 3 },
                                    animationSpec = tween(150)
                                ))
                            },
                            label = "calendarHeaderTitleTransition"
                        ) { title ->
                            Text(
                                text = title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = textPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Spacer(modifier = Modifier.width(2.dp))

                        IconButton(
                            onClick = { isExpanded = !isExpanded },
                            modifier = Modifier
                                .size(28.dp)
                                .pointerHoverIcon(PointerIcon.Hand)
                        ) {
                            Icon(
                                imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = if (isExpanded) "Collapse" else "Expand",
                                modifier = Modifier.size(20.dp),
                                tint = textMuted
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 2. Material 3 Expressive Connected Button Group (三天 | 週 | 月)
                val viewModeModes = remember { listOf(CalendarViewMode.DAY3, CalendarViewMode.WEEK, CalendarViewMode.MONTH) }
                val viewModeItems = remember {
                    listOf(
                        M3ButtonGroupItem("三天", Icons.Outlined.ViewWeek) { viewMode = CalendarViewMode.DAY3 },
                        M3ButtonGroupItem("週", Icons.Outlined.DateRange) { viewMode = CalendarViewMode.WEEK },
                        M3ButtonGroupItem("月", Icons.Outlined.CalendarMonth) { viewMode = CalendarViewMode.MONTH }
                    )
                }
                val selectedModeIndex = viewModeModes.indexOf(viewMode)

                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    M3ButtonGroup(
                        selectedIndex = selectedModeIndex,
                        items = viewModeItems,
                        height = 36.dp,
                        spacing = 6.dp,
                        showCheckmarkOnSelected = false
                    )
                }

                // Collapsible Content
                AnimatedVisibility(
                    visible = isExpanded,
                    enter = fadeIn(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) +
                            expandVertically(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)),
                    exit = fadeOut(animationSpec = spring(stiffness = Spring.StiffnessMedium)) +
                           shrinkVertically(animationSpec = spring(stiffness = Spring.StiffnessMedium))
                ) {
                    var horizontalDragAmount by remember { mutableFloatStateOf(0f) }
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .pointerInput(viewMode, currentYearMonth, anchorDate) {
                                detectHorizontalDragGestures(
                                    onDragStart = { horizontalDragAmount = 0f },
                                    onDragEnd = {
                                        val threshold = 40.dp.toPx()
                                        if (horizontalDragAmount < -threshold) {
                                            // Swiped LEFT -> Go to NEXT period
                                            when (viewMode) {
                                                CalendarViewMode.MONTH -> {
                                                    val nextMonth = currentYearMonth.plusMonths(1)
                                                    onMonthChanged(nextMonth.toString())
                                                }
                                                CalendarViewMode.WEEK -> {
                                                    val newAnchor = anchorDate.plusWeeks(1)
                                                    onMonthChanged(YearMonth.from(newAnchor).toString())
                                                    onSelectDate(newAnchor.format(DateTimeFormatter.ISO_LOCAL_DATE))
                                                }
                                                CalendarViewMode.DAY3 -> {
                                                    val newAnchor = anchorDate.plusDays(3)
                                                    onMonthChanged(YearMonth.from(newAnchor).toString())
                                                    onSelectDate(newAnchor.format(DateTimeFormatter.ISO_LOCAL_DATE))
                                                }
                                            }
                                        } else if (horizontalDragAmount > threshold) {
                                            // Swiped RIGHT -> Go to PREVIOUS period
                                            when (viewMode) {
                                                CalendarViewMode.MONTH -> {
                                                    val prevMonth = currentYearMonth.minusMonths(1)
                                                    onMonthChanged(prevMonth.toString())
                                                }
                                                CalendarViewMode.WEEK -> {
                                                    val newAnchor = anchorDate.minusWeeks(1)
                                                    onMonthChanged(YearMonth.from(newAnchor).toString())
                                                    onSelectDate(newAnchor.format(DateTimeFormatter.ISO_LOCAL_DATE))
                                                }
                                                CalendarViewMode.DAY3 -> {
                                                    val newAnchor = anchorDate.minusDays(3)
                                                    onMonthChanged(YearMonth.from(newAnchor).toString())
                                                    onSelectDate(newAnchor.format(DateTimeFormatter.ISO_LOCAL_DATE))
                                                }
                                            }
                                        }
                                        horizontalDragAmount = 0f
                                    },
                                    onHorizontalDrag = { change, dragAmount ->
                                        change.consume()
                                        horizontalDragAmount += dragAmount
                                    }
                                )
                            }
                    ) {
                        Spacer(modifier = Modifier.height(8.dp))

                        AnimatedContent(
                            targetState = viewMode,
                            transitionSpec = {
                                val forward = targetState.ordinal > initialState.ordinal
                                val slideInOffset = if (forward) { width: Int -> (width * 0.22f).toInt() } else { width: Int -> -(width * 0.22f).toInt() }
                                val slideOutOffset = if (forward) { width: Int -> -(width * 0.22f).toInt() } else { width: Int -> (width * 0.22f).toInt() }

                                ((slideInHorizontally(
                                    initialOffsetX = slideInOffset,
                                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                                ) + fadeIn(
                                    animationSpec = tween(durationMillis = 220)
                                )) togetherWith (slideOutHorizontally(
                                    targetOffsetX = slideOutOffset,
                                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                                ) + fadeOut(
                                    animationSpec = tween(durationMillis = 160)
                                ))).using(
                                    SizeTransform(clip = true) { _, _ ->
                                        spring(stiffness = Spring.StiffnessMediumLow)
                                    }
                                )
                            },
                            label = "calendarViewModeTransition"
                        ) { targetMode ->
                            Column(modifier = Modifier.fillMaxWidth()) {
                                when (targetMode) {
                                    CalendarViewMode.MONTH -> {
                                        // Weekday Header Row (Sun - Sat) stays static pinned at top of month
                                        val weekdays = listOf("週日", "週一", "週二", "週三", "週四", "週五", "週六")
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            weekdays.forEachIndexed { index, dayName ->
                                                val isWeekend = index == 0 || index == 6
                                                Text(
                                                    text = dayName,
                                                    modifier = Modifier.weight(1f),
                                                    textAlign = TextAlign.Center,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 11.sp,
                                                    color = if (isWeekend) MaterialTheme.colorScheme.primary.copy(alpha = 0.85f) else textMuted
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(4.dp))

                                        // Month Weeks Grid with directional sliding animation
                                        AnimatedContent(
                                            targetState = currentYearMonth,
                                            transitionSpec = {
                                                val forward = targetState.isAfter(initialState)
                                                val slideInOffset = if (forward) { width: Int -> width } else { width: Int -> -width }
                                                val slideOutOffset = if (forward) { width: Int -> -width } else { width: Int -> width }

                                                ((slideInHorizontally(
                                                    initialOffsetX = slideInOffset,
                                                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                                                ) + fadeIn(
                                                    animationSpec = tween(durationMillis = 200)
                                                )) togetherWith (slideOutHorizontally(
                                                    targetOffsetX = slideOutOffset,
                                                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                                                ) + fadeOut(
                                                    animationSpec = tween(durationMillis = 150)
                                                ))).using(
                                                    SizeTransform(clip = true) { _, _ ->
                                                        spring(stiffness = Spring.StiffnessMediumLow)
                                                    }
                                                )
                                            },
                                            label = "monthPagingTransition"
                                        ) { ym ->
                                            val firstDayOfMonth = ym.atDay(1)
                                            val startDayOfWeek = firstDayOfMonth.dayOfWeek.value % 7
                                            val daysInMonth = ym.lengthOfMonth()
                                            val prevMonth = ym.minusMonths(1)
                                            val daysInPrevMonth = prevMonth.lengthOfMonth()

                                            val totalRequired = startDayOfWeek + daysInMonth
                                            val totalCells = if (totalRequired > 35) 42 else 35
                                            val weeksCount = totalCells / 7

                                            val allWeeks = remember(ym) {
                                                val result = mutableListOf<List<LocalDate>>()
                                                for (w in 0 until weeksCount) {
                                                    val weekDays = mutableListOf<LocalDate>()
                                                    for (d in 0 until 7) {
                                                        val cellIndex = w * 7 + d
                                                        val cellDate = when {
                                                            cellIndex < startDayOfWeek -> {
                                                                val day = daysInPrevMonth - (startDayOfWeek - cellIndex - 1)
                                                                prevMonth.atDay(day)
                                                            }
                                                            cellIndex < startDayOfWeek + daysInMonth -> {
                                                                val day = cellIndex - startDayOfWeek + 1
                                                                ym.atDay(day)
                                                            }
                                                            else -> {
                                                                val day = cellIndex - (startDayOfWeek + daysInMonth) + 1
                                                                val nextMonth = ym.plusMonths(1)
                                                                nextMonth.atDay(day)
                                                            }
                                                        }
                                                        weekDays.add(cellDate)
                                                    }
                                                    result.add(weekDays)
                                                }
                                                result
                                            }

                                            Column(modifier = Modifier.fillMaxWidth()) {
                                                allWeeks.forEach { weekDays ->
                                                    GoogleCalendarWeekRow(
                                                        weekDays = weekDays,
                                                        currentYearMonth = ym,
                                                        todayString = todayString,
                                                        selectedDate = selectedDate,
                                                        events = events,
                                                        maxLanes = 2,
                                                        onSelectDate = onSelectDate,
                                                        onSelectEvent = onSelectEvent,
                                                        onMonthChanged = onMonthChanged
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    CalendarViewMode.WEEK -> {
                                        // Week View: 7 days starting from Sunday of anchorDate
                                        val dayOfWeekVal = anchorDate.dayOfWeek.value % 7
                                        val sunday = anchorDate.minusDays(dayOfWeekVal.toLong())

                                        AnimatedContent(
                                            targetState = sunday,
                                            transitionSpec = {
                                                val forward = targetState.isAfter(initialState)
                                                val slideInOffset = if (forward) { width: Int -> width } else { width: Int -> -width }
                                                val slideOutOffset = if (forward) { width: Int -> -width } else { width: Int -> width }

                                                ((slideInHorizontally(
                                                    initialOffsetX = slideInOffset,
                                                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                                                ) + fadeIn(
                                                    animationSpec = tween(durationMillis = 200)
                                                )) togetherWith (slideOutHorizontally(
                                                    targetOffsetX = slideOutOffset,
                                                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                                                ) + fadeOut(
                                                    animationSpec = tween(durationMillis = 150)
                                                ))).using(
                                                    SizeTransform(clip = true) { _, _ ->
                                                        spring(stiffness = Spring.StiffnessMediumLow)
                                                    }
                                                )
                                            },
                                            label = "weekPagingTransition"
                                        ) { currentSunday ->
                                            val weekDays = remember(currentSunday) {
                                                (0..6).map { currentSunday.plusDays(it.toLong()) }
                                            }

                                            val weekdays = listOf("週日", "週一", "週二", "週三", "週四", "週五", "週六")
                                            Column(modifier = Modifier.fillMaxWidth()) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    weekdays.forEachIndexed { index, dayName ->
                                                        val isWeekend = index == 0 || index == 6
                                                        val d = weekDays[index]
                                                        Text(
                                                            text = "$dayName (${d.dayOfMonth})",
                                                            modifier = Modifier.weight(1f),
                                                            textAlign = TextAlign.Center,
                                                            style = MaterialTheme.typography.labelSmall,
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 11.sp,
                                                            color = if (isWeekend) MaterialTheme.colorScheme.primary.copy(alpha = 0.85f) else textMuted
                                                        )
                                                    }
                                                }

                                                Spacer(modifier = Modifier.height(4.dp))

                                                GoogleCalendarWeekRow(
                                                    weekDays = weekDays,
                                                    currentYearMonth = YearMonth.from(currentSunday),
                                                    todayString = todayString,
                                                    selectedDate = selectedDate,
                                                    events = events,
                                                    maxLanes = 4,
                                                    onSelectDate = onSelectDate,
                                                    onSelectEvent = onSelectEvent,
                                                    onMonthChanged = onMonthChanged
                                                )
                                            }
                                        }
                                    }

                                    CalendarViewMode.DAY3 -> {
                                        // 3-Day View: anchorDate, anchorDate+1, anchorDate+2
                                        AnimatedContent(
                                            targetState = anchorDate,
                                            transitionSpec = {
                                                val forward = targetState.isAfter(initialState)
                                                val slideInOffset = if (forward) { width: Int -> width } else { width: Int -> -width }
                                                val slideOutOffset = if (forward) { width: Int -> -width } else { width: Int -> width }

                                                ((slideInHorizontally(
                                                    initialOffsetX = slideInOffset,
                                                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                                                ) + fadeIn(
                                                    animationSpec = tween(durationMillis = 200)
                                                )) togetherWith (slideOutHorizontally(
                                                    targetOffsetX = slideOutOffset,
                                                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                                                ) + fadeOut(
                                                    animationSpec = tween(durationMillis = 150)
                                                ))).using(
                                                    SizeTransform(clip = true) { _, _ ->
                                                        spring(stiffness = Spring.StiffnessMediumLow)
                                                    }
                                                )
                                            },
                                            label = "day3PagingTransition"
                                        ) { currentAnchor ->
                                            val threeDays = remember(currentAnchor) {
                                                (0..2).map { currentAnchor.plusDays(it.toLong()) }
                                            }

                                            Column(modifier = Modifier.fillMaxWidth()) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    threeDays.forEach { d ->
                                                        val dayName = when (d.dayOfWeek) {
                                                            DayOfWeek.SUNDAY -> "週日 (${d.monthValue}/${d.dayOfMonth})"
                                                            DayOfWeek.MONDAY -> "週一 (${d.monthValue}/${d.dayOfMonth})"
                                                            DayOfWeek.TUESDAY -> "週二 (${d.monthValue}/${d.dayOfMonth})"
                                                            DayOfWeek.WEDNESDAY -> "週三 (${d.monthValue}/${d.dayOfMonth})"
                                                            DayOfWeek.THURSDAY -> "週四 (${d.monthValue}/${d.dayOfMonth})"
                                                            DayOfWeek.FRIDAY -> "週五 (${d.monthValue}/${d.dayOfMonth})"
                                                            DayOfWeek.SATURDAY -> "週六 (${d.monthValue}/${d.dayOfMonth})"
                                                        }
                                                        Text(
                                                            text = dayName,
                                                            modifier = Modifier.weight(1f),
                                                            textAlign = TextAlign.Center,
                                                            style = MaterialTheme.typography.labelSmall,
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 11.sp,
                                                            color = textMuted
                                                        )
                                                    }
                                                }

                                                Spacer(modifier = Modifier.height(4.dp))

                                                GoogleCalendarWeekRow(
                                                    weekDays = threeDays,
                                                    currentYearMonth = YearMonth.from(currentAnchor),
                                                    todayString = todayString,
                                                    selectedDate = selectedDate,
                                                    events = events,
                                                    maxLanes = 5,
                                                    onSelectDate = onSelectDate,
                                                    onSelectEvent = onSelectEvent,
                                                    onMonthChanged = onMonthChanged
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Google Calendar Week Row with Continuous Multi-Day Spanning Bars.
 * Multi-day events span across multiple columns horizontally with legible wording.
 */
@Composable
private fun GoogleCalendarWeekRow(
    weekDays: List<LocalDate>,
    currentYearMonth: YearMonth,
    todayString: String,
    selectedDate: String?,
    events: List<AcademicEvent>,
    maxLanes: Int = 2,
    onSelectDate: (String?) -> Unit,
    onSelectEvent: (AcademicEvent) -> Unit,
    onMonthChanged: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = com.lingubible.app.core.theme.isAppDarkTheme()
    val textPrimary = if (isDark) Color.White else Color(0xFF0F172A)
    val textMuted = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)

    val positionedEvents = remember(weekDays, events, maxLanes) {
        layoutStrip(weekDays, events, maxLanes)
    }

    val visibleEvents = remember(positionedEvents, maxLanes) {
        positionedEvents.filter { it.lane < maxLanes }
    }

    val overflowPerCol = remember(positionedEvents, maxLanes, weekDays.size) {
        val counts = IntArray(weekDays.size)
        positionedEvents.filter { it.lane >= maxLanes }.forEach { p ->
            for (c in p.startCol..p.endCol) {
                if (c in counts.indices) counts[c]++
            }
        }
        counts
    }

    val hasOverflow = remember(overflowPerCol) { overflowPerCol.any { it > 0 } }
    val headerH = 22.dp
    val laneH = 18.dp
    val laneGap = 2.dp
    val overflowH = 14.dp
    val rowH = headerH + (laneH + laneGap) * maxLanes + (if (hasOverflow) overflowH else 0.dp) + 4.dp

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(rowH)
            .padding(vertical = 1.dp)
    ) {
        val totalWidth = maxWidth
        val colCount = weekDays.size
        val colWidth = totalWidth / colCount

        // 1. Day Column Backgrounds & Day Number Headers
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            weekDays.forEachIndexed { _, date ->
                val dateStr = date.format(DateTimeFormatter.ISO_LOCAL_DATE)
                val isCurrentMonth = date.month == currentYearMonth.month && date.year == currentYearMonth.year
                val isToday = dateStr == todayString
                val isSelected = dateStr == selectedDate

                val cellBg = when {
                    isSelected -> MaterialTheme.colorScheme.primary.copy(alpha = if (isDark) 0.22f else 0.14f)
                    isToday -> if (isDark) Color(0xFF334155).copy(alpha = 0.45f) else Color(0xFFF1F5F9).copy(alpha = 0.7f)
                    else -> Color.Transparent
                }

                val cellBorder = when {
                    isSelected -> BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                    isToday -> BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.6f))
                    else -> BorderStroke(0.5.dp, if (isDark) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.04f))
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(0.5.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(cellBg)
                        .border(cellBorder, RoundedCornerShape(4.dp))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {
                                if (!isCurrentMonth) {
                                    val targetMonth = YearMonth.from(date)
                                    onMonthChanged(targetMonth.toString())
                                }
                                onSelectDate(dateStr)
                            }
                        )
                        .pointerHoverIcon(PointerIcon.Hand)
                        .padding(top = 2.dp, start = 2.dp),
                    contentAlignment = Alignment.TopStart
                ) {
                    // Day Number Badge
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    isSelected -> MaterialTheme.colorScheme.primary
                                    isToday -> if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)
                                    else -> Color.Transparent
                                }
                            )
                            .then(
                                if (isToday && !isSelected) {
                                    Modifier.border(1.dp, MaterialTheme.colorScheme.primary, CircleShape)
                                } else Modifier
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = date.dayOfMonth.toString(),
                            fontSize = 10.sp,
                            fontWeight = when {
                                isSelected || isToday -> FontWeight.Bold
                                isCurrentMonth -> FontWeight.SemiBold
                                else -> FontWeight.Normal
                            },
                            color = when {
                                isSelected -> MaterialTheme.colorScheme.onPrimary
                                isToday -> MaterialTheme.colorScheme.primary
                                isCurrentMonth -> textPrimary
                                else -> textMuted.copy(alpha = 0.35f)
                            }
                        )
                    }
                }
            }
        }

        // 2. Google Calendar Multi-Day Spanning Event Bars Overlay
        visibleEvents.forEach { p ->
            val startX = colWidth * p.startCol
            val spanCols = p.endCol - p.startCol + 1
            val barWidth = colWidth * spanCols
            val topOffset = headerH + (laneH + laneGap) * p.lane

            val barShape = when {
                p.clippedLeft && p.clippedRight -> RoundedCornerShape(0.dp)
                p.clippedLeft -> RoundedCornerShape(topStart = 0.dp, bottomStart = 0.dp, topEnd = 4.dp, bottomEnd = 4.dp)
                p.clippedRight -> RoundedCornerShape(topStart = 4.dp, bottomStart = 4.dp, topEnd = 0.dp, bottomEnd = 0.dp)
                else -> RoundedCornerShape(4.dp)
            }

            Box(
                modifier = Modifier
                    .offset(x = startX + 1.5.dp, y = topOffset)
                    .width(barWidth - 3.dp)
                    .height(laneH)
                    .clip(barShape)
                    .background(p.event.category.composeColor)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {
                            onSelectDate(p.event.start)
                            onSelectEvent(p.event)
                        }
                    )
                    .pointerHoverIcon(PointerIcon.Hand)
                    .padding(horizontal = 4.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = p.event.displayTitle,
                    color = Color.White,
                    fontSize = 8.5.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // 3. Overflow Indicators (+N)
        for (col in 0 until colCount) {
            val count = overflowPerCol[col]
            if (count > 0) {
                val startX = colWidth * col
                val topOffset = headerH + (laneH + laneGap) * maxLanes
                val dateStr = weekDays[col].format(DateTimeFormatter.ISO_LOCAL_DATE)

                Box(
                    modifier = Modifier
                        .offset(x = startX + 2.dp, y = topOffset)
                        .width(colWidth - 4.dp)
                        .height(overflowH)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { onSelectDate(dateStr) }
                        )
                        .pointerHoverIcon(PointerIcon.Hand),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        text = "+$count",
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (dateStr == selectedDate) MaterialTheme.colorScheme.primary else textMuted,
                        modifier = Modifier.padding(horizontal = 2.dp)
                    )
                }
            }
        }
    }
}

