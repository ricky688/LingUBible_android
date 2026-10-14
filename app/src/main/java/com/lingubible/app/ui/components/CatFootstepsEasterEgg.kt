package com.lingubible.app.ui.components


import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import java.util.Random
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * 🐱 嶺南「校園貓咪 / 屯門特產」文化彩蛋
 *
 * Represents a single cat paw print along the walking path.
 */
data class CatStep(
    val id: Long,
    val x: Float,
    val y: Float,
    val angle: Float,
    val isLeftPaw: Boolean,
    val spawnTime: Long,
    var isTapped: Boolean = false
)

/**
 * Particle spawned when an active cat paw is playfully tapped.
 */
data class CatParticle(
    val id: Long,
    val startX: Float,
    val startY: Float,
    val vx: Float,
    val vy: Float,
    val symbol: String,
    val color: Color,
    val spawnTime: Long
)

/**
 * Pure math helper for generating a natural cat walking path across the screen.
 */
object CatPathGenerator {
    private val random = Random()

    fun generateSteps(
        width: Float,
        height: Float,
        startTime: Long = System.currentTimeMillis(),
        stepIntervalMs: Long = 380L,
        strideDistance: Float = 110f,
        lateralOffset: Float = 26f
    ): List<CatStep> {
        if (width <= 0f || height <= 0f) return emptyList()

        // 1. Random start and end edges
        // 0: Left, 1: Bottom, 2: Right, 3: Top
        val startSide = random.nextInt(4)
        val endSide = (startSide + 1 + random.nextInt(3)) % 4

        val start = getEdgePoint(startSide, width, height)
        val end = getEdgePoint(endSide, width, height)

        // Control point for a gentle curved path across screen
        val midX = (start.x + end.x) / 2f + (random.nextFloat() - 0.5f) * width * 0.6f
        val midY = (start.y + end.y) / 2f + (random.nextFloat() - 0.5f) * height * 0.6f
        val control = Offset(
            midX.coerceIn(width * 0.15f, width * 0.85f),
            midY.coerceIn(height * 0.15f, height * 0.85f)
        )

        // Approximate curve length
        val chord = (end - start).getDistance()
        val controlDist = (control - start).getDistance() + (end - control).getDistance()
        val approxLength = (chord + controlDist) / 2f

        val stepCount = (approxLength / strideDistance).roundToInt().coerceIn(8, 16)
        val steps = mutableListOf<CatStep>()

        var stepId = 1L
        for (i in 0 until stepCount) {
            val t = i.toFloat() / (stepCount - 1).coerceAtLeast(1)

            // Quadratic Bézier: B(t) = (1-t)^2 * P0 + 2(1-t)t * P1 + t^2 * P2
            val u = 1f - t
            val px = u * u * start.x + 2f * u * t * control.x + t * t * end.x
            val py = u * u * start.y + 2f * u * t * control.y + t * t * end.y

            // Derivative: B'(t) = 2(1-t)(P1 - P0) + 2t(P2 - P1)
            val dx = 2f * u * (control.x - start.x) + 2f * t * (end.x - control.x)
            val dy = 2f * u * (control.y - start.y) + 2f * t * (end.y - control.y)
            val length = sqrt(dx * dx + dy * dy).coerceAtLeast(0.001f)

            // Tangent & Normal vectors
            val tangentAngleRad = atan2(dy, dx)
            val tangentAngleDeg = Math.toDegrees(tangentAngleRad.toDouble()).toFloat()

            // Normal vector (perpendicular to tangent)
            val nx = -dy / length
            val ny = dx / length

            val isLeft = (i % 2 == 0)
            val sideSign = if (isLeft) -1f else 1f

            // Apply lateral gait offset perpendicular to path
            val stepX = px + nx * (lateralOffset * sideSign)
            val stepY = py + ny * (lateralOffset * sideSign)

            // Paw rotation: toes face direction of travel, with subtle natural inward angle
            val pawAngle = tangentAngleDeg + 90f + (sideSign * 6f)

            steps.add(
                CatStep(
                    id = stepId++,
                    x = stepX,
                    y = stepY,
                    angle = pawAngle,
                    isLeftPaw = isLeft,
                    spawnTime = startTime + i * stepIntervalMs
                )
            )
        }

        return steps
    }

    private fun getEdgePoint(side: Int, width: Float, height: Float): Offset {
        return when (side) {
            0 -> Offset(-30f, height * (0.2f + random.nextFloat() * 0.6f)) // Left
            1 -> Offset(width * (0.15f + random.nextFloat() * 0.7f), height + 30f) // Bottom
            2 -> Offset(width + 30f, height * (0.2f + random.nextFloat() * 0.6f)) // Right
            else -> Offset(width * (0.15f + random.nextFloat() * 0.7f), -30f) // Top
        }
    }
}

/**
 * State holder for the Cat Footsteps Easter Egg.
 */
class CatWalkState {
    var tapCount by mutableIntStateOf(0)
        private set
    var lastTapTime by mutableLongStateOf(0L)
        private set
    var logoScale by mutableFloatStateOf(1f)
        private set

    val steps = mutableStateListOf<CatStep>()
    val particles = mutableStateListOf<CatParticle>()

    private var nextParticleId = 1L
    private val particleSymbols = listOf("♥", "✦", "★", "🐾", "♥")
    private val particleColors = listOf(
        Color(0xFFFB7185), // Soft rose
        Color(0xFFF472B6), // Warm pink
        Color(0xFFFBBF24), // Amber star
        Color(0xFFFDA4AF)  // Light peach
    )

    fun onLogoTap(
        width: Float,
        height: Float,
        onHaptic: (HapticFeedbackType) -> Unit
    ) {
        onLogoTap(width, height, System.currentTimeMillis(), onHaptic)
    }

    fun onLogoTap(
        width: Float,
        height: Float,
        now: Long,
        onHaptic: (HapticFeedbackType) -> Unit
    ) {
        if (now - lastTapTime > 2500L) {
            tapCount = 1
        } else {
            tapCount++
        }
        lastTapTime = now

        // Micro-bounce visual feedback
        logoScale = 0.82f

        onHaptic(HapticFeedbackType.TextHandleMove)

        if (tapCount >= 5) {
            tapCount = 0
            triggerWalk(width, height)
        }
    }

    fun resetLogoScale() {
        logoScale = 1f
    }

    fun triggerWalk(width: Float, height: Float) {
        val now = System.currentTimeMillis()
        val newSteps = CatPathGenerator.generateSteps(width, height, startTime = now)
        steps.addAll(newSteps)
    }

    fun onPawTapped(step: CatStep, onHaptic: (HapticFeedbackType) -> Unit) {
        if (step.isTapped) return
        step.isTapped = true
        onHaptic(HapticFeedbackType.LongPress)

        // Spawn 6-8 particles at paw position
        val now = System.currentTimeMillis()
        val random = Random()
        val count = 7
        for (i in 0 until count) {
            val angle = random.nextDouble() * 2 * Math.PI
            val speed = 80f + random.nextFloat() * 140f
            val vx = (cos(angle) * speed).toFloat()
            val vy = (sin(angle) * speed).toFloat() - 70f // Upward float bias

            particles.add(
                CatParticle(
                    id = nextParticleId++,
                    startX = step.x,
                    startY = step.y,
                    vx = vx,
                    vy = vy,
                    symbol = particleSymbols[random.nextInt(particleSymbols.size)],
                    color = particleColors[random.nextInt(particleColors.size)],
                    spawnTime = now
                )
            )
        }
    }

    fun prune(now: Long) {
        // Steps fade after 3.5s and fully disappear at 4.2s
        steps.removeAll { step ->
            step.isTapped || (now - step.spawnTime > 4200L)
        }
        // Particles dissolve after 750ms
        particles.removeAll { particle ->
            now - particle.spawnTime > 750L
        }
    }
}

@Composable
fun rememberCatWalkState(): CatWalkState {
    return remember { CatWalkState() }
}

/**
 * Fullscreen non-blocking overlay that renders cat footsteps and particle bursts.
 * Touches outside of active paws pass seamlessly through to the underlying app.
 */
@Composable
fun CatFootstepsOverlay(
    state: CatWalkState,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val density = LocalDensity.current
        val widthPx = with(density) { maxWidth.toPx() }
        val heightPx = with(density) { maxHeight.toPx() }

        var currentTime by remember { mutableLongStateOf(System.currentTimeMillis()) }

        // Animation frame ticker: smoothly updates step alphas and particle physics
        val hasActiveElements = state.steps.isNotEmpty() || state.particles.isNotEmpty()
        LaunchedEffect(hasActiveElements) {
            if (hasActiveElements) {
                while (isActive) {
                    currentTime = System.currentTimeMillis()
                    state.prune(currentTime)
                    delay(16L) // ~60 FPS
                }
            }
        }

        // 1. Render Cat Paw Footsteps
        state.steps.forEach { step ->
            val age = currentTime - step.spawnTime
            if (age >= 0 && !step.isTapped) {
                val alpha = when {
                    age < 200L -> (age / 200f) * 0.88f // Smooth pop-in
                    age < 3500L -> 0.88f // Stay clearly visible for 3.5 seconds
                    age < 4200L -> 0.88f * (1f - (age - 3500L) / 700f) // Smooth fade out
                    else -> 0f
                }

                if (alpha > 0.01f) {
                    val pawDpSize = 32.dp
                    val offsetXDp = with(density) { (step.x - pawDpSize.toPx() / 2f).toDp() }
                    val offsetYDp = with(density) { (step.y - pawDpSize.toPx() / 2f).toDp() }

                    Box(
                        modifier = Modifier
                            .offset(x = offsetXDp, y = offsetYDp)
                            .size(pawDpSize)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                state.onPawTapped(step) { hapticType ->
                                    haptic.performHapticFeedback(hapticType)
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        CatPawPrint(
                            modifier = Modifier
                                .size(28.dp)
                                .graphicsLayer {
                                    rotationZ = step.angle
                                },
                            color = Color(0xFFFB7185), // Gentle warm pink
                            alpha = alpha
                        )
                    }
                }
            }
        }

        // 2. Render Heart & Sparkle Particles on Tapped Paws
        state.particles.forEach { particle ->
            val pAge = currentTime - particle.spawnTime
            if (pAge in 0..750) {
                val progress = pAge / 750f
                val easedProgress = FastOutSlowInEasing.transform(progress)
                val currentX = particle.startX + particle.vx * (pAge / 1000f)
                val currentY = particle.startY + particle.vy * (pAge / 1000f) + 120f * (progress * progress)
                val pAlpha = (1f - progress).coerceIn(0f, 1f)
                val pScale = 0.6f + (1f - easedProgress) * 0.8f

                val pXDp = with(density) { currentX.toDp() }
                val pYDp = with(density) { currentY.toDp() }

                Text(
                    text = particle.symbol,
                    color = particle.color.copy(alpha = pAlpha),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .offset(x = pXDp, y = pYDp)
                        .graphicsLayer {
                            scaleX = pScale
                            scaleY = pScale
                        }
                )
            }
        }
    }
}

/**
 * Beautiful vector cat paw print (main pad + 4 toe beans) drawn via Canvas.
 */
@Composable
fun CatPawPrint(
    modifier: Modifier = Modifier,
    color: Color = Color(0xFFFB7185),
    alpha: Float = 1f
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val effectiveColor = color.copy(alpha = color.alpha * alpha)

        // 1. Main Palm Pad (metacarpal pad at bottom center)
        val padWidth = w * 0.58f
        val padHeight = h * 0.44f
        val padLeft = (w - padWidth) / 2f
        val padTop = h * 0.48f

        val padPath = Path().apply {
            moveTo(padLeft + padWidth * 0.5f, padTop)
            cubicTo(
                padLeft + padWidth * 0.95f, padTop,
                padLeft + padWidth, padTop + padHeight * 0.55f,
                padLeft + padWidth * 0.82f, padTop + padHeight
            )
            cubicTo(
                padLeft + padWidth * 0.58f, padTop + padHeight * 0.86f,
                padLeft + padWidth * 0.42f, padTop + padHeight * 0.86f,
                padLeft + padWidth * 0.18f, padTop + padHeight
            )
            cubicTo(
                padLeft, padTop + padHeight * 0.55f,
                padLeft + padWidth * 0.05f, padTop,
                padLeft + padWidth * 0.5f, padTop
            )
            close()
        }
        drawPath(padPath, color = effectiveColor)

        // 2. Four Toe Beans (digital pads arched cleanly above palm)
        // Outer Left Toe
        drawOval(
            color = effectiveColor,
            topLeft = Offset(w * 0.12f, h * 0.26f),
            size = Size(w * 0.19f, h * 0.24f)
        )
        // Center Left Toe (highest)
        drawOval(
            color = effectiveColor,
            topLeft = Offset(w * 0.32f, h * 0.08f),
            size = Size(w * 0.20f, h * 0.27f)
        )
        // Center Right Toe (highest)
        drawOval(
            color = effectiveColor,
            topLeft = Offset(w * 0.53f, h * 0.08f),
            size = Size(w * 0.20f, h * 0.27f)
        )
        // Outer Right Toe
        drawOval(
            color = effectiveColor,
            topLeft = Offset(w * 0.73f, h * 0.26f),
            size = Size(w * 0.19f, h * 0.24f)
        )
    }
}
