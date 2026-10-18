package com.lingubible.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import com.lingubible.app.core.navigation.screenContentPadding

/** Reserves the directory's grouped-card layout without creating fake domain objects or actions. */
@Composable
internal fun DirectoryLoadingSkeleton(instructors: Boolean, modifier: Modifier = Modifier) {
    val alpha = rememberSkeletonAlpha(true)
    val color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.22f)
    FadingLazyColumn(
        modifier = modifier.fillMaxSize().clearAndSetSemantics {
            contentDescription = if (instructors) "講師名錄 Instructor directory" else "課程列表 Course directory"
            stateDescription = "載入中 Loading"
        },
        contentPadding = screenContentPadding(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(3, key = { "loading-$it" }) { index ->
            Card(
                shape = getGroupedItemShape(index, 3, outerRadius = 28.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
            ) {
                Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        SkeletonBar(Modifier.weight(1f).height(24.dp), color, alpha)
                        SkeletonBar(Modifier.width(44.dp).height(24.dp), color, alpha)
                    }
                    SkeletonBar(Modifier.fillMaxWidth(0.85f).height(18.dp), color, alpha)
                    SkeletonBar(Modifier.fillMaxWidth(0.6f).height(14.dp), color, alpha)
                    if (!instructors) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            repeat(3) { SkeletonBar(Modifier.weight(1f).height(42.dp), color, alpha) }
                        }
                    }
                    SkeletonBar(Modifier.fillMaxWidth(0.4f).height(18.dp), color, alpha)
                }
            }
        }
    }
}

@Composable
private fun SkeletonBar(modifier: Modifier, color: Color, alpha: State<Float>) {
    Box(modifier.drawWithContent {
        drawRoundRect(color = color, cornerRadius = CornerRadius(6.dp.toPx()), alpha = alpha.value)
    })
}
