package com.lingubible.app.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.util.Locale

@Serializable
internal data class HonoursCohort(val total: Int? = null, val first: Int? = null, val pct: Double? = null)
@Serializable
internal data class HonoursProgramme(val en: String, val tc: String, val faculty: String, val years: Map<String, HonoursCohort>)
@Serializable
internal data class HonoursReference(val programmes: List<HonoursProgramme>, val summary: Map<String, HonoursCohort>)

private data class FacultyInfo(val tc: String, val en: String, val color: Color)
private val facultyInfoMap = mapOf(
    "arts" to FacultyInfo("文學院", "Arts", Color(0xFFEF4444)),
    "business" to FacultyInfo("商學院", "Business", Color(0xFF3B82F6)),
    "socialSciences" to FacultyInfo("社科院", "SocSci", Color(0xFFF59E0B)),
    "interdisciplinaryStudies" to FacultyInfo("跨學科", "Interdisc", Color(0xFF8B5CF6)),
    "dataScience" to FacultyInfo("數據科學", "DataSci", Color(0xFF10B981))
)

private val faculties = linkedMapOf(
    "arts" to "文學院 · Arts",
    "business" to "商學院 · Business",
    "socialSciences" to "社會科學院 · Social Sciences",
    "interdisciplinaryStudies" to "跨學科學院 · Interdisciplinary Studies",
    "dataScience" to "數據科學學院 · Data Science"
)

private fun percentage(value: Double?) = value?.let { String.format(Locale.US, "%.1f%%", it * 100) } ?: "—"
private fun change(current: Double?, previous: Double?, percentagePoints: Boolean = false): String {
    if (current == null || previous == null) return "—"
    return if (percentagePoints) String.format(Locale.US, "%+.1f pp", (current - previous) * 100) else String.format(Locale.US, "%+.0f", current - previous)
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FirstClassHonoursContent(listState: LazyListState) {
    val context = LocalContext.current.applicationContext
    var reference by remember { mutableStateOf<HonoursReference?>(null) }
    var error by remember { mutableStateOf(false) }

    LaunchedEffect(context) {
        try {
            reference = withContext(Dispatchers.IO) {
                Json.decodeFromString<HonoursReference>(context.assets.open("data/first_class_honours.json").bufferedReader().use { it.readText() })
            }
        } catch (_: Exception) { error = true }
    }

    var selectedYear by rememberSaveable { mutableStateOf("2025") }
    var showChange by rememberSaveable { mutableStateOf(false) }
    var query by rememberSaveable { mutableStateOf("") }
    var faculty by rememberSaveable { mutableStateOf("") }
    var metric by rememberSaveable { mutableStateOf("rate") }
    var sortByName by rememberSaveable { mutableStateOf(false) }
    var groupByFaculty by rememberSaveable { mutableStateOf(false) }
    var filtersExpanded by rememberSaveable { mutableStateOf(false) }

    val data = reference
    if (data == null) {
        Box(
            modifier = Modifier.fillMaxWidth().padding(48.dp),
            contentAlignment = Alignment.Center
        ) {
            if (error) {
                Text(
                    text = "無法載入統計資料 · Unable to load reference data",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            } else {
                CircularProgressIndicator()
            }
        }
        return
    }

    val previousYear = data.summary.keys.filter { it < selectedYear }.maxOrNull()
    val summary = data.summary[selectedYear]
    val previous = data.summary[previousYear]

    val entries = remember(data, selectedYear, query, faculty, metric, sortByName, groupByFaculty) {
        val filtered = data.programmes.filter { p ->
            (faculty.isEmpty() || p.faculty == faculty) &&
                (query.isBlank() || p.en.contains(query, true) || p.tc.contains(query, true))
        }
        val grouped = if (!groupByFaculty) filtered else filtered.groupBy { it.faculty }.map { (key, programmes) ->
            HonoursProgramme(
                en = faculties.getValue(key).substringAfter(" · "),
                tc = faculties.getValue(key).substringBefore(" · "),
                faculty = key,
                years = data.summary.keys.associateWith { year ->
                    val available = programmes.mapNotNull { it.years[year] }.filter { it.total != null && it.first != null }
                    if (available.isEmpty()) HonoursCohort() else {
                        val total = available.sumOf { it.total!! }
                        val first = available.sumOf { it.first!! }
                        HonoursCohort(total, first, if (total > 0) first.toDouble() / total else null)
                    }
                }
            )
        }
        if (sortByName) grouped.sortedBy { it.en } else grouped.sortedByDescending {
            val cohort = it.years[selectedYear]
            when (metric) {
                "first" -> cohort?.first?.toDouble()
                "total" -> cohort?.total?.toDouble()
                else -> cohort?.pct
            } ?: -1.0
        }
    }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 160.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item("overview") {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize(spring(stiffness = Spring.StiffnessMediumLow)),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                GpaSectionTitle("甲等榮譽統計", "First Class Honours Statistics")

                // Cohort year selector
                StatisticsDropdown(
                    label = "畢業年度 · Cohort year",
                    selectedKey = selectedYear,
                    options = data.summary.keys.sorted().associateWith { "$it 屆 · Class of $it" },
                    onSelect = { selectedYear = it }
                )

                // YoY switch row
                ExpressiveSwitchRow(
                    checked = showChange,
                    onCheckedChange = { showChange = it },
                    title = "年度變化",
                    subtitle = "Year-over-year comparison"
                )

                AnimatedVisibility(
                    visible = showChange,
                    enter = fadeIn(spring(stiffness = Spring.StiffnessMediumLow)) +
                        expandVertically(spring(stiffness = Spring.StiffnessMediumLow)),
                    exit = fadeOut(spring(stiffness = Spring.StiffnessMedium)) +
                        shrinkVertically(spring(stiffness = Spring.StiffnessMedium))
                ) {
                    Text(
                        text = previousYear?.let { "與 $it 比較 · Compared with $it" } ?: "沒有上一屆資料 · No earlier cohort",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Expressive 3-metric summary dashboard
                HonoursSummary(summary = summary, previous = previous, showChange = showChange)

                // Search bar and expandable filter disclosure
                StatisticsFilterBar(
                    query = query,
                    onQueryChange = { query = it },
                    faculty = faculty,
                    onFacultyChange = { faculty = it },
                    metric = metric,
                    onMetricChange = { metric = it },
                    sortByName = sortByName,
                    onSortByNameChange = { sortByName = it },
                    groupByFaculty = groupByFaculty,
                    onGroupByFacultyChange = { groupByFaculty = it },
                    filtersExpanded = filtersExpanded,
                    onFiltersExpandedToggle = { filtersExpanded = !filtersExpanded },
                    onResetFilters = {
                        query = ""
                        faculty = ""
                        metric = "rate"
                        sortByName = false
                        groupByFaculty = false
                    }
                )
            }
        }

        if (entries.isEmpty()) {
            item("empty") {
                HonoursEmptyState(
                    onResetFilters = {
                        query = ""
                        faculty = ""
                        metric = "rate"
                        sortByName = false
                        groupByFaculty = false
                    }
                )
            }
        }

        items(entries, key = { "${it.faculty}_${it.en}" }) { entry ->
            HonoursProgrammeCard(
                entry = entry,
                selectedYear = selectedYear,
                previousYear = previousYear,
                metric = metric,
                showChange = showChange,
                modifier = Modifier.animateItem(
                    fadeInSpec = spring(stiffness = Spring.StiffnessMediumLow),
                    placementSpec = spring(stiffness = Spring.StiffnessMediumLow),
                    fadeOutSpec = spring(stiffness = Spring.StiffnessMedium)
                )
            )
        }

        item("source") {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
            ) {
                Text(
                    text = "資料來源：與網頁版相同的 Graduate Lists 整理資料（2024–2025）。\nSource: the web app's bundled graduate-list statistics. — 表示缺少資料；pp 表示百分點。按學院統計受目前搜尋及篩選影響。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(14.dp)
                )
            }
        }
    }
}

@Composable
private fun HonoursSummary(
    summary: HonoursCohort?,
    previous: HonoursCohort?,
    showChange: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.verticalGradient(
                listOf(
                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.40f),
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
                )
            )
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val total = summary?.total
            val prevTotal = previous?.total
            val first = summary?.first
            val prevFirst = previous?.first
            val pctVal = summary?.pct
            val prevPctVal = previous?.pct

            HonoursMetricTile(
                title = "畢業生",
                subtitle = "Grads",
                value = total?.toString() ?: "—",
                deltaStr = change(total?.toDouble(), prevTotal?.toDouble()),
                deltaNum = if (total != null && prevTotal != null) (total - prevTotal).toDouble() else null,
                showChange = showChange,
                icon = Icons.Default.School,
                accentColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f)
            )

            HonoursMetricTile(
                title = "甲等榮譽",
                subtitle = "First Class",
                value = first?.toString() ?: "—",
                deltaStr = change(first?.toDouble(), prevFirst?.toDouble()),
                deltaNum = if (first != null && prevFirst != null) (first - prevFirst).toDouble() else null,
                showChange = showChange,
                icon = Icons.Default.EmojiEvents,
                accentColor = Color(0xFFF59E0B),
                modifier = Modifier.weight(1f)
            )

            HonoursMetricTile(
                title = "甲等比例",
                subtitle = "Share",
                value = percentage(pctVal),
                deltaStr = change(pctVal, prevPctVal, percentagePoints = true),
                deltaNum = if (pctVal != null && prevPctVal != null) (pctVal - prevPctVal) else null,
                showChange = showChange,
                icon = Icons.AutoMirrored.Filled.TrendingUp,
                accentColor = Color(0xFFEF4444),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun HonoursMetricTile(
    title: String,
    subtitle: String,
    value: String,
    deltaStr: String,
    deltaNum: Double?,
    showChange: Boolean,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = RoundedCornerShape(18.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.verticalGradient(
                listOf(
                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.10f)
                )
            )
        )
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .background(accentColor.copy(alpha = 0.15f), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(15.dp)
                    )
                }
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            AnimatedContent(
                targetState = value,
                transitionSpec = {
                    (slideInVertically(spring(stiffness = Spring.StiffnessMediumLow)) { it / 2 } +
                        fadeIn(spring(stiffness = Spring.StiffnessMediumLow))) togetherWith
                        (slideOutVertically(spring(stiffness = Spring.StiffnessMedium)) { -it / 2 } +
                            fadeOut(spring(stiffness = Spring.StiffnessMedium)))
                },
                label = "metricRoll_${title}"
            ) { targetValue ->
                Text(
                    text = targetValue,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            AnimatedVisibility(
                visible = showChange && deltaStr != "—",
                enter = fadeIn(spring(stiffness = Spring.StiffnessMediumLow)) +
                    expandVertically(spring(stiffness = Spring.StiffnessMediumLow)),
                exit = fadeOut(spring(stiffness = Spring.StiffnessMedium)) +
                    shrinkVertically(spring(stiffness = Spring.StiffnessMedium))
            ) {
                val isPositive = (deltaNum ?: 0.0) > 0
                val isNegative = (deltaNum ?: 0.0) < 0
                val trendBg = when {
                    isPositive -> Color(0xFF10B981).copy(alpha = 0.14f)
                    isNegative -> Color(0xFFEF4444).copy(alpha = 0.14f)
                    else -> MaterialTheme.colorScheme.surfaceContainerHighest
                }
                val trendColor = when {
                    isPositive -> Color(0xFF10B981)
                    isNegative -> Color(0xFFEF4444)
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }
                val trendIcon = when {
                    isPositive -> Icons.Default.ArrowUpward
                    isNegative -> Icons.Default.ArrowDownward
                    else -> Icons.AutoMirrored.Filled.TrendingFlat
                }

                Surface(
                    color = trendBg,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = trendIcon,
                            contentDescription = null,
                            tint = trendColor,
                            modifier = Modifier.size(10.dp)
                        )
                        Text(
                            text = deltaStr,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = trendColor
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatisticsFilterBar(
    query: String,
    onQueryChange: (String) -> Unit,
    faculty: String,
    onFacultyChange: (String) -> Unit,
    metric: String,
    onMetricChange: (String) -> Unit,
    sortByName: Boolean,
    onSortByNameChange: (Boolean) -> Unit,
    groupByFaculty: Boolean,
    onGroupByFacultyChange: (Boolean) -> Unit,
    filtersExpanded: Boolean,
    onFiltersExpandedToggle: () -> Unit,
    onResetFilters: () -> Unit
) {
    val activeFilterCount = (if (faculty.isNotEmpty()) 1 else 0) +
        (if (metric != "rate") 1 else 0) +
        (if (sortByName) 1 else 0) +
        (if (groupByFaculty) 1 else 0)

    val filterRotation by animateFloatAsState(
        targetValue = if (filtersExpanded) 180f else 0f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "filterBtnRotation"
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                placeholder = { Text("搜尋課程 · Search programmes", style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                },
                trailingIcon = {
                    AnimatedVisibility(
                        visible = query.isNotEmpty(),
                        enter = fadeIn(spring(stiffness = Spring.StiffnessMediumLow)) + scaleIn(spring(stiffness = Spring.StiffnessMediumLow)),
                        exit = fadeOut(spring(stiffness = Spring.StiffnessMedium)) + scaleOut(spring(stiffness = Spring.StiffnessMedium))
                    ) {
                        IconButton(onClick = { onQueryChange("") }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear", modifier = Modifier.size(18.dp))
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(18.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                ),
                modifier = Modifier.weight(1f)
            )

            FilledTonalIconButton(
                onClick = onFiltersExpandedToggle,
                shape = RoundedCornerShape(18.dp),
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = if (filtersExpanded || activeFilterCount > 0)
                        MaterialTheme.colorScheme.primaryContainer
                    else MaterialTheme.colorScheme.surfaceContainerLow,
                    contentColor = if (filtersExpanded || activeFilterCount > 0)
                        MaterialTheme.colorScheme.onPrimaryContainer
                    else MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier.size(54.dp)
            ) {
                BadgedBox(
                    badge = {
                        if (activeFilterCount > 0) {
                            Badge(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ) {
                                Text("$activeFilterCount")
                            }
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = "Filters",
                        modifier = Modifier.graphicsLayer { rotationZ = filterRotation }
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = filtersExpanded,
            enter = expandVertically(spring(stiffness = Spring.StiffnessMediumLow)) +
                fadeIn(spring(stiffness = Spring.StiffnessMediumLow)),
            exit = shrinkVertically(spring(stiffness = Spring.StiffnessMedium)) +
                fadeOut(spring(stiffness = Spring.StiffnessMedium))
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.verticalGradient(
                        listOf(
                            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.40f),
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
                        )
                    )
                )
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.FilterList,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "篩選與排序 · Filter & Sort",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        if (activeFilterCount > 0) {
                            TextButton(
                                onClick = onResetFilters,
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Icon(Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("重設 Reset", style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }

                    // Faculty chips
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "學院 · Faculty",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            item {
                                FilterChip(
                                    selected = faculty.isEmpty(),
                                    onClick = { onFacultyChange("") },
                                    label = { Text("全部 All") },
                                    shape = RoundedCornerShape(12.dp)
                                )
                            }
                            items(faculties.toList()) { (key, name) ->
                                val shortTitle = name.substringBefore(" · ")
                                FilterChip(
                                    selected = faculty == key,
                                    onClick = { onFacultyChange(key) },
                                    label = { Text(shortTitle) },
                                    shape = RoundedCornerShape(12.dp)
                                )
                            }
                        }
                    }

                    // Metric dropdown & Sort button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StatisticsDropdown(
                            label = "指標 · Metric",
                            selectedKey = metric,
                            options = linkedMapOf(
                                "rate" to "甲等比例 · Share",
                                "first" to "甲等人數 · Count",
                                "total" to "畢業人數 · Total"
                            ),
                            onSelect = onMetricChange,
                            modifier = Modifier.weight(1.3f)
                        )

                        OutlinedCard(
                            onClick = { onSortByNameChange(!sortByName) },
                            modifier = Modifier.weight(1f).heightIn(min = 54.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.outlinedCardColors(
                                containerColor = if (sortByName) MaterialTheme.colorScheme.secondaryContainer
                                else MaterialTheme.colorScheme.surfaceContainerLow
                            )
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize().padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "排序 · Sort",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                AnimatedContent(
                                    targetState = sortByName,
                                    transitionSpec = {
                                        (slideInVertically(spring(stiffness = Spring.StiffnessMediumLow)) { it / 2 } + fadeIn()) togetherWith
                                            (slideOutVertically(spring(stiffness = Spring.StiffnessMedium)) { -it / 2 } + fadeOut())
                                    },
                                    label = "sortModeText"
                                ) { isByName ->
                                    Text(
                                        text = if (isByName) "名稱 Name A-Z" else "數值 Value ↓",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }

                    // Combine by faculty switch
                    ExpressiveSwitchRow(
                        checked = groupByFaculty,
                        onCheckedChange = onGroupByFacultyChange,
                        title = "按學院合併課程",
                        subtitle = "Combine programmes by faculty"
                    )
                }
            }
        }
    }
}

@Composable
private fun HonoursProgrammeCard(
    entry: HonoursProgramme,
    selectedYear: String,
    previousYear: String?,
    metric: String,
    showChange: Boolean,
    modifier: Modifier = Modifier
) {
    val cohort = entry.years[selectedYear]
    val older = entry.years[previousYear]
    val pctVal = cohort?.pct ?: 0.0

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1.0f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "cardPressScale_${entry.en}"
    )

    val animatedProgress by animateFloatAsState(
        targetValue = pctVal.toFloat().coerceIn(0f, 1f),
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "progPct_${entry.en}"
    )

    val facInfo = facultyInfoMap[entry.faculty]
    val facColor = facInfo?.color ?: MaterialTheme.colorScheme.primary

    Card(
        modifier = modifier
            .fillMaxWidth()
            .scale(pressScale)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = {}
            ),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.verticalGradient(
                listOf(
                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.10f)
                )
            )
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f).padding(end = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = entry.tc,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = entry.en,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                facInfo?.let { info ->
                    Surface(
                        color = facColor.copy(alpha = 0.14f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "${info.tc} · ${info.en}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = facColor,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "${cohort?.first ?: "—"} / ${cohort?.total ?: "—"}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "人 · graduates",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }

                AnimatedContent(
                    targetState = percentage(cohort?.pct),
                    transitionSpec = {
                        (slideInVertically(spring(stiffness = Spring.StiffnessMediumLow)) { it / 2 } + fadeIn()) togetherWith
                            (slideOutVertically(spring(stiffness = Spring.StiffnessMedium)) { -it / 2 } + fadeOut())
                    },
                    label = "entryPct_${entry.en}"
                ) { targetPct ->
                    Text(
                        text = targetPct,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp),
                strokeCap = StrokeCap.Round,
                trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                color = facColor
            )

            AnimatedVisibility(
                visible = showChange,
                enter = fadeIn(spring(stiffness = Spring.StiffnessMediumLow)) +
                    expandVertically(spring(stiffness = Spring.StiffnessMediumLow)),
                exit = fadeOut(spring(stiffness = Spring.StiffnessMedium)) +
                    shrinkVertically(spring(stiffness = Spring.StiffnessMedium))
            ) {
                val delta = when (metric) {
                    "first" -> change(cohort?.first?.toDouble(), older?.first?.toDouble())
                    "total" -> change(cohort?.total?.toDouble(), older?.total?.toDouble())
                    else -> change(cohort?.pct, older?.pct, true)
                }
                val isPos = delta.startsWith("+")
                val isNeg = delta.startsWith("-")
                val pillBg = when {
                    isPos -> Color(0xFF10B981).copy(alpha = 0.14f)
                    isNeg -> Color(0xFFEF4444).copy(alpha = 0.14f)
                    else -> MaterialTheme.colorScheme.surfaceContainerLow
                }
                val pillColor = when {
                    isPos -> Color(0xFF10B981)
                    isNeg -> Color(0xFFEF4444)
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }
                val trendIcon = when {
                    isPos -> Icons.Default.ArrowUpward
                    isNeg -> Icons.Default.ArrowDownward
                    else -> Icons.AutoMirrored.Filled.TrendingFlat
                }

                Surface(
                    color = pillBg,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = trendIcon,
                            contentDescription = null,
                            tint = pillColor,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = "年度變化 YoY: $delta",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = pillColor
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HonoursEmptyState(
    onResetFilters: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(
                        MaterialTheme.colorScheme.surfaceContainerHighest,
                        RoundedCornerShape(16.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.SearchOff,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(28.dp)
                )
            }
            Text(
                text = "找不到符合條件的課程",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                text = "請嘗試更改搜尋關鍵字或清除篩選條件。\nTry adjusting your search terms or clearing filters.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            FilledTonalButton(
                onClick = onResetFilters,
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("重設全部篩選 · Reset all")
            }
        }
    }
}

@Composable
private fun ExpressiveSwitchRow(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    title: String,
    subtitle: String
) {
    val containerColor by animateColorAsState(
        targetValue = if (checked) MaterialTheme.colorScheme.primaryContainer
        else MaterialTheme.colorScheme.surfaceContainerLow,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "statisticsSwitchContainer"
    )
    Surface(
        color = containerColor,
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 64.dp)
            .clickable { onCheckedChange(!checked) }
    ) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(
                checked = checked,
                onCheckedChange = null,
                thumbContent = if (checked) {
                    {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(SwitchDefaults.IconSize)
                        )
                    }
                } else null,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                    checkedTrackColor = MaterialTheme.colorScheme.primary,
                    checkedIconColor = MaterialTheme.colorScheme.primary,
                    uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                    uncheckedTrackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                    uncheckedBorderColor = MaterialTheme.colorScheme.outline
                )
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StatisticsDropdown(
    label: String,
    selectedKey: String,
    options: Map<String, String>,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier
    ) {
        OutlinedCard(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth().menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
        ) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    AnimatedContent(
                        targetState = options[selectedKey].orEmpty(),
                        transitionSpec = {
                            (slideInVertically(spring(stiffness = Spring.StiffnessMediumLow)) { it / 3 } +
                                fadeIn(spring(stiffness = Spring.StiffnessMediumLow))) togetherWith
                                (slideOutVertically(spring(stiffness = Spring.StiffnessMedium)) { -it / 3 } +
                                    fadeOut(spring(stiffness = Spring.StiffnessMedium)))
                        },
                        label = "statisticsSelectorValue"
                    ) { selectedLabel ->
                        Text(selectedLabel, style = MaterialTheme.typography.bodyMedium, maxLines = 2)
                    }
                }
                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
            }
        }
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { (key, option) ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onSelect(key)
                        expanded = false
                    }
                )
            }
        }
    }
}
