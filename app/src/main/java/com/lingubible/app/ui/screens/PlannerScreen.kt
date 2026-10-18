package com.lingubible.app.ui.screens

import com.lingubible.app.ui.components.AppButtonDefaults
import com.lingubible.app.ui.components.fadingVerticalScroll
import com.lingubible.app.ui.common.hapticClick

import com.lingubible.app.core.navigation.screenBottomPadding
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.TableView
import androidx.compose.material.icons.outlined.ViewAgenda
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.rememberScrollState
import com.lingubible.app.domain.model.timetableShareLink
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import com.lingubible.app.core.navigation.LocalTopBarState
import com.lingubible.app.core.settings.AppLanguage
import com.lingubible.app.core.settings.AppSettingsManager
import com.lingubible.app.core.theme.*
import com.lingubible.app.domain.model.TimetableTerm
import com.lingubible.app.ui.components.CourseSectionSelector
import com.lingubible.app.ui.components.FrostedGlassHeader
import com.lingubible.app.ui.components.M3ButtonGroup
import com.lingubible.app.ui.components.Material3TermSelector
import com.lingubible.app.ui.components.TimeConflictAlertCard
import com.lingubible.app.ui.components.TimetableGrid
import com.lingubible.app.ui.viewmodels.TimetableViewModel
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

enum class TimetableSubView {
    GRID,
    SELECTOR
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlannerScreen(
    timetableViewModel: TimetableViewModel = koinViewModel(),
    modifier: Modifier = Modifier,
    initialShareUrl: String? = null
) {
    val topBarState = LocalTopBarState.current
    val context = LocalContext.current
    val isDark = isAppDarkTheme()
    val settingsManager: AppSettingsManager = koinInject()
    val appLanguage by settingsManager.appLanguage.collectAsState()

    val isZh = appLanguage == AppLanguage.ZH_TW
    val gridText = if (isZh) "課表" else "Grid"
    val browseText = if (isZh) "+ 加選" else "+ Browse"

    val timetableState by timetableViewModel.uiState.collectAsStateWithLifecycle()

    var activeSubView by rememberSaveable { mutableStateOf(TimetableSubView.GRID) }

    var showAppearance by rememberSaveable { mutableStateOf(false) }
    if (showAppearance) com.lingubible.app.ui.components.TimetableAppearanceSheet(timetableState.selectedTerm, timetableState.selectedSections, timetableState.appearance, { showAppearance = false }, timetableViewModel::updateAppearance)
    var menuOpen by remember { mutableStateOf(false) }
    var showImport by rememberSaveable { mutableStateOf(false) }
    var importLink by rememberSaveable { mutableStateOf("") }
    var showClear by remember { mutableStateOf(false) }
    var incomingHandled by rememberSaveable(initialShareUrl) { mutableStateOf(false) }
    LaunchedEffect(initialShareUrl) {
        if (!incomingHandled && initialShareUrl != null) {
            incomingHandled = true; importLink = initialShareUrl; showImport = true
            timetableViewModel.prepareImport(initialShareUrl)
        }
    }
    if (showImport) AlertDialog(
        onDismissRequest = { showImport = false; timetableViewModel.cancelImport() },
        title = { Text("匯入課表 Import timetable") },
        text = {
            Column(Modifier.fadingVerticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("貼上網頁或 App 分享的課表連結。確認後加入現有課表。\nPaste a web or app timetable link. Import adds to your saved timetable.")
                OutlinedTextField(importLink, { importLink = it; timetableViewModel.cancelImport() }, label = { Text("課表連結 Timetable link") }, modifier = Modifier.fillMaxWidth(), enabled = !timetableState.importLoading)
                if (timetableState.importLoading) CircularProgressIndicator()
                timetableState.importError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                timetableState.importPreview?.let { preview ->
                    Text(preview.term.name, style = MaterialTheme.typography.titleMedium)
                    Text("新增 ${preview.newCount} · 已存在 ${preview.existingCount} · 未找到 ${preview.missingIds.size}\nNew · Already saved · Not found")
                    if (preview.missingIds.isNotEmpty()) Text(preview.missingIds.joinToString(), style = MaterialTheme.typography.bodySmall)
                    if (preview.conflictCount > 0) Text("${preview.conflictCount} 個時間衝突 · Time conflicts", color = MaterialTheme.colorScheme.error)
                }
            }
        },
        confirmButton = { Button(shapes = AppButtonDefaults.shapes(), enabled = importLink.isNotBlank() && !timetableState.importLoading, onClick = hapticClick {
            if (timetableState.importPreview == null) timetableViewModel.prepareImport(importLink)
            else { timetableViewModel.confirmImport(); activeSubView = TimetableSubView.GRID; showImport = false }
        }) { Text(if (timetableState.importPreview == null) "預覽 Preview" else "確認匯入 Import") } },
        dismissButton = { TextButton(shapes = AppButtonDefaults.shapes(), onClick = hapticClick { showImport = false; timetableViewModel.cancelImport() }) { Text("取消 Cancel") } }
    )
    if (showClear) AlertDialog(onDismissRequest = { showClear = false }, title = { Text("清空此學期？ Clear this term?") },
        text = { Text("清空後可按復原。其他學期不受影響。\nYou can undo this. Other terms are unchanged.") },
        confirmButton = { TextButton(shapes = AppButtonDefaults.shapes(), onClick = hapticClick { timetableViewModel.clearAll(); showClear = false }) { Text("清空 Clear") } },
        dismissButton = { TextButton(shapes = AppButtonDefaults.shapes(), onClick = hapticClick { showClear = false }) { Text("取消 Cancel") } })

    val containerBg = MaterialTheme.colorScheme.background
    val cardBg = MaterialTheme.colorScheme.surfaceContainer
    val cardBorder = MaterialTheme.colorScheme.outlineVariant
    val textMuted = MaterialTheme.colorScheme.onSurfaceVariant

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(containerBg)
    ) {
        // Timetable Header Action Bars linked to scroll hide/rebound
        AnimatedVisibility(
            visible = topBarState.isSubBarVisible,
            enter = expandVertically(
                expandFrom = Alignment.Top,
                animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
            ) + slideInVertically(
                initialOffsetY = { -it },
                animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
            ) + fadeIn(
                animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
            ),
            exit = shrinkVertically(
                shrinkTowards = Alignment.Top,
                animationSpec = spring(stiffness = Spring.StiffnessMedium)
            ) + slideOutVertically(
                targetOffsetY = { -it },
                animationSpec = spring(stiffness = Spring.StiffnessMedium)
            ) + fadeOut(
                animationSpec = spring(stiffness = Spring.StiffnessMedium)
            )
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Timetable Header: Term Selector & View Switcher Bar
                FrostedGlassHeader(
                    modifier = Modifier.fillMaxWidth(),
                    borderBottomOnly = true
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                // Material 3 Expressive Term Selector Dropdown Menu
                Material3TermSelector(
                    selectedTerm = timetableState.selectedTerm,
                    availableTerms = timetableState.availableTerms,
                    onSelectTerm = { term ->
                        timetableViewModel.selectTerm(term)
                    }
                )

                // Material 3 Expressive Button Group for View Toggle (Grid vs Course Browser)
                M3ButtonGroup(
                    selectedIndex = if (activeSubView == TimetableSubView.GRID) 0 else 1,
                    onLeadingClick = { activeSubView = TimetableSubView.GRID },
                    onTrailingClick = { activeSubView = TimetableSubView.SELECTOR },
                    leadingText = gridText,
                    trailingText = browseText
                )
            }
        }

        // Summary Pill & Action Bar (Export to ICS, Total Credits, Course Count)
        FrostedGlassHeader(
            modifier = Modifier.fillMaxWidth(),
            borderBottomOnly = true
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Credits Chip
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "已選 ${timetableState.totalCredits} 學分",
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    // Courses Count Chip
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "${timetableState.distinctCourseCount} 門課程",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium,
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                // ICS Export Button
                FilledTonalButton(shapes = AppButtonDefaults.shapes(),
                    onClick = hapticClick { timetableViewModel.exportTimetableIcs(context) },
                    enabled = timetableState.selectedSections.isNotEmpty(),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.pointerHoverIcon(PointerIcon.Hand)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.FileDownload,
                        contentDescription = "Export ICS",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "匯出日曆",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

        Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(shapes = AppButtonDefaults.iconShapes(), enabled = timetableState.canUndo && !timetableState.isLoading, onClick = hapticClick { timetableViewModel.undo() }) { Icon(Icons.AutoMirrored.Filled.Undo, "復原 Undo") }
            IconButton(shapes = AppButtonDefaults.iconShapes(), enabled = timetableState.canRedo && !timetableState.isLoading, onClick = hapticClick { timetableViewModel.redo() }) { Icon(Icons.AutoMirrored.Filled.Redo, "重做 Redo") }
            Spacer(Modifier.weight(1f))
            if (timetableState.isLoading) CircularProgressIndicator(Modifier.size(24.dp))
            Box {
                IconButton(shapes = AppButtonDefaults.iconShapes(), onClick = hapticClick { menuOpen = true }) { Icon(Icons.Default.MoreVert, "課表工具 Timetable tools") }
                DropdownMenu(menuOpen, { menuOpen = false }) {
                    DropdownMenuItem(text = { Text("課表自訂 Customize timetable") }, enabled = !timetableState.isLoading, onClick = hapticClick { menuOpen = false; showAppearance = true })
                    DropdownMenuItem(text = { Text("分享課表 Share link") }, enabled = timetableState.selectedSections.isNotEmpty() && !timetableState.isLoading, onClick = hapticClick {
                        menuOpen = false
                        try {
                            val link = timetableShareLink(timetableState.selectedTerm, timetableState.selectedSections.map { it.id }.toSet())
                            context.startActivity(android.content.Intent.createChooser(android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                type = "text/plain"; putExtra(android.content.Intent.EXTRA_TEXT, link)
                            }, "分享課表 Share timetable").addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK))
                        } catch (e: Exception) { android.widget.Toast.makeText(context, e.localizedMessage ?: "無法分享 · Could not share", android.widget.Toast.LENGTH_LONG).show() }
                    })
                    DropdownMenuItem(text = { Text("匯入課表 Import link") }, onClick = hapticClick { menuOpen = false; timetableViewModel.cancelImport(); showImport = true })
                    DropdownMenuItem(text = { Text("清空此學期 Clear term") }, enabled = timetableState.selectedSectionIds.isNotEmpty() && !timetableState.isLoading, onClick = hapticClick { menuOpen = false; showClear = true })
                }
            }
        }
        timetableState.errorMessage?.let { message ->
            Text(message, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(horizontal = 16.dp))
            TextButton(shapes = AppButtonDefaults.shapes(), onClick = hapticClick { timetableViewModel.retry() }) { Text("重試 Retry") }
        }

        // Material 3 Expressive Collision Warning Alert Card
        TimeConflictAlertCard(
            conflicts = timetableState.conflicts,
            onSelectConflict = {
                activeSubView = TimetableSubView.GRID
            },
            onResolveClick = {
                activeSubView = TimetableSubView.SELECTOR
            }
        )

        // Main Content: Smooth Transition between Timetable Weekly Grid AND Adding Course Selector
        AnimatedContent(
            targetState = activeSubView,
            transitionSpec = {
                val isForward = targetState.ordinal > initialState.ordinal
                val slideOffset = 0.18f
                if (isForward) {
                    (slideInHorizontally(
                        initialOffsetX = { (it * slideOffset).toInt() },
                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                    ) + fadeIn(
                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                    )).togetherWith(
                        slideOutHorizontally(
                            targetOffsetX = { (-it * slideOffset).toInt() },
                            animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                        ) + fadeOut(
                            animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                        )
                    )
                } else {
                    (slideInHorizontally(
                        initialOffsetX = { (-it * slideOffset).toInt() },
                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                    ) + fadeIn(
                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                    )).togetherWith(
                        slideOutHorizontally(
                            targetOffsetX = { (it * slideOffset).toInt() },
                            animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                        ) + fadeOut(
                            animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                        )
                    )
                }.using(SizeTransform(clip = true) { _, _ -> spring(stiffness = Spring.StiffnessMediumLow) })
            },
            label = "timetableSubViewTransition",
            modifier = Modifier.fillMaxSize()
        ) { subView ->
            when (subView) {
                TimetableSubView.GRID -> {
                    TimetableGrid(
                        selectedSections = timetableState.selectedSections,
                        appearance = timetableState.appearance,
                        creditsByCode = timetableState.creditsByCode,
                        term = timetableState.selectedTerm,
                        conflicts = timetableState.conflicts,
                        onRemoveSection = { timetableViewModel.removeSection(it) },
                        onNavigateToAddCourse = { activeSubView = TimetableSubView.SELECTOR },
                        onAddMeetingToCalendar = { section, meeting -> timetableViewModel.addMeetingToCalendar(context, section, meeting) },
                        onExportMeetingIcs = { section, meeting -> timetableViewModel.exportMeetingIcs(context, section, meeting) },
                        modifier = Modifier.fillMaxSize()
                    )
                }
                TimetableSubView.SELECTOR -> {
                    CourseSectionSelector(
                        sections = timetableState.filteredSections,
                        selectedSectionIds = timetableState.selectedSectionIds,
                        selectedSections = timetableState.selectedSections,
                        searchQuery = timetableState.searchQuery,
                        selectedDepartment = timetableState.selectedDepartment,
                        onSearchQueryChange = { timetableViewModel.setSearchQuery(it) },
                        onDepartmentSelect = { timetableViewModel.setSelectedDepartment(it) },
                        onToggleSection = { timetableViewModel.toggleSection(it) },
                        modifier = Modifier.fillMaxSize().screenBottomPadding(extra = 0.dp)
                    )
                }
            }
        }
    }
}
