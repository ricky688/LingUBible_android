# Full-width dividers — 2026-10-21

Removed separator-specific horizontal padding from the two profile navigation separators, navigation drawer group/footer separators, and academic-year term-menu separators. All horizontal dividers now fill their available parent content width; structural card padding remains. Existing vertical spacing, 1 dp thickness and active theme outlineVariant are preserved. This supersedes the earlier recommendation for inset profile navigation dividers.

Release build passed in 2m22s. Existing release certificate retained; installed APK hash matches the build. On Android 13 Waydroid, visually verified that the two previously inset profile lines now align with the other full-width lines. Saved screenshot is cropped to the preferences group to exclude account details. Theme and data actions were untouched.

Static inspection checked all 26 HorizontalDivider calls: none retain explicit horizontal/start/end padding. This cosmetic adjustment adds no logic or tests; the previous 554-test pass was not repeated. Drawer/menu visuals, light mode and physical devices were not rechecked. Timetable accessibility findings from the preceding retest remain open.

APK SHA-256: bee2011471c61b6504f6cdf414f6245d4be84448f6b86c0093957abd9aed1424
