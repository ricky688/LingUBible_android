package com.lingubible.app.ui.components

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.LoadingIndicatorDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lingubible.app.core.theme.LingnanRed
import com.lingubible.app.core.theme.md_theme_dark_primary
import com.lingubible.app.core.theme.md_theme_dark_surfaceVariant
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshState
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState

/**
 * Official Material 3 Expressive Loading Indicator styled with Lingnan Red brand color.
 * Features dynamic shape morphing across rounded polygons (scalloped flower, squircle, circle).
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun M3LoadingIndicator(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary
) {
    LoadingIndicator(
        modifier = modifier,
        color = color
    )
}

/**
 * Contained Material 3 Expressive Loading Indicator with a squircle background container.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ContainedM3LoadingIndicator(
    modifier: Modifier = Modifier,
    indicatorColor: Color = MaterialTheme.colorScheme.primary,
    containerColor: Color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
) {
    ContainedLoadingIndicator(
        modifier = modifier,
        indicatorColor = indicatorColor,
        containerColor = containerColor,
        containerShape = RoundedCornerShape(16.dp)
    )
}

/**
 * Full-screen or section-level Loading State with M3 Expressive morphing LoadingIndicator and red accent.
 */
@Composable
fun M3LoadingState(
    modifier: Modifier = Modifier.fillMaxSize(),
    message: String? = null,
    color: Color = MaterialTheme.colorScheme.primary
) {
    Box(
        modifier = modifier.padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            M3LoadingIndicator(
                color = color
            )
            if (message != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Official Material 3 Expressive Pull-to-Refresh Container.
 *
 * Wraps scrollable content with pull-to-refresh functionality powered by
 * Material 3's PullToRefreshBox and the Expressive shape-morphing LoadingIndicator.
 *
 * @param isRefreshing Whether a refresh operation is currently in progress.
 * @param onRefresh Callback invoked when the user pulls past the refresh threshold.
 * @param modifier Modifier applied to the outer PullToRefreshBox.
 * @param state PullToRefreshState controlling the pull gesture and progress.
 * @param indicatorColor Color for the expressive loading indicator (defaults to primary/Lingnan Red).
 * @param containerColor Background color for the indicator capsule/container.
 * @param content The scrollable content (e.g. LazyColumn).
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun M3PullToRefreshBox(
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    state: PullToRefreshState = rememberPullToRefreshState(),
    indicatorColor: Color = MaterialTheme.colorScheme.primary,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainerHigh,
    content: @Composable BoxScope.() -> Unit
) {
    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        modifier = modifier,
        state = state,
        indicator = {
            M3PullToRefreshIndicator(
                state = state,
                isRefreshing = isRefreshing,
                modifier = Modifier.align(Alignment.TopCenter),
                indicatorColor = indicatorColor,
                containerColor = containerColor
            )
        },
        content = content
    )
}

/**
 * Material 3 Expressive Pull-to-Refresh Indicator using the expressive loading indicator.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun BoxScope.M3PullToRefreshIndicator(
    state: PullToRefreshState,
    isRefreshing: Boolean,
    modifier: Modifier = Modifier,
    indicatorColor: Color = MaterialTheme.colorScheme.primary,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainerHigh
) {
    PullToRefreshDefaults.LoadingIndicator(
        state = state,
        isRefreshing = isRefreshing,
        modifier = modifier,
        containerColor = containerColor,
        color = indicatorColor
    )
}

