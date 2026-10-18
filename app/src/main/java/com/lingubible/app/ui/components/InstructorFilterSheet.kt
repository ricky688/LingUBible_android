package com.lingubible.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.semantics.*
import com.lingubible.app.domain.model.*
import com.lingubible.app.ui.common.hapticClick
import com.lingubible.app.ui.common.hapticChange

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun InstructorFilterSheet(filters: InstructorFilters, instructors: List<Instructor>, terms: List<CourseTerm>, onDismiss: () -> Unit, onApply: (InstructorFilters) -> Unit) {
    var draft by remember { mutableStateOf(filters) }
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)) {
        Column(Modifier.fillMaxWidth().imePadding().padding(horizontal = 20.dp)) {
            Text("講師篩選與排序 Instructor filters", style = MaterialTheme.typography.titleLarge)
            Column(Modifier.weight(1f, false).fadingVerticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                FilterMultiSelect("學系 Departments", draft.departments, instructors.map { it.department }.filter { it.isNotBlank() }.distinct().sorted().map { it to it }) { draft = draft.copy(departments = it) }
                FilterMultiSelect("授課學期 Teaching term", draft.terms, terms.map { it.code to it.label }) { draft = draft.copy(terms = it) }
                FilterMultiSelect("教學語言 Teaching language", draft.languages, instructors.flatMap { it.teachingLanguages }.distinct().sorted().map { it to teachingLanguageLabel(it) }) { draft = draft.copy(languages = it) }
                Row {
                    Text("包括已離任講師 Include former staff", Modifier.weight(1f))
                    Switch(draft.showFormerStaff, hapticChange { draft = draft.copy(showFormerStaff = it) }, modifier = Modifier.semantics { contentDescription = "包括已離任講師 Include former staff" })
                }
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    InstructorSort.entries.forEach { sort -> FilterChip(draft.sort == sort, onClick = hapticClick { draft = draft.copy(sort = sort) }, label = { Text(sort.label) }) }
                }
                Row {
                    Text("由大至小 Descending", Modifier.weight(1f))
                    Switch(draft.descending, hapticChange { draft = draft.copy(descending = it) }, modifier = Modifier.semantics { contentDescription = "降冪排序 Descending order" })
                }
            }
            FlowRow(Modifier.padding(vertical = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(shapes = AppButtonDefaults.shapes(), onClick = hapticClick { draft = InstructorFilters() }) { Text("重設 Reset") }
                Button(onClick = hapticClick { onApply(draft); onDismiss() }, shapes = AppButtonDefaults.shapes()) { Text("套用 Apply") }
            }
        }
    }
}
