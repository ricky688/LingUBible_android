package com.lingubible.app.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lingubible.app.core.theme.*
import com.lingubible.app.ui.common.mouseScrollbar
import com.lingubible.app.ui.components.ExpressiveInlineReviewButton
import com.lingubible.app.ui.components.ExpressiveReviewFab
import com.lingubible.app.ui.components.FloatingCircles
import com.lingubible.app.ui.components.M3LoadingIndicator
import com.lingubible.app.ui.components.M3LoadingState
import com.lingubible.app.ui.components.ReviewCard
import com.lingubible.app.ui.viewmodels.ReviewsViewModel
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewsScreen(
    onNavigateToCourse: (String) -> Unit,
    onNavigateToWriteReview: (String) -> Unit,
    viewModel: ReviewsViewModel = koinViewModel()
) {
    val isDark = isAppDarkTheme()
    val uiState by viewModel.uiState.collectAsState()

    val listState = rememberLazyListState()
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

        if (uiState.isLoading && uiState.reviews.isEmpty()) {
            M3LoadingState(
                modifier = Modifier.fillMaxSize(),
                message = "正在載入評價資料..."
            )
        } else if (uiState.reviews.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "目前尚無評價記錄",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                ExpressiveInlineReviewButton(
                    onClick = { onNavigateToWriteReview("") },
                    text = "搶先評價 Be First to Review"
                )
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .mouseScrollbar(listState),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(uiState.reviews) { review ->
                    ReviewCard(
                        review = review,
                        onVote = { voteType ->
                            viewModel.voteReview(review.id, voteType)
                        },
                        modifier = Modifier
                            .pointerHoverIcon(PointerIcon.Hand)
                            .clickable { onNavigateToCourse(review.courseCode) }
                    )
                }
            }
        }

        // Expressive Review FAB with scroll-aware spring movement
        val navBarBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
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

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(end = fabEndPadding, bottom = fabBottomPadding),
            contentAlignment = Alignment.BottomEnd
        ) {
            ExpressiveReviewFab(
                onClick = { onNavigateToWriteReview("") },
                expanded = isFabExpanded,
                text = "撰寫評價"
            )
        }
    }
}
