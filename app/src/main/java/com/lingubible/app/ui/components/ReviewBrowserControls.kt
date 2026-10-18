package com.lingubible.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.lingubible.app.domain.model.*
import com.lingubible.app.ui.common.hapticClick
import com.lingubible.app.ui.common.hapticChange
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Composable
fun rememberReviewFilters(key: String = ""): MutableState<ReviewFilters> = rememberSaveable(key, stateSaver = Saver(
    save = { Json.encodeToString(it) }, restore = { runCatching { Json.decodeFromString<ReviewFilters>(it) }.getOrDefault(ReviewFilters()) }
)) { mutableStateOf(ReviewFilters()) }

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ReviewBrowserControls(reviews: List<Review>, filters: ReviewFilters, onChange: (ReviewFilters) -> Unit, count: Int) {
    var sheet by remember { mutableStateOf(false) }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(filters.search, { onChange(filters.copy(search = it)) }, label = { Text("搜尋評價 Search reviews") },
            modifier = Modifier.fillMaxWidth(), singleLine = true,
            trailingIcon = { IconButton(shapes = AppButtonDefaults.iconShapes(), onClick = hapticClick { sheet = true }) { Icon(Icons.Default.Tune, contentDescription = "篩選與排序 Filter and sort reviews") } })
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("$count / ${reviews.size} 則評價 Reviews", modifier = Modifier.weight(1f), style = MaterialTheme.typography.labelLarge)
            if (filters != ReviewFilters()) TextButton(shapes = AppButtonDefaults.shapes(), onClick = hapticClick { onChange(ReviewFilters()) }) { Text("重設 Reset") }
        }
    }
    if (sheet) {
        var draft by remember { mutableStateOf(filters) }
        ModalBottomSheet(onDismissRequest = { sheet = false }, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)) {
            Column(Modifier.fillMaxWidth().imePadding().padding(horizontal = 20.dp)) {
                Text("篩選與排序 Filter and sort reviews", style = MaterialTheme.typography.titleLarge)
                Column(Modifier.weight(1f, fill = false).fadingVerticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    fun options(values: List<String>) = values.filter { it.isNotBlank() }.groupingBy { it }.eachCount().toSortedMap().map { (v, n) -> v to "$v ($n)" }
                    ReviewMultiSelect("課程 Courses", draft.courses, options(reviews.map { it.courseCode })) { draft = draft.copy(courses = it) }
                    ReviewMultiSelect("講師 Instructors", draft.instructors, options(reviews.flatMap { r -> r.instructors.map { it.name }.ifEmpty { listOf(r.instructorName) }.distinct() })) { draft = draft.copy(instructors = it) }
                    ReviewMultiSelect("成績 Grades", draft.grades, options(reviews.map { it.grade }).map { if (it.first == "-1") it.first to "N/A (${reviews.count { r -> r.grade == "-1" }})" else it }) { draft = draft.copy(grades = it) }
                    ReviewMultiSelect("學期 Terms", draft.terms, options(reviews.map { it.term })) { draft = draft.copy(terms = it) }
                    ReviewMultiSelect("評價語言 Review language", draft.languages, options(reviews.map { it.language })) { draft = draft.copy(languages = it) }
                    ReviewMultiSelect("課節 Session types", draft.sessions, options(reviews.flatMap { r -> r.instructors.map { it.session }.distinct() })) { draft = draft.copy(sessions = it) }
                    ReviewMultiSelect("課程要求 Assessment", draft.requirements, REVIEW_REQUIREMENTS.toList()) { draft = draft.copy(requirements = it) }
                    ReviewMultiSelect("服務學習 Service learning", draft.serviceLearning, listOf("none" to "無 None", "optional" to "選修 Optional", "compulsory" to "必修 Compulsory")) { draft = draft.copy(serviceLearning = it) }
                    Text("排序 Sort", style = MaterialTheme.typography.titleMedium)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ReviewSort.entries.forEach { sort -> FilterChip(draft.sort == sort, onClick = hapticClick { draft = draft.copy(sort = sort) }, label = { Text(sort.label) }) }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("由大至小／最新優先 Descending", Modifier.weight(1f))
                        Switch(draft.descending, hapticChange { draft = draft.copy(descending = it) }, modifier = Modifier.semantics { contentDescription = "降冪排序 Descending order" })
                    }
                }
                FlowRow(Modifier.padding(vertical = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(shapes = AppButtonDefaults.shapes(), onClick = hapticClick { draft = ReviewFilters() }) { Text("重設 Reset") }
                    Button(onClick = hapticClick { onChange(draft); sheet = false }, shapes = AppButtonDefaults.shapes()) { Text("套用 Apply") }
                }
            }
        }
    }
}

@Composable
private fun ReviewMultiSelect(label: String, selected: Set<String>, options: List<Pair<String, String>>, onChange: (Set<String>) -> Unit) {
    var open by remember { mutableStateOf(false) }
    var query by rememberSaveable { mutableStateOf("") }
    OutlinedButton(onClick = hapticClick { query = ""; open = true }, modifier = Modifier.fillMaxWidth(), shapes = AppButtonDefaults.shapes()) {
        Text("$label · ${if (selected.isEmpty()) "全部 All" else "${selected.size} 已選 Selected"}")
    }
    if (open) AlertDialog(onDismissRequest = { open = false }, title = { Text(label) },
        text = { Column {
            OutlinedTextField(query, { query = it }, label = { Text("搜尋 Search") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            TextButton(shapes = AppButtonDefaults.shapes(), onClick = hapticClick { onChange(emptySet()) }) { Text("全部 All") }
            FadingLazyColumn(Modifier.heightIn(max = 360.dp), bottomInset = 0.dp) {
                items(options.filter { it.second.contains(query.trim(), true) }, key = { it.first }) { (code, title) ->
                    DropdownMenuItem(text = { Text(title) }, onClick = hapticClick { onChange(if (code in selected) selected - code else selected + code) },
                        trailingIcon = { Checkbox(code in selected, null) })
                }
            }
        } }, confirmButton = { TextButton(shapes = AppButtonDefaults.shapes(), onClick = hapticClick { open = false }) { Text("完成 Done") } })
}
