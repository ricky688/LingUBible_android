package com.lingubible.app.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lingubible.app.core.theme.*
import com.lingubible.app.domain.calculator.HonoursCalculator
import com.lingubible.app.domain.calculator.HonoursCalculator.HonoursTier
import com.lingubible.app.domain.calculator.HonoursCalculator.YearAward
import com.lingubible.app.ui.components.*
import com.lingubible.app.ui.viewmodels.GpaCourseEntry
import com.lingubible.app.ui.viewmodels.GpaHonsViewModel
import com.lingubible.app.ui.viewmodels.GpaTerm
import com.lingubible.app.ui.viewmodels.TermPart
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun GpaHonsScreen(viewModel: GpaHonsViewModel = koinViewModel(), modifier: Modifier = Modifier) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var statistics by rememberSaveable { mutableStateOf(false) }
    var showResetDialog by rememberSaveable { mutableStateOf(false) }
    val viewState = rememberSaveableStateHolder()
    val calculatorScroll = rememberLazyListState()
    val statisticsScroll = rememberLazyListState()
    val years = remember(state.document) { state.document.terms.map { it.year }.distinct().sorted() }

    Column(modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).imePadding()) {
        SecondaryTabRow(
            selectedTabIndex = if (statistics) 1 else 0,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            listOf("計算器" to "Calculator", "榮譽統計" to "Statistics").forEachIndexed { index, (zh, en) ->
                Tab(
                    selected = statistics == (index == 1),
                    onClick = { statistics = index == 1 },
                    selectedContentColor = MaterialTheme.colorScheme.primary,
                    unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    text = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(zh, style = MaterialTheme.typography.titleSmall)
                            Text(en, style = MaterialTheme.typography.labelMedium)
                        }
                    }
                )
            }
        }
        AnimatedContent(
            targetState = statistics,
            modifier = Modifier.weight(1f),
            transitionSpec = {
                val direction = if (targetState) 1 else -1
                ((slideInHorizontally(
                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                    initialOffsetX = { direction * it / 4 }
                ) + fadeIn(animationSpec = spring(stiffness = Spring.StiffnessMediumLow))) togetherWith
                    (slideOutHorizontally(
                        animationSpec = spring(stiffness = Spring.StiffnessMedium),
                        targetOffsetX = { -direction * it / 4 }
                    ) + fadeOut(animationSpec = spring(stiffness = Spring.StiffnessMedium))))
                    .using(SizeTransform(clip = true) { _, _ ->
                        spring(stiffness = Spring.StiffnessMediumLow)
                    })
            },
            label = "gpaCalculatorStatisticsTransition"
        ) { showStatistics ->
            viewState.SaveableStateProvider(showStatistics) {
                if (showStatistics) {
                    FirstClassHonoursContent(listState = statisticsScroll)
                } else {
                LazyColumn(
                    state = calculatorScroll,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 160.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item("summary") {
                        GpaSectionTitle("GPA 與榮譽規劃", "GPA & Honours Planner")
                        Text("資料只儲存於本機 · Saved on this device", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(12.dp))
                        GpaSummary(state)
                    }
                    item("trend") { GpaTrendCard(state) }
                    item("target") { GpaTargetCard(state, viewModel::setTargetCgpa, viewModel::setRemainingCredits) }
                    item("editor_title") {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                GpaSectionTitle("學期與課程", "Terms & Courses")
                            }
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceContainerLow,
                                shape = RoundedCornerShape(14.dp),
                                border = CardDefaults.outlinedCardBorder().copy(
                                    brush = Brush.verticalGradient(
                                        listOf(
                                            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                                            MaterialTheme.colorScheme.outline.copy(alpha = 0.10f)
                                        )
                                    )
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    IconButton(
                                        onClick = viewModel::undo,
                                        enabled = state.canUndo,
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.Undo,
                                            contentDescription = "復原 Undo",
                                            tint = if (state.canUndo) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    IconButton(
                                        onClick = viewModel::redo,
                                        enabled = state.canRedo,
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.Redo,
                                            contentDescription = "重做 Redo",
                                            tint = if (state.canRedo) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    VerticalDivider(
                                        modifier = Modifier.height(18.dp).padding(horizontal = 2.dp),
                                        color = MaterialTheme.colorScheme.outlineVariant
                                    )
                                    IconButton(
                                        onClick = { showResetDialog = true },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.RestartAlt,
                                            contentDescription = "重設 Reset",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                    items(years, key = { "year_$it" }) { year ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .animateItem(
                                    fadeInSpec = spring(stiffness = Spring.StiffnessMediumLow),
                                    placementSpec = spring(stiffness = Spring.StiffnessMediumLow),
                                    fadeOutSpec = spring(stiffness = Spring.StiffnessMedium)
                                ),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            AcademicYearSelector(
                                selected = state.document.yearAcademic[year] ?: "${2021 + year}-${2022 + year}",
                                onSelect = { viewModel.setAcademicYear(year, it) }
                            )
                            YearCard(
                                year = year,
                                terms = state.document.terms.filter { it.year == year }.sortedBy { it.part.ordinal },
                                yearStat = state.yearStats.find { it.year == year },
                                courseCatalog = state.courseCatalog,
                                onUpdateCourse = viewModel::updateCourse,
                                onPickCourse = viewModel::pickCourseSuggestion,
                                onAddCourse = viewModel::addCourse,
                                onRemoveCourse = viewModel::removeCourse,
                                onAddTerm = { viewModel.addTerm(year) },
                                onRemoveTerm = viewModel::removeTerm,
                                onRemoveYear = { viewModel.removeYear(year) },
                                canRemoveYear = years.size > 1
                            )
                        }
                    }
                    if (years.size < 8) item("add_year") {
                        Surface(
                            onClick = viewModel::addYear,
                            shape = RoundedCornerShape(18.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerLow,
                            border = CardDefaults.outlinedCardBorder().copy(
                                brush = Brush.verticalGradient(
                                    listOf(
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.40f),
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                    )
                                )
                            ),
                            modifier = Modifier.fillMaxWidth().heightIn(min = 50.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = "新增學年 · Add Academic Year",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                    item("rules") {
                        Text("年度榮譽榜：全年至少 24 學分、其中一學期至少 12 學分，且無 F／FAIL／U／I。校長榜 ≥ 3.70；院長榜 ≥ 3.30。\nAnnual awards also require the credit and grade conditions above; chart reference lines alone do not establish eligibility.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                }
            }
        }
    }
    if (showResetDialog) AlertDialog(
        onDismissRequest = { showResetDialog = false },
        title = { Text("重設所有成績？ Reset planner?") },
        text = { Text("學期、課程和成績將回復初始狀態。可按「復原 Undo」還原。") },
        confirmButton = { TextButton(onClick = { viewModel.resetAll(); showResetDialog = false }) { Text("重設 Reset") } },
        dismissButton = { TextButton(onClick = { showResetDialog = false }) { Text("取消 Cancel") } }
    )
}

@Composable
private fun AcademicYearSelector(selected: String, onSelect: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val borderColor = MaterialTheme.colorScheme.outlineVariant
    val surfaceColor = MaterialTheme.colorScheme.surfaceContainerLow

    Box(modifier = Modifier.fillMaxWidth()) {
        Surface(
            onClick = { expanded = true },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 52.dp)
                .pointerHoverIcon(PointerIcon.Hand),
            shape = RoundedCornerShape(16.dp),
            color = surfaceColor,
            border = BorderStroke(
                1.dp,
                Brush.verticalGradient(
                    listOf(
                        borderColor.copy(alpha = 0.8f),
                        borderColor.copy(alpha = 0.3f)
                    )
                )
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Outlined.CalendarToday,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    Column {
                        Text(
                            text = "學年 · Academic Year",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = selected,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            shape = RoundedCornerShape(16.dp),
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ) {
            (2022..2029).forEach { year ->
                val label = "$year-${year + 1}"
                val isSelected = label == selected
                DropdownMenuItem(
                    text = {
                        Text(
                            text = label,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    },
                    leadingIcon = if (isSelected) {
                        {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    } else null,
                    onClick = {
                        onSelect(label)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun YearCard(
    year: Int,
    terms: List<GpaTerm>,
    yearStat: com.lingubible.app.ui.viewmodels.YearComputedStats?,
    courseCatalog: Map<String, com.lingubible.app.domain.model.Course>,
    onUpdateCourse: (String, String, String?, String?, String?, String?) -> Unit,
    onPickCourse: (String, String, com.lingubible.app.domain.model.Course) -> Unit,
    onAddCourse: (String) -> Unit,
    onRemoveCourse: (String, String) -> Unit,
    onAddTerm: () -> Unit,
    onRemoveTerm: (String) -> Unit,
    onRemoveYear: () -> Unit,
    canRemoveYear: Boolean
) {
    val cardBg = MaterialTheme.colorScheme.surfaceContainer
    val borderColor = MaterialTheme.colorScheme.outlineVariant
    val textMuted = MaterialTheme.colorScheme.onSurfaceVariant

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = cardBg,
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(
            1.dp,
            Brush.verticalGradient(
                listOf(
                    borderColor.copy(alpha = 0.7f),
                    borderColor.copy(alpha = 0.25f)
                )
            )
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .animateContentSize(spring(stiffness = Spring.StiffnessMediumLow))
        ) {
            // Year Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.size(34.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "Y$year",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }

                    Column {
                        Text(
                            text = "第 $year 學年 · Year $year",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        if (yearStat?.yearGpa != null || yearStat?.award != null) {
                            Row(
                                modifier = Modifier.padding(top = 2.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (yearStat.yearGpa != null) {
                                    Surface(
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "GPA ",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.primary,
                                                fontWeight = FontWeight.Medium
                                            )
                                            AnimatedContent(
                                                targetState = String.format(java.util.Locale.US, "%.3f", yearStat.yearGpa),
                                                transitionSpec = {
                                                    (slideInVertically(spring(stiffness = Spring.StiffnessMediumLow)) { it } + fadeIn())
                                                        .togetherWith(slideOutVertically(spring(stiffness = Spring.StiffnessMediumLow)) { -it } + fadeOut())
                                                },
                                                label = "YearGpaRoll"
                                            ) { gpaStr ->
                                                Text(
                                                    text = gpaStr,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        }
                                    }
                                }

                                if (yearStat?.award != null) {
                                    Surface(
                                        color = MaterialTheme.colorScheme.tertiaryContainer,
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Outlined.EmojiEvents,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onTertiaryContainer,
                                                modifier = Modifier.size(12.dp)
                                            )
                                            Text(
                                                text = yearStat.award.titleZh,
                                                color = MaterialTheme.colorScheme.onTertiaryContainer,
                                                fontWeight = FontWeight.Bold,
                                                style = MaterialTheme.typography.labelSmall
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                if (canRemoveYear) {
                    IconButton(
                        onClick = onRemoveYear,
                        modifier = Modifier
                            .size(36.dp)
                            .pointerHoverIcon(PointerIcon.Hand)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Remove Year",
                            tint = textMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Terms in this Year
            terms.forEachIndexed { index, term ->
                TermSection(
                    term = term,
                    courseCatalog = courseCatalog,
                    onUpdateCourse = { cId, code, title, credits, grade ->
                        onUpdateCourse(term.id, cId, code, title, credits, grade)
                    },
                    onPickCourse = { cId, course -> onPickCourse(term.id, cId, course) },
                    onAddCourse = { onAddCourse(term.id) },
                    onRemoveCourse = { cId -> onRemoveCourse(term.id, cId) },
                    onRemoveTerm = { onRemoveTerm(term.id) },
                    canRemoveTerm = terms.size > 1
                )
                if (index < terms.lastIndex) {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 12.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
                }
            }

            // Add Term Button (if < 3 terms in this year)
            if (terms.size < 3) {
                HorizontalDivider(
                    modifier = Modifier.padding(top = 10.dp, bottom = 8.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )
                Surface(
                    onClick = onAddTerm,
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.6f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .pointerHoverIcon(PointerIcon.Hand)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "新增此學年學期 · Add Term",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TermSection(
    term: GpaTerm,
    courseCatalog: Map<String, com.lingubible.app.domain.model.Course>,
    onUpdateCourse: (String, String?, String?, String?, String?) -> Unit,
    onPickCourse: (String, com.lingubible.app.domain.model.Course) -> Unit,
    onAddCourse: () -> Unit,
    onRemoveCourse: (String) -> Unit,
    onRemoveTerm: () -> Unit,
    canRemoveTerm: Boolean
) {
    val textMuted = MaterialTheme.colorScheme.onSurfaceVariant

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(spring(stiffness = Spring.StiffnessMediumLow))
    ) {
        // Term Title Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "${term.part.titleZh} · ${term.part.titleEn}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "${term.courses.size}/${term.part.maxCourses} 門課",
                        style = MaterialTheme.typography.labelSmall,
                        color = textMuted,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            if (canRemoveTerm) {
                IconButton(
                    onClick = onRemoveTerm,
                    modifier = Modifier
                        .size(36.dp)
                        .pointerHoverIcon(PointerIcon.Hand)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Remove Term",
                        tint = textMuted,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Courses List
        term.courses.forEachIndexed { index, course ->
            key(course.id) {
                CourseRow(
                    course = course,
                    courseCatalog = courseCatalog,
                    onUpdateCode = { onUpdateCourse(course.id, it, null, null, null) },
                    onUpdateTitle = { onUpdateCourse(course.id, null, it, null, null) },
                    onUpdateCredits = { onUpdateCourse(course.id, null, null, it, null) },
                    onUpdateGrade = { onUpdateCourse(course.id, null, null, null, it) },
                    onPickCourse = { onPickCourse(course.id, it) },
                    onRemove = { onRemoveCourse(course.id) },
                    canRemove = term.courses.size > 1
                )
                if (index < term.courses.lastIndex) {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                    )
                }
            }
        }

        // Add Course Button
        if (term.courses.size < term.part.maxCourses) {
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                onClick = onAddCourse,
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.5f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .pointerHoverIcon(PointerIcon.Hand)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "新增課程 · Add Course",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CourseRow(
    course: GpaCourseEntry,
    courseCatalog: Map<String, com.lingubible.app.domain.model.Course>,
    onUpdateCode: (String) -> Unit,
    onUpdateTitle: (String) -> Unit,
    onUpdateCredits: (String) -> Unit,
    onUpdateGrade: (String) -> Unit,
    onPickCourse: (com.lingubible.app.domain.model.Course) -> Unit,
    onRemove: () -> Unit,
    canRemove: Boolean
) {
    val textMuted = MaterialTheme.colorScheme.onSurfaceVariant

    var showGradeMenu by remember { mutableStateOf(false) }
    var showCreditsMenu by remember { mutableStateOf(false) }
    var showSuggestions by remember { mutableStateOf(false) }

    val matchingCourses = remember(course.code, courseCatalog) {
        val q = course.code.trim().uppercase()
        if (q.length >= 2) {
            courseCatalog.values.filter {
                it.code.contains(q) || it.titleEn.contains(q, ignoreCase = true) || it.titleZh.contains(q)
            }.take(5)
        } else emptyList()
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Course Code Input with Suggestions Dropdown
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = course.code,
                onValueChange = {
                    onUpdateCode(it)
                    showSuggestions = it.trim().length >= 2
                },
                placeholder = {
                    Text(
                        text = "課程代號 · Course Code (例: BUS1102)",
                        fontSize = 12.sp,
                        color = textMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                textStyle = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                shape = RoundedCornerShape(14.dp),
                trailingIcon = if (canRemove) {
                    {
                        IconButton(
                            onClick = onRemove,
                            modifier = Modifier.pointerHoverIcon(PointerIcon.Hand)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Remove Course",
                                tint = textMuted,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                } else null
            )

            DropdownMenu(
                expanded = showSuggestions && matchingCourses.isNotEmpty(),
                onDismissRequest = { showSuggestions = false },
                shape = RoundedCornerShape(16.dp),
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            ) {
                matchingCourses.forEach { match ->
                    DropdownMenuItem(
                        text = {
                            Column {
                                Text(text = match.code, fontWeight = FontWeight.Bold)
                                Text(
                                    text = if (match.titleZh.isNotBlank()) "${match.titleZh} ${match.titleEn}" else match.titleEn,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = textMuted
                                )
                            }
                        },
                        onClick = {
                            onPickCourse(match)
                            showSuggestions = false
                        }
                    )
                }
            }
        }

        // Credits and Grade Selectors Row
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            // Credits Selector Dropdown Trigger
            Box(Modifier.weight(1f)) {
                Surface(
                    onClick = { showCreditsMenu = true },
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 46.dp)
                        .pointerHoverIcon(PointerIcon.Hand)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${course.credits} 學分 · Credits",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelMedium
                        )
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = null,
                            tint = textMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                DropdownMenu(
                    expanded = showCreditsMenu,
                    onDismissRequest = { showCreditsMenu = false },
                    shape = RoundedCornerShape(16.dp),
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                ) {
                    listOf("0", "1", "3", "6").forEach { cr ->
                        val isSelected = course.credits == cr
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = "$cr 學分 · Credits",
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                            },
                            leadingIcon = if (isSelected) {
                                {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            } else null,
                            onClick = {
                                onUpdateCredits(cr)
                                showCreditsMenu = false
                            }
                        )
                    }
                }
            }

            // Grade Selector Dropdown Trigger
            Box(Modifier.weight(1f)) {
                val isBearing = HonoursCalculator.isGpaBearing(course.grade)
                val gp = HonoursCalculator.getGradePoint(course.grade)
                val hasGrade = course.grade.isNotBlank()
                val displayGrade = if (hasGrade) {
                    if (isBearing && gp != null) "${course.grade} (${String.format(java.util.Locale.US, "%.2f", gp)})"
                    else course.grade
                } else "成績 · Grade"

                Surface(
                    onClick = { showGradeMenu = true },
                    shape = RoundedCornerShape(14.dp),
                    color = if (hasGrade) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerLow,
                    border = BorderStroke(
                        1.dp,
                        if (hasGrade) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 46.dp)
                        .pointerHoverIcon(PointerIcon.Hand)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = displayGrade,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelMedium,
                            color = if (hasGrade) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = null,
                            tint = if (hasGrade) MaterialTheme.colorScheme.onPrimaryContainer else textMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                DropdownMenu(
                    expanded = showGradeMenu,
                    onDismissRequest = { showGradeMenu = false },
                    shape = RoundedCornerShape(16.dp),
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                ) {
                    Text(
                        text = "計入 GPA 之成績 · GPA Bearing",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                    HonoursCalculator.GPA_BEARING_GRADES.forEach { g ->
                        val pt = HonoursCalculator.getGradePoint(g) ?: 0.0
                        val isSelected = course.grade == g
                        DropdownMenuItem(
                            text = {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = g,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = String.format(java.util.Locale.US, "%.2f", pt),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = textMuted
                                    )
                                }
                            },
                            leadingIcon = if (isSelected) {
                                {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            } else null,
                            onClick = {
                                onUpdateGrade(g)
                                showGradeMenu = false
                            }
                        )
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 4.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
                    Text(
                        text = "不計入 GPA 之成績 · Non-GPA",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = textMuted,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                    HonoursCalculator.NON_GPA_GRADES.forEach { g ->
                        val isSelected = course.grade == g
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = g,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                            },
                            leadingIcon = if (isSelected) {
                                {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            } else null,
                            onClick = {
                                onUpdateGrade(g)
                                showGradeMenu = false
                            }
                        )
                    }
                }
            }
        }

        // Resolved Title Display (if present)
        if (course.title.isNotBlank()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.padding(start = 4.dp, top = 2.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(13.dp)
                )
                Text(
                    text = course.title,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
