package com.lingubible.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.semantics.*
import com.lingubible.app.domain.model.*
import com.lingubible.app.ui.common.hapticClick
import com.lingubible.app.ui.common.hapticChange
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TimetableAppearanceSheet(term: TimetableTerm, sections: List<TimetableSection>, appearance: TimetableAppearance,
    onDismiss: () -> Unit, onApply: (TimetableAppearance) -> Boolean) {
    var draft by rememberSaveable(term.id, stateSaver = Saver<TimetableAppearance, String>(
        save = { Json.encodeToString(it) }, restore = { runCatching { Json.decodeFromString<TimetableAppearance>(it) }.getOrDefault(appearance) }
    )) { mutableStateOf(appearance) }
    var start by rememberSaveable { mutableStateOf(timetableClock(draft.startMinutes)) }
    var end by rememberSaveable { mutableStateOf(timetableClock(draft.endMinutes)) }
    var attempted by rememberSaveable { mutableStateOf(false) }
    var colorCourse by rememberSaveable { mutableStateOf<String?>(null) }
    val candidate = if (draft.autoRange) draft else draft.copy(startMinutes = parseTimetableClock(start) ?: -1, endMinutes = parseTimetableClock(end) ?: -1)
    val error = timetableAppearanceError(candidate, term)
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)) {
        Column(Modifier.fillMaxWidth().imePadding().padding(horizontal = 20.dp)) {
            Text("課表自訂 Customize timetable", style = MaterialTheme.typography.titleLarge)
            Text(term.name, style = MaterialTheme.typography.bodySmall)
            Column(Modifier.weight(1f, false).fadingVerticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(draft.title, { draft = draft.copy(title = it.take(120)) }, label = { Text("自訂標題 Custom title") }, placeholder = { Text(term.name) }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Text("課堂內容 Meeting fields", style = MaterialTheme.typography.titleMedium)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TIMETABLE_FIELDS.forEach { (key, label) -> FilterChip(key in draft.fields, onClick = hapticClick { draft = draft.copy(fields = if (key in draft.fields) draft.fields - key else draft.fields + key) }, label = { Text(label) }) }
                }
                Text("顯示星期 Visible days", style = MaterialTheme.typography.titleMedium)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    DAYS_SUN_TO_SAT.forEach { day -> FilterChip(day.code in draft.days, onClick = hapticClick { draft = draft.copy(days = if (day.code in draft.days) draft.days - day.code else draft.days + day.code) }, label = { Text("${day.zh} ${day.en}") }) }
                }
                Row {
                    Text("星期一為首日 Monday first", Modifier.weight(1f))
                    Switch(draft.mondayFirst, hapticChange { draft = draft.copy(mondayFirst = it) }, modifier = Modifier.semantics { contentDescription = "星期一為首日 Monday first" })
                }
                Row {
                    Text("自動時間範圍 Fit meeting times", Modifier.weight(1f))
                    Switch(draft.autoRange, hapticChange { draft = draft.copy(autoRange = it) }, modifier = Modifier.semantics { contentDescription = "自動時間範圍 Fit meeting times" })
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(start, { start = it.take(5) }, label = { Text("開始 Start") }, enabled = !draft.autoRange, singleLine = true, modifier = Modifier.weight(1f))
                    OutlinedTextField(end, { end = it.take(5) }, label = { Text("結束 End") }, enabled = !draft.autoRange, singleLine = true, modifier = Modifier.weight(1f))
                }
                Text("24 小時格式 HH:mm；顯示設定不會移除課程或限制日曆匯出。\nHH:mm (24-hour). Display settings preserve courses and calendar exports.", style = MaterialTheme.typography.bodySmall)
                Text("課程色彩 Course colors", style = MaterialTheme.typography.titleMedium)
                if (sections.isEmpty()) Text("加入課程後可選擇色彩 Add courses to choose colors")
                sections.distinctBy { it.courseCode }.forEach { section ->
                    Row(Modifier.fillMaxWidth()) {
                        FilledTonalButton(onClick = hapticClick { colorCourse = section.courseCode }, modifier = Modifier.weight(1f), shapes = AppButtonDefaults.shapes()) {
                            Text("${section.courseCode} · ${draft.colors[section.courseCode] ?: section.colorHex}")
                        }
                        if (section.courseCode in draft.colors) TextButton(shapes = AppButtonDefaults.shapes(), onClick = hapticClick { draft = draft.copy(colors = draft.colors - section.courseCode) }) { Text("重設 Reset") }
                    }
                }
                Text("匯出解像度 Export resolution", style = MaterialTheme.typography.titleMedium)
                M3ButtonGroup(draft.resolution - 1, listOf("低 Low", "標準 Standard", "高 High").mapIndexed { index, label -> M3ButtonGroupItem(label, onClick = { draft = draft.copy(resolution = index + 1) }) }, height = 48.dp)
                Text("日曆匯出日期 Calendar export dates", style = MaterialTheme.typography.titleMedium)
                Text("留空使用學期日期 Leave blank for official term dates", style = MaterialTheme.typography.bodySmall)
                OutlinedTextField(draft.icsStart, { draft = draft.copy(icsStart = it.take(10)) }, label = { Text("開始 Start · YYYY-MM-DD") }, placeholder = { Text(term.startDate) }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(draft.icsEnd, { draft = draft.copy(icsEnd = it.take(10)) }, label = { Text("結束 End · YYYY-MM-DD") }, placeholder = { Text(term.endDate) }, singleLine = true, modifier = Modifier.fillMaxWidth())
            }
            if (attempted && error != null) Text(error, color = MaterialTheme.colorScheme.error, modifier = Modifier.semantics { liveRegion = LiveRegionMode.Assertive })
            Row(Modifier.fillMaxWidth().padding(vertical = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(shapes = AppButtonDefaults.shapes(), onClick = hapticClick { draft = TimetableAppearance(); start = "08:30"; end = "21:30"; attempted = false }) { Text("重設 Reset") }
                Button(onClick = hapticClick { attempted = true; if (error == null && onApply(candidate)) onDismiss() }, shapes = AppButtonDefaults.shapes()) { Text("套用 Apply") }
            }
        }
    }
    colorCourse?.let { code ->
        val hex = draft.colors[code] ?: sections.firstOrNull { it.courseCode == code }?.colorHex ?: "#DC2626"
        CustomColorPickerDialog(Color(android.graphics.Color.parseColor(hex)), onColorSelected = { color ->
            draft = draft.copy(colors = draft.colors + (code to "#%06X".format(color.toArgb() and 0xFFFFFF))); colorCourse = null
        }, onDismiss = { colorCourse = null })
    }
}
