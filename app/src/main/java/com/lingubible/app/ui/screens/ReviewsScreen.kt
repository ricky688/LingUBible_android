package com.lingubible.app.ui.screens

import com.lingubible.app.ui.common.hapticClick

import com.lingubible.app.core.navigation.screenContentPadding
import com.lingubible.app.core.navigation.screenBottomPadding
import com.lingubible.app.core.navigation.LocalScreenBottomClearance
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity

import androidx.compose.animation.core.*
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lingubible.app.core.theme.*
import com.lingubible.app.ui.common.mouseScrollbar
import com.lingubible.app.ui.components.ExpressiveInlineReviewButton
import com.lingubible.app.ui.components.ExpressiveReviewFab
import com.lingubible.app.ui.components.FloatingCircles
import com.lingubible.app.ui.components.M3LoadingIndicator
import com.lingubible.app.ui.components.M3LoadingState
import com.lingubible.app.ui.components.M3PullToRefreshBox
import com.lingubible.app.ui.components.ReviewCard
import com.lingubible.app.ui.viewmodels.ReviewsViewModel
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewsScreen(
    onNavigateToCourse: (String) -> Unit,
    onNavigateToWriteReview: (String) -> Unit,
    myReviewsOnly: Boolean = false,
    onNavigateToAuth: () -> Unit = {},
    viewModel: ReviewsViewModel = koinViewModel(parameters = { parametersOf(myReviewsOnly) })
) {
    val isDark = isAppDarkTheme()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val listState = rememberLazyListState()
    val density = LocalDensity.current
    var fabHeight by remember { mutableStateOf(0.dp) }
    var isFabExpanded by rememberSaveable { mutableStateOf(true) }
    var previousIndex by remember { mutableIntStateOf(0) }
    var previousScrollOffset by remember { mutableIntStateOf(0) }

    LaunchedEffect(listState.firstVisibleItemIndex, listState.firstVisibleItemScrollOffset) {
        val currentIdx = listState.firstVisibleItemIndex
        val currentOffset = listState.firstVisibleItemScrollOffset
        if (currentIdx == 0 && currentOffset <= 4) {
            isFabExpanded = true
        } else if (currentIdx > previousIndex || (currentIdx == previousIndex && currentOffset > previousScrollOffset + 4)) {
            // Scrolling down -> collapse
            isFabExpanded = false
        } else if (currentIdx < previousIndex || (currentIdx == previousIndex && currentOffset < previousScrollOffset - 4)) {
            // Scrolling up -> expand
            isFabExpanded = true
        }
        previousIndex = currentIdx
        previousScrollOffset = currentOffset
    }

    Box(modifier = Modifier.fillMaxSize()) {
        FloatingCircles(modifier = Modifier.fillMaxSize())

        if (uiState.signInRequired) {
            Column(
                modifier = Modifier.fillMaxSize().screenBottomPadding(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("登入以查看我的評價 · Sign in to view your reviews")
                Spacer(Modifier.height(16.dp))
                Button(onClick = hapticClick(onNavigateToAuth), shapes = ButtonDefaults.shapes()) {
                    Text("登入 · Sign in")
                }
            }
        } else if (uiState.isLoading && uiState.reviews.isEmpty()) {
            M3LoadingState(
                modifier = Modifier.fillMaxSize(),
                message = "正在載入評價資料..."
            )
        } else {
            M3PullToRefreshBox(
                isRefreshing = uiState.isLoading,
                onRefresh = { viewModel.loadReviews() },
                modifier = Modifier.fillMaxSize()
            ) {
                if (uiState.reviews.isEmpty()) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = when {
                                uiState.errorMessage != null -> "載入失敗，請重試 · Could not load reviews"
                                myReviewsOnly -> "你尚未撰寫評價 · You haven’t written any reviews yet"
                                else -> "目前尚無評價記錄 · No reviews yet"
                            },
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        ExpressiveInlineReviewButton(
                            onClick = { if (uiState.errorMessage != null) viewModel.loadReviews() else onNavigateToWriteReview("") },
                            text = if (uiState.errorMessage != null) "重試 · Retry" else "撰寫評價 · Write review"
                        )
                    }
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxSize()
                            .mouseScrollbar(listState),
                        contentPadding = screenContentPadding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = fabHeight + 40.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(uiState.reviews, key = { it.id }) { review ->
                            ReviewCard(
                                review = review,
                                onVote = { voteType ->
                                    viewModel.voteReview(review.id, voteType)
                                },
                                onClick = { onNavigateToCourse(review.courseCode) }
                            )
                        }
                    }
                }
            }
        }


        // Expressive Review FAB with scroll-aware spring movement
        val navBarBottom = LocalScreenBottomClearance.current
        val fabBottomPadding by animateDpAsState(
            targetValue = if (isFabExpanded) (24.dp + navBarBottom) else (20.dp + navBarBottom),
            animationSpec = if (isFabExpanded) {
                spring(stiffness = Spring.StiffnessMedium)
            } else {
                spring(stiffness = Spring.StiffnessHigh)
            },
            label = "fabBottomPadding"
        )
        val fabEndPadding by animateDpAsState(
            targetValue = if (isFabExpanded) 16.dp else 20.dp,
            animationSpec = if (isFabExpanded) {
                spring(stiffness = Spring.StiffnessMedium)
            } else {
                spring(stiffness = Spring.StiffnessHigh)
            },
            label = "fabEndPadding"
        )

        if (!uiState.signInRequired) Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(end = fabEndPadding, bottom = fabBottomPadding),
            contentAlignment = Alignment.BottomEnd
        ) {
            ExpressiveReviewFab(
                onClick = { onNavigateToWriteReview("") },
                expanded = isFabExpanded,
                text = "撰寫評價",
                modifier = Modifier.onSizeChanged { fabHeight = with(density) { it.height.toDp() } }
            )
        }
    }
}
