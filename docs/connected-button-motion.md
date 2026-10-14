# Connected-button color motion

Light Mode uses a perimeter-to-center primary fill for a new selection or held button. The incoming fill stays opaque behind the label; the previous selection clears from the center toward the perimeter and fades away. Re-pressing a selected button replays a subtle inward highlight without changing its selection.

Dark Mode uses a uniform color crossfade with no spatial color wash. This includes OLED. Material 3 pressed/checked outlines and the existing haptics preference remain active. Interaction-source changes reset the transient pose. Quick taps finish the inward shape morph before returning; holds keep the pressed shape until release. Actions and selection commit immediately.

## Shared implementation

- `M3ButtonGroup.kt` coordinates press focus across the group, restores selection focus on cancellation and drives focus from selection and press state for regular, equally weighted and scrollable groups. Animation uses pure stiffness springs and the Compose system duration scale.
- `ConnectedButtonShape.kt` consumes native press events, completes the inward spring, waits for release when held, then returns to the current selected/rest outline. Overlapping taps coalesce instead of queuing stale animations. It interpolates native rounded/rectangular outlines and bypasses ToggleButton’s release-driven retargeting.
- `TactileMotionTokens.CONNECTED_COLOR_STIFFNESS` is 800 (previously 200), targeting about half the Light Mode color duration with no damping override.
- `ConnectedButtonFill.kt` draws feathered perimeter rings behind the label, clipped by the native animated button shape. Reversing focus expands the empty center and fades the remaining perimeter fill to zero.
- Label pixels reached by the primary fill are tinted with `onPrimary`; the remaining label uses `primary`. This replaces the previous fill/text color-merging effect.
- Drawing caches its Path/Paint and reads progress during drawing. No shaders, new dependencies or newer minimum Android version are needed.
- The default touch-origin press ripple is suppressed for this component so it does not compete with the requested effect; native keyboard-focus and hover state layers remain. Selection semantics, callbacks, checkmarks and haptics are retained.

All colors come from the selected Material color scheme, including dynamic and custom palettes.

## References

[Compose drawing modifiers](https://developer.android.com/develop/ui/compose/graphics/draw/modifiers) describe cached drawing and draw order. [Compose animation configuration](https://developer.android.com/develop/ui/compose/animation/customize) covers spring animation.

## Verification

The original 411-test spatial-transition check passed; the later complete-shape/faster-color update and its latest test/build results are in the timing report. Normal-speed recordings and pixel checks verified the Light Mode perimeter motion and Dark/OLED uniform crossfade. See [the verification report](verification/2026-10-14/connected-button-motion/report.md). Physical Samsung/Pixel smoothness remains a hardware check.

### Timing follow-up

See [quick/held press and faster-color verification](verification/2026-10-14/connected-button-timing/report.md) for the latest behavior.
