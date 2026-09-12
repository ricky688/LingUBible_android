package com.lingubible.app.ui.screens

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lingubible.app.core.theme.*
import com.lingubible.app.ui.common.mouseScrollbar
import com.lingubible.app.ui.components.M3ButtonGroupItem
import com.lingubible.app.ui.components.M3LoadingIndicator
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
    val subjects = listOf(
        "ALL" to "全部 ALL",
        "CLC" to "核心 CLC",
        "BUS" to "商學 BUS",
        "ACT" to "會計 ACT",
        "ENG" to "英文 ENG",
        "CHI" to "中文 CHI",
        "FIN" to "金融 FIN",
        "MKT" to "市場 MKT",
        "ECO" to "經濟 ECO",
        "HST" to "歷史 HST",
        "PHI" to "哲學 PHI",
        "PSY" to "心理 PSY",
        "SOC" to "社會 SOC"
    )
    val favoriteCourses = remember { mutableStateListOf<String>() }
    val listState = rememberLazyListState()

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
                placeholder = { Text("搜尋課程代碼或名稱...") },
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

            Spacer(modifier = Modifier.height(12.dp))

            // Subject Filter Connected Button Group
            val subjectItems = remember(subjects, uiState.selectedSubject) {
                subjects.map { (code, label) ->
                    M3ButtonGroupItem(
                        label = label,
                        onClick = {
                            if (code == "ALL") viewModel.filterSubject(null)
                            else viewModel.filterSubject(code)
                        }
                    )
                }
            }
            val selectedSubjectIndex = remember(subjects, uiState.selectedSubject) {
                if (uiState.selectedSubject == null) 0
                else subjects.indexOfFirst { it.first == uiState.selectedSubject }.coerceAtLeast(0)
            }

            ScrollableM3ButtonGroup(
                selectedIndex = selectedSubjectIndex,
                items = subjectItems,
                modifier = Modifier.fillMaxWidth(),
                height = 36.dp,
                spacing = 5.dp
            )

            Spacer(modifier = Modifier.height(14.dp))

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
                    items(uiState.courses) { course ->
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
