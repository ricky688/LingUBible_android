package com.lingubible.app.ui.screens

import android.content.res.Configuration
import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.platform.LocalConfiguration
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
import com.lingubible.app.domain.model.COURSE_CATEGORIES
import com.lingubible.app.domain.model.CourseCategory
import com.lingubible.app.ui.common.mouseScrollbar
import com.lingubible.app.ui.components.M3ButtonGroupItem
import com.lingubible.app.ui.components.M3LoadingState
import com.lingubible.app.ui.components.PopularCourseCard
import com.lingubible.app.ui.components.ScrollableM3ButtonGroup
import com.lingubible.app.ui.viewmodels.CoursesViewModel
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoursesScreen(
    onNavigateToCourse: (String) -> Unit,
    viewModel: CoursesViewModel = koinViewModel()
) {
    val isDark = isAppDarkTheme()
    val uiState by viewModel.uiState.collectAsState()
    var searchInput by remember { mutableStateOf("") }
    var isGridExpanded by remember { mutableStateOf(false) }
    val favoriteCourses = remember { mutableStateListOf<String>() }
    val listState = rememberLazyListState()

    val cardBorder = if (isDark) md_theme_dark_outlineVariant else Color(0xFFE2E8F0)
    val textMuted = if (isDark) md_theme_dark_onSurfaceVariant else Color(0xFF64748B)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .imePadding()
    ) {
        Spacer(modifier = Modifier.height(6.dp))

        // Search Bar
        OutlinedTextField(
            value = searchInput,
            onValueChange = {
                searchInput = it
                viewModel.search(it)
            },
            placeholder = { Text("搜尋課程代碼、名稱或學系...") },
            leadingIcon = {
                Icon(
                    Icons.Filled.Search,
                    contentDescription = "Search",
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            trailingIcon = {
                if (searchInput.isNotEmpty()) {
                    IconButton(
                        onClick = {
                            searchInput = ""
                            viewModel.search("")
                        },
                        modifier = Modifier.pointerHoverIcon(PointerIcon.Hand)
                    ) {
                        Icon(Icons.Filled.Clear, contentDescription = "Clear")
                    }
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = if (isDark) 0.35f else 0.5f),
                focusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer
            ),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Compact Quick-Select Bar with Grid Expand Toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Quick-select button group
            val subjectItems = remember(COURSE_CATEGORIES, uiState.selectedSubject) {
                COURSE_CATEGORIES.map { cat ->
                    M3ButtonGroupItem(
                        label = "${cat.nameZh} ${cat.code}",
                        onClick = {
                            if (cat.code == "ALL") {
                                viewModel.filterSubject(null)
                            } else {
                                viewModel.filterSubject(cat.code)
                            }
                        }
                    )
                }
            }
            val selectedSubjectIndex = remember(COURSE_CATEGORIES, uiState.selectedSubject) {
                if (uiState.selectedSubject == null) 0
                else COURSE_CATEGORIES.indexOfFirst { it.code.equals(uiState.selectedSubject, ignoreCase = true) }.coerceAtLeast(0)
            }

            Box(modifier = Modifier.weight(1f)) {
                ScrollableM3ButtonGroup(
                    selectedIndex = selectedSubjectIndex,
                    items = subjectItems,
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
                label = "coursesGridChevronRotation"
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
                            val currentCat = COURSE_CATEGORIES.find { it.code.equals(uiState.selectedSubject ?: "ALL", ignoreCase = true) }
                            Text(
                                text = "${currentCat?.nameZh ?: "全部"} (${uiState.selectedSubject ?: "ALL"})",
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

                // Scrollable 3-Column Grid of Category Chips
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
                                val isSelected = if (category.code == "ALL") {
                                    uiState.selectedSubject == null || uiState.selectedSubject.equals("ALL", ignoreCase = true)
                                } else {
                                    uiState.selectedSubject.equals(category.code, ignoreCase = true)
                                }

                                CourseCategoryGridChip(
                                    category = category,
                                    isSelected = isSelected,
                                    onClick = {
                                        if (category.code == "ALL") {
                                            viewModel.filterSubject(null)
                                        } else {
                                            viewModel.filterSubject(category.code)
                                        }
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

        Spacer(modifier = Modifier.height(8.dp))

        // Result count indicator
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val countInfo = if (uiState.isLoading) {
                "載入課程中..."
            } else {
                val base = "共 ${uiState.courses.size} 門課程"
                if (uiState.selectedSubject != null && !uiState.selectedSubject.equals("ALL", ignoreCase = true)) {
                    val catName = COURSE_CATEGORIES.find { it.code.equals(uiState.selectedSubject, ignoreCase = true) }?.nameZh ?: ""
                    "$base · $catName (${uiState.selectedSubject})"
                } else {
                    base
                }
            }

            Text(
                text = countInfo,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = textMuted
            )

            if (uiState.selectedSubject != null && !uiState.selectedSubject.equals("ALL", ignoreCase = true)) {
                TextButton(
                    onClick = { viewModel.filterSubject(null) },
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
                    modifier = Modifier.height(26.dp)
                ) {
                    Text(
                        text = "重設分類 Reset",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Courses List
        if (uiState.isLoading) {
            M3LoadingState(
                modifier = Modifier.fillMaxSize(),
                message = "正在載入課程..."
            )
        } else if (uiState.courses.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "找不到相關課程",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .mouseScrollbar(listState),
                contentPadding = PaddingValues(bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.courses, key = { it.code }) { course ->
                    val isFav = favoriteCourses.contains(course.code)
                    PopularCourseCard(
                        course = course,
                        isFavorited = isFav,
                        onFavoriteToggle = {
                            if (isFav) favoriteCourses.remove(course.code)
                            else favoriteCourses.add(course.code)
                        },
                        onClick = { onNavigateToCourse(course.code) }
                    )
                }
            }
        }
    }
}

/**
 * Material 3 Expressive Category Chip in the Expandable Selection Grid.
 */
@Composable
private fun CourseCategoryGridChip(
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
