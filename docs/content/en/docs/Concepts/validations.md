---
tags: [ "concept", "feature", "extension" ]
title: "Validations"
linkTitle: "Validations"
weight: 6
---

{{% pageinfo color="info" %}}
_**Validations**_ are applied to inbound resources to ensure that the resource entities adhere to specific rules or
constraints.
These validations are carried out after the execution of the transformation chain and before the reconciliation process
takes place.
{{% /pageinfo %}}

## Available Validations

You can list all the available validations using the Jikkou CLI command:

```bash
jikkou api-extensions list --category=validation [--kinds <a resource kind to filter returned results>]
```

## Validation chain

When using Jikkou CLI, you can configure a _validation chain_ that will be applied to every resource.
This chain consists of multiple validations, each designed to handle different types of resources. Jikkou ensures
that a validation is executed only for the resource types it supports. In cases where a resource is not
accepted by a validation, it is passed to the next validation in the chain.
This process continues until a suitable validation is found or until all validations have been attempted.

### Configuration

```hocon
jikkou {
  # The list of validations to execute
  validations: [
    {
      # Custom name for the validation rule
      name = ""
      # Simple or fully qualified class name of the validation extension.
      type = ""
      config = {
        # Configuration properties for this validation
      }
    }
  ]
}
```

{{% alert title="Tips" color="info" %}}
The `config` object of a _Validation_ always fallback on the top-level `jikkou` config. This allows you to globally
declare some properties of the validation configuration.
{{% /alert %}}

### Example

```hocon
jikkou {
  # The list of transformations to execute
  validations: [
    {
      # Custom name for the validation rule
      name = topicMustBePrefixedWithRegion
      # Simple or fully qualified class name of the validation extension.
      type = TopicNameRegexValidation
      # The config values that will be passed to the validation.
      config = {
        topicNameRegex = "(europe|northamerica|asiapacific)-.+"
      }
    }
  ]
}
```