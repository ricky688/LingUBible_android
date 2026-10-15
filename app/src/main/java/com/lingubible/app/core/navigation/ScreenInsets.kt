package com.lingubible.app.core.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Root owns top/cutout/IME insets. Destinations own bottom clearance so lists
 * can still draw and scroll behind the translucent navigation bar. */
val LocalScreenBottomClearance = compositionLocalOf { 0.dp }

@Composable
fun screenContentPadding(
    start: Dp = 0.dp,
    top: Dp = 0.dp,
    end: Dp = 0.dp,
    bottom: Dp = 16.dp
): PaddingValues = PaddingValues(start, top, end, bottom + LocalScreenBottomClearance.current)

/** For fixed viewports, or after verticalScroll for scrollable forms. */
fun Modifier.screenBottomPadding(extra: Dp = 16.dp): Modifier = composed {
    val padding = PaddingValues(bottom = LocalScreenBottomClearance.current + extra)
    padding(padding).consumeWindowInsets(padding)
}
