# Calculator backups and spreadsheet exports

Open **學業工具 → GPA 與榮譽 → 計算器**. The **備份與匯出 · Backup & export** card appears beneath the GPA summary.

- **備份 Backup:** choose a location in Android's file picker to save a timestamped `.json` backup. It includes academic years, terms, course codes/names, grades, credits, unfinished entries and target-honours inputs.
- **匯出 CSV:** save a spreadsheet report containing course data, grade points, term GPA and cumulative GPA. CSV is for viewing; use JSON for restoration.
- **還原 Restore:** select a LingUBible JSON backup. Review the term/course count and confirm replacing the current calculator entries and goals. Cancel keeps current data. Use **復原 Undo** in Terms & Courses to reverse a changed import during the current session.

Backups contain the entered information as plain JSON. The calculator does not upload them automatically; the chosen Android document provider stores the exported file. Files saved outside app storage survive app removal, following the [Android Storage Access Framework](https://developer.android.com/training/data-storage/shared/documents-files).

## Format and validation

JSON uses `format: "lingubible-gpa"`, `version: 1` and a `planner` record with `document`, `targetCgpaInput`, and `remainingCreditsInput`. The document retains the existing native calculator representation (`terms`, `yearAcademic`). GPA values are calculated again from imported grades and credits.

Import accepts backups up to 1 MiB. Invalid/incomplete files, unsupported versions, duplicate IDs, unsupported grades and invalid structure are rejected before current data changes. CSV uses UTF-8 with BOM, quoted fields and CRLF; text that starts with a spreadsheet formula marker is escaped.

## Verification

Waydroid checks covered manual course entry, saving JSON/CSV through the system picker, exact exported values, restoring, cancelling, Undo/Redo and persistence after restart. The full local unit suite passed (371 tests). Screenshots are under `docs/verification/2026-10-09/`.

On this Android 13 Waydroid image, document-provider read-only opening can throw a package-attribution null pointer. A narrowly scoped fallback opens a non-truncating read/write descriptor when the picker granted it, and uses it only for reading. Source file checksums were unchanged. Physical devices and third-party cloud providers have not been verified.
