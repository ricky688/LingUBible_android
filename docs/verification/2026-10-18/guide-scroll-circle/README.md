# Guide sheet stability and course circular loading

## Changes

- The first-launch guide disables sheet dragging with `sheetGesturesEnabled = false` and hides its drag handle. Its inner list still scrolls; the pinned Next/Previous/Close controls remain. Other app sheets and the full-page guide are unchanged.
- Course search uses Material3 `CircularProgressIndicator()` in its existing initial/empty loading slot, using the selected theme’s primary color. The existing transition and loaded/empty/error states remain; Discover and instructor skeletons are unchanged.
- No backend, preference migration, version/signing or dependency changes.

Native reference: [ModalBottomSheet API](https://developer.android.com/reference/kotlin/androidx/compose/material3/ModalBottomSheet.composable). Nested scroll can pass unconsumed input to a parent, as described in [Compose nested scrolling](https://developer.android.com/develop/ui/compose/touch-input/scroll/nested-scroll-modifiers).

## Verification

ARTEMIS explored the real first-launch sheet and content scroll before the regression runner was authored. Trace `76c61b94-98c3-4269-9f63-2299afb1f902` completed; its action summary was inspected. Raw daemon stderr was not locally available.

| Same boundary gesture | Old release | Fixed release |
| --- | --- | --- |
| Header movement during top overscroll | 83 px | 0 px |
| Next during the gesture | Off-screen | Visible at y=905 |
| Sheet after gesture | Dismissed | Open |
| Header movement at bottom boundary | Not needed for reproduction | 0 px |

Next advanced to step 2 and Close returned to Home. The circular course loader was visually captured after a cold start; cached searches can complete before a loader is visible. `course-cold-loading.mp4` and `course-circular-loading.png` show the actual circular indicator. No artificial delay, network change or backend change was introduced.

All **551 unit tests passed**, and the optimized release APK built in **4m 11s**. The installed APK hash matches `9c42a99659c0b9a5363bde731131c01c30c1fc9bdf34d6c21582a9bbbbfc3f2f`; its certificate matches the existing release. No fatal error was logged in the restored owner app process. APK, mapping/configuration and build log: `build/guide-scroll-circle-2026-10-18/`. This APK-only check did not rebuild the older AAB.

## Safe test isolation and replay

First-launch testing used a new, disposable Android user (10) on Waydroid. App data resets were explicitly limited to that test user. It was removed after testing, and owner user 0 was restored. The owner account and existing 3-course/9-credit timetable were verified intact. The connected physical Samsung phone was not used.

The executable `check-guide-boundary.py` uses semantic labels, explicit waits and a verified coordinate fallback. Run it with the first-launch guide open at the top in a disposable non-owner Android user:

```sh
python3 check-guide-boundary.py DEVICE_SERIAL QA_USER_ID OUTPUT_DIR
```

For the bottom boundary, first scroll the content list to its end, then add `--bottom`. Both fixed checks assert stationary header and visible, stationary Next controls. `--baseline` was used to record the old release’s failure; it is not needed for future checks of the fix. The runner refuses owner user 0 and never clears app data or changes preferences. It records and pulls a bounded video and removes its own device recording.

## Evidence and limits

- `before/`, `after/`, `after-bottom/`: before/during/after layout snapshots, measurements and videos.
- `guide-before-overscroll.png`, `guide-fixed-overscroll.png`: inspected comparison frames.
- `course-circular-loading.png`, `course-cold-loading.mp4`: inspected live circular loader.
- `installation-and-cleanup.json`, `unit-tests.json`, `test-results.json`, `owner-restored.png`: final results and restoration.

Only Waydroid Android 13 at the current viewport/font size was checked. Physical Samsung/Pixel, large fonts and landscape were not retested for this change.
