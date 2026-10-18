# UI and release readiness — 2026-10-18

Completed available Waydroid checks and fixed the issues found: compact header with keyboard/short windows; wrapping honours headings and metric rows; single-line bilingual tabs and bounded navigation labels; stacked summary tiles at narrow widths/large text. Only four existing UI files changed; no backend, dependencies, signing/version changes or data migration.

**Validation:** 546 JVM tests pass; optimized APK/AAB and release guard pass. Signed replacement installed and hash verified. Final320dp/200% summary/programme and landscape200% keyboard assertions pass. Original density/font/orientation/night mode and Dark/OLED/red appearance restored; same three timetable courses/nine credits. No account/GPA/favourite edits or publishing.

**Handoff:** [full report/screenshots](verification/2026-10-18/release-readiness/report.md), [focused patch](verification/2026-10-18/release-readiness/source.patch), machine results/guard/installed hash in the same folder. APK/AAB/mapping/log at build/release-readiness-2026-10-18/. Final APK SHA256509ad1b6eb65ea7e20f164ca4a2454518f11d3d75b95563546c2a9538c7c6946.

**Blocked outside the available environment:** live website still lacks native asset association and apex redirects301; prepared matching JSON in web source must be deployed with direct responses on both hosts and confirmed Play signing certificate. No Android15/16 images/AVDs or physical Samsung/Pixel devices available. Populated favourites/study files unavailable; matrix is sampled, not exhaustive. Nothing published or force-approved.
