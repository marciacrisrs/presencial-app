# Fix Lint/Build issues in PresencialWidget.kt

This plan aims to resolve `LongParameterList`, `MaxLineLength`, and `UnusedPrivateProperty` findings in `PresencialWidget.kt` while maintaining existing functionality and improving code quality.

## Proposed Changes

### [Component] App Widget

#### [MODIFY] [PresencialWidget.kt](file:///C:/Users/marci/Projects/Presencial/app/src/main/java/com/presencial/app/widget/PresencialWidget.kt)

- **LongParameterList**:
    - Introduce two private data classes to group related parameters:
        - `WidgetProgress`: holds `completed`, `required`, and `remaining` (Int values).
        - `WidgetColors`: holds `success` and `secondaryText` (ColorProvider values).
    - Refactor `MediumLayout` and `LargeLayout` to accept these objects instead of individual parameters.
    - Update `SmallLayout` to also use these objects for consistency, even though it wasn't strictly over the limit.
- **MaxLineLength**:
    - Break long lines in function signatures (93, 94, 107, 113, 132) and function calls (121) using idiomatic Kotlin formatting (parameters on separate lines).
- **UnusedPrivateProperty**:
    - Remove `WIDGET_FONT_SIZE_LABEL` and `WIDGET_FONT_SIZE_MAIN` as they are not used in the codebase.

## Verification Plan

### Automated Tests
- Run the build for the `:app` module to ensure no compilation errors:
  `gradlew :app:assembleDebug`
- Run lint/detekt check (if possible via gradle task) to verify findings are gone.
  I will try to find the specific task, likely `:app:lintDebug` or `:app:detekt`.

### Manual Verification
- Review the diff to ensure logic and appearance are preserved.
- Use `render_compose_preview` if applicable (though Glance previews are limited).
