package com.lingubible.app.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.lingubible.app.domain.calculator.GpaFortune
import com.lingubible.app.domain.calculator.GpaFortuneRepository
import com.lingubible.app.ui.common.hapticClick
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun GpaGodFortuneModal(
    cgpa: Double?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val isFourFlat = cgpa != null && cgpa >= 4.0
    var currentFortune by remember {
        mutableStateOf(GpaFortuneRepository.getRandomFortune(isFourFlat = isFourFlat))
    }
    var shakeCount by remember { mutableIntStateOf(1) }
    var isShaking by remember { mutableStateOf(true) }
    val shakeRotation = remember { Animatable(0f) }

    LaunchedEffect(shakeCount) {
        isShaking = true
        // Oscillate back and forth with lively spring/tween
        launch {
            repeat(4) {
                shakeRotation.animateTo(14f, tween(75, easing = FastOutSlowInEasing))
                shakeRotation.animateTo(-14f, tween(75, easing = FastOutSlowInEasing))
            }
            shakeRotation.animateTo(0f, spring(stiffness = Spring.StiffnessMediumLow))
        }
        delay(650)
        isShaking = false
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
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
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            tonalElevation = 8.dp,
            border = BorderStroke(
                width = 1.5.dp,
                brush = Brush.verticalGradient(
                    listOf(
                        Color(0xFFF59E0B), // Warm Gold
                        Color(0xFFDC2626).copy(alpha = 0.5f), // Temple Red
                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
                    )
                )
            )
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header with close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .background(Color(0xFFDC2626), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("吉", color = Color.White, fontWeight = FontWeight.Black, fontSize = 16.sp)
                        }
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "GPA God 靈籤祈福",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Icon(
                                    Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = Color(0xFFF59E0B),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Text(
                                text = if (isFourFlat) "4.0 滿分神蹟顯靈 · 廣東話解籤" else "過三望四保 Grade 庇佑 · 廣東話解籤",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    IconButton(
                        onClick = hapticClick(onDismiss),
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "關閉",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                AnimatedContent(
                    targetState = isShaking,
                    transitionSpec = {
                        (fadeIn(spring(stiffness = Spring.StiffnessMediumLow)) +
                            scaleIn(spring(stiffness = Spring.StiffnessMediumLow), initialScale = 0.92f)) togetherWith
                            (fadeOut(spring(stiffness = Spring.StiffnessMedium)) +
                                scaleOut(spring(stiffness = Spring.StiffnessMedium), targetScale = 0.92f))
                    },
                    label = "gpaGodShakingContent"
                ) { shaking ->
                    if (shaking) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 36.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "🎋",
                                fontSize = 64.sp,
                                modifier = Modifier.rotate(shakeRotation.value)
                            )
                            Text(
                                text = "誠心祈求 GPA God 中...",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFF59E0B)
                            )
                            Text(
                                text = "搖緊籤筒 · 爛 Grade 退散 · 靚 Curve 降臨",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            // Stamp Badge (傳統廟宇大紅印章)
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFDC2626),
                                border = BorderStroke(1.dp, Color(0xFFEF4444)),
                                shadowElevation = 2.dp
                            ) {
                                Text(
                                    text = currentFortune.grade,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                                )
                            }

                            // Fortune Title
                            Text(
                                text = currentFortune.title,
                                style = MaterialTheme.typography.headlineSmall.copy(fontSize = 20.sp),
                                fontWeight = FontWeight.ExtraBold,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            // Poem Parchment Box (押韻籤詩)
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(18.dp),
                                color = MaterialTheme.colorScheme.surfaceContainerLowest,
                                border = BorderStroke(
                                    width = 1.dp,
                                    brush = Brush.verticalGradient(
                                        listOf(
                                            Color(0xFFF59E0B).copy(alpha = 0.4f),
                                            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
                                        )
                                    )
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    currentFortune.poem.forEach { line ->
                                        Text(
                                            text = line,
                                            style = MaterialTheme.typography.bodyLarge.copy(
                                                fontSize = 16.sp,
                                                letterSpacing = 1.sp
                                            ),
                                            fontWeight = FontWeight.Bold,
                                            textAlign = TextAlign.Center,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }

                            // Interpretation Box (廣東話解曰)
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.surfaceContainerLow
                            ) {
                                Column(
                                    modifier = Modifier.padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = "【解曰】",
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFF59E0B)
                                        )
                                        Text(
                                            text = "嶺南白話指點",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Text(
                                        text = currentFortune.interpretation,
                                        style = MaterialTheme.typography.bodyMedium,
                                        lineHeight = 22.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }

                            // Oracle row (宜 & 忌)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // 宜
                                Surface(
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(14.dp),
                                    color = Color(0xFF10B981).copy(alpha = 0.12f),
                                    border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.35f))
                                ) {
                                    Column(
                                        modifier = Modifier.padding(10.dp),
                                        verticalArrangement = Arrangement.spacedBy(2.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(18.dp)
                                                    .background(Color(0xFF10B981), CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text("宜", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }
                                            Text(
                                                text = "GPA God 建議",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color(0xFF10B981),
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        Text(
                                            text = currentFortune.oracleAppropriate,
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }

                                // 忌
                                Surface(
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(14.dp),
                                    color = Color(0xFFEF4444).copy(alpha = 0.12f),
                                    border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.35f))
                                ) {
                                    Column(
                                        modifier = Modifier.padding(10.dp),
                                        verticalArrangement = Arrangement.spacedBy(2.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(18.dp)
                                                    .background(Color(0xFFEF4444), CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text("忌", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }
                                            Text(
                                                text = "切忌踩雷",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color(0xFFEF4444),
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        Text(
                                            text = currentFortune.oracleInappropriate,
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = hapticClick {
                            currentFortune = GpaFortuneRepository.getRandomFortune(
                                isFourFlat = false,
                                excludeId = currentFortune.id
                            )
                            shakeCount++
                        },
                        modifier = Modifier.weight(1f),
                        enabled = !isShaking,
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Default.Casino, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("再求一籤")
                    }

                    Button(
                        onClick = hapticClick(onDismiss),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFDC2626) // Auspicious Red
                        )
                    ) {
                        Text("謝過 GPA God 🙇‍♂️")
                    }
                }
            }
        }
    }
}
