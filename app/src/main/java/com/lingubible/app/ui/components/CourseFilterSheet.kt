package com.lingubible.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.dp
import com.lingubible.app.core.settings.AppLanguage
import com.lingubible.app.core.settings.AppSettingsManager
import com.lingubible.app.domain.model.*
import com.lingubible.app.ui.common.hapticClick
import com.lingubible.app.ui.common.hapticChange
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CourseFilterSheet(filters: CourseFilters, terms: List<CourseTerm>, signedIn: Boolean, optionsError: String?, onRetry: () -> Unit, onDismiss: () -> Unit, onApply: (CourseFilters) -> Unit) {
    val settingsManager: AppSettingsManager = koinInject()
    val appLanguage by settingsManager.appLanguage.collectAsState()
    val isZh = appLanguage == AppLanguage.ZH_TW
    var draft by remember { mutableStateOf(filters) }
    LaunchedEffect(signedIn) { if (!signedIn) draft = draft.copy(hasMaterials = false) }
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)) {
        Column(Modifier.fillMaxWidth().imePadding().padding(horizontal = 20.dp)) {
            Text(if (isZh) "篩選與排序 Filter and sort" else "Filter and sort", style = MaterialTheme.typography.titleLarge)
            Column(Modifier.weight(1f, fill = false).fadingVerticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                FilterMultiSelect(
                    if (isZh) "學系 Departments" else "Departments",
                    draft.subjects,
                    COURSE_CATEGORIES.filter { it.code != "ALL" }.map { it.code to (if (isZh) "${it.nameZh} ${it.code}" else "${it.nameEn} (${it.code})") }
                ) { draft = draft.copy(subjects = it) }
                FilterMultiSelect(
                    if (isZh) "教學語言 Teaching language" else "Teaching language",
                    draft.languages,
                    if (isZh) listOf("E" to "英語 English", "C" to "粵語 Cantonese", "P" to "普通話 Putonghua", "1" to "英語／粵語 E/C", "2" to "英語／普通話 E/P", "3" to "粵語／普通話 C/P", "4" to "英語／粵語／普通話 E/C/P", "5" to "其他 Others")
                    else listOf("E" to "English", "C" to "Cantonese", "P" to "Putonghua", "1" to "English / Cantonese", "2" to "English / Putonghua", "3" to "Cantonese / Putonghua", "4" to "English / Cantonese / Putonghua", "5" to "Others")
                ) { draft = draft.copy(languages = it) }
                FilterMultiSelect(
                    if (isZh) "服務學習 Service learning" else "Service learning",
                    draft.serviceLearning,
                    if (isZh) listOf("compulsory" to "必修 Compulsory", "optional" to "選修 Optional", "none" to "無 None")
                    else listOf("compulsory" to "Compulsory", "optional" to "Optional", "none" to "None")
                ) { draft = draft.copy(serviceLearning = it) }
                FilterMultiSelect(
                    if (isZh) "開設學期 Offered term" else "Offered term",
                    draft.terms,
                    terms.map { it.code to (if (isZh) it.label else it.label.replace(Regex("[\\u4e00-\\u9fff\\u3000-\\u303f\\uff00-\\uffef]+\\s*"), "").trim()) }
                ) { draft = draft.copy(terms = it) }
                if (terms.isEmpty()) TextButton(shapes = AppButtonDefaults.shapes(), onClick = hapticClick(onRetry)) { Text(optionsError ?: if (isZh) "載入學期 Load terms" else "Load terms") }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        if (isZh) {
                            if (signedIn) "僅顯示有學習資料的課程\nHas study materials" else "登入後可篩選學習資料\nSign in to filter materials"
                        } else {
                            if (signedIn) "Has study materials" else "Sign in to filter materials"
                        },
                        modifier = Modifier.weight(1f)
                    )
                    Switch(draft.hasMaterials, hapticChange { draft = draft.copy(hasMaterials = it) }, enabled = signedIn, modifier = Modifier.semantics { contentDescription = if (isZh) "僅顯示有學習資料 Has study materials" else "Has study materials" })
                }
                Text(if (isZh) "排序 Sort" else "Sort", style = MaterialTheme.typography.titleMedium)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CourseSort.entries.forEach { sort ->
                        val label = if (isZh) sort.label else sort.label.replace(Regex("[\\u4e00-\\u9fff\\u3000-\\u303f\\uff00-\\uffef]+\\s*"), "").trim()
                        FilterChip(draft.sort == sort, onClick = hapticClick { draft = draft.copy(sort = sort) }, label = { Text(label) })
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(if (isZh) "由大至小／Z–A Descending" else "Descending", modifier = Modifier.weight(1f))
                    Switch(draft.descending, hapticChange { draft = draft.copy(descending = it) }, modifier = Modifier.semantics { contentDescription = if (isZh) "降冪排序 Descending order" else "Descending order" })
                }
            }
            FlowRow(Modifier.fillMaxWidth().padding(vertical = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(shapes = AppButtonDefaults.shapes(), onClick = hapticClick { draft = CourseFilters() }) { Text(if (isZh) "重設 Reset" else "Reset") }
                Button(shapes = AppButtonDefaults.shapes(), onClick = hapticClick { onApply(draft); onDismiss() }) { Text(if (isZh) "套用 Apply" else "Apply") }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun FilterMultiSelect(label: String, selected: Set<String>, options: List<Pair<String, String>>, onChange: (Set<String>) -> Unit) {
    val settingsManager: AppSettingsManager = koinInject()
    val appLanguage by settingsManager.appLanguage.collectAsState()
    val isZh = appLanguage == AppLanguage.ZH_TW
    val allLabel = if (isZh) "全部 All" else "All"
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded, { expanded = !expanded }) {
        val summary = if (selected.isEmpty()) allLabel else options.filter { it.first in selected }.joinToString { it.second }
        OutlinedTextField(summary, {}, readOnly = true, label = { Text(label) }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) }, modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable), maxLines = 2)
        ExposedDropdownMenu(expanded, { expanded = false }) {
            DropdownMenuItem(text = { Text(allLabel) }, onClick = hapticClick { onChange(emptySet()) }, trailingIcon = { Checkbox(selected.isEmpty(), null) })
            options.forEach { (code, title) ->
                DropdownMenuItem(text = { Text(title) }, onClick = hapticClick { onChange(if (code in selected) selected - code else selected + code) }, trailingIcon = { Checkbox(code in selected, null) })
            }
        }
    }
}
