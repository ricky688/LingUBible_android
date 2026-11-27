# Project Directives (`LingUBible_android`)

## 1. Google Play AAB Release & Upload
- **Upload Mechanism:**
  - Use the project's Google Play Developer API v3 scripts (package `com.lingubible.app`):
    - Build + upload: `./tools/build_and_upload.sh [track] [status]` (defaults: `track=alpha` for Closed Testing, `status=completed`).
    - Upload an existing `.aab`: `python3 tools/upload_to_play_store.py --aab <path> --track <track> --status <status>`
  - Credentials: `play-service-account.json` at the repo root (git-ignored; never commit or print its contents).
- **Pre-Upload Checklist:**
  - Increment `versionCode` (and `versionName` as needed) in `app/build.gradle.kts` before building; Google Play rejects duplicate or lower `versionCode`s.
  - Ensure `release` signing config (`app/release.keystore`) is intact.
- **Upload Failure & Troubleshooting Protocol:**
  - If `build_and_upload.sh` or `upload_to_play_store.py` exits non-zero, **never** ignore the failure or claim the upload succeeded. Read the printed API/script error and diagnose by cause:
    - **Duplicate `versionCode` (`400`/`403`):** Bump `versionCode` in `app/build.gradle.kts` above the latest Play Console release and rebuild.
    - **Draft app (`Only releases with status draft may be created on a draft app`):** Retry with `--status draft` (e.g. `./tools/build_and_upload.sh alpha draft`) or inform the user that initial Play Console store-listing/review setup must be completed manually first.
    - **Permission denied (`401`/`403`) or missing key:** Verify `play-service-account.json` exists and tell the user to check Google Play Console → **API access** / **Users and permissions** for the service account's release permissions.
    - **Track not found:** Confirm the exact track name in Play Console (default Closed Testing is `alpha`; custom closed tracks use their slug).
    - **Signing mismatch / Python dependency (`google-auth`, `google-api-python-client`) / network block:** Report the exact error message and required manual action clearly to the user.
