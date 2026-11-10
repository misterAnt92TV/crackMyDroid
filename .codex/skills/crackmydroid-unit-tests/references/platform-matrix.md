# Test source-set routing

Use the shared source-of-truth matrix in
[the development skill](../../crackmydroid-development/references/platform-matrix.md). This
reference exists only to route a test task back to that matrix; it intentionally contains no second
table of frameworks or Gradle commands.

For test-specific design guidance, use this skill's `SKILL.md`: tests must remain behavior-focused,
use the framework available in the selected source set, and must not silently expand into production
changes.

## Pattern selection

### ViewModel or state holder

Use a deterministic dispatcher, stub repository calls with `coEvery`, invoke the public action, call
`advanceUntilIdle()`, and assert the exposed state. Include an empty/error response when the
ViewModel maps those states. Verify a repository interaction only when it represents a meaningful
contract.

### UseCase

Prefer real domain models and assert the returned result or thrown error. Use MockK for repository
boundaries, stub success and failure, and verify that delegation or persistence occurs exactly when
it is part of the UseCase contract.

### Repository or platform adapter

For Desktop process, ADB, or filesystem adapters, use a fake bridge or isolated temporary input when
stateful behavior matters. Assert the parsed result, generated command, or error mapping. Never
depend on a connected device, the user's filesystem, or ambient environment variables.

### Parser, serializer, or analyzer

Use real representative input. Cover valid input, empty/malformed input, and the security or domain
boundary that the parser/analyzer is responsible for. Avoid mocks unless an external boundary is
genuinely involved.

When a task name differs by build variant or is unavailable, inspect the shared matrix and Gradle
tasks, then use the closest task for the selected source set and report the exact command used.
