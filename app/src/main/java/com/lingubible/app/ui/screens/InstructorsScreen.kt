package com.lingubible.app.ui.screens

import com.lingubible.app.ui.components.AppButtonDefaults
import com.lingubible.app.ui.components.FadingLazyColumn
import com.lingubible.app.data.repository.SearchHistoryStore
import com.lingubible.app.ui.components.RecentSearchesButton
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import com.lingubible.app.ui.common.hapticClick

import com.lingubible.app.core.navigation.screenContentPadding
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lingubible.app.ui.components.InstructorFilterSheet
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.lingubible.app.core.theme.*
import com.lingubible.app.ui.common.mouseScrollbar
import com.lingubible.app.ui.components.M3ButtonGroupItem
import com.lingubible.app.ui.components.M3LoadingIndicator
import com.lingubible.app.ui.components.M3LoadingState
import com.lingubible.app.ui.components.M3PullToRefreshBox
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
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val historyContext = LocalContext.current
    val searchHistory = remember(historyContext) { SearchHistoryStore(historyContext) }
    val keyboard = LocalSoftwareKeyboardController.current
    val searchInput = uiState.searchQuery
    var showFilters by remember { mutableStateOf(false) }
    if (showFilters) InstructorFilterSheet(uiState.filters, uiState.allInstructors, uiState.terms,
        onDismiss = { showFilters = false }, onApply = viewModel::applyFilters)
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
                    viewModel.search(it)
                },
                placeholder = { Text("搜尋講師姓名...", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                leadingIcon = {
                    Icon(
                        Icons.Filled.Search,
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                trailingIcon = {
                    Row {
                    RecentSearchesButton(searchHistory, "instructors", viewModel::search)
                    IconButton(shapes = AppButtonDefaults.iconShapes(), onClick = hapticClick { showFilters = true }) { Icon(Icons.Default.Tune, "篩選與排序 Filter and sort instructors") }
                    if (searchInput.isNotEmpty()) {
                        IconButton(shapes = AppButtonDefaults.iconShapes(), onClick = hapticClick {
                            viewModel.search("")
                        }) {
                            Icon(
                                Icons.Filled.Clear,
                                contentDescription = "Clear",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
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
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { searchHistory.add("instructors", searchInput); keyboard?.hide() }),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text("${uiState.instructors.size} / ${uiState.allInstructors.size} 位講師 Instructors", style = MaterialTheme.typography.labelLarge)
            (uiState.errorMessage ?: uiState.optionsError)?.let { message ->
                Text(message, color = MaterialTheme.colorScheme.error)
                TextButton(shapes = AppButtonDefaults.shapes(), onClick = hapticClick(viewModel::loadInstructors)) { Text("重試 Retry") }
            }
            Spacer(Modifier.height(8.dp))

            // List
            if (uiState.isLoading && uiState.instructors.isEmpty()) {
                M3LoadingState(
                    modifier = Modifier.fillMaxSize(),
                    message = "正在載入講師名錄..."
                )
            } else {
                M3PullToRefreshBox(
                    isRefreshing = uiState.isLoading,
                    onRefresh = { viewModel.loadInstructors() },
                    modifier = Modifier.fillMaxSize()
                ) {
                    if (uiState.instructors.isEmpty()) {
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
                        FadingLazyColumn(
                            state = listState,
                            modifier = Modifier
                                .fillMaxSize()
                                .mouseScrollbar(listState),
                            contentPadding = screenContentPadding(),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            itemsIndexed(uiState.instructors, key = { _, it -> it.id }) { index, instructor ->
                                PopularInstructorCard(
                                    instructor = instructor,
                                    groupIndex = index,
                                    groupSize = uiState.instructors.size,

                                    onClick = { searchHistory.add("instructors", searchInput); onNavigateToInstructor(instructor.name) }
                                )
                            }
                        }
                    }
                }
            }
        }
}

