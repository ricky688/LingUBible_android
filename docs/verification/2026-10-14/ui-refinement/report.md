# Light-mode press colors, Discover corners and timetable popup — October 14, 2026

## User clarification and changes

The user confirmed that “Convergent–Divergent” means fill and text colors converge while the button is held, then separate on release. The shared M3ButtonGroupButton now observes its native interaction source. Light-mode pressed container/content targets meet at an opaque midpoint derived from the current selected palette colors; release returns to existing selected/unselected targets. Dark/OLED mode keeps its previous colors. Stiffness-only springs and native pressed/checked shapes remain. The visual label blends into the fill during a light-mode hold as requested; its text and accessibility semantics remain present.

Discover provides a local20dp card corner token (was28dp), with8dp while pressed (was16dp). This covers stats, academic tools, popular course/instructor and review cards within Discover. Shared card instances on other pages retain28/16dp. Existing fixed smaller-radius containers and button/icon shapes remain.

Timetable popup gives each meeting its own time/type line and a separate bilingual Location row, preserving selected-meeting check and original export target. Footer order is Add to calendar, ICS export, then Remove/Close as the final row. This supersedes the earlier popup footer order. Credits and metadata still appear once. No backend, stored preference, version or signing changes. User's committed removal of the Easter egg remains intact; no icon-rain source exists.

## Validation

- 403 JVM unit tests pass, zero failures/errors/skips. Three new tests cover selected/unselected light press convergence over red/blue/green palettes, release targets and unchanged dark press targets.
- Optimized validation APK built in2m51s; signed release APK and AAB built in2m7s. Local release guard PASS, matching DEX and embedded mapping (652 renamed application types; not Play Console coverage). Version1.0.5/code6 unchanged; no publishing.
- Installed matching-certificate validation build on the previously selected Waydroid with adb install -r. No uninstall/data clear; installed hash matches current output. Connected Samsung phone untouched.
- ARTEMIS observations and grounded ADB holds verify light-held blending, released label restoration and dark-held label visibility. Profile was a guest screen. Original Dark theme restored in finally and visually verified. No palette/language/OLED settings changed.
- Screenshots: light-held.png, light-after-release.png, dark-held.png, restored-dark.png; discover-light.png and discover-dark.png show reduced card corners. popup.png shows CLC9022 credits3, WED14:30–17:29/LEC, separate MBG11 Location and final Remove/Close row. The popup remains open for review; selected course unchanged.
- No app-PID-scoped AndroidRuntime errors in recent logs. No new UI test scripts authored. Physical devices, TalkBack manual use, large fonts/rotated/split-screen and multi-meeting/conflict fixtures were not repeated.

Artifacts, R8 mappings, test XML and logs: build/ui-refinement-2026-10-14/.
