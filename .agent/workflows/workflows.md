# Standard Engineering Workflows

These workflows define how changes are proposed and implemented.

## 1. Feature Development
1.  **Understand**: Clarify requirements before writing code.
2.  **Design**: Propose a minimal, clean design.
3.  **Implement**: Write code incrementally.
4.  **Verify**: Add linting and tests where suitable.
5.  **Document**: Update relevant documentation (README, API docs).

## 2. Refactoring
- **Restriction**: No functional changes unless explicitly requested.
- **Execution**: Refactor in small, atomic commits/steps.
- **Safety**: Preserve backward compatibility; ensure tests pass.

## 3. Bug Fixing
1.  **Analyze**: Identify the root cause.
2.  **Guard**: Add a regression test if possible.
3.  **Fix**: Apply the minimal necessary fix.

## 4. CI/CD
- **Mandatory**: CI pipelines must pass before merging.
- **Checks**: Linting and Build steps are mandatory gateways.
- **Discipline**: Never bypass the pipeline.

## 5. Monorepo Rules
- **Independence**: Frontend and Backend life cycles are independent.
- **Isolation**: No shared config files between root/frontend/backend unless fully justified.
- **Boundaries**: Respect module boundaries.
