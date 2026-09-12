# Rule: Material 3 Expressive Native Android Guidelines

## 1. Stack & Architecture
- Language: 100% Kotlin with Jetpack Compose (No legacy XML layouts).
- Library: `androidx.compose.material3:material3` (ensure Expressive API support).
- Target Android 15/16 visual standards with mandatory Edge-to-Edge (`enableEdgeToEdge()`).

## 2. Material 3 Expressive Design Pillars
- **Motion:** Never use linear or generic easings. Use physics-based springs and `MotionScheme.expressive()`.
- **Shapes:** Leverage expressive shape tokens (prominent rounded corners, squircles, and asymmetrical card corners where appropriate).
- **Color & Mood:** Support Dynamic Color (`dynamicLightColorScheme` / `dynamicDarkColorScheme`) with high-contrast, mood-driven tonal palettes. Avoid flat mono surfaces.
- **Typography:** Utilize variable typography with distinct hierarchy and emphasized text styles (Roboto Flex / Variable fonts).
- **Expressive Components:** Prefer modern M3 Expressive components:
  - `FloatingToolbar` or `DockedToolbar` instead of standard rigid bottom bars.
  - Expressive Floating Action Buttons (FABs) with dynamic icon morphing.
  - Pill-shaped chips and segmented toggles.

## 3. Code Standards
- Include `@OptIn(ExperimentalMaterial3ExpressiveApi::class)` when accessing expressive composables.
- Always provide `@Preview` annotations for light, dark, and dynamic color modes.
