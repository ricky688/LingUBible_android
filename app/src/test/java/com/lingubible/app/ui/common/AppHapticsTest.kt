package com.lingubible.app.ui.common

import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import com.lingubible.app.core.theme.TactileMotionTokens
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AppHapticsTest {
    private val pulses = mutableListOf<HapticFeedbackType>()
    private val platform = object : HapticFeedback {
        override fun performHapticFeedback(hapticFeedbackType: HapticFeedbackType) {
            pulses += hapticFeedbackType
        }
    }

    @Test
    fun tapsUseTheButtonEffectRatherThanTextSelectionFeedback() {
        assertEquals(HapticFeedbackType.VirtualKey, TactileMotionTokens.hapticTap)
    }

    @Test
    fun enabledTapForwardsExactlyOnePulse() {
        PreferenceHapticFeedback(platform) { true }.performHapticFeedback(TactileMotionTokens.hapticTap)
        assertEquals(listOf(TactileMotionTokens.hapticTap), pulses)
    }

    @Test
    fun disabledSettingSuppressesTapAndNativeLongPressFeedback() {
        val feedback = PreferenceHapticFeedback(platform) { false }
        feedback.performHapticFeedback(TactileMotionTokens.hapticTap)
        feedback.performHapticFeedback(HapticFeedbackType.LongPress)
        assertTrue(pulses.isEmpty())
    }

    @Test
    fun settingChangesApplyWithoutRecreatingTheFeedbackProvider() {
        var enabled = false
        val feedback = PreferenceHapticFeedback(platform) { enabled }
        feedback.performHapticFeedback(TactileMotionTokens.hapticTap)
        enabled = true
        feedback.performHapticFeedback(TactileMotionTokens.hapticTap)
        enabled = false
        feedback.performHapticFeedback(TactileMotionTokens.hapticTap)
        assertEquals(1, pulses.size)
    }

    @Test
    fun nativeFeedbackTypeIsPreserved() {
        PreferenceHapticFeedback(platform) { true }.performHapticFeedback(HapticFeedbackType.LongPress)
        assertEquals(listOf(HapticFeedbackType.LongPress), pulses)
    }
}
