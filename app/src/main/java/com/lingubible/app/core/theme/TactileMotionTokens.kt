package com.lingubible.app.core.theme

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.ui.hapticfeedback.HapticFeedbackType

/**
 * Motion Tokens and Bouncy Physics for Material 3 Expressive UI.
 *
 * Sourced from pixelthing ExpressiveLab (TactileMotionTokens).
 * Standardizes spring motion and tactile haptic feedback.
 */
object TactileMotionTokens {
    /** Standard haptic feedback for button taps */
    val hapticTap = HapticFeedbackType.VirtualKey

    const val CONNECTED_COLOR_STIFFNESS = 800f

    /**
     * Bouncy spring animation specification for playful, organic motion.
     * Uses medium bounce and low stiffness for an elastic feel.
     */
    fun <T> bouncySpring() = spring<T>(
        stiffness = Spring.StiffnessLow
    )

    /**
     * Gentle spring animation specification without overshoot.
     * Ideal for layout transitions and structural movement.
     */
    fun <T> gentleSpring() = spring<T>(
        stiffness = Spring.StiffnessMedium
    )
}
