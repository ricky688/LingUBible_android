package com.lingubible.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lingubible.app.core.theme.*
import com.lingubible.app.domain.model.AcademicEvent
import com.lingubible.app.domain.model.CalendarCategory
import java.time.LocalDate

/**
 * School Calendar (校歷日程) Screen Content.
 * Exactly matches the web app layout (src/pages/Calendar.tsx) and media_1789044938073.jpg:
 * - Informational notice block
 * - Overview Google Calendar month grid with spanning bars
 * - "所選日期" (Selected Day) detail card with small colored bullet dots (no big icons)
 * - "圖例" (Legend) 2-column interactive category filter grid
 */
@Composable
fun SchoolCalendarView(
    allEvents: List<AcademicEvent>,
    visibleEvents: List<AcademicEvent> = allEvents,
    selectedDayEvents: List<AcademicEvent> = emptyList(),
    selectedMonthKey: String? = null,
    selectedDate: String = "2026-09-10",
    hiddenCategories: Set<CalendarCategory> = emptySet(),
    onSelectMonth: (String?) -> Unit = {},
    onSelectDate: (String) -> Unit = {},
    onToggleCategory: (CalendarCategory) -> Unit = {},
    onShowAllCategories: () -> Unit = {},
    onJumpTermStart: () -> Unit = {},
    onAddToCalendar: (AcademicEvent) -> Unit = {},
    onExportIcs: (AcademicEvent) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val isDark = com.lingubible.app.core.theme.isAppDarkTheme()
    val cardBg = if (isDark) md_theme_dark_surfaceContainer else Color.White
    val cardBorder = if (isDark) md_theme_dark_outlineVariant else Color(0xFFE2E8F0)
    val textMuted = if (isDark) md_theme_dark_onSurfaceVariant else Color(0xFF64748B)

    LazyColumn(
        contentPadding = PaddingValues(top = 8.dp, bottom = 140.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = modifier.fillMaxSize()
    ) {
        // 1. Informational Notice Block matching web app
        item(key = "notice_header") {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "嶺南大學學年的重要日期與活動",
                    style = MaterialTheme.typography.bodyMedium,
                    color = textMuted,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        modifier = Modifier.size(13.dp),
                        tint = textMuted
                    )
                    Text(
                        text = "僅供參考，以官方公佈為準",
                        style = MaterialTheme.typography.labelSmall,
                        color = textMuted,
                        fontSize = 11.sp
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(13.dp),
                        tint = textMuted
                    )
                    Text(
                        text = "資料更新日期: 2026/07/24",
                        style = MaterialTheme.typography.labelSmall,
                        color = textMuted,
                        fontSize = 11.sp
                    )
                }
            }
        }

        // 2. Overview Monthly Calendar with Google Calendar Spanning Bars
        item(key = "monthly_calendar_overview") {
            MonthlyCalendarOverview(
                events = visibleEvents,
                selectedMonthKey = selectedMonthKey,
                selectedDate = selectedDate,
                onSelectDate = { dateStr ->
                    if (dateStr != null) onSelectDate(dateStr)
                },
                onMonthChanged = onSelectMonth,
                onSelectEvent = { event ->
                    onSelectDate(event.start)
                },
                onJumpTermStart = onJumpTermStart
            )
        }

        // 3. "所選日期" (Selected Date) Details Card matching media_1789044938073.jpg
        item(key = "selected_day_card") {
            val formattedDate = remember(selectedDate) {
                try {
                    val date = LocalDate.parse(selectedDate)
                    val weekdayZh = when (date.dayOfWeek.value) {
                        1 -> "星期一"
                        2 -> "星期二"
                        3 -> "星期三"
                        4 -> "星期四"
                        5 -> "星期五"
                        6 -> "星期六"
                        else -> "星期日"
                    }
                    "${date.year}年${date.monthValue}月${date.dayOfMonth}日 $weekdayZh"
                } catch (e: Exception) {
                    selectedDate
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, cardBorder, RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        // Header row: 所選日期 ... 2026年9月10日 星期四
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "所選日期",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) Color(0xFFCBD5E1) else Color(0xFF475569)
                            )
                            Text(
                                text = formattedDate,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isDark) Color.White else Color(0xFF1E293B),
                                fontSize = 13.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        if (selectedDayEvents.isEmpty()) {
                            Text(
                                text = "所選日期當天沒有特定校歷日程\nNo events scheduled on this day",
                                style = MaterialTheme.typography.bodyMedium,
                                color = textMuted,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 24.dp)
                            )
                        } else {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                selectedDayEvents.forEach { event ->
                                    WebCalendarEventItem(
                                        event = event,
                                        isDark = isDark,
                                        textMuted = textMuted,
                                        onExportIcs = onExportIcs,
                                        onAddToCalendar = onAddToCalendar
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 4. "圖例" (Category Legend Card) matching media_1789044938073.jpg
        item(key = "legend_card") {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, cardBorder, RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        // Legend Header: 圖例 ... 顯示全部
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "圖例",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) Color(0xFFCBD5E1) else Color(0xFF475569)
                            )

                            TextButton(
                                onClick = onShowAllCategories,
                                enabled = hiddenCategories.isNotEmpty(),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "顯示全部",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (hiddenCategories.isNotEmpty()) MaterialTheme.colorScheme.primary else textMuted.copy(alpha = 0.4f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // 2-Column Grid matching exactly the order in media_1789044938073.jpg
                        val col1 = listOf(
                            CalendarCategory.TERM,
                            CalendarCategory.HOLIDAY,
                            CalendarCategory.REGISTRATION,
                            CalendarCategory.ASSESSMENT,
                            CalendarCategory.EVENT
                        )
                        val col2 = listOf(
                            CalendarCategory.EXAM,
                            CalendarCategory.ADD_DROP,
                            CalendarCategory.DEADLINE,
                            CalendarCategory.GRADUATION,
                            CalendarCategory.HOSTEL
                        )

                        Row(modifier = Modifier.fillMaxWidth()) {
                            // Column 1
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                col1.forEach { cat ->
                                    LegendCategoryItem(
                                        category = cat,
                                        isHidden = hiddenCategories.contains(cat),
                                        isDark = isDark,
                                        textMuted = textMuted,
                                        onToggle = { onToggleCategory(cat) }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            // Column 2
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                col2.forEach { cat ->
                                    LegendCategoryItem(
                                        category = cat,
                                        isHidden = hiddenCategories.contains(cat),
                                        isDark = isDark,
                                        textMuted = textMuted,
                                        onToggle = { onToggleCategory(cat) }
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

/**
 * Event list item styled exactly like web app (src/pages/Calendar.tsx lines 855-893):
 * - Small colored bullet dot (No large 48dp badge/icon)
 * - Event title in bold typography
 * - Date range formatted as "9月10日" or "8月24日 – 1月3日"
 * - Soft colored category pill badge
 * - Tap to expand quick actions (.ics export & add to calendar)
 */
@Composable
private fun WebCalendarEventItem(
    event: AcademicEvent,
    isDark: Boolean,
    textMuted: Color,
    onExportIcs: (AcademicEvent) -> Unit,
    onAddToCalendar: (AcademicEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    val itemBg = MaterialTheme.colorScheme.surfaceContainerLow
    val itemBorder = MaterialTheme.colorScheme.outlineVariant.copy(alpha = if (isDark) 0.35f else 0.5f)
    var isExpanded by remember { mutableStateOf(false) }

    Surface(
        color = itemBg,
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(0.8.dp, itemBorder),
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(spring(stiffness = Spring.StiffnessMediumLow))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { isExpanded = !isExpanded }
                .pointerHoverIcon(PointerIcon.Hand)
                .padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                // Clean colored bullet dot (No icon, no letter, exactly matching web app!)
                Box(
                    modifier = Modifier
                        .padding(top = 5.dp)
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(event.category.composeColor)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = event.displayTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        color = if (isDark) Color.White else Color(0xFF0F172A)
                    )

                    Spacer(modifier = Modifier.height(5.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = event.formatWebDateRange(),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = textMuted,
                            fontSize = 12.sp
                        )

                        // Category soft pill badge
                        Surface(
                            color = event.category.composeColor.copy(alpha = if (isDark) 0.22f else 0.14f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = event.category.labelZh,
                                color = event.category.composeColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        if (event.category2 != null) {
                            Surface(
                                color = event.category2.composeColor.copy(alpha = if (isDark) 0.22f else 0.14f),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = event.category2.labelZh,
                                    color = event.category2.composeColor,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Tap-to-reveal quick actions (.ics & Add to Calendar)
            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = itemBorder, thickness = 0.8.dp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = { onExportIcs(event) },
                            shape = RoundedCornerShape(14.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 3.dp),
                            modifier = Modifier
                                .height(30.dp)
                                .pointerHoverIcon(PointerIcon.Hand)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Share,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(".ics 匯出", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(
                            onClick = { onAddToCalendar(event) },
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 3.dp),
                            modifier = Modifier
                                .height(30.dp)
                                .pointerHoverIcon(PointerIcon.Hand)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.CalendarMonth,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("加入日曆 Add", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

/**
 * Individual Legend category toggle item matching web app.
 */
@Composable
private fun LegendCategoryItem(
    category: CalendarCategory,
    isHidden: Boolean,
    isDark: Boolean,
    textMuted: Color,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .clickable { onToggle() }
            .pointerHoverIcon(PointerIcon.Hand)
            .padding(vertical = 6.dp, horizontal = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(if (isHidden) textMuted.copy(alpha = 0.35f) else category.composeColor)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = category.labelZh,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = if (isHidden) textMuted.copy(alpha = 0.45f) else (if (isDark) Color(0xFFE2E8F0) else Color(0xFF1E293B)),
            textDecoration = if (isHidden) TextDecoration.LineThrough else null
        )
    }
}
