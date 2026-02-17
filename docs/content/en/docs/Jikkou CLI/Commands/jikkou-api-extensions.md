---
title: "jikkou api-extensions"
linkTitle: "jikkou api-extensions"
---

Print the supported API extensions.

## Synopsis

The `api-extensions` command lists and inspects the API extensions registered in Jikkou. Extensions include resource
collectors, transformations, validations, actions, health indicators, and controllers.

This command has two subcommands:

- `api-extensions list` - List all extensions
- `api-extensions get` - Get details about a specific extension

## Subcommands

## `jikkou api-extensions list`

Print a summary table of all supported API extensions.

```bash
jikkou api-extensions list [flags]
```

## Examples

```bash
# List all extensions
jikkou api-extensions list

# List extensions of a specific category
jikkou api-extensions list --category Transformation

# List extensions from a specific provider
jikkou api-extensions list --provider kafka

# List extensions that support a specific resource kind
jikkou api-extensions list --kind KafkaTopic
```

## Options

| Flag | Default | Description |
|------|---------|-------------|
| `--category` | | Limit to extensions of the specified category |
| `--provider` | | Limit to extensions of the specified provider |
| `--kind` | | Limit to extensions that support the specified resource kind |

---

## `jikkou api-extensions get`

Print detailed information about a specific API extension, including its title, description, configuration options,
and usage examples.

```bash
jikkou api-extensions get <name> [flags]
```

## Examples

```bash
# Get details about an extension in WIDE format (default)
jikkou api-extensions get KafkaTopicMaxRetentionMs

# Get details in JSON format
jikkou api-extensions get KafkaTopicMaxRetentionMs -o JSON

# Get details in YAML format
jikkou api-extensions get KafkaTopicMaxRetentionMs -o YAML
```

## Options

| Flag | Short | Default | Description |
|------|-------|---------|-------------|
| `--output` | `-o` | `WIDE` | Output format. Valid values: JSON, YAML, WIDE |

## Options inherited from parent commands

| Flag | Description |
|------|-------------|
| `--logger-level=<level>` | Log level: TRACE, DEBUG, INFO, WARN, ERROR |

## SEE ALSO

- [jikkou api-resources](../jikkou-api-resources) - List available resource types
- [jikkou get](../jikkou-get) - Display resources of a given type
