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
import com.lingubible.app.ui.components.PopularInstructorCard
import com.lingubible.app.ui.components.ScrollableM3ButtonGroup
import com.lingubible.app.ui.viewmodels.InstructorsViewModel
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InstructorsScreen(
    onNavigateToInstructor: (String) -> Unit,
    viewModel: InstructorsViewModel = koinViewModel()
) {
    val isDark = isAppDarkTheme()
    val uiState by viewModel.uiState.collectAsState()
    var searchInput by remember { mutableStateOf("") }
    val departments = listOf(
        "ALL" to "全部 ALL",
        "CHI" to "中文 CHI",
        "ENG" to "英文 ENG",
        "BUS" to "商學 BUS",
        "ACCT" to "會計 ACCT",
        "FIN" to "金融 FIN",
        "MKT" to "市場 MKT",
        "ECON" to "經濟 ECON",
        "PHILO" to "哲學 PHILO",
        "SOCSP" to "社會 SOCSP",
        "CS" to "計算機 CS"
    )
    val favoriteInstructors = remember { mutableStateListOf<String>() }
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
                placeholder = { Text("搜尋講師姓名...") },
                leadingIcon = {
                    Icon(
                        Icons.Filled.Search,
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                trailingIcon = {
                    if (searchInput.isNotEmpty()) {
                        IconButton(onClick = {
                            searchInput = ""
                            viewModel.search("")
                        }) {
                            Icon(
                                Icons.Filled.Clear,
                                contentDescription = "Clear",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
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

            // Department Filter Connected Button Group
            val departmentItems = remember(departments, uiState.selectedDepartment) {
                departments.map { (code, label) ->
                    M3ButtonGroupItem(
                        label = label,
                        onClick = {
                            if (code == "ALL") viewModel.filterDepartment(null)
                            else viewModel.filterDepartment(code)
                        }
                    )
                }
            }
            val selectedDepartmentIndex = remember(departments, uiState.selectedDepartment) {
                if (uiState.selectedDepartment == null) 0
                else departments.indexOfFirst { it.first == uiState.selectedDepartment }.coerceAtLeast(0)
            }

            ScrollableM3ButtonGroup(
                selectedIndex = selectedDepartmentIndex,
                items = departmentItems,
                modifier = Modifier.fillMaxWidth(),
                height = 36.dp,
                spacing = 5.dp
            )

            Spacer(modifier = Modifier.height(14.dp))

            // List
            if (uiState.isLoading) {
                M3LoadingState(
                    modifier = Modifier.fillMaxSize(),
                    message = "正在載入講師名錄..."
                )
            } else if (uiState.instructors.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "找不到相關講師",
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
                    items(uiState.instructors) { instructor ->
                        val isFav = favoriteInstructors.contains(instructor.name)
                        PopularInstructorCard(
                            instructor = instructor,
                            isFavorited = isFav,
                            onFavoriteToggle = {
                                if (isFav) favoriteInstructors.remove(instructor.name)
                                else favoriteInstructors.add(instructor.name)
                            },
                            onClick = { onNavigateToInstructor(instructor.name) }
                        )
                    }
            }
        }
    }
}
