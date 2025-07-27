---
categories: [ ]
tags: [ "concept", "feature", "extension" ]
title: "Resource Repositories"
linkTitle: "Resource Repositories"
weight: 12
---

## Overview

A **Resource Repository** is an extensible component in Jikkou that enables dynamic loading of resources from various
sources (e.g., local directories, remote Git repositories) into the execution context.

Repositories are typically used to:

- Load reusable resources across multiple environments or teams
- Keep transient or computed resources separate from persistent definitions
- Inject configuration or validation policies dynamically

This feature is particularly useful when you want to define **shareable** or **transient** resources such
as `ConfigMap`, `ValidatingResourcePolicy`, or other resource types outside the CLI input or local context.

## Configuration

Jikkou allows you to configure multiple repositories as follows:

```hocon
jikkou {
  repositories: [
    {
      # Unique name for the repository
      name = "<repository-name>"

      # Fully qualified class name or alias for the repository type
      type = "<repository-class-or-alias>"

      # Optional configuration specific to the repository implementation
      config = {
        # key = value
      }
    }
  ]
}
```

## Built-in implementations

Jikkou ships with the following built-in `ResourceRepository` implementations:

### LocalResourceRepository

Loads resources from local files or directories.

**Type**: `io.streamthoughts.jikkou.core.repository.LocalResourceRepository`

**Example Configuration**

```yaml
jikkou {
  repositories = [
    {
      name = "local"
      type = io.streamthoughts.jikkou.core.repository.LocalResourceRepository
      config {
        files = [
          "./resources/",
          "./policies/"
        ]
      }
    }
  ]
}
```

See more: [LocalResourceRepository Configuration]({{% relref "../Providers/Core/Repositories/local" %}})

### GitHubResourceRepository

Loads resources from a public or private GitHub repository.

**Type**: `io.streamthoughts.jikkou.core.repository.GitHubResourceRepository`

**Example Configuration**

```yaml
jikkou {
  repositories = [
    {
      name = "github-repository"
      type = io.streamthoughts.jikkou.core.repository.GitHubResourceRepository
      config {
        repository = "streamthoughts/jikkou"
        branch = "main"
        paths = [
          "examples/",
          "config/"
        ]
        # Optionally set an access token for private repositories
        # token = ${?GITHUB_TOKEN}
      }
    }
  ]
}
```

See more: [GitHubResourceRepository Configuration]({{% relref "../Providers/Core/Repositories/github" %}})
