package com.lingubible.app.ui.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import com.lingubible.app.core.settings.AppSettingsManager
import com.lingubible.app.core.theme.TactileMotionTokens
import org.koin.compose.koinInject

internal class PreferenceHapticFeedback(
    private val platform: HapticFeedback,
    private val isEnabled: () -> Boolean
) : HapticFeedback {
    override fun performHapticFeedback(hapticFeedbackType: HapticFeedbackType) {
        if (isEnabled()) platform.performHapticFeedback(hapticFeedbackType)
    }
}

@Composable
private fun rememberAppHapticFeedback(): HapticFeedback {
    val platform = LocalHapticFeedback.current
    val settings = koinInject<AppSettingsManager>()
    return remember(platform, settings) {
        PreferenceHapticFeedback(platform) { settings.isHapticsEnabled.value }
    }
}

@Composable
internal fun hapticClick(action: () -> Unit): () -> Unit {
    val feedback = rememberAppHapticFeedback()
    return {
        feedback.performHapticFeedback(TactileMotionTokens.hapticTap)
        action()
    }
}

@Composable
internal fun hapticChange(action: (Boolean) -> Unit): (Boolean) -> Unit {
    val feedback = rememberAppHapticFeedback()
    return { value ->
        // Apply a haptics-setting change before its feedback, so enabling previews it once.
        action(value)
        feedback.performHapticFeedback(TactileMotionTokens.hapticTap)
    }
}
