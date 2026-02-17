---
title: "jikkou get"
linkTitle: "jikkou get"
---

Display one or many specific resources.

## Synopsis

Display one or many resources of a given type from the target platform. The `get` command uses dynamic subcommands
based on the resource types available from your configured providers. Use `jikkou api-resources` to discover the
available resource types.

When a resource name is specified, it retrieves that specific resource. Otherwise, it lists all resources of the
given type.

```bash
jikkou get <resource-type> [name] [flags]
```

## Examples

```bash
# List all Kafka topics
jikkou get kafkatopics

# Get a specific Kafka topic by name
jikkou get kafkatopics my-topic

# List resources with a selector
jikkou get kafkatopics -s 'metadata.name MATCHES (my-.*)'

# List resources with JSON output
jikkou get kafkatopics -o JSON

# List resources as a ResourceListObject
jikkou get kafkatopics --list

# List resources with custom options
jikkou get kafkatopics --options describe-default-configs=true

# List resources targeting a specific provider
jikkou get kafkatopics --provider kafka-prod

# Discover available resource types
jikkou api-resources --verbs LIST,GET
```

## Options

| Flag | Short | Default | Description |
|------|-------|---------|-------------|
| `--selector` | `-s` | | Selector expression for including or excluding resources |
| `--selector-match` | | `ALL` | Selector matching strategy. Valid values: ALL, ANY, NONE |
| `--output` | `-o` | `YAML` | Output format. Valid values: JSON, YAML |
| `--list` | | `false` | Output resources as a ResourceListObject |
| `--provider` | | | Select a specific provider instance |

Additional options may be available depending on the resource type. These are dynamically registered based on the
provider's `ApiOptionSpec` definitions. Use `jikkou api-extensions get <extension-name>` to view options for a
specific resource collector.

## Options inherited from parent commands

| Flag | Description |
|------|-------------|
| `--logger-level=<level>` | Log level: TRACE, DEBUG, INFO, WARN, ERROR |

## SEE ALSO

- [jikkou api-resources](../jikkou-api-resources) - List available resource types
- [jikkou api-extensions](../jikkou-api-extensions) - List and inspect extensions
- [jikkou diff](../jikkou-diff) - Show changes between desired and actual state
