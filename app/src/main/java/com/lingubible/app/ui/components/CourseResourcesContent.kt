package com.lingubible.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import com.lingubible.app.core.navigation.screenContentPadding
import com.lingubible.app.domain.model.*
import com.lingubible.app.domain.repository.AuthRepository
import com.lingubible.app.domain.repository.MaterialRepository
import com.lingubible.app.ui.common.hapticClick
import kotlinx.coroutines.*
import org.koin.compose.koinInject

@Composable
fun CourseReadingsContent(raw: String) {
    val readings = remember(raw) { parseCourseReadings(raw) }
    val context = LocalContext.current
    FadingLazyColumn(contentPadding = screenContentPadding(start = 16.dp, end = 16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        if (readings.isEmpty()) item { Text("暫無閱讀書目 · No reading list available") }
        listOf(true, false).forEach { required ->
            val group = readings.filter { it.required == required }
            if (group.isNotEmpty()) item { Text(if (required) "必讀 Required" else "選讀 Recommended", style = MaterialTheme.typography.titleMedium) }
            itemsIndexed(group) { index, reading ->
                Card(shape = getGroupedItemShape(index, group.size, outerRadius = 12.dp)) {
                    Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(reading.kind, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                        Text(reading.title)
                        if (reading.source.isNotBlank() && reading.source != reading.title) Text(reading.source, style = MaterialTheme.typography.bodySmall)
                        reading.url?.let { url ->
                            TextButton(shapes = AppButtonDefaults.shapes(), onClick = hapticClick {
                                try { context.startActivity(android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(url)).addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)) }
                                catch (_: Exception) { android.widget.Toast.makeText(context, "無法開啟連結 · Could not open link", android.widget.Toast.LENGTH_LONG).show() }
                            }) { Text("開啟資源 Open resource") }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StudyMaterialsContent(courseCode: String, signedIn: Boolean, onSignIn: () -> Unit, onPdf: (StudyMaterial) -> Unit) {
    val repository = koinInject<MaterialRepository>()
    val auth = koinInject<AuthRepository>()
    var files by remember(courseCode) { mutableStateOf(emptyList<StudyMaterial>()) }
    var selected by remember(courseCode) { mutableStateOf(emptySet<String>()) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var retry by remember { mutableIntStateOf(0) }
    val export = rememberMaterialExport("study_materials", "${courseCode}_study_materials.zip")
    val exporting = export.busy
    val userId by auth.currentUser.collectAsStateWithLifecycle()
    LaunchedEffect(courseCode, signedIn, userId?.id, retry) {
        files = emptyList(); selected = emptySet(); error = null
        if (!signedIn) return@LaunchedEffect
        loading = true
        repository.getStudyMaterials(courseCode).onSuccess { files = it }.onFailure { error = it.localizedMessage }
        loading = false
    }
    FadingLazyColumn(contentPadding = screenContentPadding(start = 16.dp, end = 16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        if (!signedIn) item {
            Text("登入後查看學習資料 · Sign in to view study materials")
            Button(shapes = AppButtonDefaults.shapes(), onClick = hapticClick(onSignIn)) { Text("登入 Sign in") }
        } else {
            item {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(shapes = AppButtonDefaults.shapes(), onClick = hapticClick { selected = if (selected.size == files.size) emptySet() else files.map { it.id }.toSet() }, enabled = !exporting && files.isNotEmpty()) { Text("全選／取消 Select all / clear") }
                    Button(shapes = AppButtonDefaults.shapes(), onClick = hapticClick {
                        export.download(files.filter { it.id in selected })
                    }, enabled = selected.isNotEmpty() && !exporting) { Text("下載所選 Download (${selected.size})") }
                }
            }
            if (loading || exporting) item { CircularProgressIndicator() }
            (error ?: export.error)?.let { message -> item {
                Text(message, color = MaterialTheme.colorScheme.error)
                TextButton(shapes = AppButtonDefaults.shapes(), onClick = hapticClick { export.clearError(); retry++ }, enabled = !exporting) { Text("重試 Retry") }
            } }
            if (!loading && files.isEmpty() && error == null) item { Text("暫無學習資料 · No study materials available") }
            itemsIndexed(files, key = { _, file -> file.id }) { index, file ->
                Card(shape = getGroupedItemShape(index, files.size, outerRadius = 12.dp)) {
                    Column(Modifier.fillMaxWidth().padding(12.dp)) {
                        Row {
                            Checkbox(file.id in selected, onCheckedChange = com.lingubible.app.ui.common.hapticChange { checked -> selected = if (checked) selected + file.id else selected - file.id }, enabled = !exporting, modifier = Modifier.semantics { contentDescription = "選取檔案 Select ${file.name}" })
                            Column(Modifier.weight(1f)) {
                                Text(file.displayName, style = MaterialTheme.typography.titleSmall)
                                Text("${file.name} · ${file.size / 1024} KB", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (file.isPdf) TextButton(shapes = AppButtonDefaults.shapes(), onClick = hapticClick { onPdf(file) }, enabled = !exporting) { Text("檢視 PDF View") }
                            TextButton(shapes = AppButtonDefaults.shapes(), onClick = hapticClick {
                                export.download(listOf(file))
                            }, enabled = !exporting) { Text("下載 Download") }
                        }
                    }
                }
            }
        }
    }
}
