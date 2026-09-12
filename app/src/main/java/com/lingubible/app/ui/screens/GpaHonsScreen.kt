package com.lingubible.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import com.lingubible.app.core.navigation.LocalTopBarState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lingubible.app.core.theme.*
import com.lingubible.app.domain.calculator.HonoursCalculator
import com.lingubible.app.domain.calculator.HonoursCalculator.HonoursTier
import com.lingubible.app.domain.calculator.HonoursCalculator.RequiredAvgStatus
import com.lingubible.app.domain.calculator.HonoursCalculator.YearAward
import com.lingubible.app.ui.common.mouseScrollbar
import com.lingubible.app.ui.components.FrostedGlassHeader
import com.lingubible.app.ui.components.M3ButtonGroup
import com.lingubible.app.ui.components.M3ButtonGroupItem
import com.lingubible.app.ui.components.ScrollableM3ButtonGroup
import com.lingubible.app.ui.viewmodels.GpaCourseEntry
import com.lingubible.app.ui.viewmodels.GpaHonsViewModel
import com.lingubible.app.ui.viewmodels.GpaTerm
import com.lingubible.app.ui.viewmodels.TermPart
import org.koin.androidx.compose.koinViewModel

val HONOURS_TIER_COLORS = mapOf(
    HonoursTier.FIRST to Color(0xFFDC2626), // Lingnan Red / Crimson
    HonoursTier.UPPER_SECOND to Color(0xFFEA580C), // Orange
    HonoursTier.LOWER_SECOND to Color(0xFFCA8A04), // Dark Yellow
    HonoursTier.THIRD to Color(0xFF4B5563), // Slate Gray
    HonoursTier.PASS to Color(0xFF64748B) // Light Slate
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GpaHonsScreen(
    viewModel: GpaHonsViewModel = koinViewModel(),
    modifier: Modifier = Modifier
) {
    val topBarState = LocalTopBarState.current
    val isDark = isAppDarkTheme()
    val state by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    var showResetDialog by remember { mutableStateOf(false) }

    val containerBg = MaterialTheme.colorScheme.background
    val cardBg = MaterialTheme.colorScheme.surfaceContainer
    val cardBorder = MaterialTheme.colorScheme.outlineVariant
    val textMuted = MaterialTheme.colorScheme.onSurfaceVariant

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(containerBg)
    ) {
        // Sticky Header Toolbar (Title, Undo, Redo, Reset) linked to scroll hide/rebound
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
                FrostedGlassHeader(
                    modifier = Modifier.fillMaxWidth(),
                    borderBottomOnly = true
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "GPA 與榮譽計算",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "GPA & Honours Planner",
                                style = MaterialTheme.typography.labelSmall,
                                color = textMuted
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            IconButton(
                                onClick = { viewModel.undo() },
                                enabled = state.canUndo,
                                modifier = Modifier.pointerHoverIcon(PointerIcon.Hand)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Undo,
                                    contentDescription = "Undo",
                                    tint = if (state.canUndo) (if (isDark) Color.White else Color(0xFF1E293B)) else textMuted.copy(alpha = 0.4f)
                                )
                            }

                            IconButton(
                                onClick = { viewModel.redo() },
                                enabled = state.canRedo,
                                modifier = Modifier.pointerHoverIcon(PointerIcon.Hand)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Redo,
                                    contentDescription = "Redo",
                                    tint = if (state.canRedo) (if (isDark) Color.White else Color(0xFF1E293B)) else textMuted.copy(alpha = 0.4f)
                                )
                            }

                            FilledTonalButton(
                                onClick = { showResetDialog = true },
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0),
                                    contentColor = if (isDark) Color.White else Color(0xFF334155)
                                ),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                modifier = Modifier.pointerHoverIcon(PointerIcon.Hand)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.RotateLeft,
                                    contentDescription = "Reset",
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "重設 Reset",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
        }

        // Scrollable Body
        Column(
            modifier = Modifier
                .fillMaxSize()
                .mouseScrollbar(scrollState)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Summary Dashboard Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                border = androidx.compose.foundation.BorderStroke(1.dp, cardBorder),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.School,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "累積學術概況",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Academic Summary",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = textMuted
                                )
                            }
                        }

                        // Local only storage notice pill
                        Surface(
                            color = if (isDark) md_theme_dark_surfaceContainerHigh else Color(0xFFF1F5F9),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = textMuted,
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = "本機儲存 Local only",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = textMuted,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 3 Metric Boxes
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Cumulative GPA
                        Surface(
                            modifier = Modifier.weight(1f),
                            color = MaterialTheme.colorScheme.surfaceContainerLow,
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, cardBorder)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "累積 cGPA",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = textMuted
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = if (state.cgpa != null) String.format("%.3f", state.cgpa) else "—",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "/ 4.000",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = textMuted,
                                    fontSize = 10.sp
                                )
                            }
                        }

                        // Classification
                        Surface(
                            modifier = Modifier.weight(1.3f),
                            color = MaterialTheme.colorScheme.surfaceContainerLow,
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, cardBorder)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "榮譽學位等級",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = textMuted
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                if (state.currentHonoursTier != null) {
                                    val tierColor = HONOURS_TIER_COLORS[state.currentHonoursTier] ?: MaterialTheme.colorScheme.primary
                                    Surface(
                                        color = tierColor,
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(
                                                text = state.currentHonoursTier!!.titleZh,
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                style = MaterialTheme.typography.labelMedium
                                            )
                                            Text(
                                                text = state.currentHonoursTier!!.titleEn,
                                                color = Color.White.copy(alpha = 0.9f),
                                                fontSize = 10.sp
                                            )
                                        }
                                    }
                                } else {
                                    Text(
                                        text = if (state.cgpa != null) "尚低於榮譽線" else "尚未計入成績",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = textMuted,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }

                        // Earned GPA Credits
                        Surface(
                            modifier = Modifier.weight(1f),
                            color = MaterialTheme.colorScheme.surfaceContainerLow,
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, cardBorder)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "GPA 學分",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = textMuted
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = String.format("%.0f", state.earnedCredits),
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Credits",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = textMuted,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }

                    // Merit Awards Pills (President's / Dean's List)
                    val activeAwards = state.yearStats.mapNotNull { it.award }.distinct()
                    if (activeAwards.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            activeAwards.forEach { award ->
                                val (bgCol, fgCol) = when (award) {
                                    YearAward.PRESIDENTS_LIST -> Color(0xFFF59E0B) to Color.Black
                                    YearAward.DEANS_LIST -> Color(0xFF06B6D4) to Color.Black
                                }
                                Surface(
                                    color = bgCol,
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Star,
                                            contentDescription = null,
                                            tint = fgCol,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Text(
                                            text = "${award.titleZh} ${award.titleEn}",
                                            color = fgCol,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 2. Honours & cGPA Target Calculator Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.TrackChanges,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "目標榮譽與剩餘學分預測",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Target Honours & Required GPA Projection",
                                style = MaterialTheme.typography.labelSmall,
                                color = textMuted
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Target cGPA & Quick Set Chips
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "目標累積 GPA (Target cGPA)",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold
                            )

                            OutlinedTextField(
                                value = state.targetCgpaInput,
                                onValueChange = { viewModel.setTargetCgpa(it) },
                                modifier = Modifier.width(110.dp),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                textStyle = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.End
                                ),
                                shape = RoundedCornerShape(10.dp)
                            )
                        }

                        // Quick Set Connected Button Group
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "快速設定:",
                                style = MaterialTheme.typography.labelSmall,
                                color = textMuted
                            )
                            val quickTiers = remember { HonoursTier.entries.take(4) }
                            val tierItems = remember(state.targetCgpaInput) {
                                quickTiers.map { tier ->
                                    val targetVal = String.format("%.2f", tier.minCgpa)
                                    M3ButtonGroupItem(
                                        label = "${tier.titleZh} $targetVal",
                                        onClick = { viewModel.setTargetCgpa(targetVal) }
                                    )
                                }
                            }
                            val selectedTierIndex = remember(state.targetCgpaInput) {
                                quickTiers.indexOfFirst { tier ->
                                    val targetVal = String.format("%.2f", tier.minCgpa)
                                    state.targetCgpaInput == targetVal
                                }
                            }

                            ScrollableM3ButtonGroup(
                                selectedIndex = selectedTierIndex,
                                items = tierItems,
                                height = 32.dp,
                                spacing = 4.dp,
                                activeCornerRadius = 16.dp,
                                inactiveCornerRadius = 8.dp,
                                showCheckmarkOnSelected = true
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Remaining Credits Input
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "預計剩餘總學分 (Remaining Credits)",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "以典型 120 學分計，預設為剩餘 ${maxOf(0, (120 - state.earnedCredits).toInt())} 學分",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = textMuted,
                                    fontSize = 11.sp
                                )
                            }

                            OutlinedTextField(
                                value = state.remainingCreditsInput,
                                onValueChange = { viewModel.setRemainingCredits(it) },
                                placeholder = {
                                    Text(
                                        text = "${maxOf(0, (120 - state.earnedCredits).toInt())}",
                                        textAlign = TextAlign.End,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                },
                                modifier = Modifier.width(110.dp),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                textStyle = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.End
                                ),
                                shape = RoundedCornerShape(10.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Projection Result Display Box
                    Surface(
                        color = when (state.targetResult.status) {
                            RequiredAvgStatus.FEASIBLE -> Color(0xFF10B981).copy(alpha = 0.12f)
                            RequiredAvgStatus.ACHIEVED -> Color(0xFF3B82F6).copy(alpha = 0.12f)
                            RequiredAvgStatus.IMPOSSIBLE -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                            RequiredAvgStatus.NO_REMAINING -> Color(0xFF64748B).copy(alpha = 0.12f)
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            when (state.targetResult.status) {
                                RequiredAvgStatus.FEASIBLE -> {
                                    Text(
                                        text = "若要達到目標累積 GPA ${state.targetCgpaInput}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(
                                        verticalAlignment = Alignment.Bottom,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = "在剩餘學分需平均取得",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = String.format("%.3f", state.targetResult.required),
                                            style = MaterialTheme.typography.headlineMedium,
                                            fontWeight = FontWeight.Black,
                                            color = Color(0xFF059669)
                                        )
                                        Text(
                                            text = "GPA",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF059669)
                                        )
                                    }
                                }
                                RequiredAvgStatus.ACHIEVED -> {
                                    Text(
                                        text = "🎉 已穩奪目標累積 GPA ${state.targetCgpaInput}！",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF2563EB)
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "只要未來每學期平均維持最低 1.00 GPA（不違反退學規定），目標榮譽便已確定鎖定。",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Center
                                    )
                                }
                                RequiredAvgStatus.IMPOSSIBLE -> {
                                    Text(
                                        text = "⚠️ 目標累積 GPA ${state.targetCgpaInput} 已超出最高可能上限",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "即便剩餘學分全部取得最高 4.000 GPA (A)，最高可能畢業 cGPA 為 ${String.format("%.3f", state.targetResult.projectedCgpa)}。",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.error,
                                        textAlign = TextAlign.Center
                                    )
                                }
                                RequiredAvgStatus.NO_REMAINING -> {
                                    Text(
                                        text = "剩餘學分為 0，目前最終 cGPA 為 ${String.format("%.3f", state.targetResult.projectedCgpa)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = textMuted
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 3. Academic Years & Terms Editor
            Text(
                text = "學期與課程編輯器 Terms & Courses",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )

            // Render Years
            val distinctYears = state.document.terms.map { it.year }.distinct().sorted()
            distinctYears.forEach { yearNum ->
                val termsInYear = state.document.terms.filter { it.year == yearNum }
                val yearStat = state.yearStats.find { it.year == yearNum }

                YearCard(
                    year = yearNum,
                    terms = termsInYear,
                    yearStat = yearStat,
                    courseCatalog = state.courseCatalog,
                    onUpdateCourse = { termId, courseId, code, title, credits, grade ->
                        viewModel.updateCourse(termId, courseId, code, title, credits, grade)
                    },
                    onPickCourse = { termId, courseId, course ->
                        viewModel.pickCourseSuggestion(termId, courseId, course)
                    },
                    onAddCourse = { termId -> viewModel.addCourse(termId) },
                    onRemoveCourse = { termId, courseId -> viewModel.removeCourse(termId, courseId) },
                    onAddTerm = { viewModel.addTerm(yearNum) },
                    onRemoveTerm = { termId -> viewModel.removeTerm(termId) },
                    onRemoveYear = { viewModel.removeYear(yearNum) },
                    canRemoveYear = distinctYears.size > 1
                )
            }

            // Add Year Button
            if (distinctYears.size < 8) {
                OutlinedButton(
                    onClick = { viewModel.addYear() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .pointerHoverIcon(PointerIcon.Hand),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "新增第 ${distinctYears.size + 1} 學年 (Add Year ${distinctYears.size + 1})")
                }
            }

            Spacer(modifier = Modifier.height(140.dp))
        }
    }

    // Reset Confirmation Dialog
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("重設所有成績？") },
            text = { Text("這將清除您輸入的所有學期、課程和成績紀錄，並回復為初始狀態。此操作無法復原。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.resetAll()
                        showResetDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("確定重設 Reset All")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("取消 Cancel")
                }
            }
        )
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
    val isDark = isAppDarkTheme()
    val cardBg = if (isDark) md_theme_dark_surfaceContainer else Color.White
    val cardBorder = if (isDark) md_theme_dark_outlineVariant else Color(0xFFE2E8F0)
    val textMuted = if (isDark) md_theme_dark_onSurfaceVariant else Color(0xFF64748B)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = androidx.compose.foundation.BorderStroke(1.dp, cardBorder),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Year Header
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
                        text = "第 $year 學年 Year $year",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    if (yearStat?.yearGpa != null) {
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "年 GPA: ${String.format("%.3f", yearStat.yearGpa)}",
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    if (yearStat?.award != null) {
                        Surface(
                            color = Color(0xFFF59E0B),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = yearStat.award.titleZh,
                                color = Color.Black,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                if (canRemoveYear) {
                    IconButton(
                        onClick = onRemoveYear,
                        modifier = Modifier
                            .size(28.dp)
                            .pointerHoverIcon(PointerIcon.Hand)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Remove Year",
                            tint = textMuted,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Terms in this Year
            terms.forEach { term ->
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
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Add Term Button (if < 3 terms in this year)
            if (terms.size < 3) {
                TextButton(
                    onClick = onAddTerm,
                    modifier = Modifier.pointerHoverIcon(PointerIcon.Hand)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "新增此學年學期 (+ Add Term)")
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
    val isDark = isAppDarkTheme()
    val sectionBg = if (isDark) md_theme_dark_surfaceContainerLow else Color(0xFFF1F5F9)
    val textMuted = if (isDark) md_theme_dark_onSurfaceVariant else Color(0xFF64748B)

    Surface(
        color = sectionBg,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Term Title Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "${term.part.titleZh} ${term.part.titleEn}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "(${term.courses.size}/${term.part.maxCourses} 門課)",
                        style = MaterialTheme.typography.labelSmall,
                        color = textMuted
                    )
                }

                if (canRemoveTerm) {
                    IconButton(
                        onClick = onRemoveTerm,
                        modifier = Modifier
                            .size(24.dp)
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

            Spacer(modifier = Modifier.height(8.dp))

            // Courses List
            term.courses.forEach { course ->
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
                Spacer(modifier = Modifier.height(6.dp))
            }

            // Add Course Button
            if (term.courses.size < term.part.maxCourses) {
                TextButton(
                    onClick = onAddCourse,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    modifier = Modifier.pointerHoverIcon(PointerIcon.Hand)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "新增課程 Add Course", fontSize = 12.sp)
                }
            }
        }
    }
}

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
    val isDark = isAppDarkTheme()
    val rowBg = if (isDark) md_theme_dark_surfaceContainer else Color.White
    val cardBorder = if (isDark) md_theme_dark_outlineVariant else Color(0xFFE2E8F0)
    val textMuted = if (isDark) md_theme_dark_onSurfaceVariant else Color(0xFF64748B)

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

    Surface(
        color = rowBg,
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, cardBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Course Code Input with Suggestions Dropdown
                Box(modifier = Modifier.weight(1.5f)) {
                    OutlinedTextField(
                        value = course.code,
                        onValueChange = {
                            onUpdateCode(it)
                            showSuggestions = it.trim().length >= 2
                        },
                        placeholder = { Text("課程代號 e.g. BUS1102", fontSize = 12.sp) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        shape = RoundedCornerShape(8.dp)
                    )

                    DropdownMenu(
                        expanded = showSuggestions && matchingCourses.isNotEmpty(),
                        onDismissRequest = { showSuggestions = false }
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

                // Credits Selector Dropdown Trigger
                Box {
                    OutlinedButton(
                        onClick = { showCreditsMenu = true },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                        modifier = Modifier.pointerHoverIcon(PointerIcon.Hand)
                    ) {
                        Text(text = "${course.credits} cr", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    DropdownMenu(
                        expanded = showCreditsMenu,
                        onDismissRequest = { showCreditsMenu = false }
                    ) {
                        listOf("0", "1", "3", "6").forEach { cr ->
                            DropdownMenuItem(
                                text = { Text("$cr 學分 (Credits)", fontWeight = if (course.credits == cr) FontWeight.Bold else FontWeight.Normal) },
                                onClick = {
                                    onUpdateCredits(cr)
                                    showCreditsMenu = false
                                }
                            )
                        }
                    }
                }

                // Grade Selector Dropdown Trigger
                Box {
                    val isBearing = HonoursCalculator.isGpaBearing(course.grade)
                    val gp = HonoursCalculator.getGradePoint(course.grade)
                    val displayGrade = if (course.grade.isNotBlank()) {
                        if (isBearing && gp != null) "${course.grade} (${String.format("%.2f", gp)})"
                        else course.grade
                    } else "成績 Grade"

                    Button(
                        onClick = { showGradeMenu = true },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (course.grade.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (course.grade.isNotBlank()) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                        modifier = Modifier.pointerHoverIcon(PointerIcon.Hand)
                    ) {
                        Text(
                            text = displayGrade,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }

                    DropdownMenu(
                        expanded = showGradeMenu,
                        onDismissRequest = { showGradeMenu = false }
                    ) {
                        Text(
                            text = "計入 GPA 之成績",
                            style = MaterialTheme.typography.labelSmall,
                            color = textMuted,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                        HonoursCalculator.GPA_BEARING_GRADES.forEach { g ->
                            val pt = HonoursCalculator.getGradePoint(g) ?: 0.0
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(text = g, fontWeight = FontWeight.Bold)
                                        Text(text = String.format("%.2f", pt), color = textMuted)
                                    }
                                },
                                onClick = {
                                    onUpdateGrade(g)
                                    showGradeMenu = false
                                }
                            )
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                        Text(
                            text = "不計入 GPA 之成績",
                            style = MaterialTheme.typography.labelSmall,
                            color = textMuted,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                        HonoursCalculator.NON_GPA_GRADES.forEach { g ->
                            DropdownMenuItem(
                                text = { Text(text = g, fontWeight = FontWeight.Medium) },
                                onClick = {
                                    onUpdateGrade(g)
                                    showGradeMenu = false
                                }
                            )
                        }
                    }
                }

                // Delete Course Button
                if (canRemove) {
                    IconButton(
                        onClick = onRemove,
                        modifier = Modifier
                            .size(28.dp)
                            .pointerHoverIcon(PointerIcon.Hand)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Remove Course",
                            tint = textMuted,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Resolved Title Display (if present)
            if (course.title.isNotBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = course.title,
                    style = MaterialTheme.typography.labelSmall,
                    color = textMuted,
                    maxLines = 1,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
        }
    }
}
