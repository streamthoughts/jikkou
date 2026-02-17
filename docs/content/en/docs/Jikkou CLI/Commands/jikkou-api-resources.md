---
title: "jikkou api-resources"
linkTitle: "jikkou api-resources"
---

Print the supported API resources.

## Synopsis

List the API resources supported by the Jikkou CLI or Jikkou API Server (in proxy mode). This command shows the
resource name, short names, API version, kind, and supported verbs for each resource type.

Use this command to discover which resource types are available with your current configuration and providers.

```bash
jikkou api-resources [flags]
```

## Examples

```bash
# List all available API resources
jikkou api-resources

# List resources for a specific API group
jikkou api-resources --api-group kafka.jikkou.io

# List resources that support specific verbs
jikkou api-resources --verbs LIST,GET

# List resources that support CREATE
jikkou api-resources --verbs CREATE

# Combine filters
jikkou api-resources --api-group kafka.jikkou.io --verbs LIST
```

## Sample output

```
NAME               SHORTNAMES   APIVERSION               KIND                  VERBS
kafkatopics        kt           kafka.jikkou.io/v1beta2  KafkaTopic            CREATE, DELETE, GET, LIST, UPDATE
kafkaconsumergroups              kafka.jikkou.io/v1beta2  KafkaConsumerGroup    DELETE, GET, LIST
```

## Options

| Flag | Default | Description |
|------|---------|-------------|
| `--api-group` | | Limit to resources in the specified API group |
| `--verbs` | | Limit to resources that support the specified verbs (comma-separated) |

## Options inherited from parent commands

| Flag | Description |
|------|-------------|
| `--logger-level=<level>` | Log level: TRACE, DEBUG, INFO, WARN, ERROR |

## SEE ALSO

- [jikkou api-extensions](../jikkou-api-extensions) - List and inspect available extensions
- [jikkou get](../jikkou-get) - Display resources of a given type
