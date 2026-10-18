package com.lingubible.app.ui.components

import com.lingubible.app.domain.model.*
import com.lingubible.app.core.util.TimetableExportColors
import com.lingubible.app.ui.common.hapticClick

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lingubible.app.core.theme.LingnanRed
import com.lingubible.app.core.theme.isAppDarkTheme
import com.lingubible.app.domain.model.TimeConflict
import com.lingubible.app.domain.model.TimetableMeeting
import com.lingubible.app.domain.model.TimetableSection
import com.lingubible.app.domain.model.TimetableTerm
import com.lingubible.app.core.util.TimetableExportOrientation
import com.lingubible.app.core.util.TimetableImageExport
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Locale
import com.lingubible.app.core.navigation.LocalScreenBottomClearance
import com.lingubible.app.core.navigation.screenBottomPadding

data class DayInfo(
    val code: String,
    val zh: String,
    val en: String
)

/**
 * 7 Days of the Week strictly ordered from Sunday to Saturday (left to right)
 */
val DAYS_SUN_TO_SAT = listOf(
    DayInfo("SUN", "週日", "Sun"),
    DayInfo("MON", "週一", "Mon"),
    DayInfo("TUE", "週二", "Tue"),
    DayInfo("WED", "週三", "Wed"),
    DayInfo("THU", "週四", "Thu"),
    DayInfo("FRI", "週五", "Fri"),
    DayInfo("SAT", "週六", "Sat")
)

private val HOUR_HEIGHT = 64.dp

@Composable
fun TimetableGrid(
    selectedSections: List<TimetableSection>,
    creditsByCode: Map<String, Int>,
    term: TimetableTerm,
    conflicts: List<TimeConflict>,
    onRemoveSection: (String) -> Unit,
    onNavigateToAddCourse: () -> Unit,
    onAddMeetingToCalendar: (TimetableSection, TimetableMeeting) -> Unit,
    onExportMeetingIcs: (TimetableSection, TimetableMeeting) -> Unit,
    modifier: Modifier = Modifier,
    appearance: TimetableAppearance = TimetableAppearance()
) {
    val isDark = isAppDarkTheme()
    val visibleDays = appearance.orderedDays.map { code -> DAYS_SUN_TO_SAT.first { it.code == code } }
    val (rangeStart, rangeEnd) = remember(selectedSections, appearance) { timetableMinuteRange(selectedSections, appearance) }
    val gridMinutes = rangeEnd - rangeStart
    val naturalGridHeight = HOUR_HEIGHT * (gridMinutes / 60f)
    val exportColors = TimetableExportColors(MaterialTheme.colorScheme.background.toArgb(), MaterialTheme.colorScheme.surfaceContainer.toArgb(), MaterialTheme.colorScheme.onSurface.toArgb(), MaterialTheme.colorScheme.outlineVariant.toArgb())
    var selectedMeetingForDialog by remember { mutableStateOf<Pair<TimetableSection, TimetableMeeting>?>(null) }
    var isDirectView by remember { mutableStateOf(true) }

    val verticalScrollState = rememberScrollState()
    val horizontalScrollState = rememberScrollState()

    val gridBg = MaterialTheme.colorScheme.surfaceContainerLow
    val gridBorder = MaterialTheme.colorScheme.outlineVariant.copy(alpha = if (isDark) 0.5f else 0.8f)
    val textMuted = MaterialTheme.colorScheme.onSurfaceVariant
    val bottomClearance = LocalScreenBottomClearance.current
    val fadeSurface = MaterialTheme.colorScheme.background
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val headerLayer = rememberGraphicsLayer()
    val bodyLayer = rememberGraphicsLayer()
    val imageBackground = MaterialTheme.colorScheme.background.toArgb()
    var exportingImage by remember { mutableStateOf(false) }
    var pendingImagePath by rememberSaveable { mutableStateOf<String?>(null) }
    var showExportPreviewDialog by remember { mutableStateOf(false) }
    val saveImage = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val uri = result.data?.data?.takeIf { result.resultCode == android.app.Activity.RESULT_OK }
        val file = pendingImagePath?.let(::File)
        pendingImagePath = null
        if (file != null) {
            exportingImage = true
            scope.launch {
                try {
                    if (uri != null) {
                        withContext(Dispatchers.IO) { TimetableImageExport.saveToDocument(context, file, uri) }
                        Toast.makeText(context, "已匯出課表 / Timetable saved", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    if (uri != null) withContext(kotlinx.coroutines.NonCancellable + Dispatchers.IO) { runCatching { android.provider.DocumentsContract.deleteDocument(context.contentResolver, uri) } }
                    if (e is CancellationException) throw e
                    Toast.makeText(context, "無法儲存課表，請重試 / Could not save timetable; please retry", Toast.LENGTH_LONG).show()
                } finally {
                    file.delete()
                    exportingImage = false
                }
            }
        }
    }

    if (selectedSections.isEmpty()) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .screenBottomPadding(extra = 0.dp)
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = null,
                    modifier = Modifier.size(56.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "課表尚無課程 No Courses in Timetable",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) Color.White else Color(0xFF1E293B),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "點擊下方按鈕搜尋並加選課程，即可即時預覽每週課表與時間衝突！",
                    style = MaterialTheme.typography.bodyMedium,
                    color = textMuted,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = hapticClick(onNavigateToAddCourse),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    shapes = AppButtonDefaults.shapes(shape = RoundedCornerShape(20.dp)),
                    modifier = Modifier.pointerHoverIcon(PointerIcon.Hand)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("加選課程 Add Courses", fontWeight = FontWeight.Bold)
                }
            }
        }
        return
    }

    Column(modifier = modifier.fillMaxWidth()) {
        // Control Bar: Sunday - Saturday header indicator and Direct vs Expanded Scroll Mode Toggle
        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(modifier = Modifier.align(Alignment.CenterVertically), verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = appearance.title.ifBlank { "每週課表 Weekly timetable" },
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) Color.White else Color(0xFF1E293B)
                )
            }

            // Direct Viewport (7-Day Fit) vs Scroll View Mode Toggle Pill
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = if (isDark) Color(0xFF1E293B) else Color(0xFFF1F5F9),
                border = BorderStroke(1.dp, gridBorder),
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .clickable (onClick = hapticClick { isDirectView = !isDirectView })
                    .pointerHoverIcon(PointerIcon.Hand)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isDirectView) "直觀全覽 Direct" else "展開捲動 Scroll",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isDirectView) MaterialTheme.colorScheme.primary else textMuted,
                        fontSize = 11.sp
                    )
                }
            }
            FilledTonalButton(
                shapes = AppButtonDefaults.shapes(),
                enabled = !exportingImage && pendingImagePath == null,
                contentPadding = PaddingValues(horizontal = 10.dp),
                onClick = hapticClick {
                    showExportPreviewDialog = true
                }
            ) {
                Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text("匯出 PNG/PDF")
            }
        }

        // Timetable Matrix with BoxWithConstraints for Direct Left-to-Right Viewing
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val gridHeight = if (constraints.hasBoundedHeight) maxOf(naturalGridHeight, (maxHeight - 45.dp).coerceAtLeast(0.dp)) else naturalGridHeight
            val hourHeight = gridHeight * (60f / gridMinutes)
            val totalWidth = maxWidth
            val timeColWidth = if (isDirectView) 38.dp else 48.dp
            val availableWidth = (totalWidth - timeColWidth).coerceAtLeast(0.dp)
            val dayColWidth = if (isDirectView) {
                (availableWidth / visibleDays.size).coerceAtLeast(44.dp)
            } else {
                135.dp
            }
            val needsHorizontalScroll = !isDirectView || (dayColWidth * visibleDays.size + timeColWidth > totalWidth)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(
                        if (needsHorizontalScroll) Modifier.horizontalScroll(horizontalScrollState)
                        else Modifier
                    )
            ) {
                Column {
                    // Day Headers (SUN to SAT)
                    Row(
                        modifier = Modifier
                            .drawWithContent {
                                headerLayer.record { this@drawWithContent.drawContent() }
                                drawLayer(headerLayer)
                            }
                            .background(MaterialTheme.colorScheme.surfaceContainer)
                            .padding(bottom = 1.dp)
                    ) {
                        // Top-left blank corner for time labels
                        Box(
                            modifier = Modifier
                                .width(timeColWidth)
                                .height(44.dp)
                                .border(0.5.dp, gridBorder),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "時間",
                                style = MaterialTheme.typography.labelSmall,
                                color = textMuted,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = if (isDirectView) 10.sp else 11.sp
                            )
                        }

                        // 7 Day columns: Sunday to Saturday
                        visibleDays.forEach { day ->
                            Box(
                                modifier = Modifier
                                    .width(dayColWidth)
                                    .height(44.dp)
                                    .border(0.5.dp, gridBorder),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = day.zh,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = if (isDirectView) 11.sp else 13.sp,
                                        color = if (isDark) Color.White else Color(0xFF0F172A)
                                    )
                                    Text(
                                        text = day.en,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = if (isDirectView) 9.sp else 11.sp,
                                        color = textMuted
                                    )
                                }
                            }
                        }
                    }

                    // Grid Body with vertical scroll
                    Box(
                        modifier = Modifier
                            .height(gridHeight)
                            .fadingBlurEdges(
                                scrollState = verticalScrollState,
                                surfaceColor = fadeSurface,
                                bottomInset = bottomClearance
                            )
                            .verticalScroll(verticalScrollState)
                    ) {
                        // Record the full non-lazy body inside the scroll viewport, including off-screen rows.
                        Row(modifier = Modifier.screenBottomPadding(extra = 0.dp).drawWithContent {
                            bodyLayer.record { this@drawWithContent.drawContent() }
                            drawLayer(bodyLayer)
                        }) {
                            // Time Column (08:30 to 20:30)
                            Column(modifier = Modifier.width(timeColWidth)) {
                                for (h in 0 until ((gridMinutes + 59) / 60)) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(hourHeight)
                                            .border(0.5.dp, gridBorder),
                                        contentAlignment = Alignment.TopCenter
                                    ) {
                                        Text(
                                            text = timetableClock(rangeStart + h * 60),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontSize = if (isDirectView) 9.sp else 10.sp,
                                            color = textMuted,
                                            modifier = Modifier.padding(top = 4.dp)
                                        )
                                    }
                                }
                            }

                            // Day Columns with Course Blocks: Sunday to Saturday
                            visibleDays.forEach { day ->
                                Box(
                                    modifier = Modifier
                                        .width(dayColWidth)
                                        .height(gridHeight)
                                        .background(gridBg)
                                        .border(0.5.dp, gridBorder)
                                ) {
                                    // Background hour grid lines
                                    for (h in 0 until ((gridMinutes + 59) / 60)) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .offset(y = hourHeight * h)
                                                .height(hourHeight)
                                                .border(0.25.dp, gridBorder.copy(alpha = 0.5f))
                                        )
                                    }

                                    // Course Blocks for this Day
                                    val dayMeetings = mutableListOf<Pair<TimetableSection, TimetableMeeting>>()
                                    for (s in selectedSections) {
                                        for (m in s.meetings) {
                                            if (m.day.equals(day.code, ignoreCase = true)) {
                                                dayMeetings.add(s to m)
                                            }
                                        }
                                    }

                                    dayMeetings.forEach { (section, meeting) ->
                                        if (meeting.endMinutes + 1 <= rangeStart || meeting.startMinutes >= rangeEnd) return@forEach
                                        val startMin = (meeting.startMinutes - rangeStart).coerceAtLeast(0)
                                        val durationMin = minOf(meeting.endMinutes + 1, rangeEnd) - maxOf(meeting.startMinutes, rangeStart)

                                        val topOffset = (startMin.toFloat() / gridMinutes.toFloat()) * gridHeight.value
                                        val blockHeight = (durationMin.toFloat() / gridMinutes.toFloat()) * gridHeight.value

                                        val isClashing = conflicts.any {
                                            (it.sectionA.id == section.id || it.sectionB.id == section.id) && it.day.equals(day.code, ignoreCase = true)
                                        }

                                        val blockColor = try {
                                            Color(android.graphics.Color.parseColor(appearance.colors[section.courseCode] ?: section.colorHex))
                                        } catch (e: Exception) {
                                            MaterialTheme.colorScheme.primary
                                        }

                                        Surface(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .offset(y = topOffset.dp)
                                                .height(blockHeight.dp)
                                                .padding(horizontal = 1.5.dp, vertical = 1.dp)
                                                .clip(RoundedCornerShape(6.dp))
                                                .border(
                                                    width = if (isClashing) 2.dp else 1.dp,
                                                    color = if (isClashing) Color(0xFFEF4444) else blockColor.copy(alpha = 0.8f),
                                                    shape = RoundedCornerShape(6.dp)
                                                )
                                                .clickable (onClick = hapticClick { selectedMeetingForDialog = section to meeting })
                                                .pointerHoverIcon(PointerIcon.Hand),
                                            color = blockColor.copy(alpha = 0.92f),
                                            shape = RoundedCornerShape(6.dp),
                                            shadowElevation = 2.dp
                                        ) {
                                            val hex = appearance.colors[section.courseCode] ?: section.colorHex
                                            val foreground = if (timetableUsesLightText(hex)) Color.White else Color.Black
                                            Column(Modifier.fillMaxSize().padding(3.dp), verticalArrangement = Arrangement.spacedBy(1.dp)) {
                                                timetableBlockLines(section, meeting, appearance, creditsByCode[section.courseCode.uppercase()]).forEachIndexed { index, line ->
                                                    Text(line, style = MaterialTheme.typography.labelSmall, fontSize = if (isDirectView) 9.sp else 12.sp,
                                                        fontWeight = if (index == 0) FontWeight.Bold else FontWeight.Normal, color = foreground,
                                                        maxLines = 1, overflow = TextOverflow.Ellipsis)
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

    // Section Detail Dialog
    selectedMeetingForDialog?.let { (section, meeting) ->
        val credits = creditsByCode[section.courseCode.trim().uppercase(Locale.ROOT)]
        val sectionConflicts = conflicts.filter {
            it.sectionA.id == section.id || it.sectionB.id == section.id
        }

        AlertDialog(
            onDismissRequest = { selectedMeetingForDialog = null },
            icon = {
                Icon(
                    imageVector = if (sectionConflicts.isNotEmpty()) Icons.Default.Warning else Icons.Default.School,
                    contentDescription = null,
                    tint = if (sectionConflicts.isNotEmpty()) Color(0xFFDC2626) else try {
                        Color(android.graphics.Color.parseColor(appearance.colors[section.courseCode] ?: section.colorHex))
                    } catch (e: Exception) { MaterialTheme.colorScheme.primary }
                )
            },
            title = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${section.courseCode} Section ${section.section}",
                        fontWeight = FontWeight.Bold
                    )

                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth().fadingVerticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Expressive conflict warning callout if clashing
                    if (sectionConflicts.isNotEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isDark) Color(0xFF3B1216).copy(alpha = 0.85f) else Color(0xFFFEF2F2),
                            border = BorderStroke(1.dp, if (isDark) Color(0xFFEF4444).copy(alpha = 0.6f) else Color(0xFFFCA5A5)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = if (isDark) Color(0xFFF87171) else Color(0xFFDC2626),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "衝堂警告 · Conflict Notice",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isDark) Color(0xFFFCA5A5) else Color(0xFF991B1B)
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                sectionConflicts.forEach { conf ->
                                    val otherSec = if (conf.sectionA.id == section.id) conf.sectionB else conf.sectionA
                                    Text(
                                        text = "• 與 ${otherSec.courseCode} (S${otherSec.section}) 在 ${conf.day} ${conf.timeDescription} 重疊",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (isDark) Color(0xFFFCA5A5) else Color(0xFFB91C1C),
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }

                    Text(
                        text = section.courseTitle,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    HorizontalDivider()
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text("學分 Credits:", style = MaterialTheme.typography.labelMedium, color = textMuted)
                        Text(
                            text = credits?.toString() ?: "暫無資料 / Unavailable",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    if (section.crn.isNotBlank()) {
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("CRN:", style = MaterialTheme.typography.labelMedium, color = textMuted)
                            Text(section.crn, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        }
                    }
                    if (section.instructors.isNotEmpty()) {
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("講師 Instructor:", style = MaterialTheme.typography.labelMedium, color = textMuted, modifier = Modifier.weight(1f))
                            Text(
                                section.instructors.joinToString(", "),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.End,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    Text("上課時間 Meetings:", style = MaterialTheme.typography.labelMedium, color = textMuted)
                    section.meetings.forEach { m ->
                        val isSelected = section.meetings.size > 1 && m == meeting
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "${if (isSelected) "✓" else "•"} ${m.day} ${m.start} – ${m.end} · ${m.type}",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else textMuted
                            )
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("地點 Location:", style = MaterialTheme.typography.labelMedium, color = textMuted)
                                Text(
                                    m.venue.ifBlank { "待定 TBD" },
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Medium,
                                    textAlign = TextAlign.End,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                    Text(
                        "每週重複 Weekly · ${term.startDate} – ${term.endDate}",
                        style = MaterialTheme.typography.bodySmall,
                        color = textMuted
                    )
                }
            },
            confirmButton = {
                Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        shapes = AppButtonDefaults.shapes(),
                        modifier = Modifier.fillMaxWidth(),
                        onClick = hapticClick { onAddMeetingToCalendar(section, meeting) }
                    ) {
                        Icon(Icons.Default.EventAvailable, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("加至日曆 Add to calendar", textAlign = TextAlign.Center)
                    }
                    OutlinedButton(
                        shapes = AppButtonDefaults.shapes(),
                        modifier = Modifier.fillMaxWidth(),
                        onClick = hapticClick { onExportMeetingIcs(section, meeting) }
                    ) { Text("匯出此時段 Export meeting (.ics)", textAlign = TextAlign.Center) }
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        TextButton(
                            shapes = AppButtonDefaults.shapes(),
                            onClick = hapticClick {
                                onRemoveSection(section.id)
                                selectedMeetingForDialog = null
                            },
                            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                            modifier = Modifier.pointerHoverIcon(PointerIcon.Hand)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("移除 Remove")
                        }
                        TextButton(
                            shapes = AppButtonDefaults.shapes(),
                            onClick = hapticClick { selectedMeetingForDialog = null },
                            modifier = Modifier.pointerHoverIcon(PointerIcon.Hand)
                        ) { Text("關閉 Close") }
                    }
                }
            }
        )
    }

    if (showExportPreviewDialog) {
        TimetableExportPreviewModal(
            term = term,
            selectedSections = selectedSections,
            conflicts = conflicts,
            creditsByCode = creditsByCode,
            appearance = appearance,
            onDismiss = { showExportPreviewDialog = false },
            onConfirmSave = { orientation, showGridLines, format ->
                showExportPreviewDialog = false
                exportingImage = true
                scope.launch {
                    try {
                        val file = TimetableImageExport.capture(
                            context = context,
                            orientation = orientation,
                            term = term,
                            selectedSections = selectedSections,
                            conflicts = conflicts,
                            isDark = isDark,
                            creditsByCode = creditsByCode,
                            showGridLines = showGridLines,
                            format = format,
                            appearance = appearance,
                            colors = exportColors
                        )
                        pendingImagePath = file.absolutePath
                        val suffix = if (orientation == TimetableExportOrientation.LANDSCAPE) "Landscape" else "Portrait"
                        saveImage.launch(android.content.Intent(android.content.Intent.ACTION_CREATE_DOCUMENT).apply {
                            addCategory(android.content.Intent.CATEGORY_OPENABLE)
                            type = format.mime
                            putExtra(android.content.Intent.EXTRA_TITLE, "LingUBible_${term.short}_Timetable_${suffix}.${format.extension}")
                        })
                    } catch (cancelled: CancellationException) {
                        throw cancelled
                    } catch (_: Exception) {
                        pendingImagePath?.let { File(it).delete() }
                        pendingImagePath = null
                        Toast.makeText(context, "無法擷取課表圖片，請重試 / Could not capture timetable; please retry", Toast.LENGTH_LONG).show()
                    } finally {
                        exportingImage = false
                    }
                }
            }
        )
    }
}
