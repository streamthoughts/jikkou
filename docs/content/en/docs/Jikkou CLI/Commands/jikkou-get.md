---
title: "jikkou get"
linkTitle: "jikkou get"
---

Display one or many specific resources.

## Synopsis

Display one or many resources of a given type from the target platform. The `get` command groups resources under
their provider (e.g. `kafka`, `schemaregistry`, `aiven`), so invocations follow the form
`jikkou get <provider> <kind>`. Use `jikkou api-providers list` to discover the available providers and
`jikkou api-resources` to discover the available resource types.

When a resource name is specified via `--name`, it retrieves that specific resource. Otherwise, it lists all
resources of the given type.

```bash
jikkou get <provider> <resource-type> [flags]
```

**Resource naming.** Under a provider, each resource is exposed by its provider-local name when one is declared
(e.g. `topics` under `kafka`, `subjects` under `schemaregistry`). The full plural name (e.g. `kafkatopics`,
`schemaregistrysubjects`) and any short names (e.g. `kt`, `sr`) remain valid aliases inside the provider scope.

**Backward compatibility.** The flat form `jikkou get <resource-type>` is still supported but prints a deprecation
notice on stderr and will be removed in a future release. Prefer the qualified form
`jikkou get <provider> <resource-type>`.

## Examples

```bash
# List all Kafka topics (canonical form)
jikkou get kafka topics

# Same, using the plural name as alias
jikkou get kafka kafkatopics

# Same, using the short name as alias
jikkou get kafka kt

# Get a specific Kafka topic by name
jikkou get kafka topics --name my-topic

# List resources with a selector
jikkou get kafka topics -s 'metadata.name MATCHES (my-.*)'

# List resources with JSON output
jikkou get kafka topics -o JSON

# List resources as a ResourceListObject
jikkou get kafka topics --list

# List resources with custom options
jikkou get kafka topics --options describe-default-configs=true

# List resources targeting a specific provider instance
jikkou get kafka topics --provider kafka-prod

# List schema registry subjects
jikkou get schemaregistry subjects

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
