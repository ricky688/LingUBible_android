---
name: compose-m3-expressive
description: Generates native Android Jetpack Compose UI following Material 3 Expressive standards. Use whenever the user asks for new screens, components, or theme setup.
---

# Material 3 Expressive Implementation Blueprint

### Theme Configuration:
```kotlin
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = ExpressiveTypography,
        shapes = ExpressiveShapes,
        motionScheme = MotionScheme.expressive(), // Expressive spring physics
        content = content
    )
}
