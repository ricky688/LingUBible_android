#!/usr/bin/env python3
"""
Uploads an Android App Bundle (.aab) to Google Play Console via Google Play Developer API v3.
"""

import sys
import os
import argparse
from google.oauth2 import service_account
from googleapiclient.discovery import build
from googleapiclient.http import MediaFileUpload

PACKAGE_NAME = "com.lingubible.app"
DEFAULT_KEY_FILE = "play-service-account.json"

def upload_bundle(aab_path, key_path=DEFAULT_KEY_FILE, track="alpha", release_status="completed"):
    if not os.path.exists(aab_path):
        print(f"[-] Error: AAB file not found at {aab_path}")
        sys.exit(1)

    if not os.path.exists(key_path):
        print(f"[-] Error: Service account key not found at {key_path}")
        sys.exit(1)

    print(f"[*] Authenticating with service account: {key_path}")
    credentials = service_account.Credentials.from_service_account_file(
        key_path,
        scopes=["https://www.googleapis.com/auth/androidpublisher"]
    )

    service = build("androidpublisher", "v3", credentials=credentials)

    print(f"[*] Starting edit session for package: {PACKAGE_NAME}...")
    edit_request = service.edits().insert(body={}, packageName=PACKAGE_NAME)
    result = edit_request.execute()
    edit_id = result["id"]
    print(f"[+] Edit session created with ID: {edit_id}")

    try:
        print(f"[*] Uploading App Bundle: {aab_path} ({os.path.getsize(aab_path) / 1024 / 1024:.2f} MB)...")
        media = MediaFileUpload(aab_path, mimetype="application/octet-stream", resumable=True)
        bundle_response = service.edits().bundles().upload(
            packageName=PACKAGE_NAME,
            editId=edit_id,
            media_body=media
        ).execute()

        version_code = bundle_response["versionCode"]
        print(f"[+] Successfully uploaded bundle with versionCode: {version_code}")

        print(f"[*] Assigning bundle to track: '{track}' (status: {release_status})...")
        track_response = service.edits().tracks().update(
            packageName=PACKAGE_NAME,
            editId=edit_id,
            track=track,
            body={
                "releases": [
                    {
                        "name": f"Release v{version_code}",
                        "versionCodes": [str(version_code)],
                        "status": release_status,
                    }
                ]
            }
        ).execute()
        print(f"[+] Track updated: {track_response['track']}")

        print("[*] Committing changes...")
        commit_response = service.edits().commit(
            packageName=PACKAGE_NAME,
            editId=edit_id
        ).execute()
        print(f"[✔] Successfully published to Google Play Console! (Edit ID: {commit_response['id']})")

    except Exception as e:
        print(f"[-] Failed during upload/commit: {e}")
        try:
            print("[*] Cleaning up edit session...")
            service.edits().delete(packageName=PACKAGE_NAME, editId=edit_id).execute()
        except Exception:
            pass
        sys.exit(1)

if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="Upload AAB to Google Play Console")
    parser.add_argument("--aab", help="Path to the .aab file", required=False)
    parser.add_argument("--key", default=DEFAULT_KEY_FILE, help="Path to play-service-account.json")
    parser.add_argument("--track", default="alpha", help="Target track (e.g., alpha, internal, beta, production)")
    parser.add_argument("--status", default="completed", help="Release status (completed, draft, inProgress)")

    args = parser.parse_args()

    aab_file = args.aab
    if not aab_file:
        default_release_bundle = "app/build/outputs/bundle/release/app-release.aab"
        if os.path.exists(default_release_bundle):
            aab_file = default_release_bundle
        else:
            aab_candidates = [f for f in os.listdir(".") if f.endswith(".aab")]
            if aab_candidates:
                aab_candidates.sort(key=lambda x: os.path.getmtime(x), reverse=True)
                aab_file = aab_candidates[0]

    if not aab_file:
        print("[-] No AAB file specified and none found in app/build/outputs/bundle/release or root.")
        print("    Usage: python3 tools/upload_to_play_store.py --aab path/to/app.aab")
        sys.exit(1)

    print(f"Target AAB: {aab_file}")
    upload_bundle(aab_file, key_path=args.key, track=args.track, release_status=args.status)
