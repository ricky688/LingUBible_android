package com.lingubible.app.ui.components

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.lingubible.app.core.navigation.LocalScreenBottomClearance

/** Main pages draw behind navigation. Sheets and already-inset viewports pass zero. */
@Composable
fun FadingLazyColumn(
    modifier: Modifier = Modifier,
    state: LazyListState = rememberLazyListState(),
    contentPadding: PaddingValues = PaddingValues(0.dp),
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    bottomInset: Dp = LocalScreenBottomClearance.current,
    content: LazyListScope.() -> Unit
) {
    LazyColumn(
        modifier = modifier.fadingBlurEdges(lazyListState = state, bottomInset = bottomInset),
        state = state, contentPadding = contentPadding,
        verticalArrangement = verticalArrangement, horizontalAlignment = horizontalAlignment, content = content
    )
}

fun Modifier.fadingVerticalScroll(state: ScrollState, bottomInset: Dp = 0.dp): Modifier =
    fadingBlurEdges(scrollState = state, bottomInset = bottomInset).verticalScroll(state)
