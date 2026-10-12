#!/usr/bin/env python3
"""Check the optimized Play bundle before upload (no device or network needed).

Build :app:assembleRelease :app:bundleRelease, then run this script.
These local checks do not reproduce Play Console's optimization percentages.
"""
import argparse
import hashlib
import json
import re
from pathlib import Path
from zipfile import ZipFile


def verify(root, minimum_version_code=4):
    outputs = root / "app/build/outputs"
    apk = outputs / "apk/release/app-release.apk"
    bundle = outputs / "bundle/release/app-release.aab"
    mapping = (outputs / "mapping/release/mapping.txt").read_bytes()
    configuration = (outputs / "mapping/release/configuration.txt").read_text()
    if re.search(r"^\s*-(dontobfuscate|dontoptimize|dontshrink)\b", configuration, re.M):
        raise ValueError("Effective R8 configuration disables release optimization")
    if b"# compiler: R8" not in mapping:
        raise ValueError("Release mapping was not produced by R8")

    with ZipFile(bundle) as archive:
        packaged_mapping = archive.read("BUNDLE-METADATA/com.android.tools.build.obfuscation/proguard.map")
        if packaged_mapping != mapping:
            raise ValueError("Bundle mapping does not match the current release mapping")
        dex_files = {Path(i.filename).name: archive.read(i) for i in archive.infolist()
                     if i.filename.startswith("base/dex/") and i.filename.endswith(".dex")}
    if not dex_files or any(not d.startswith(b"dex\n") for d in dex_files.values()):
        raise ValueError("Bundle has no valid base DEX files")
    with ZipFile(apk) as archive:
        apk_dex = {Path(i.filename).name: archive.read(i) for i in archive.infolist()
                   if i.filename.endswith(".dex")}
        if apk_dex != dex_files:
            raise ValueError("APK and AAB have different DEX; rebuild both release artifacts")

    # Match renamed application descriptors against the shipped bytecode, rather
    # than merely trusting that minification was requested in the Gradle file.
    descriptors = set(re.findall(rb"L[A-Za-z0-9_$/]+;", b"".join(dex_files.values())))
    renamed = []
    for original, compiled in re.findall(r"^(\S+) -> (\S+):$", mapping.decode(), re.M):
        descriptor = ("L" + compiled.replace(".", "/") + ";").encode()
        if original.startswith("com.lingubible.app.") and original != compiled and descriptor in descriptors:
            renamed.append((original, compiled))
    if not renamed:
        raise ValueError("No obfuscated application types found in the shipped DEX")

    metadata = json.loads((outputs / "apk/release/output-metadata.json").read_text())
    element = metadata["elements"][0]
    if element["versionCode"] < minimum_version_code:
        raise ValueError(f"Version code must be at least {minimum_version_code}; the old Play release used code 3")
    return {
        "status": "PASS",
        "version_code": element["versionCode"],
        "version_name": element["versionName"],
        "apk_bytes": apk.stat().st_size,
        "aab_bytes": bundle.stat().st_size,
        "uncompressed_dex_bytes": sum(map(len, dex_files.values())),
        "renamed_application_types_present_in_dex": len(renamed),
        "matching_r8_mapping_embedded": True,
        "sha256": {p.name: hashlib.sha256(p.read_bytes()).hexdigest() for p in (apk, bundle)},
        "note": "Type count and size are local evidence, not Play Console coverage percentages.",
    }


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--root", type=Path, default=Path(__file__).resolve().parents[1])
    parser.add_argument("--minimum-version-code", type=int, default=4)
    parser.add_argument("--output", type=Path)
    args = parser.parse_args()
    result = json.dumps(verify(args.root, args.minimum_version_code), indent=2) + "\n"
    if args.output:
        args.output.parent.mkdir(parents=True, exist_ok=True)
        args.output.write_text(result)
    print(result, end="")


if __name__ == "__main__":
    main()
