# Button haptics

Buttons, bottom navigation, tabs, connected groups, clickable cards, selectors, menu actions and switches now request the same native Android tap feedback.

Use **User Settings → Appearance & Customization → Haptic feedback** (觸覺回饋振動) to enable or disable it. Changes apply immediately. Enabling the switch previews one tap; disabling it stays silent. Android system settings and device hardware also determine whether feedback can be felt.

Feedback follows a completed action rather than a finger-down event, so dragging to scroll or canceling a tap does not request button feedback. Existing shape and color press animations are preserved.

## Implementation

`ui/common/AppHaptics.kt` provides preference-aware click and Boolean-change callbacks. It obtains the platform feedback from the current Compose window, including dialogs. Existing manual Settings/connected-group pulses were removed to prevent duplicates. `TactileMotionTokens.hapticTap` now uses `VirtualKey`: the former text-selection effect was disabled by Waydroid's text-handle configuration.

This uses Android's platform feedback without adding a vibration permission or dependency. See [Android haptic guidance](https://developer.android.com/develop/ui/views/haptics/haptic-feedback).

## Verification

See [2026-10-14 report](verification/2026-10-14/app-haptics/report.md). Physical feedback strength on Samsung/Pixel remains to be checked.
