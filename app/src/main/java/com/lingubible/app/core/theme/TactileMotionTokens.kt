package com.lingubible.app.core.theme

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.ui.hapticfeedback.HapticFeedbackType

/**
 * Motion Tokens and Bouncy Physics for Material 3 Expressive UI.
 *
 * Sourced from pixelthing ExpressiveLab (TactileMotionTokens).
 * Standardizes press scales, bouncy springs, and tactile haptic feedback.
 */
object TactileMotionTokens {
    /** Standard press scale reduction factor for regular buttons */
    const val PRESS_SCALE_STANDARD = 0.92f

    /** Pronounced scale reduction factor for Floating Action Buttons (FAB) */
    const val PRESS_SCALE_FAB = 0.88f

    /** Subtle scale reduction factor for cards and interactive tiles */
    const val PRESS_SCALE_CARD = 0.96f

    /** Standard haptic feedback for button taps */
    val hapticTap = HapticFeedbackType.TextHandleMove

    /**
     * Bouncy spring animation specification for playful, organic motion.
     * Uses medium bounce and low stiffness for an elastic feel.
     */
    fun <T> bouncySpring() = spring<T>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessLow
    )

    /**
     * Gentle spring animation specification without overshoot.
     * Ideal for layout transitions and structural movement.
     */
    fun <T> gentleSpring() = spring<T>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMedium
    )
}
