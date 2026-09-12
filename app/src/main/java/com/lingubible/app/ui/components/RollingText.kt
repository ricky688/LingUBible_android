package com.lingubible.app.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun RollingText(
    texts: List<String>,
    modifier: Modifier = Modifier,
    intervalMillis: Long = 2000L
) {
    if (texts.isEmpty()) return

    var currentIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(texts, intervalMillis) {
        while (true) {
            delay(intervalMillis)
            currentIndex = (currentIndex + 1) % texts.size
        }
    }

    Box(
        modifier = modifier.wrapContentSize(),
        contentAlignment = Alignment.CenterStart
    ) {
        AnimatedContent(
            targetState = currentIndex,
            transitionSpec = {
                (slideInVertically { height -> height } + fadeIn()) togetherWith
                        (slideOutVertically { height -> -height } + fadeOut())
            },
            label = "RollingTextAnimation"
        ) { targetIndex ->
            Text(
                text = texts[targetIndex],
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                fontSize = 17.sp
            )
        }
    }
}
