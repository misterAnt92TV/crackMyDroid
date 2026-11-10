---
name: crackmydroid-unit-tests
description: Generate or correct purposeful Kotlin Multiplatform unit tests for CrackMyDroid, selecting the right source set for common, JVM, Android, Desktop, or iOS code and validating with the matching Gradle task.
metadata:
  short-description: Create platform-aware CrackMyDroid unit tests
---

# CrackMyDroid Unit Tests

Use this skill when the user asks to add, repair, or improve unit tests in this repository. The
output must protect a real behavior of the application, not merely increase the test count.

This skill owns test files and test-only dependencies. It does not change production contracts or
architecture; if production code must change for compilation or correctness, stop at the smallest
diagnosis and apply the development skill for that change.

## Scope and discovery

1. Identify the requested production target and its Gradle module. Read the production class, its
   public collaborators, and any existing test before editing.
2. Inspect the target source-set path and choose the narrowest applicable test source set using
   [the platform matrix](../crackmydroid-development/references/platform-matrix.md), which is the
   single source of truth for source sets, frameworks, and Gradle tasks.
3. Derive cases from observable behavior: returned values, emitted state, thrown errors,
   persisted/exported data, commands sent to collaborators, and meaningful calls to dependencies.
4. Keep production code unchanged. Only add or update test files and, when required for compilation,
   the smallest test-only dependency change.

If the request names a package or module rather than one class, work in bounded batches and
prioritize ViewModels, UseCases, repositories, then pure models/parsers. Do not generate a blanket
suite for unrelated code.

## Platform selection

- `shared/src/commonMain` → `shared/src/commonTest`: use only Kotlin Multiplatform-compatible test
  APIs and real domain values. Do not import JVM-only MockK or JUnit.
- Shared ViewModels, UseCases, and JVM-compatible collaborators → `shared/src/jvmCommonTest`: use
  MockK and `kotlin.test` with coroutine-test, following the existing project patterns.
- Desktop/ADB/process/filesystem implementations → `shared/src/desktopTest`: isolate process and
  filesystem boundaries with fakes or MockK where supported; never require a real ADB device or
  host-specific state.
- Android-only production code → `androidApp/src/test` or the module's `androidUnitTest`: use MockK
  and JUnit 4 (`org.junit.Test`, `org.junit.Assert`), with Android framework dependencies mocked or
  avoided.
- iOS/native code → the matching `iosTest` source set only when the test is
  Kotlin/Native-compatible. Do not force MockK or JUnit into native tests; prefer `kotlin.test` and
  platform-neutral fakes.

If the requested platform cannot support the user's requested library, explain the constraint in the
final report and preserve the platform-compatible test rather than creating a test that cannot
compile.

## Test design requirements

Every target must have purposeful cases covering, where applicable:

- the normal successful path;
- a meaningful failure, exception, rejected result, or dependency failure;
- empty, boundary, duplicate, or malformed input;
- initial/loading/empty state and asynchronous completion for stateful code;
- observable side effects such as repository calls, persistence, exports, navigation intent, or
  command construction.

Use real model instances for pure transformation and business-rule tests. Mock only a collaborator
whose behavior is outside the unit under test. A test must assert an outcome or observable
interaction; do not assert private implementation details, call counts without behavioral meaning,
or a mock setup with no assertion.

For coroutine-based code:

- use `kotlinx-coroutines-test` and a deterministic test dispatcher/test scope;
- advance scheduled work with `advanceUntilIdle()` or the smallest relevant virtual-time operation;
- verify state after the coroutine has settled;
- reset any globally installed dispatcher and cancel/close owned scopes in teardown.

For MockK-based tests:

- use `every`/`coEvery` for stubbing and `verify`/`coVerify` for meaningful effects;
- use `confirmVerified` only after all relevant interactions are asserted;
- clear or unmock resources in teardown when static/object mocking is used;
- avoid relaxed mocks when their default values could hide a broken behavior.

Prefer descriptive backtick test names that state the behavior and result. Arrange, Act, and Assert
should be obvious from the test body. Reuse existing project fixtures and naming conventions when
they are correct, but derive coverage from production code rather than copying existing tests.

## Validation

Run the narrowest applicable Gradle task from the platform matrix, then widen validation only when
shared code or dependencies require it. At minimum:

- compile and execute the changed test source set;
- run the relevant test class or method first when iterating;
- run `git diff --check` on changed files;
- report files changed, cases covered, command executed, and any platform limitation.

Do not claim coverage percentages unless a coverage task was explicitly requested and actually run.
