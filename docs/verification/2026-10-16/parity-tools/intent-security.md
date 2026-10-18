# Best Practices and Security Alignment Update: Timetable link imports

## Security alignment area

Incoming Android links and saved timetable data isolation.

## Impact and priority

Medium: external links must not silently change a student's saved timetable or redirect an external intent to another component.

## Scope of changes

- `app/src/main/AndroidManifest.xml`: public HTTPS `/planner` link entry on the existing launcher activity, for `lingubible.com` and `www.lingubible.com`.
- `MainActivity.kt`: retain newly delivered intents via `setIntent`.
- `core/navigation/Screen.kt`, `NavGraph.kt`: dedicated import destination; an AndroidX new-intent listener handles valid warm-entry links.
- `domain/model/TimetableShare.kt`: pure parser and web-compatible link generator.
- `ui/screens/PlannerScreen.kt`, `ui/viewmodels/TimetableViewModel.kt`: preview, explicit confirmation, additive import and undo.
- No new dependencies, providers, services, receivers, permissions or PendingIntents.

## Implementation summary

- Parse HTTPS links only from the two named hosts and the exact `/planner` path. Reject user-info, unexpected ports, fragments, oversized input, duplicate required query parameters, unknown terms, malformed encoding and invalid section IDs. Limit imports and shares to 60 distinct IDs, matching the web format.
- Resolve IDs against the packaged target-term catalogue before confirmation. Show missing IDs and conflicts. Cancel/invalid input never writes; confirmation merges recognized IDs with that term's current saved selection. It preserves other terms and creates one undo step.
- Cold entry uses declared Navigation links and reaches the same importer validation. Warm entry additionally validates the original URL before `handleDeepLink`. MainActivity remains in its existing standard launch mode; no assumption that every launch delivers `onNewIntent`.
- No nested incoming intent is forwarded or launched. Sharing constructs a fresh Android chooser with only the public timetable URL. No account credentials or review text appear in the URL.
- The existing private FileProvider and OAuth callback remain configured as before. Timetable links are public cross-user links, so a signature-only permission would prevent the intended feature.
- Universal automatic browser-to-app opening is not claimed: verified App Links require a server-side Digital Asset Links association. Paste/import works independently of domain verification.

## Code diff

[Unified routing/parser diff](intent-security.patch). This includes the working tree's earlier navigation additions so future agents can see the complete current routing context.

## Testing and verification

- JVM checks cover round trips, duplicate IDs, untrusted hosts/schemes/paths, malformed IDs/encoding, duplicate parameters, unknown terms and limits.
- ViewModel checks cover preview/cancel without writes, additive target-term import, missing IDs, undo/redo, history limits, term isolation, delayed responses and catalogue failures.
- Android link delivery, chooser and device UI remain pending: the user requested source changes first while Waydroid is in use elsewhere.
