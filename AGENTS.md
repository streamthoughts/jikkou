# AGENTS.md - Development Guidelines for AI Coding Agents

This document provides comprehensive guidelines for AI coding agents working on the **Jikkou** project - an open-source Resource as Code framework for Apache Kafka.

## Project Overview

**Jikkou** is a Java based multi-module Maven project that provides Infrastructure as Code management for Apache Kafka resources. It's designed with a kubectl-inspired approach for managing Topics, ACLs, Quotas, Schemas, and Connectors.

- **Language**: Java 25
- **Build System**: Maven (multi-module)
- **Architecture**: Micronaut-based CLI and API server
- **Distribution**: Native binaries (GraalVM), JAR, Docker

## Project Structure

```
jikkou/
├── cli/                              # Command-line interface (main entry)
├── core/                             # Core APIs and engine
├── server/                           # REST API server components
├── providers/                        # Resource provider implementations
├── template-jinja/                   # Jinja templating support
├── extension-rest-client/            # REST client extensions
├── resource-generator/               # Resource generation utilities
└── processor/                        # Processing components
```

**Main Entry Points**:
- CLI: `io.streamthoughts.jikkou.client.Jikkou`
- Server: `io.streamthoughts.jikkou.rest.JikkouApiServer`

## Build, Test, and Lint Commands

### Build Commands
```bash
# Full build with tests
./mvnw clean verify

# Build without tests
./mvnw clean verify -DskipTests

# Build specific module
./mvnw clean verify -pl cli

# Native build (requires GraalVM)
./mvnw clean verify -Pnative

# Docker image build
make
```

### Test Commands
```bash
# Run all tests (unit + integration)
./mvnw test

# Run unit tests only
./mvnw surefire:test

# Run integration tests only
./mvnw failsafe:integration-test

# Run single test class
./mvnw test -Dtest=ClassNameTest

# Run single test method
./mvnw test -Dtest=ClassNameTest#methodName

# Run tests for specific module
./mvnw test -pl core

# Run tests with specific profile
./mvnw test -P native
```

### Lint and Code Quality
```bash
# Apply code formatting
./mvnw spotless:apply

# Check code formatting
./mvnw spotless:check

# Run SpotBugs analysis
./mvnw spotbugs:check

# Generate coverage report
./mvnw jacoco:report
```

## Code Style Guidelines

### Import Organization
- Use **standard import order** (enforced by Spotless)
- Remove unused imports automatically
- Static imports should be grouped separately and placed after regular imports
- Example ordering:
```java
import static picocli.CommandLine.Model.CommandSpec;
import static picocli.CommandLine.Model.UsageMessageSpec.SECTION_KEY_COMMAND_LIST;

import io.micronaut.configuration.picocli.MicronautFactory;
import io.streamthoughts.jikkou.client.banner.Banner;
import jakarta.inject.Singleton;
import java.io.PrintWriter;
import java.util.ArrayList;
import org.jetbrains.annotations.NotNull;
```

### Formatting Standards
- **Indentation**: 4 spaces (no tabs)
- **Line Length**: 120 characters maximum
- **Encoding**: UTF-8
- **License Header**: Apache 2.0 (enforced by Spotless)
```java
/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
```

### Type System
- Use **Java 25 features** appropriately (records, pattern matching, etc.)
- Prefer **immutable objects** and **builder patterns**
- Use **generic types** with proper bounds
- Annotate with `@NotNull` and `@Nullable` from JetBrains annotations
- Example:
```java
public interface ApiBuilder<A extends JikkouApi, B extends JikkouApi.ApiBuilder<A, B>> {
    B register(@NotNull ExtensionProvider provider);
}
```

### Naming Conventions
- **Classes**: PascalCase (e.g., `JikkouApi`, `ResourceTemplateRenderer`)
- **Methods/Variables**: camelCase (e.g., `getResourceTypes`, `extensionProvider`)
- **Constants**: SCREAMING_SNAKE_CASE (e.g., `SECTION_KEY_COMMAND_LIST`)
- **Packages**: lowercase with dots (e.g., `io.streamthoughts.jikkou.core`)
- **Test Classes**: Suffix with `Test` (e.g., `JikkouApiTest`)
- **Integration Tests**: Suffix with `IT` (e.g., `KafkaProviderIT`)

### Error Handling
- Use **custom exception hierarchies** extending `JikkouApiException`
- Include **meaningful error messages** with context
- Prefer **checked exceptions** for recoverable errors
- Use **runtime exceptions** for programming errors
- Example:
```java
public class ResourceNotFoundException extends JikkouApiException {
    public ResourceNotFoundException(String message, Object... args) {
        super(String.format(message, args));
    }
}
```

### Dependency Injection
- Use **Micronaut annotations** (`@Singleton`, `@Inject`)
- Prefer **constructor injection** over field injection
- Use **factory patterns** for complex object creation
- Example:
```java
@Singleton
public class ApiExtensionCommand {
    private final JikkouApi api;

    public ApiExtensionCommand(JikkouApi api) {
        this.api = api;
    }
}
```

## Testing Guidelines

### Test Structure
- **Unit Tests**: `src/test/java/`
- **Integration Tests**: `src/integration-test/java/`
- Use **JUnit 5** as primary testing framework
- Use **TestContainers** for integration tests requiring external services
- Use **Mockito** for mocking dependencies

### Test Naming
```java
class JikkouApiTest {
    @Test
    void shouldReturnResourceTypes_whenValidProviderRegistered() {
        // Test implementation
    }

    @Test
    void shouldThrowResourceNotFoundException_whenResourceNotFound() {
        // Test implementation
    }
}
```

### Test Categories
- **@Test**: Standard unit tests
- **@ParameterizedTest**: Data-driven tests
- **@TestMethodOrder**: For ordered test execution
- **@TestContainers**: For integration tests with Docker

## Documentation Standards

### JavaDoc
- **Public APIs** must have comprehensive JavaDoc
- Include `@param`, `@return`, `@throws` annotations
- Use `@since` for version information
- Example:
```java
/**
 * Registers an extension provider with the given configuration.
 * <p>
 * This method is responsible for registering all extensions and resources
 * provided by the given provider.
 *
 * @param provider      the provider.
 * @param configuration the configuration.
 * @return the builder.
 * @throws ConflictingExtensionDefinitionException if provider conflicts
 * @since 0.35.0
 */
```

### Code Comments
- Use `//` for single-line comments
- Use `/* */` for multi-line explanations
- Explain **why**, not **what**
- Document complex algorithms and business logic

## Configuration Management

### Application Configuration
- Use **TypeSafe Config** for configuration management
- Support **YAML** and **properties** formats
- Environment variable overrides supported
- Configuration validation with Jakarta Validation

### Module Configuration
Each provider module follows this pattern:
```
providers/jikkou-provider-{name}/
├── src/main/java/io/streamthoughts/jikkou/provider/{name}/
├── src/test/java/
├── src/integration-test/java/
└── pom.xml
```

## Key Dependencies

### Core Dependencies
- **Micronaut**: Dependency injection and framework
- **Picocli**: Command-line interface
- **Jackson**: JSON/YAML serialization
- **Kafka Client**: Apache Kafka integration
- **Jinja**: Template rendering engine

### Development Tools
- **Spotless**: Code formatting and license headers
- **SpotBugs**: Static analysis with security rules
- **JaCoCo**: Code coverage reporting
- **TestContainers**: Integration testing

## Common Pitfalls and Best Practices

### Performance Considerations
- Use **reactive programming** with Reactor Core for I/O operations
- Implement **proper resource management** (try-with-resources)
- Consider **GraalVM native compilation** constraints
- Avoid blocking operations in reactive streams

### Security Guidelines
- **Validate all inputs** with Jakarta Validation
- Use **secure coding practices** (SpotBugs security rules enforced)
- Handle **sensitive configuration** properly
- Follow **least privilege** principle

### Concurrency
- Use **thread-safe collections** when needed
- Prefer **immutable objects** to reduce synchronization
- Use **CompletableFuture** for async operations
- Consider **reactive patterns** over traditional threading

## Pre-commit Checklist

Before committing code, ensure:
- [ ] Code compiles successfully: `./mvnw compile`
- [ ] All tests pass: `./mvnw test`
- [ ] Code formatting applied: `./mvnw spotless:apply`
- [ ] No SpotBugs violations: `./mvnw spotbugs:check`
- [ ] Integration tests pass (if applicable): `./mvnw verify`
- [ ] License headers present on new files
- [ ] JavaDoc added for public APIs
- [ ] Commit message follows conventional format

## Pull request guidelines

Always add tests, keep your branch rebased instead of merged,
and adhere to the commit message recommendations from https://www.conventionalcommits.org/en/v1.0.0.

---

This document should be updated as the project evolves. For the most current information, refer to the [official documentation](https://streamthoughts.github.io/jikkou/).
