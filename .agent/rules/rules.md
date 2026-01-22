---
trigger: always_on
---

# Global Engineering Rules

These rules are **non-negotiable** and apply to all future actions.

## 1. Design Principles
- **SOLID Principles**: Applied by default.
- **Composition over Inheritance**: Favor modular composition.
- **Readability**: Code must be self-documenting and clean.
- **No Premature Abstraction**: Don't generalize until necessary.
- **No Over-Engineering**: Solve the problem at hand, nothing more.å
- **YAGNI**: "You Aren't Gonna Need It" — features are added only when requested.
- **KISS**: Keep It Simple, Stupid — simplicity beats cleverness.

## 2. Architecture
- **Separation of Concerns**: Strict boundaries between Frontend, Backend, and Infra.
- **No Leakage**: Implementation details must not leak across layers.
- **Stateless Backend**: Backend services should be stateless where possible.
- **Explicit Dependencies**: Dependency injection over global state.

## 3. Code Quality
- **Functions**: Small, single-responsibility functions.
- **Naming**: Variables and functions must have meaningful names.
- **Dead Code**: Strictly forbidden. Remove if unused.
- **Error Handling**: Fail fast with clear, actionable error messages.
- **Logging**: Use structured logging instead of `print` statements (except for defined CLI tools).
- **Mandatory Testing**: Test cases must be written for all backend (JUnit/Mockito) and frontend (Vitest/RTL) code implemented.

## 4. Configuration
- **Environment-Based**: Configuration must be injected via environment variables.
- **Secrets**: No hardcoded secrets in code or git.
- **Compatibility**: Configs must work across Docker and CI environments.

## 5. Default Assumptions
- **Production-Ready**: Code is written for production, not just a demo.
- **Simplicity**: Prefer the confusingly simple over the flexible but complex.
- **Explicit**: Explicit configuration and code paths over implicit magic.
- **Avoid over engineering**: Follow the given context and dont assume by yourself