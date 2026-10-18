package com.lingubible.app.ui.screens

import com.lingubible.app.ui.components.AppButtonDefaults
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.spring
import androidx.compose.foundation.selection.toggleable
import androidx.compose.ui.semantics.Role
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lingubible.app.core.navigation.screenBottomPadding
import com.lingubible.app.domain.model.*
import com.lingubible.app.domain.util.WordCountValidator
import com.lingubible.app.ui.common.hapticChange
import com.lingubible.app.ui.common.hapticClick
import com.lingubible.app.ui.components.*
import com.lingubible.app.ui.viewmodels.WriteReviewViewModel
import org.koin.androidx.compose.koinViewModel
import kotlin.math.roundToInt

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WriteReviewScreen(initialCourseCode: String = "", onNavigateBack: () -> Unit,
    onNavigateToAuth: () -> Unit = {}, viewModel: WriteReviewViewModel = koinViewModel()) {
    val s by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(initialCourseCode) { viewModel.start(initialCourseCode) }
    val f = s.form
    var step by rememberSaveable(s.userId) { mutableIntStateOf(0) }
    var choosingCourse by remember { mutableStateOf(false) }
    var pendingCourse by remember { mutableStateOf<String?>(null) }
    var discard by remember { mutableStateOf(false) }
    var pendingTerm by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(s.userId) { choosingCourse = false; pendingCourse = null; pendingTerm = null; discard = false }
    if (choosingCourse) ReviewCoursePicker(s.courses, onDismiss = { choosingCourse = false }) { code ->
        choosingCourse = false
        if (code != f.courseCode && f != ReviewForm(courseCode = f.courseCode, anonymous = f.anonymous, language = f.language)) pendingCourse = code
        else viewModel.selectCourse(code)
    }
    if (pendingCourse != null || pendingTerm != null || discard) AlertDialog(onDismissRequest = { pendingCourse = null; pendingTerm = null; discard = false },
        title = { Text(if (discard) "捨棄草稿？ Discard draft?" else if (pendingTerm != null) "更改學期？ Change term?" else "更改課程？ Change course?") },
        text = { Text(if (pendingTerm != null) "更改學期會清除講師評價。 Changing term clears instructor evaluations." else "此草稿的答案將會清除。 Answers in this draft will be cleared.") },
        confirmButton = { TextButton(shapes = AppButtonDefaults.shapes(), onClick = hapticClick {
            if (discard) { viewModel.discard(); step = 0 }
            else if (pendingTerm != null) pendingTerm?.let(viewModel::selectTerm)
            else pendingCourse?.let(viewModel::selectCourse)
            pendingCourse = null; pendingTerm = null; discard = false
        }) { Text("確認 Confirm") } },
        dismissButton = { TextButton(shapes = AppButtonDefaults.shapes(), onClick = hapticClick { pendingCourse = null; pendingTerm = null; discard = false }) { Text("取消 Cancel") } })

    if (s.userId == null) {
        Column(Modifier.fillMaxSize().screenBottomPadding().padding(24.dp), verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally) {
            Text("登入後撰寫評價 · Sign in to write a review", style = MaterialTheme.typography.titleMedium)
            Button(onClick = hapticClick(onNavigateToAuth), shapes = AppButtonDefaults.shapes()) { Text("登入 Sign in") }
        }
        return
    }
    Column(Modifier.fillMaxSize().screenBottomPadding().imePadding()) {
        FlowRow(Modifier.padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("1 課程 Course", "2 評價 Evaluate", "3 預覽 Preview").forEachIndexed { i, label ->
                FilterChip(step == i, onClick = hapticClick {
                    if (i < 2 || viewModel.validate()) step = i
                }, enabled = !s.isLoading, label = { Text(label) })
            }
        }
        s.errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(horizontal = 16.dp)) }
        AnimatedContent(step, transitionSpec = { fadeIn(spring(stiffness = 500f)) togetherWith fadeOut(spring(stiffness = 500f)) },
            label = "Review form step", modifier = Modifier.weight(1f)) { page ->
            Column(Modifier.fillMaxSize().fadingVerticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(if (s.draftRestored) "已還原此帳戶的草稿 · Draft restored for this account" else "草稿自動儲存於此裝置，保留 7 天 · Draft saved on this device for 7 days",
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                s.draftError?.let { Text("無法儲存／還原草稿 · Draft storage failed: $it", color = MaterialTheme.colorScheme.error) }
                when (page) {
                    0 -> {
                        ReviewFormCard("課程與學期 Course and term") {
                            if (s.loadingOptions) LinearProgressIndicator(Modifier.fillMaxWidth())
                            val course = s.courses.firstOrNull { it.code == f.courseCode }
                            OutlinedButton(onClick = hapticClick { choosingCourse = true }, modifier = Modifier.fillMaxWidth(), enabled = !s.isLoading && s.courses.isNotEmpty(), shapes = AppButtonDefaults.shapes()) {
                                Text(if (f.courseCode.isBlank()) "選擇課程 Select course" else "${f.courseCode} · ${course?.titleEn.orEmpty()}")
                            }
                            s.optionsError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                            if (s.optionsError != null) TextButton(shapes = AppButtonDefaults.shapes(), onClick = hapticClick(viewModel::loadOptions)) { Text("重新載入課程／學期 Retry catalogue") }
                            if (s.loadingRecords) LinearProgressIndicator(Modifier.fillMaxWidth())
                            val termCodes = s.records.map { it.term }.distinct().sortedDescending()
                            ReviewChoice("學期 Term", s.terms.firstOrNull { it.code == f.term }?.label ?: f.term,
                                termCodes.map { code -> s.terms.firstOrNull { it.code == code }?.let { "${it.label} · $code" } ?: code },
                                !s.isLoading && !s.loadingRecords) { label ->
                                val code = termCodes.first { code -> label == code || label.endsWith(" · $code") }
                                if (code != f.term && f.instructors.isNotEmpty()) pendingTerm = code
                                else viewModel.selectTerm(code)
                            }
                            if (f.courseCode.isNotBlank() && s.records.isEmpty() && !s.loadingRecords) {
                                Text("未能找到授課紀錄 · No teaching records available")
                                TextButton(shapes = AppButtonDefaults.shapes(), onClick = hapticClick(viewModel::loadRecords)) { Text("重試 Retry") }
                            }
                            Text("選擇你曾上課的講師／課節 · Select the instructors and sessions you attended", style = MaterialTheme.typography.bodyMedium)
                            s.records.filter { it.term == f.term && it.instructorName.isNotBlank() && it.section.isNotBlank() }
                                .distinctBy { "${it.instructorName}|${it.section}" }.forEach { record ->
                                    val selected = f.instructors.any { it.name == record.instructorName && it.session == record.section }
                                    Row(Modifier.fillMaxWidth().heightIn(min = 48.dp).toggleable(value = selected, enabled = !s.isLoading, role = Role.Checkbox, onValueChange = hapticChange { viewModel.toggleInstructor(record) }), verticalAlignment = Alignment.CenterVertically) {
                                        Checkbox(selected, null); Text("${record.instructorName}\n${record.section}", modifier = Modifier.weight(1f))
                                    }
                                }
                            if (f.instructors.isNotEmpty()) Text("已選 ${f.instructors.size} 個講師／課節 · ${f.instructors.size} selected")
                        }
                    }
                    1 -> {
                        ReviewFormCard("${f.courseCode} · 課程評價 Course evaluation") {
                            ReviewChoice("成績 Grade", if (f.grade == "-1") "不適用／不願透露 N/A" else f.grade, REVIEW_GRADES.map { if (it == "-1") "不適用／不願透露 N/A" else it }, !s.isLoading) { value -> viewModel.change { it.copy(grade = if (value == "不適用／不願透露 N/A") "-1" else value) } }
                            ReviewScoreField("工作量 Workload", f.workload, !s.isLoading) { value -> viewModel.change { it.copy(workload = value) } }
                            ReviewScoreField("難度 Difficulty", f.difficulty, !s.isLoading) { value -> viewModel.change { it.copy(difficulty = value) } }
                            ReviewScoreField("實用性 Usefulness", f.usefulness, !s.isLoading) { value -> viewModel.change { it.copy(usefulness = value) } }
                            ReviewCommentField("課程評價 Course comments", f.comment, !s.isLoading) { value -> viewModel.change { it.copy(comment = value) } }
                            ReviewChoice("評價語言 Review language", f.language, listOf("en", "zh-TW", "zh-CN"), !s.isLoading) { value -> viewModel.change { it.copy(language = value) } }
                            ReviewFormSwitch("匿名 Anonymous", f.anonymous, !s.isLoading) { value -> viewModel.change { it.copy(anonymous = value) } }
                        }
                        f.instructors.forEach { evaluation ->
                            key(evaluation.key) {
                                ReviewFormCard("${evaluation.name}\n${evaluation.session}") {
                                    fun update(transform: (InstructorEvaluation) -> InstructorEvaluation) = viewModel.updateInstructor(evaluation.key, transform)
                                    ReviewFormSwitch("未曾上課 Not attended", evaluation.notAttended, !s.isLoading) { value -> update { it.copy(notAttended = value) } }
                                    if (!evaluation.notAttended) ReviewScoreField("教學 Teaching", evaluation.teaching, !s.isLoading) { value -> update { it.copy(teaching = value) } }
                                    ReviewScoreField("給分 Grading", evaluation.grading, !s.isLoading) { value -> update { it.copy(grading = value) } }
                                    Text("課程要求（至少一項） Assessment requirements (select at least one)", style = MaterialTheme.typography.titleSmall)
                                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        REVIEW_REQUIREMENTS.forEach { (requirement, label) ->
                                            FilterChip(requirement in evaluation.requirements, enabled = !s.isLoading,
                                                onClick = hapticClick { update { it.copy(requirements = if (requirement in it.requirements) it.requirements - requirement else it.requirements + requirement) } }, label = { Text(label) })
                                        }
                                    }
                                    val serviceOptions = listOf("none" to "無 None", "optional" to "選修 Optional", "compulsory" to "必修 Compulsory")
                                    val recordedService = s.records.firstOrNull { it.term == f.term && it.instructorName == evaluation.name && it.section == evaluation.session }?.serviceLearning
                                    ReviewChoice("服務學習 Service learning", serviceOptions.firstOrNull { it.first == evaluation.serviceLearning }?.second.orEmpty(), serviceOptions.map { it.second }, !s.isLoading && recordedService == null) { label ->
                                        update { it.copy(serviceLearning = serviceOptions.first { it.second == label }.first) }
                                    }
                                    if (evaluation.serviceLearning != "none") ReviewCommentField("服務學習描述 Service-learning description", evaluation.serviceDescription, !s.isLoading,
                                        if (evaluation.serviceLearning == "compulsory") 5 else 0) { value -> update { it.copy(serviceDescription = value) } }
                                    ReviewCommentField("講師評價 Instructor comments", evaluation.comment, !s.isLoading) { value -> update { it.copy(comment = value) } }
                                }
                            }
                        }
                    }
                    2 -> {
                        Text("提交後公開顯示 · Published after submission", style = MaterialTheme.typography.titleMedium)
                        Text("${f.term} · ${f.language} · ${if (f.anonymous) "匿名 Anonymous" else "顯示帳戶 Show account"}")
                        ReviewCard(Review(id = "preview", courseCode = f.courseCode, term = f.term, grade = f.grade,
                            comment = f.comment, isAnonymous = f.anonymous, language = f.language, instructors = f.instructors,
                            instructorName = f.instructors.joinToString { it.name }, workloadRating = f.workload ?: -1.0,
                            difficultyRating = f.difficulty ?: -1.0, usefulnessRating = f.usefulness ?: -1.0,
                            teachingRating = reviewRatingAverage(f.instructors.map { if (it.notAttended) -1.0 else it.teaching }),
                            gradingRating = reviewRatingAverage(f.instructors.map { it.grading })))
                    }
                }
                if (s.isLoading) LinearProgressIndicator(Modifier.fillMaxWidth())
                FlowRow(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (page > 0) OutlinedButton(onClick = hapticClick { step-- }, enabled = !s.isLoading, shapes = AppButtonDefaults.shapes()) { Text("上一步 Back") }
                    Button(onClick = hapticClick {
                        if (page == 2) viewModel.submitReview(onNavigateBack)
                        else if (page == 0 || viewModel.validate()) step++
                    }, enabled = !s.isLoading && !s.loadingRecords && (page != 0 || (f.term.isNotBlank() && f.instructors.isNotEmpty())), shapes = AppButtonDefaults.shapes()) {
                        Text(if (page == 2) "提交評價 Submit review" else "下一步 Next")
                    }
                    TextButton(shapes = AppButtonDefaults.shapes(), onClick = hapticClick { discard = true }, enabled = !s.isLoading) { Text("捨棄草稿 Discard draft") }
                }
            }
        }
    }
}

@Composable
private fun ReviewFormCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium); content()
        }
    }
}

@Composable
private fun ReviewFormSwitch(label: String, checked: Boolean, enabled: Boolean, onChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(label, Modifier.weight(1f))
        Switch(checked, hapticChange(onChange), enabled = enabled, modifier = Modifier.semantics { contentDescription = label })
    }
}

@Composable
private fun ReviewScoreField(label: String, value: Double?, enabled: Boolean, onChange: (Double) -> Unit) {
    Column {
        Text("$label · ${when { value == null -> "未選擇 Not selected"; value < 0 -> "N/A"; else -> value.toString() }}")
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(value != null && value >= 0, onClick = hapticClick { onChange(if (value != null && value >= 0) value else 3.0) }, enabled = enabled, label = { Text("評分 Rate") })
            FilterChip(value == -1.0, onClick = hapticClick { onChange(-1.0) }, enabled = enabled, label = { Text("N/A") })
        }
        if (value != null && value >= 0) Slider(value.toFloat(), { onChange((it * 2).roundToInt() / 2.0) }, valueRange = 0f..5f, steps = 9,
            enabled = enabled, modifier = Modifier.semantics { contentDescription = label })
    }
}

@Composable
private fun ReviewCommentField(label: String, value: String, enabled: Boolean, minWords: Int = 5, onChange: (String) -> Unit) {
    val count = WordCountValidator.countWords(value)
    OutlinedTextField(value, onChange, enabled = enabled, label = { Text(label) }, minLines = 3, modifier = Modifier.fillMaxWidth(),
        isError = value.isNotBlank() && !WordCountValidator.isValid(value, minWords, 1000),
        supportingText = { Text("$count / $minWords–1000 字 words（以空格分隔 · separated by spaces）") })
}

@Composable
private fun ReviewCoursePicker(courses: List<Course>, onDismiss: () -> Unit, onSelect: (String) -> Unit) {
    var query by rememberSaveable { mutableStateOf("") }
    AlertDialog(onDismissRequest = onDismiss, title = { Text("選擇課程 Select course") },
        text = { Column {
            OutlinedTextField(query, { query = it }, label = { Text("課程代碼／名稱 Search course") }, modifier = Modifier.fillMaxWidth())
            FadingLazyColumn(Modifier.heightIn(max = 400.dp), bottomInset = 0.dp) {
                items(courses.filter { query.isBlank() || "${it.code} ${it.titleEn} ${it.titleZh}".contains(query.trim(), true) }, key = { it.code }) { course ->
                    TextButton(shapes = AppButtonDefaults.shapes(), onClick = hapticClick { onSelect(course.code) }, modifier = Modifier.fillMaxWidth()) { Text("${course.code}\n${course.titleZh.ifBlank { course.titleEn }}") }
                }
            }
        } }, confirmButton = { TextButton(shapes = AppButtonDefaults.shapes(), onClick = hapticClick(onDismiss)) { Text("關閉 Close") } })
}
