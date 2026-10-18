package com.lingubible.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.dp
import com.lingubible.app.domain.model.*
import com.lingubible.app.ui.common.hapticClick
import com.lingubible.app.ui.common.hapticChange

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CourseFilterSheet(filters: CourseFilters, terms: List<CourseTerm>, signedIn: Boolean, optionsError: String?, onRetry: () -> Unit, onDismiss: () -> Unit, onApply: (CourseFilters) -> Unit) {
    var draft by remember { mutableStateOf(filters) }
    LaunchedEffect(signedIn) { if (!signedIn) draft = draft.copy(hasMaterials = false) }
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)) {
        Column(Modifier.fillMaxWidth().imePadding().padding(horizontal = 20.dp)) {
            Text("篩選與排序 Filter and sort", style = MaterialTheme.typography.titleLarge)
            Column(Modifier.weight(1f, fill = false).fadingVerticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                FilterMultiSelect("學系 Departments", draft.subjects, COURSE_CATEGORIES.filter { it.code != "ALL" }.map { it.code to "${it.nameZh} ${it.code}" }) { draft = draft.copy(subjects = it) }
                FilterMultiSelect("教學語言 Teaching language", draft.languages, listOf("E" to "英語 English", "C" to "粵語 Cantonese", "P" to "普通話 Putonghua", "1" to "英語／粵語 E/C", "2" to "英語／普通話 E/P", "3" to "粵語／普通話 C/P", "4" to "英語／粵語／普通話 E/C/P", "5" to "其他 Others")) { draft = draft.copy(languages = it) }
                FilterMultiSelect("服務學習 Service learning", draft.serviceLearning, listOf("compulsory" to "必修 Compulsory", "optional" to "選修 Optional", "none" to "無 None")) { draft = draft.copy(serviceLearning = it) }
                FilterMultiSelect("開設學期 Offered term", draft.terms, terms.map { it.code to it.label }) { draft = draft.copy(terms = it) }
                if (terms.isEmpty()) TextButton(shapes = AppButtonDefaults.shapes(), onClick = hapticClick(onRetry)) { Text(optionsError ?: "載入學期 Load terms") }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(if (signedIn) "僅顯示有學習資料的課程\nHas study materials" else "登入後可篩選學習資料\nSign in to filter materials", modifier = Modifier.weight(1f))
                    Switch(draft.hasMaterials, hapticChange { draft = draft.copy(hasMaterials = it) }, enabled = signedIn, modifier = Modifier.semantics { contentDescription = "僅顯示有學習資料 Has study materials" })
                }
                Text("排序 Sort", style = MaterialTheme.typography.titleMedium)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CourseSort.entries.forEach { sort -> FilterChip(draft.sort == sort, onClick = hapticClick { draft = draft.copy(sort = sort) }, label = { Text(sort.label) }) }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("由大至小／Z–A Descending", modifier = Modifier.weight(1f))
                    Switch(draft.descending, hapticChange { draft = draft.copy(descending = it) }, modifier = Modifier.semantics { contentDescription = "降冪排序 Descending order" })
                }
            }
            FlowRow(Modifier.fillMaxWidth().padding(vertical = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(shapes = AppButtonDefaults.shapes(), onClick = hapticClick { draft = CourseFilters() }) { Text("重設 Reset") }
                Button(shapes = AppButtonDefaults.shapes(), onClick = hapticClick { onApply(draft); onDismiss() }) { Text("套用 Apply") }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun FilterMultiSelect(label: String, selected: Set<String>, options: List<Pair<String, String>>, onChange: (Set<String>) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded, { expanded = !expanded }) {
        val summary = if (selected.isEmpty()) "全部 All" else options.filter { it.first in selected }.joinToString { it.second }
        OutlinedTextField(summary, {}, readOnly = true, label = { Text(label) }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) }, modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable), maxLines = 2)
        ExposedDropdownMenu(expanded, { expanded = false }) {
            DropdownMenuItem(text = { Text("全部 All") }, onClick = hapticClick { onChange(emptySet()) }, trailingIcon = { Checkbox(selected.isEmpty(), null) })
            options.forEach { (code, title) ->
                DropdownMenuItem(text = { Text(title) }, onClick = hapticClick { onChange(if (code in selected) selected - code else selected + code) }, trailingIcon = { Checkbox(code in selected, null) })
            }
        }
    }
}
