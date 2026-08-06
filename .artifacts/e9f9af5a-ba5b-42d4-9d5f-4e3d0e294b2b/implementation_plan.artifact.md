# Fix Gradle Sync Error: Unresolved reference 'android'

The project is failing to sync because several plugin and library references in the `build.gradle.kts` files are missing from the Version Catalog (`libs.versions.toml`). The specific error `Unresolved reference 'android'` refers to `libs.plugins.kotlin.android`.

## Proposed Changes

### [Gradle Configuration]

#### [MODIFY] [libs.versions.toml](file:///C:/Users/marci/Projects/Presencial/gradle/libs.versions.toml)
- Add missing plugin definitions: `kotlin-android`, `ksp`, and `hilt`.
- Add missing version definitions for KSP, Hilt, Room, Work, Navigation, etc.
- Add missing library definitions used in `app/build.gradle.kts`.
- Standardize library names to match the usage in `app/build.gradle.kts` (e.g., `androidx-ui` instead of `androidx-compose-ui`).

## Verification Plan

### Automated Tests
- Run `./gradlew help` or trigger a Gradle Sync in Android Studio to verify that the "Unresolved reference" errors are resolved.

### Manual Verification
- Verify that the `app` module builds successfully.
