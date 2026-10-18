# Optimized startup regression found on Waydroid

- Installed signed APK SHA-256 `f27c8cb0d6ea404aba3577be14748e945f3638f0f2a3b2dcfed26e0514aafa41`, with certificate checked and replacement installation; no data clear.
- ARTEMIS exploration trace `5afa249c-140e-4bbf-bc7f-1b46eef37605` could not launch LingUBible; app returned to launcher. This run verifies a failure, not the information-page checklist.
- AndroidRuntime confirms `IllegalArgumentException: Cannot find class with name com.lingubible.app.domain.model.InformationPage` during navigation graph construction.
- Root cause: Navigation 2.8 resolves enum arguments using the serializer's original fully qualified name, while R8 renamed the new enum. The enum requires `androidx.annotation.Keep`.
- Fix: a narrowly scoped `@Keep` annotation on `InformationPage`. No global obfuscation disable or whole-model-package preservation.
- Regression guard: `tools/verify-play-release.py` now checks that this enum's original name is retained in both the current R8 mapping and shipped DEX. The pre-fix APK/AAB failed this added guard, recorded in `release-guard-regression-red.txt`.
- Official reference: https://developer.android.com/jetpack/androidx/releases/navigation (2.8 enum argument Keep requirement).
- Rebuild and replacement-install verification follows in device results. No recovery email, contact message, password change or sign-out occurred before this failure.
