# AGENTS.md

## Engineering principle: TDD

Test-Driven Development (TDD) is a mandatory development principle for this repository.

### Rule
For every new behavior or business rule, follow **Red → Green → Refactor**:

1. **Red** — write the smallest test that expresses the expected behavior and verify that it fails for the right reason.
2. **Green** — implement the minimum production code required to make the test pass.
3. **Refactor** — improve the design while keeping the tests green.

### Instructions for AI coding agents

- **Tests come before implementation** for new behavior whenever the change is testable.
- Do not implement a feature first and add tests afterward merely to increase coverage.
- When fixing a bug, add a regression test that reproduces the bug before changing production code when practical.
- Prefer fast JVM unit tests for domain and business rules.
- Keep tests deterministic, isolated, readable, and focused on behavior rather than implementation details.
- Do not weaken, delete, skip, or bypass an existing test just to make the build pass.
- Do not change production behavior solely to satisfy a test unless that behavior is part of the intended requirement.
- If a requirement is ambiguous, clarify the expected behavior through tests before implementation.
- Existing tests are part of the contract. Preserve them unless the intended behavior has deliberately changed.
- Before considering a change complete, run the narrowest relevant tests and then the repository's normal verification command.

### Definition of done

A behavior change is not complete when the code compiles. It is complete when:

- the expected behavior is expressed by automated tests;
- the tests pass;
- the implementation is appropriately simple;
- regressions are covered where applicable; and
- the normal CI/verification checks pass.

### Android guidance

- Keep business rules in testable domain code whenever possible.
- Prefer unit tests over instrumented tests when Android framework behavior is not required.
- UI tests are appropriate when the behavior genuinely depends on Compose/UI integration.
- Do not use coverage percentage as a substitute for meaningful tests.

### Commands

Use the repository's documented Gradle commands and prefer the CI-parity verification command when available.
