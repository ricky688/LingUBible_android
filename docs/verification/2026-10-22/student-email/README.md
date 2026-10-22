# Native student email restriction — 2026-10-22

Native password login, registration and Google sign-in now require an exact @ln.hk student address. Added EmailValidator.isValidStudentEmail and used it in AuthViewModel guards and AppwriteAuthRepository.isValidEmail (also checked after Google OAuth). Updated native entry hints/errors to show only @ln.hk. Domain matching remains case-insensitive and trimmed; malformed and spoofed domains are rejected.

Preserved the broader institutional validator for recovery, allowing existing @ln.edu.hk accounts to retain password-recovery access. Existing sessions are not revoked. No web/backend policy, stored account data, signing, version or dependency changes.

All 555 unit tests passed, including the new student-address regression test and existing recovery tests. Optimized release built in 3m49s and installed as a matching-certificate update on Waydroid; installed bytes verified. No live password/Google sign-in was attempted, and the owner account was not signed out. No new UI test was authored. Existing nonfatal toolchain metadata warnings remain.

APK SHA-256: 0618fadbc9913a3f73d9cd28909708ed7336ecec9be50a69bdfead6e07d01878
