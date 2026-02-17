---
tags: [ "concept", "feature", "extension" ]
title: "Providers"
linkTitle: "Providers"
weight: 11
---

{{% pageinfo color="info" %}}
**Providers** are pluggable modules that supply Jikkou with extensions and resource definitions for a specific
platform or service (e.g., Apache Kafka, Schema Registry, Aiven).
{{% /pageinfo %}}

## What is a Provider?

A provider is a pluggable module that registers a cohesive set of
extensions (controllers, collectors, transformations, validations, actions, health
indicators) and [resource]({{% relref "./resource" %}}) types into Jikkou at runtime.

Each provider targets a specific platform and is responsible for:

- **Registering extensions** such as controllers, collectors, transformations, validations, and actions.
- **Registering resource types** that describe the API resources it manages.
- **Accepting configuration** specific to its platform (e.g., Kafka bootstrap servers, Schema Registry URL).

## Configuration

Providers are configured in the Jikkou configuration file under the `jikkou.provider` namespace. Each provider entry
has a unique name and includes its type, an `enabled` flag, and a `config` block.

```hocon
jikkou {
  provider.kafka {
    enabled = true
    type = io.streamthoughts.jikkou.kafka.KafkaExtensionProvider
    config = {
      client {
        bootstrap.servers = "localhost:9092"
      }
    }
  }
}
```

### Configuration Properties

| Property  | Type    | Description                                                                      |
|-----------|---------|----------------------------------------------------------------------------------|
| `enabled` | Boolean | Whether this provider is active. Defaults to `true`.                             |
| `type`    | String  | Fully qualified class name of the provider implementation.                       |
| `default` | Boolean | Mark this instance as the default for its provider type. Defaults to `false`.    |
| `config`  | Object  | Provider-specific configuration (e.g., connection settings).                     |

## Multiple Instances

You can configure multiple instances of the same provider type by giving each a unique name. This is useful when you
need to manage resources across different environments (e.g., dev vs. production Kafka clusters) from a single Jikkou
installation.

Use `default = true` to mark one instance as the default for its provider type:

```hocon
jikkou {
  provider.kafka-prod {
    enabled = true
    type = io.streamthoughts.jikkou.kafka.KafkaExtensionProvider
    default = true
    config = {
      client.bootstrap.servers = "kafka-prod:9092"
    }
  }
  provider.kafka-staging {
    enabled = true
    type = io.streamthoughts.jikkou.kafka.KafkaExtensionProvider
    config = {
      client.bootstrap.servers = "kafka-staging:9092"
    }
  }
}
```

Use the `--provider` flag to target a specific provider instance when running commands:

```bash
# Uses the default provider (kafka-prod)
jikkou get kafkatopics

# Explicitly target the staging provider
jikkou get kafkatopics --provider kafka-staging

# Apply resources to staging
jikkou apply -f my-resources.yaml --provider kafka-staging
```

### Provider Resolution

The `--provider` flag is **always optional**. Jikkou resolves the target provider using the following fallback chain:

1. **Single provider of a given type**: It is used automatically — no `--provider` flag needed, no `default` property
   needed. If you only have one Kafka provider configured, everything works exactly as before.
2. **Multiple providers, one marked `default = true`**: The default is used when `--provider` is omitted. You only need
   the flag when targeting a non-default instance.
3. **Multiple providers, no default**: You **must** specify `--provider` on every command. Omitting it results in an
   error: *"No default configuration defined, and multiple configurations found for provider type"*.

{{% alert title="Note" color="info" %}}
Existing single-provider configurations continue to work without any changes. The `default` property and `--provider`
flag only matter once you add a second instance of the same provider type.
{{% /alert %}}

Provider selection works across all commands — `apply`, `create`, `update`, `delete`, `diff`, `validate`, `replace`,
`patch`, `get`, `action`, and `health` — and extends to the REST API server with a `provider` field in reconciliation
request bodies.

## Built-in Providers

Jikkou ships with the following built-in extension providers:

| Provider                                                         | Description                                                     |
|------------------------------------------------------------------|-----------------------------------------------------------------|
| [Apache Kafka]({{< ref "/docs/providers/kafka" >}})              | Manage Kafka Topics, ACLs, Quotas, and Consumer Groups          |
| [Schema Registry]({{< ref "/docs/providers/schema registry" >}}) | Manage Schema Registry subjects and schemas                     |
| [Kafka Connect]({{< ref "/docs/providers/kafka connect" >}})     | Manage Kafka Connect connectors                                 |
| [Aiven]({{< ref "/docs/providers/aiven" >}})                     | Manage Aiven-specific resources (ACLs, Quotas, Schema Registry) |
| [AWS]({{< ref "/docs/providers/aws" >}})                         | Manage AWS Glue Schema Registry resources                       |
| [Core]({{< ref "/docs/providers/core" >}})                       | Core resource types (e.g., ConfigMap)                           |

## Discovering Providers

You can inspect providers and the extensions they contribute using the CLI:

```bash
# List all registered extensions with their provider
jikkou api-extensions list

# List extensions for a specific provider
jikkou api-extensions list --provider kafka

# List API resources and their supported verbs
jikkou api-resources
```

## SEE ALSO

- [Controllers]({{% relref "./controller" %}}) - Extensions that reconcile resources
- [Collectors]({{% relref "./collector" %}}) - Extensions that collect resource state
- [Configuration]({{% relref "../Jikkou CLI/CLI-Configuration" %}}) - How to configure providers via contexts
