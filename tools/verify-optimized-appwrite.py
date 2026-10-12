#!/usr/bin/env python3
"""Verify real optimized Appwrite JSON on Android without installing/clearing app data.

Build assembleReleaseValidation first, then run:
python3 tools/verify-optimized-appwrite.py --serial <adb-serial>
An APK and matching R8 mapping may be supplied for another optimized variant.
"""
import argparse
import os
from pathlib import Path
import re
import subprocess
import tempfile
import uuid


def main():
    root = Path(__file__).resolve().parents[1]
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--serial", required=True)
    parser.add_argument("--apk", type=Path, default=root / "app/build/outputs/apk/releaseValidation/app-releaseValidation.apk")
    parser.add_argument("--mapping", type=Path, default=root / "app/build/outputs/mapping/releaseValidation/mapping.txt")
    parser.add_argument("--sdk", type=Path, default=Path(os.environ.get("ANDROID_HOME", "/opt/android-sdk")))
    args = parser.parse_args()
    match = re.search(r"^io\.appwrite\.Query -> ([\w.$]+):$", args.mapping.read_text(), re.M)
    if not match:
        raise RuntimeError("Optimized Query class is missing from the matching R8 mapping")
    tools = max((args.sdk / "build-tools").glob("[0-9]*"),
                key=lambda p: tuple(int(n) for n in re.findall(r"\d+", p.name)))
    adb = [str(args.sdk / "platform-tools/adb"), "-s", args.serial]
    remote = "/data/local/tmp/lingubible-query-smoke-" + uuid.uuid4().hex

    def run(command):
        subprocess.run(command, check=True)

    with tempfile.TemporaryDirectory(prefix="lingubible-query-smoke-") as directory:
        temp = Path(directory)
        (temp / "classes").mkdir()
        (temp / "dex").mkdir()
        run(["javac", "--release", "8", "-Xlint:-options", "-cp", str(args.sdk / "platforms/android-36/android.jar"),
             "-d", str(temp / "classes"), str(root / "tools/AppwriteQuerySmoke.java")])
        run([str(tools / "d8"), "--min-api", "26", "--output", str(temp / "dex"),
             str(temp / "classes/AppwriteQuerySmoke.class")])
        run(adb + ["shell", "mkdir", "-p", remote])
        try:
            run(adb + ["push", str(args.apk.resolve()), remote + "/app.apk"])
            run(adb + ["push", str(temp / "dex/classes.dex"), remote + "/probe.dex"])
            run(adb + ["shell", "env", "CLASSPATH=" + remote + "/app.apk:" + remote + "/probe.dex",
                       "app_process", "/system/bin", "AppwriteQuerySmoke", match.group(1)])
        finally:
            run(adb + ["shell", "rm", "-rf", remote])


if __name__ == "__main__":
    main()
