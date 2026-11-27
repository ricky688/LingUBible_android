package com.lingubible.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.semantics.*
import com.lingubible.app.core.settings.AppLanguage
import com.lingubible.app.core.settings.AppSettingsManager
import com.lingubible.app.domain.model.*
import com.lingubible.app.ui.common.hapticClick
import com.lingubible.app.ui.common.hapticChange
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun InstructorFilterSheet(filters: InstructorFilters, instructors: List<Instructor>, terms: List<CourseTerm>, onDismiss: () -> Unit, onApply: (InstructorFilters) -> Unit) {
    val settingsManager: AppSettingsManager = koinInject()
    val appLanguage by settingsManager.appLanguage.collectAsState()
    val isZh = appLanguage == AppLanguage.ZH_TW
    var draft by remember { mutableStateOf(filters) }
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)) {
        Column(Modifier.fillMaxWidth().imePadding().padding(horizontal = 20.dp)) {
            Text(if (isZh) "講師篩選與排序 Instructor filters" else "Instructor filters", style = MaterialTheme.typography.titleLarge)
            Column(Modifier.weight(1f, fill = false).fadingVerticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                FilterMultiSelect(if (isZh) "學系 Departments" else "Departments", draft.departments, instructors.map { it.department }.filter { it.isNotBlank() }.distinct().sorted().map { it to it }) { draft = draft.copy(departments = it) }
                FilterMultiSelect(
                    if (isZh) "授課學期 Teaching term" else "Teaching term",
                    draft.terms,
                    terms.map { it.code to (if (isZh) it.label else it.label.replace(Regex("[\\u4e00-\\u9fff\\u3000-\\u303f\\uff00-\\uffef]+\\s*"), "").trim()) }
                ) { draft = draft.copy(terms = it) }
                FilterMultiSelect(if (isZh) "教學語言 Teaching language" else "Teaching language", draft.languages, instructors.flatMap { it.teachingLanguages }.distinct().sorted().map { it to teachingLanguageLabel(it) }) { draft = draft.copy(languages = it) }
                Row {
                    Text(if (isZh) "包括已離任講師 Include former staff" else "Include former staff", Modifier.weight(1f))
                    Switch(draft.showFormerStaff, hapticChange { draft = draft.copy(showFormerStaff = it) }, modifier = Modifier.semantics { contentDescription = if (isZh) "包括已離任講師 Include former staff" else "Include former staff" })
                }
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    InstructorSort.entries.forEach { sort ->
                        val label = if (isZh) sort.label else sort.label.replace(Regex("[\\u4e00-\\u9fff\\u3000-\\u303f\\uff00-\\uffef]+\\s*"), "").trim()
                        FilterChip(draft.sort == sort, onClick = hapticClick { draft = draft.copy(sort = sort) }, label = { Text(label) })
                    }
                }
                Row {
                    Text(if (isZh) "由大至小 Descending" else "Descending", Modifier.weight(1f))
                    Switch(draft.descending, hapticChange { draft = draft.copy(descending = it) }, modifier = Modifier.semantics { contentDescription = if (isZh) "降冪排序 Descending order" else "Descending order" })
                }
            }
            FlowRow(Modifier.padding(vertical = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(shapes = AppButtonDefaults.shapes(), onClick = hapticClick { draft = InstructorFilters() }) { Text(if (isZh) "重設 Reset" else "Reset") }
                Button(onClick = hapticClick { onApply(draft); onDismiss() }, shapes = AppButtonDefaults.shapes()) { Text(if (isZh) "套用 Apply" else "Apply") }
            }
        }
    }
}
