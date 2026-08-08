# Implementation Plan - Widget Quality and Coverage Fixes

This plan addresses SonarQube findings related to the small, medium, and large widgets, focusing on code coverage, internationalization, XML hierarchy optimization, and architectural improvements.

## User Review Required

> [!IMPORTANT]
> The month name logic will be updated to use the system locale instead of hardcoded "pt-BR".

> [!NOTE]
> The `previewLayout` attribute will be handled to avoid warnings on APIs < 31 while maintaining preview functionality for newer versions.

## Proposed Changes

### 1. Logic Extraction & Architecture

#### [NEW] [WidgetInfoProvider.kt](file:///C:/Users/marci/Projects/Presencial/app/src/main/java/com/presencial/app/widget/WidgetInfoProvider.kt)
Create a new class to handle all calculations and data formatting. This makes the logic testable without Glance or Android UI components.
- Progress calculation.
- Remaining days calculation.
- Formatting progress text (e.g., "1 / 13").
- Formatting remaining days text using plurals.
- Formatting month title.

#### [MODIFY] [PresencialWidget.kt](file:///C:/Users/marci/Projects/Presencial/app/src/main/java/com/presencial/app/widget/PresencialWidget.kt)
- Refactor `WidgetContent` to use `WidgetInfoProvider`.
- Remove manual pluralization logic.
- Use system locale for month names.
- Clean up duplication between layouts.

---

### 2. Resources (Strings & Plurals)

#### [NEW] [plurals.xml](file:///C:/Users/marci/Projects/Presencial/app/src/main/res/values/plurals.xml)
- Add `widget_remaining_days` plural.

#### [MODIFY] [strings.xml](file:///C:/Users/marci/Projects/Presencial/app/src/main/res/values/strings.xml)
- Ensure all required strings are present and correctly parameterized.
- Remove redundant/unused strings if confirmed.

---

### 3. XML Layouts & Previews

#### [MODIFY] [widget_preview_small.xml](file:///C:/Users/marci/Projects/Presencial/app/src/main/res/layout/widget_preview_small.xml)
#### [MODIFY] [widget_preview_medium.xml](file:///C:/Users/marci/Projects/Presencial/app/src/main/res/layout/widget_preview_medium.xml)
#### [MODIFY] [widget_preview_large.xml](file:///C:/Users/marci/Projects/Presencial/app/src/main/res/layout/widget_preview_large.xml)
- Remove unnecessary `FrameLayout` wrappers.
- Fix 10sp font size in `large` layout to 11sp.
- Replace hardcoded strings with resource references (using tools:text for design-time values if possible, or just @string).

#### [MODIFY] [presencial_widget_info_small.xml](file:///C:/Users/marci/Projects/Presencial/app/src/main/res/xml/presencial_widget_info_small.xml)
#### [MODIFY] [presencial_widget_info_medium.xml](file:///C:/Users/marci/Projects/Presencial/app/src/main/res/xml/presencial_widget_info_medium.xml)
#### [MODIFY] [presencial_widget_info_large.xml](file:///C:/Users/marci/Projects/Presencial/app/src/main/res/xml/presencial_widget_info_large.xml)
- Move `previewLayout` to `res/xml-v31/` to satisfy Sonar while keeping functionality on supported APIs.

---

### 4. Verification & Testing

#### [NEW] [WidgetInfoProviderTest.kt](file:///C:/Users/marci/Projects/Presencial/app/src/test/java/com/presencial/app/widget/WidgetInfoProviderTest.kt)
- Unit tests for `WidgetInfoProvider`.
- Coverage for all scenarios: 0 days, 1 day, multiple days, goal reached, etc.

## Verification Plan

### Automated Tests
- Run `./gradlew test` to verify all unit tests pass and coverage is > 80% on new code.
- Run `./gradlew lint` to check for XML/resource issues.

### Manual Verification
- Inspect the widgets in the emulator/device to ensure the UI remains identical (except for the requested font size increase).
- Verify the month name changes according to the system language.
