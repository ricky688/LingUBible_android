package com.lingubible.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import com.lingubible.app.core.navigation.screenContentPadding
import com.lingubible.app.core.settings.AppLanguage
import com.lingubible.app.core.settings.AppSettingsManager
import com.lingubible.app.domain.model.*
import com.lingubible.app.ui.common.hapticClick
import com.lingubible.app.ui.common.hapticChange
import org.koin.compose.koinInject

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PastPapersContent(courseCode: String, papers: List<PastPaper>, records: List<TeachingRecord>, signedIn: Boolean,
    loading: Boolean, error: String?, onRetry: () -> Unit, onSignIn: () -> Unit, onView: (PastPaper) -> Unit) {
    val settingsManager: AppSettingsManager = koinInject()
    val appLanguage by settingsManager.appLanguage.collectAsState()
    val isZh = appLanguage == AppLanguage.ZH_TW
    var years by remember(courseCode) { mutableStateOf(emptySet<String>()) }
    var instructors by remember(courseCode) { mutableStateOf(emptySet<String>()) }
    var oldest by remember(courseCode) { mutableStateOf(false) }
    var selected by remember(courseCode, years, instructors, oldest, signedIn) { mutableStateOf(emptySet<String>()) }
    val filtered = remember(papers, records, years, instructors, oldest) { filterPastPapers(papers, records, years, instructors, oldest) }
    val export = rememberMaterialExport("past_exam_papers", "${courseCode}_past_papers.zip")
    fun download(chosen: List<PastPaper>) = export.download(chosen.map { StudyMaterial(it.fileId, it.fileName, it.fileSize) })
    FadingLazyColumn(Modifier.fillMaxSize(), contentPadding = screenContentPadding(start = 16.dp, end = 16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterMultiSelect(
                    if (isZh) "學年 Academic year" else "Academic year",
                    years,
                    papers.map { it.academicYear }.distinct().sortedDescending().map { it to it.ifBlank { if (isZh) "未知 Unknown" else "Unknown" } }
                ) { years = it }
                FilterMultiSelect(if (isZh) "講師 Instructor" else "Instructor", instructors, papers.flatMap { pastPaperInstructors(it, records) }.distinct().sorted().map { it to it }) { instructors = it }
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(!oldest, hapticClick { oldest = false }, label = { Text(if (isZh) "最新優先 Newest" else "Newest") })
                    FilterChip(oldest, hapticClick { oldest = true }, label = { Text(if (isZh) "最舊優先 Oldest" else "Oldest") })
                }
                Text(if (isZh) "${filtered.size} 份試題 Past papers" else "${filtered.size} past papers", style = MaterialTheme.typography.labelLarge)
                if (signedIn) FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(shapes = AppButtonDefaults.shapes(), enabled = filtered.isNotEmpty() && !export.busy, onClick = hapticClick { selected = if (filtered.all { it.fileId in selected }) emptySet() else filtered.map { it.fileId }.toSet() }) { Text(if (isZh) "全選／取消 Select all / clear" else "Select all / clear") }
                    Button(shapes = AppButtonDefaults.shapes(), enabled = selected.isNotEmpty() && !export.busy, onClick = hapticClick { download(filtered.filter { it.fileId in selected }) }) { Text(if (isZh) "下載所選 Download (${selected.size})" else "Download (${selected.size})") }
                } else Button(shapes = AppButtonDefaults.shapes(), onClick = hapticClick(onSignIn)) { Text(if (isZh) "登入下載 Sign in to download" else "Sign in to download") }
            }
        }
        if (loading || export.busy) item { CircularProgressIndicator() }
        (error ?: export.error)?.let { message -> item {
            Text(message, color = MaterialTheme.colorScheme.error)
            TextButton(shapes = AppButtonDefaults.shapes(), enabled = !export.busy, onClick = hapticClick { export.clearError(); onRetry() }) { Text(if (isZh) "重試 Retry" else "Retry") }
        } }
        if (!loading && filtered.isEmpty() && error == null) item { Text(if (isZh) "沒有符合條件的試題 · No matching past papers" else "No matching past papers") }
        filtered.groupBy { it.academicYear }.forEach { (year, group) ->
            item(key = "paper-year:$year") {
                Text(
                    if (year.isBlank() || year.startsWith("Unknown", true)) {
                        if (isZh) "學年未知 Unknown year" else "Unknown year"
                    } else {
                        if (isZh) "$year 學年 Academic year" else "$year Academic year"
                    },
                    style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 12.dp, bottom = 8.dp))
            }
            itemsIndexed(group, key = { _, paper -> paper.fileId }) { index, paper ->
                PastPaperListItem(paper, pastPaperInstructors(paper, records), index, group.size,
                    selectable = signedIn, selected = paper.fileId in selected, busy = export.busy,
                    onSelected = { checked -> selected = if (checked) selected + paper.fileId else selected - paper.fileId },
                    onView = { onView(paper) }, onDownload = { if (signedIn) download(listOf(paper)) else onSignIn() })
            }
        }
    }
}
