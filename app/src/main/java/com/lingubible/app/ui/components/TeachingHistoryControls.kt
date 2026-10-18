package com.lingubible.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lingubible.app.domain.model.*
import com.lingubible.app.ui.common.hapticClick
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Composable
internal fun rememberTeachingHistoryFilters(key: String) = rememberSaveable(key, stateSaver = Saver<TeachingHistoryFilters, String>(
    save = { Json.encodeToString(it) }, restore = { runCatching { Json.decodeFromString<TeachingHistoryFilters>(it) }.getOrDefault(TeachingHistoryFilters()) }
)) { mutableStateOf(TeachingHistoryFilters()) }

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
internal fun TeachingHistoryControls(records: List<TeachingRecord>, filters: TeachingHistoryFilters, onChange: (TeachingHistoryFilters) -> Unit) {
    var open by remember { mutableStateOf(false) }
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedButton(onClick = hapticClick { open = true }, modifier = Modifier.weight(1f), shapes = AppButtonDefaults.shapes()) { Text("任教篩選 Teaching filters") }
        if (filters != TeachingHistoryFilters()) TextButton(shapes = AppButtonDefaults.shapes(), onClick = hapticClick { onChange(TeachingHistoryFilters()) }) { Text("重設 Reset") }
    }
    if (open) {
        var draft by remember { mutableStateOf(filters) }
        ModalBottomSheet(onDismissRequest = { open = false }, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)) {
            Column(Modifier.fillMaxWidth().imePadding().padding(horizontal = 20.dp)) {
                Text("任教記錄與成績篩選 History & grade filters", style = MaterialTheme.typography.titleLarge)
                Column(Modifier.weight(1f, false).fadingVerticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    FilterMultiSelect("學期 Terms", draft.terms, records.map { it.term }.distinct().sortedDescending().map { it to courseTermLabel(it) }) { draft = draft.copy(terms = it) }
                    FilterMultiSelect("教學語言 Teaching language", draft.languages, records.map { it.teachingLanguage }.distinct().sorted().map { it to it.ifBlank { "未知 Unknown" }.let { v -> teachingLanguageLabel(v) } }) { draft = draft.copy(languages = it) }
                    FilterMultiSelect("課節 Session", draft.sessions, records.map { teachingSessionLabel(it.section) }.distinct().sorted().map { it to it.ifBlank { "未知 Unknown" } }) { draft = draft.copy(sessions = it) }
                    FilterMultiSelect("課程 Courses", draft.courses, records.map { it.courseCode }.distinct().sorted().map { it to it }) { draft = draft.copy(courses = it) }
                }
                FlowRow(Modifier.padding(vertical = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(shapes = AppButtonDefaults.shapes(), onClick = hapticClick { draft = TeachingHistoryFilters() }) { Text("重設 Reset") }
                    Button(onClick = hapticClick { onChange(draft); open = false }, shapes = AppButtonDefaults.shapes()) { Text("套用 Apply") }
                }
            }
        }
    }
}
