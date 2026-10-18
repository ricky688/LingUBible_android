package com.lingubible.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.lingubible.app.domain.model.ReviewEdit
import com.lingubible.app.domain.repository.ReviewRepository
import com.lingubible.app.ui.common.hapticClick
import com.lingubible.app.ui.common.hapticChange
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import kotlinx.serialization.json.*
import org.koin.compose.koinInject

@Composable
fun ReviewEditor(reviewId: String, onDismiss: () -> Unit, onSaved: () -> Unit) {
    val repository = koinInject<ReviewRepository>()
    val scope = rememberCoroutineScope()
    val auth = koinInject<com.lingubible.app.domain.repository.AuthRepository>()
    val owner = remember { auth.currentUser.value?.id }
    val user by auth.currentUser.collectAsStateWithLifecycle()
    LaunchedEffect(user?.id) { if (user?.id != owner) onDismiss() }
    var draft by remember(reviewId) { mutableStateOf<ReviewEdit?>(null) }
    var busy by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var retry by remember { mutableIntStateOf(0) }
    LaunchedEffect(reviewId, retry) {
        busy = true
        repository.getReviewEdit(reviewId).onSuccess { draft = it }.onFailure { error = it.localizedMessage }
        busy = false
    }
    Dialog(onDismissRequest = { if (!busy) onDismiss() }, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(shape = MaterialTheme.shapes.extraLarge, modifier = Modifier.padding(16.dp).widthIn(max = 560.dp).fillMaxWidth().imePadding()) {
            Column(Modifier.padding(20.dp)) {
                Text("修改評價 Edit review", style = MaterialTheme.typography.titleLarge)
                Column(Modifier.weight(1f, fill = false).fadingVerticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (busy) CircularProgressIndicator()
                    val value = draft
                    if (value != null) {
                        ReviewChoice("成績 Grade", value.grade, ReviewEdit.GRADES, !busy) { draft = value.copy(grade = it) }
                        ReviewChoice("評價語言 Language", value.language, listOf("en", "zh-TW", "zh-CN"), !busy) { draft = value.copy(language = it) }
                        OutlinedTextField(value.comment, { draft = value.copy(comment = it) }, label = { Text("課程評價 Course comments") }, modifier = Modifier.fillMaxWidth(), minLines = 3, enabled = !busy)
                        ReviewRating("工作量 Workload", value.workload, !busy) { draft = value.copy(workload = it) }
                        ReviewRating("難度 Difficulty", value.difficulty, !busy) { draft = value.copy(difficulty = it) }
                        ReviewRating("實用性 Usefulness", value.usefulness, !busy) { draft = value.copy(usefulness = it) }
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("匿名 Anonymous", modifier = Modifier.weight(1f))
                            Switch(value.anonymous, hapticChange { draft = value.copy(anonymous = it) }, enabled = !busy, modifier = Modifier.semantics { contentDescription = "匿名評價 Anonymous review" })
                        }
                        value.instructors.forEachIndexed { index, element ->
                            val instructor = element.jsonObject
                            Text(instructor["instructor_name"]?.jsonPrimitive?.content.orEmpty() + " · " + instructor["session_type"]?.jsonPrimitive?.content.orEmpty(), style = MaterialTheme.typography.titleMedium)
                            fun update(key: String, field: JsonPrimitive) {
                                draft = value.copy(instructors = JsonArray(value.instructors.mapIndexed { i, original ->
                                    if (i == index) JsonObject(instructor + (key to field)) else original
                                }))
                            }
                            ReviewRating("評分 Grading", instructor["grading"]?.jsonPrimitive?.doubleOrNull ?: -1.0, !busy) { update("grading", JsonPrimitive(it)) }
                            ReviewRating("教學 Teaching", instructor["teaching"]?.jsonPrimitive?.doubleOrNull ?: -1.0, !busy) { update("teaching", JsonPrimitive(it)) }
                            OutlinedTextField(instructor["comments"]?.jsonPrimitive?.contentOrNull.orEmpty(), { update("comments", JsonPrimitive(it)) }, label = { Text("講師評價 Instructor comments") }, minLines = 3, modifier = Modifier.fillMaxWidth(), enabled = !busy)
                        }
                    }
                    error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                    if (draft == null && !busy) TextButton(shapes = AppButtonDefaults.shapes(), onClick = hapticClick { error = null; retry++ }) { Text("重試 Retry") }
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(shapes = AppButtonDefaults.shapes(), onClick = hapticClick(onDismiss), enabled = !busy) { Text("取消 Cancel") }
                    Button(shapes = AppButtonDefaults.shapes(), onClick = hapticClick {
                        val edit = draft ?: return@hapticClick
                        busy = true; error = null
                        scope.launch {
                            repository.updateReview(reviewId, edit).onSuccess { onSaved() }.onFailure { error = it.localizedMessage }
                            busy = false
                        }
                    }, enabled = draft != null && !busy) { Text("儲存 Save") }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ReviewChoice(label: String, value: String, options: List<String>, enabled: Boolean, onChange: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded, { if (enabled) expanded = !expanded }) {
        OutlinedTextField(value, {}, readOnly = true, enabled = enabled, label = { Text(label) }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) }, modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable))
        ExposedDropdownMenu(expanded, { expanded = false }) {
            options.forEach { option -> DropdownMenuItem(text = { Text(option) }, onClick = hapticClick { onChange(option); expanded = false }) }
        }
    }
}

@Composable
private fun ReviewRating(label: String, value: Double, enabled: Boolean, onChange: (Double) -> Unit) {
    Column {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("$label · ${if (value < 0) "N/A" else value}", modifier = Modifier.weight(1f))
            Switch(value < 0, hapticChange { onChange(if (it) -1.0 else 3.0) }, enabled = enabled, modifier = Modifier.semantics { contentDescription = "$label: N/A" })
            Text("N/A")
        }
        if (value >= 0) Slider(value.toFloat(), { onChange((it * 2).roundToInt() / 2.0) }, valueRange = 0f..5f, steps = 9, enabled = enabled, modifier = Modifier.semantics { contentDescription = label })
    }
}
