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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lingubible.app.core.settings.AppLanguage
import com.lingubible.app.core.settings.AppSettingsManager
import com.lingubible.app.domain.model.*
import com.lingubible.app.ui.common.hapticClick
import com.lingubible.app.ui.common.hapticChange
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.koin.compose.koinInject

@Composable
fun rememberReviewFilters(key: String = ""): MutableState<ReviewFilters> = rememberSaveable(key, stateSaver = Saver(
    save = { Json.encodeToString(it) }, restore = { runCatching { Json.decodeFromString<ReviewFilters>(it) }.getOrDefault(ReviewFilters()) }
)) { mutableStateOf(ReviewFilters()) }

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ReviewBrowserControls(reviews: List<Review>, filters: ReviewFilters, onChange: (ReviewFilters) -> Unit, count: Int) {
    val settingsManager = koinInject<AppSettingsManager>()
    val appLanguage by settingsManager.appLanguage.collectAsStateWithLifecycle()
    val isZh = appLanguage == AppLanguage.ZH_TW
    var sheet by remember { mutableStateOf(false) }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(filters.search, { onChange(filters.copy(search = it)) }, label = { Text(if (isZh) "搜尋評價 Search reviews" else "Search reviews") },
            modifier = Modifier.fillMaxWidth(), singleLine = true,
            trailingIcon = { IconButton(shapes = AppButtonDefaults.iconShapes(), onClick = hapticClick { sheet = true }) { Icon(Icons.Default.Tune, contentDescription = if (isZh) "篩選與排序 Filter and sort reviews" else "Filter and sort reviews") } })
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(if (isZh) "$count / ${reviews.size} 則評價 Reviews" else "$count / ${reviews.size} Reviews", modifier = Modifier.weight(1f), style = MaterialTheme.typography.labelLarge)
            if (filters != ReviewFilters()) TextButton(shapes = AppButtonDefaults.shapes(), onClick = hapticClick { onChange(ReviewFilters()) }) { Text(if (isZh) "重設 Reset" else "Reset") }
        }
    }
    if (sheet) {
        var draft by remember { mutableStateOf(filters) }
        ModalBottomSheet(onDismissRequest = { sheet = false }, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)) {
            Column(Modifier.fillMaxWidth().imePadding().padding(horizontal = 20.dp)) {
                Text(if (isZh) "篩選與排序 Filter and sort reviews" else "Filter and sort reviews", style = MaterialTheme.typography.titleLarge)
                Column(Modifier.weight(1f, fill = false).fadingVerticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    fun options(values: List<String>) = values.filter { it.isNotBlank() }.groupingBy { it }.eachCount().toSortedMap().map { (v, n) -> v to "$v ($n)" }
                    ReviewMultiSelect(if (isZh) "課程 Courses" else "Courses", draft.courses, options(reviews.map { it.courseCode }), isZh) { draft = draft.copy(courses = it) }
                    ReviewMultiSelect(if (isZh) "講師 Instructors" else "Instructors", draft.instructors, options(reviews.flatMap { r -> r.instructors.map { it.name }.ifEmpty { listOf(r.instructorName) }.distinct() }), isZh) { draft = draft.copy(instructors = it) }
                    ReviewMultiSelect(if (isZh) "成績 Grades" else "Grades", draft.grades, options(reviews.map { it.grade }).map { if (it.first == "-1") it.first to "N/A (${reviews.count { r -> r.grade == "-1" }})" else it }, isZh) { draft = draft.copy(grades = it) }
                    ReviewMultiSelect(if (isZh) "學期 Terms" else "Terms", draft.terms, options(reviews.map { it.term }), isZh) { draft = draft.copy(terms = it) }
                    ReviewMultiSelect(if (isZh) "評價語言 Review language" else "Review language", draft.languages, options(reviews.map { it.language }), isZh) { draft = draft.copy(languages = it) }
                    ReviewMultiSelect(if (isZh) "課節 Session types" else "Session types", draft.sessions, options(reviews.flatMap { r -> r.instructors.map { it.session }.distinct() }), isZh) { draft = draft.copy(sessions = it) }
                    ReviewMultiSelect(if (isZh) "課程要求 Assessment" else "Assessment", draft.requirements, REVIEW_REQUIREMENTS.map { (k, v) -> k to reviewRequirementLabel(k, v, isZh) }, isZh) { draft = draft.copy(requirements = it) }
                    ReviewMultiSelect(
                        if (isZh) "服務學習 Service learning" else "Service learning",
                        draft.serviceLearning,
                        if (isZh) listOf("none" to "無 None", "optional" to "選修 Optional", "compulsory" to "必修 Compulsory") else listOf("none" to "None", "optional" to "Optional", "compulsory" to "Compulsory"),
                        isZh
                    ) { draft = draft.copy(serviceLearning = it) }
                    Text(if (isZh) "排序 Sort" else "Sort", style = MaterialTheme.typography.titleMedium)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ReviewSort.entries.forEach { sort -> FilterChip(draft.sort == sort, onClick = hapticClick { draft = draft.copy(sort = sort) }, label = { Text(reviewSortLabel(sort, isZh)) }) }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(if (isZh) "由大至小／最新優先 Descending" else "Descending", Modifier.weight(1f))
                        Switch(draft.descending, hapticChange { draft = draft.copy(descending = it) }, modifier = Modifier.semantics { contentDescription = if (isZh) "降冪排序 Descending order" else "Descending order" })
                    }
                }
                FlowRow(Modifier.padding(vertical = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(shapes = AppButtonDefaults.shapes(), onClick = hapticClick { draft = ReviewFilters() }) { Text(if (isZh) "重設 Reset" else "Reset") }
                    Button(onClick = hapticClick { onChange(draft); sheet = false }, shapes = AppButtonDefaults.shapes()) { Text(if (isZh) "套用 Apply" else "Apply") }
                }
            }
        }
    }
}

@Composable
private fun ReviewMultiSelect(label: String, selected: Set<String>, options: List<Pair<String, String>>, isZh: Boolean, onChange: (Set<String>) -> Unit) {
    var open by remember { mutableStateOf(false) }
    var query by rememberSaveable { mutableStateOf("") }
    OutlinedButton(onClick = hapticClick { query = ""; open = true }, modifier = Modifier.fillMaxWidth(), shapes = AppButtonDefaults.shapes()) {
        Text(
            "$label · ${
                if (selected.isEmpty()) {
                    if (isZh) "全部 All" else "All"
                } else {
                    if (isZh) "${selected.size} 已選 Selected" else "${selected.size} selected"
                }
            }"
        )
    }
    if (open) AlertDialog(onDismissRequest = { open = false }, title = { Text(label) },
        text = { Column {
            OutlinedTextField(query, { query = it }, label = { Text(if (isZh) "搜尋 Search" else "Search") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            TextButton(shapes = AppButtonDefaults.shapes(), onClick = hapticClick { onChange(emptySet()) }) { Text(if (isZh) "全部 All" else "All") }
            FadingLazyColumn(Modifier.heightIn(max = 360.dp), bottomInset = 0.dp) {
                items(options.filter { it.second.contains(query.trim(), true) }, key = { it.first }) { (code, title) ->
                    DropdownMenuItem(text = { Text(title) }, onClick = hapticClick { onChange(if (code in selected) selected - code else selected + code) },
                        trailingIcon = { Checkbox(code in selected, null) })
                }
            }
        } }, confirmButton = { TextButton(shapes = AppButtonDefaults.shapes(), onClick = hapticClick { open = false }) { Text(if (isZh) "完成 Done" else "Done") } })
}

internal fun reviewSortLabel(sort: ReviewSort, isZh: Boolean): String {
    if (isZh) return sort.label
    return when (sort) {
        ReviewSort.DATE -> "Date"
        ReviewSort.WORKLOAD -> "Workload"
        ReviewSort.DIFFICULTY -> "Difficulty"
        ReviewSort.USEFULNESS -> "Usefulness"
        ReviewSort.TEACHING -> "Teaching"
        ReviewSort.GRADING -> "Grading"
        ReviewSort.GRADE -> "Grade"
        ReviewSort.UPVOTES -> "Upvotes"
        ReviewSort.DOWNVOTES -> "Downvotes"
    }
}
