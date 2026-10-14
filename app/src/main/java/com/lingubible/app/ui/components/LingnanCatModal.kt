package com.lingubible.app.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.lingubible.app.core.theme.TactileMotionTokens
import com.lingubible.app.domain.model.LingnanCat
import com.lingubible.app.domain.model.LingnanCatRepository
import com.lingubible.app.ui.common.hapticClick
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

private data class FloatingHeart(
    val id: Long,
    val offsetX: Float,
    val text: String
)

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun LingnanCatModal(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()
    val cats = LingnanCatRepository.CATS
    var selectedCat by remember { mutableStateOf(cats[0]) }

    // Quote cycling
    var quoteIndex by remember { mutableIntStateOf(0) }
    val currentQuote = selectedCat.quotes[quoteIndex % selectedCat.quotes.size]

    // Petting interactions & bounce
    var petCount by remember { mutableIntStateOf(0) }
    val catScale = remember { Animatable(1f) }
    val catRotation = remember { Animatable(0f) }

    // Floating hearts & emojis on petting
    val floatingHearts = remember { mutableStateListOf<FloatingHeart>() }

    fun petCat() {
        haptic.performHapticFeedback(TactileMotionTokens.hapticTap)
        petCount++
        // Cycle quote randomly or progressively
        quoteIndex = (quoteIndex + 1) % selectedCat.quotes.size

        // Add a floating heart/sparkle
        val emojiPool = listOf("❤️", "🐾", "✨", "🐟", "😽", "💖")
        val newHeart = FloatingHeart(
            id = System.currentTimeMillis() + Random.nextLong(1000),
            offsetX = Random.nextFloat() * 120f - 60f,
            text = emojiPool.random()
        )
        floatingHearts.add(newHeart)

        coroutineScope.launch {
            // Spring bounce scale
            launch {
                catScale.animateTo(1.22f, spring(stiffness = Spring.StiffnessHigh))
                catScale.animateTo(1f, spring(stiffness = Spring.StiffnessMediumLow))
            }
            // Spring playful wiggle
            launch {
                catRotation.animateTo(8f, tween(60, easing = FastOutSlowInEasing))
                catRotation.animateTo(-8f, tween(60, easing = FastOutSlowInEasing))
                catRotation.animateTo(0f, spring(stiffness = Spring.StiffnessMediumLow))
            }
            delay(1200)
            floatingHearts.remove(newHeart)
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = modifier
                .fillMaxWidth(0.92f)
                .widthIn(max = 440.dp)
                .padding(vertical = 24.dp),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            border = BorderStroke(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                    )
                )
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // ── Top Header Row with Close Icon ──
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                            shape = CircleShape,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Pets,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Column {
                            Text(
                                text = "嶺大校園貓咪 · 屯門特產",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Lingnan Campus Cats & Tuen Mun Vibes",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(onClick = hapticClick(onDismiss)) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "關閉 Close",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // ── Cat Selector Filter Chips ──
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 2.dp)
                ) {
                    items(cats) { cat ->
                        val isSelected = cat.id == selectedCat.id
                        FilterChip(
                            selected = isSelected,
                            onClick = hapticClick {
                                selectedCat = cat
                                quoteIndex = 0
                            },
                            label = {
                                Text(
                                    text = "${cat.emoji} ${cat.nameZh.substringBefore("（")}",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            shape = RoundedCornerShape(14.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // ── Interactive Cat Avatar Stage ──
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .clickable(
                            indication = null,
                            interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                            onClick = { petCat() }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    // Atmospheric subtle background halo
                    Surface(
                        modifier = Modifier
                            .size(130.dp)
                            .scale(catScale.value * 0.95f),
                        shape = CircleShape,
                        color = selectedCat.primaryColor.copy(alpha = 0.16f)
                    ) {}

                    Surface(
                        modifier = Modifier
                            .size(105.dp)
                            .scale(catScale.value)
                            .rotate(catRotation.value),
                        shape = CircleShape,
                        color = selectedCat.primaryColor.copy(alpha = 0.28f),
                        border = BorderStroke(2.dp, selectedCat.primaryColor.copy(alpha = 0.8f))
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = selectedCat.emoji,
                                fontSize = 54.sp
                            )
                        }
                    }

                    // Floating Hearts / Paws
                    floatingHearts.forEach { heart ->
                        Text(
                            text = heart.text,
                            fontSize = 26.sp,
                            modifier = Modifier.offset { IntOffset(heart.offsetX.toInt(), -70) }
                        )
                    }
                }

                Text(
                    text = selectedCat.nameZh,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "${selectedCat.roleZh} · ${selectedCat.locationZh}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 2.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // ── Cantonese Speech Bubble ──
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "喵語日常 · 屯門專屬溫度",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        AnimatedContent(
                            targetState = currentQuote,
                            transitionSpec = {
                                (fadeIn(spring(stiffness = Spring.StiffnessMediumLow)) +
                                    slideInVertically(spring(stiffness = Spring.StiffnessMediumLow)) { it / 2 }) togetherWith
                                    (fadeOut(spring(stiffness = Spring.StiffnessMedium)) +
                                        slideOutVertically(spring(stiffness = Spring.StiffnessMedium)) { -it / 2 })
                            },
                            label = "catSpeechQuote"
                        ) { quote ->
                            Text(
                                text = "「$quote」",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface,
                                lineHeight = 24.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // ── Petting Counter & Title Badge ──
                val petTitle = when {
                    petCount >= 15 -> "🌟 嶺大榮譽貓舍舍監 · 屯門牛尊榮之友"
                    petCount >= 8 -> "🐾 嶺大認證資深鏟屎官"
                    petCount >= 3 -> "❤️ 貓咪心腹好友"
                    petCount >= 1 -> "😸 初次摸摸成功"
                    else -> "👉 點擊下方按鈕或貓咪進行摸摸"
                }

                Surface(
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = petTitle,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "摸摸次數: $petCount",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // ── Action Buttons ──
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { petCat() },
                        modifier = Modifier.weight(1.3f).height(48.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "摸摸貓咪 🐾",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    OutlinedButton(
                        onClick = hapticClick {
                            quoteIndex = (quoteIndex + 1) % selectedCat.quotes.size
                        },
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "換句喵語",
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // ── Tuen Mun Cultural Trivia & Campus Welfare Note ──
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.5f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "🐱 嶺大常識 · ${selectedCat.nameZh.substringBefore("（")}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = selectedCat.tuenMunTrivia,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 18.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "💡 貼心提醒：請勿隨便餵食人類食物，愛護校園貓咪，溫暖整個屯門！",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }
    }
}
