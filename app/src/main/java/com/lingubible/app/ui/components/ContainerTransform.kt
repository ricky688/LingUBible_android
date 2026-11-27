package com.lingubible.app.ui.components

import android.provider.Settings
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.BoundsTransform
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.EnterExitState
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.animateColor
import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.lingubible.app.core.theme.AppMotion
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** Provided by LingUBibleNavHost; null outside it (previews, modals), which turns the transform off. */
@OptIn(ExperimentalSharedTransitionApi::class)
val LocalSharedTransitionScope = staticCompositionLocalOf<SharedTransitionScope?> { null }
val LocalNavAnimatedScope = compositionLocalOf<AnimatedVisibilityScope?> { null }

/**
 * Card ↔ detail container transform. Put the same [key] on the list card and on the detail root;
 * screens without a matching partner keep the normal page transition.
 * [detail] = this is the full-screen side: it paints the page background, tinted with the card's
 * container color while it grows out of / shrinks back into the card so the morph is visible.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun Modifier.containerTransform(key: String, detail: Boolean = false): Modifier {
    val page = MaterialTheme.colorScheme.background
    val shared = LocalSharedTransitionScope.current
    val nav = LocalNavAnimatedScope.current
    if (shared == null || nav == null) return if (detail) background(page) else this
    val card = MaterialTheme.colorScheme.surfaceContainer
    val fill by nav.transition.animateColor(transitionSpec = { AppMotion.effects() }, label = "containerFill") {
        if (it == EnterExitState.Visible) page else card
    }
    return with(shared) {
        val state = rememberSharedContentState(key)
        val bounds = this@containerTransform.sharedBounds(
            sharedContentState = state,
            animatedVisibilityScope = nav,
            boundsTransform = BoundsTransform { _, _ -> AppMotion.page() },
            clipInOverlayDuringTransition = OverlayClip(RoundedCornerShape(18.dp))
        )
        if (detail) bounds.background(if (state.isMatchFound) fill else page) else bounds
    }
}

/**
 * Staggered entrance for a detail screen's top-level sections. Plays once per back-stack entry
 * (saved, so popping back or scrolling a lazy item back in does not replay it) and is skipped
 * when the system "Remove animations" setting is on.
 */
@Composable
fun rememberStaggeredEntrance(count: Int, stepMillis: Long = 55L): List<Animatable<Float, AnimationVector1D>> {
    val context = LocalContext.current
    var played by rememberSaveable { mutableStateOf(false) }
    val reduceMotion = remember {
        Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f
    }
    val progress = remember { List(count) { Animatable(if (played || reduceMotion) 1f else 0f) } }
    LaunchedEffect(Unit) {
        if (played || reduceMotion) return@LaunchedEffect
        played = true
        progress.forEachIndexed { i, anim ->
            launch {
                delay(80L + i * stepMillis) // let the container start expanding first
                anim.animateTo(1f, AppMotion.spatial())
            }
        }
    }
    return progress
}

@Composable
fun Modifier.staggeredEntrance(progress: List<Animatable<Float, AnimationVector1D>>, index: Int): Modifier {
    val anim = progress.getOrNull(index) ?: return this
    val offsetPx = with(LocalDensity.current) { 24.dp.toPx() }
    return graphicsLayer {
        val p = anim.value
        alpha = p.coerceIn(0f, 1f)
        translationY = (1f - p) * offsetPx
    }
}
