package com.lingubible.app.ui.components

import android.content.res.Configuration
import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lingubible.app.core.theme.*
import com.lingubible.app.domain.model.TimetableSection

/**
 * Course Category / Department metadata with bilingual titles.
 */
data class CourseCategory(
    val code: String,
    val nameZh: String,
    val nameEn: String
)

val COURSE_CATEGORIES = listOf(
    CourseCategory("ALL", "全部", "All Departments"),
    CourseCategory("ACT", "會計", "Accountancy"),
    CourseCategory("BUS", "商學", "Business Admin"),
    CourseCategory("CDS", "數據科學", "Data Science"),
    CourseCategory("CHI", "中文", "Chinese"),
    CourseCategory("ECO", "經濟", "Economics"),
    CourseCategory("ENG", "英文", "English"),
    CourseCategory("FIN", "金融", "Finance"),
    CourseCategory("GOV", "政府政治", "Gov & Int Affairs"),
    CourseCategory("HST", "歷史", "History"),
    CourseCategory("MGT", "管理", "Management"),
    CourseCategory("MKT", "市場", "Marketing"),
    CourseCategory("PHI", "哲學", "Philosophy"),
    CourseCategory("POL", "政治", "Political Sci"),
    CourseCategory("PSY", "心理", "Psychology"),
    CourseCategory("SCI", "科學", "Science"),
    CourseCategory("SOC", "社會", "Sociology"),
    CourseCategory("TRA", "翻譯", "Translation")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseSectionSelector(
    sections: List<TimetableSection>,
    selectedSectionIds: Set<String>,
    selectedSections: List<TimetableSection>,
    searchQuery: String,
    selectedDepartment: String,
    onSearchQueryChange: (String) -> Unit,
    onDepartmentSelect: (String) -> Unit,
    onToggleSection: (TimetableSection) -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isAppDarkTheme()
    val cardBg = if (isDark) md_theme_dark_surfaceContainer else Color.White
    val cardBorder = if (isDark) md_theme_dark_outlineVariant else Color(0xFFE2E8F0)
    val textMuted = if (isDark) md_theme_dark_onSurfaceVariant else Color(0xFF64748B)

    var isGridExpanded by remember { mutableStateOf(false) }
    var headerHeightPx by remember { mutableIntStateOf(0) }
    val density = LocalDensity.current
    val headerHeightDp = with(density) { headerHeightPx.toDp() }

    Box(modifier = modifier.fillMaxSize()) {
        // Results List - scrolling smoothly beneath the Frosted Glass Header
        if (sections.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        top = if (headerHeightDp > 0.dp) headerHeightDp + 16.dp else 120.dp,
                        start = 16.dp,
                        end = 16.dp
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "沒有找到匹配的課程 No matching courses found",
                    color = textMuted,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = if (headerHeightDp > 0.dp) headerHeightDp + 10.dp else 130.dp,
                    bottom = 140.dp
                ),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(sections, key = { it.id }) { section ->
                    val isSelected = selectedSectionIds.contains(section.id)

                    // Check for potential conflict if not yet selected
                    val potentialConflict = remember(selectedSections, section) {
                        if (isSelected) null
                        else {
                            selectedSections.firstNotNullOfOrNull { alreadySelected ->
                                val conflicts = section.conflictsWith(alreadySelected)
                                if (conflicts.isNotEmpty()) alreadySelected else null
                            }
                        }
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else cardBorder,
                                shape = RoundedCornerShape(14.dp)
                            ),
                        colors = CardDefaults.cardColors(containerColor = cardBg),
                        shape = RoundedCornerShape(14.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 3.dp else 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Surface(
                                            color = try {
                                                Color(android.graphics.Color.parseColor(section.colorHex)).copy(alpha = 0.15f)
                                            } catch (e: Exception) { MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) },
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text(
                                                text = section.courseCode,
                                                color = try {
                                                    Color(android.graphics.Color.parseColor(section.colorHex))
                                                } catch (e: Exception) { MaterialTheme.colorScheme.primary },
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 14.sp,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(6.dp))

                                        Text(
                                            text = "Sect ${section.section}",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = textMuted
                                        )

                                        if (section.crn.isNotBlank()) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "CRN ${section.crn}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = textMuted.copy(alpha = 0.8f)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Text(
                                        text = section.courseTitle,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                // Toggle Action Button
                                Button(
                                    onClick = { onToggleSection(section) },
                                    colors = if (isSelected) {
                                        ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFF059669),
                                            contentColor = Color.White
                                        )
                                    } else {
                                        ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.primary,
                                            contentColor = MaterialTheme.colorScheme.onPrimary
                                        )
                                    },
                                    shape = RoundedCornerShape(18.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                    modifier = Modifier.pointerHoverIcon(PointerIcon.Hand)
                                ) {
                                    Icon(
                                        imageVector = if (isSelected) Icons.Default.Check else Icons.Default.Add,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (isSelected) "已加選" else "加選",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Meetings badges
                            section.meetings.forEach { m ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(vertical = 2.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AccessTime,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp),
                                        tint = textMuted
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${m.day} ${m.start} - ${m.end}",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Medium,
                                        color = if (isDark) Color(0xFFCBD5E1) else Color(0xFF334155)
                                    )

                                    if (m.venue.isNotBlank()) {
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Icon(
                                            imageVector = Icons.Default.Place,
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp),
                                            tint = textMuted
                                        )
                                        Spacer(modifier = Modifier.width(2.dp))
                                        Text(
                                            text = m.venue,
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.SemiBold,
                                            color = textMuted
                                        )
                                    }
                                }
                            }

                            if (section.instructors.isNotEmpty()) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(top = 4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp),
                                        tint = textMuted
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = section.instructors.joinToString(", "),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = textMuted
                                    )
                                }
                            }

                            // Material 3 Expressive Conflict warning chip
                            if (potentialConflict != null) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Surface(
                                    color = if (isDark) Color(0xFF451A03).copy(alpha = 0.75f) else Color(0xFFFEF3C7),
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(
                                        1.dp,
                                        if (isDark) Color(0xFFD97706).copy(alpha = 0.65f) else Color(0xFFF59E0B).copy(alpha = 0.65f)
                                    )
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(20.dp)
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(if (isDark) Color(0xFF78350F) else Color(0xFFFDE68A)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Warning,
                                                contentDescription = "Conflict",
                                                tint = if (isDark) Color(0xFFFBBF24) else Color(0xFFD97706),
                                                modifier = Modifier.size(13.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "時間衝突：與已選 ${potentialConflict.courseCode} (S${potentialConflict.section}) 時段重疊",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (isDark) Color(0xFFFDE68A) else Color(0xFF92400E),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Pinned Frosted Glass Header: Search Toolbar & Expandable Category Selection Grid
        FrostedGlassHeader(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .onGloballyPositioned { coordinates ->
                    headerHeightPx = coordinates.size.height
                },
            borderBottomOnly = true
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                // Search Input with status bar insets protection
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    placeholder = { Text("搜尋課程代碼、名稱、講師或 CRN...") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotBlank()) {
                            IconButton(onClick = { onSearchQueryChange("") }) {
                                Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = cardBorder,
                        focusedContainerColor = if (isDark) md_theme_dark_surfaceContainerHigh.copy(alpha = 0.50f) else Color.White.copy(alpha = 0.70f),
                        unfocusedContainerColor = if (isDark) md_theme_dark_surfaceContainer.copy(alpha = 0.40f) else Color.White.copy(alpha = 0.50f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Compact Quick-Select Bar with Grid Expand Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Quick-select button group
                    val deptItems = remember(COURSE_CATEGORIES, selectedDepartment) {
                        COURSE_CATEGORIES.map { cat ->
                            M3ButtonGroupItem(
                                label = "${cat.nameZh} ${cat.code}",
                                onClick = { onDepartmentSelect(cat.code) }
                            )
                        }
                    }
                    val selectedDeptIndex = remember(COURSE_CATEGORIES, selectedDepartment) {
                        COURSE_CATEGORIES.indexOfFirst { it.code.equals(selectedDepartment, ignoreCase = true) }.coerceAtLeast(0)
                    }

                    Box(modifier = Modifier.weight(1f)) {
                        ScrollableM3ButtonGroup(
                            selectedIndex = selectedDeptIndex,
                            items = deptItems,
                            contentPadding = PaddingValues(horizontal = 2.dp, vertical = 2.dp),
                            modifier = Modifier.fillMaxWidth(),
                            height = 36.dp,
                            spacing = 5.dp
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Expand / Collapse Grid Toggle Button
                    val chevronRotation by animateFloatAsState(
                        targetValue = if (isGridExpanded) 180f else 0f,
                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                        label = "gridChevronRotation"
                    )

                    Surface(
                        onClick = { isGridExpanded = !isGridExpanded },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isGridExpanded) {
                            if (isDark) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                        } else {
                            if (isDark) md_theme_dark_surfaceContainerHigh else Color(0xFFF1F5F9)
                        },
                        border = BorderStroke(
                            1.dp,
                            if (isGridExpanded) MaterialTheme.colorScheme.primary.copy(alpha = 0.6f) else cardBorder
                        ),
                        modifier = Modifier
                            .height(36.dp)
                            .semantics {
                                stateDescription = if (isGridExpanded) "已展開 Expanded" else "已收起 Collapsed"
                                contentDescription = if (isGridExpanded) "收起學系分類網格 Collapse Category Grid" else "展開學系分類網格 Expand Category Grid"
                                role = Role.Button
                            }
                            .pointerHoverIcon(PointerIcon.Hand)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        ) {
                            Icon(
                                imageVector = if (isGridExpanded) Icons.Default.GridView else Icons.Outlined.GridView,
                                contentDescription = null,
                                tint = if (isGridExpanded) MaterialTheme.colorScheme.primary else (if (isDark) Color.White else Color(0xFF334155)),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                tint = if (isGridExpanded) MaterialTheme.colorScheme.primary else textMuted,
                                modifier = Modifier
                                    .size(16.dp)
                                    .rotate(chevronRotation)
                            )
                        }
                    }
                }

                // In-Line Expandable Multi-Column Category Selection Grid
                AnimatedVisibility(
                    visible = isGridExpanded,
                    enter = expandVertically(
                        expandFrom = Alignment.Top,
                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                    ) + fadeIn(
                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                    ),
                    exit = shrinkVertically(
                        shrinkTowards = Alignment.Top,
                        animationSpec = spring(stiffness = Spring.StiffnessMedium)
                    ) + fadeOut(
                        animationSpec = spring(stiffness = Spring.StiffnessMedium)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp, bottom = 4.dp)
                    ) {
                        // Header Bar of the Expanded Grid
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 2.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "學系分類篩選",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDark) Color.White else Color(0xFF0F172A)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = if (isDark) 0.5f else 0.35f)
                                ) {
                                    val currentCat = COURSE_CATEGORIES.find { it.code.equals(selectedDepartment, ignoreCase = true) }
                                    Text(
                                        text = "${currentCat?.nameZh ?: "全部"} (${selectedDepartment})",
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.labelSmall,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            TextButton(
                                onClick = { isGridExpanded = false },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                modifier = Modifier
                                    .pointerHoverIcon(PointerIcon.Hand)
                                    .semantics {
                                        contentDescription = "收起學系分類網格 Collapse Category Grid"
                                    }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowUp,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = textMuted
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    text = "收起",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = textMuted,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Scrollable 3-Column Grid of Category Chips with max-height bounds for landscape and small viewports
                        val configuration = LocalConfiguration.current
                        val maxGridHeight = if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) 120.dp else 190.dp
                        val gridScrollState = rememberScrollState()

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = maxGridHeight)
                                .verticalScroll(gridScrollState)
                        ) {
                            val chunkedCategories = remember { COURSE_CATEGORIES.chunked(3) }
                            chunkedCategories.forEach { rowCategories ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 3.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    rowCategories.forEach { category ->
                                        CategoryGridChip(
                                            category = category,
                                            isSelected = selectedDepartment.equals(category.code, ignoreCase = true),
                                            onClick = {
                                                onDepartmentSelect(category.code)
                                            },
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                    repeat(3 - rowCategories.size) {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Material 3 Expressive Category Chip in the Expandable Selection Grid.
 */
@Composable
private fun CategoryGridChip(
    category: CourseCategory,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isAppDarkTheme()
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1.0f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "chipPressScale"
    )

    val chipShape = RoundedCornerShape(10.dp)

    Surface(
        onClick = onClick,
        interactionSource = interactionSource,
        shape = chipShape,
        color = if (isSelected) {
            MaterialTheme.colorScheme.primary
        } else {
            if (isDark) MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.70f)
            else MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.65f)
        },
        border = BorderStroke(
            width = if (isSelected) 1.5.dp else 1.dp,
            color = if (isSelected) {
                if (isDark) Color.White.copy(alpha = 0.40f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
            } else {
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = if (isDark) 0.45f else 0.70f)
            }
        ),
        shadowElevation = if (isSelected) 3.dp else 0.dp,
        modifier = modifier
            .scale(scale)
            .semantics {
                selected = isSelected
                role = Role.RadioButton
                stateDescription = if (isSelected) "已選取 Selected" else "未選取 Not Selected"
            }
            .pointerHoverIcon(PointerIcon.Hand)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 5.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = category.code,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isSelected) Color.White else (if (isDark) Color.White else Color(0xFF0F172A))
                    )
                    Text(
                        text = category.nameZh,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) Color.White.copy(alpha = 0.95f) else (if (isDark) Color(0xFFCBD5E1) else Color(0xFF475569)),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Selected",
                        tint = Color.White,
                        modifier = Modifier.size(13.dp)
                    )
                }
            }

            Text(
                text = category.nameEn,
                fontSize = 9.sp,
                fontWeight = FontWeight.Normal,
                color = if (isSelected) Color.White.copy(alpha = 0.80f) else (if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
