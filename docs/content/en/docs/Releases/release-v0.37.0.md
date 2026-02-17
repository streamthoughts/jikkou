---
title: "Release v0.37.0"
linkTitle: "Release v0.37.0"
weight: -37
---

## 🚀 Introducing Jikkou 0.37.0

We're pleased to announce [Jikkou 0.37.0](https://github.com/streamthoughts/jikkou/releases/tag/v0.37.0).

This release tackles real pain points reported by the community — from managing multiple Kafka environments in a single config, to fixing broken Schema Registry workflows. Here's the overview:

- 🆕 **Multiple provider instances** — manage prod, staging, and dev from one configuration
- 🔄 New **`replace`** command for full resource recreation
- 🛡️ Schema Registry overhaul: **subject modes**, **failover**, **regex validation**, and more
- ⚙️ **KIP-980**: create Kafka connectors in STOPPED or PAUSED state
- 📦 **Directories** as input for `--values-files`
- 📑 **Jinja template file locations** for reusable templates
- 📐 All resource schemas promoted to **v1** API version
- 🔧 Java 25 migration and REST client modernization

To install, check out the [installation guide](https://www.jikkou.io/docs/install/).
For the full changelog, see the [GitHub release page](https://github.com/streamthoughts/jikkou/releases/tag/v0.37.0).

---

## 📐 All Resource Schemas Promoted to v1

All resource `apiVersion` values have been promoted from `v1beta1`/`v1beta2` to `v1`. This applies across every provider — Kafka, Schema Registry, Kafka Connect, Aiven, AWS, and core resources.

```yaml
# Before (still works, but deprecated)
apiVersion: "kafka.jikkou.io/v1beta2"

# After
apiVersion: "kafka.jikkou.io/v1"
```

Existing YAML files using `v1beta1` or `v1beta2` will continue to work — Jikkou automatically resolves old versions to the latest registered resource class and normalises the `apiVersion` during deserialisation. But we recommend updating your files to `v1` going forward.

---

## 🆕 Multiple Provider Instances

Most teams don't run a single Kafka cluster. You have production, staging, maybe a dev cluster — and until now, you needed separate Jikkou configurations or manual overrides to target each one. There was no way to register multiple instances of the same provider type.

Jikkou 0.37.0 introduces **named provider instances**. Register as many Kafka (or Schema Registry, or Kafka Connect) providers as you need, each with its own name and configuration. Then target the right one with `--provider` on the CLI or `"provider"` in API requests.

```hocon
jikkou {
  provider.kafka-prod {
    enabled = true
    type = io.streamthoughts.jikkou.kafka.KafkaExtensionProvider
    default = true
    config = {
      client { bootstrap.servers = "prod-kafka:9092" }
    }
  }
  provider.kafka-staging {
    enabled = true
    type = io.streamthoughts.jikkou.kafka.KafkaExtensionProvider
    config = {
      client { bootstrap.servers = "staging-kafka:9092" }
    }
  }
}
```

```bash
# Preview changes against production
jikkou diff --values-files topics.yaml --provider kafka-prod

# Apply to staging
jikkou create --values-files topics.yaml --provider kafka-staging
```

### How Provider Selection Works

The `--provider` flag is **always optional**. Jikkou resolves the target provider with a simple fallback chain:

- **Single provider of a given type**: It's used automatically — no `--provider` flag needed, no `default` flag needed. If you only have one Kafka provider configured, everything works exactly as before.
- **Multiple providers, one marked `default = true`**: The default is used when `--provider` is omitted. You only need the flag when targeting a non-default provider.
- **Multiple providers, no default**: You **must** specify `--provider` on every command. Omitting it will fail with an explicit error: *"No default configuration defined, and multiple configurations found for provider type"*.

This means existing single-provider configurations continue to work without any changes. The `default` and `--provider` flags only matter once you add a second provider.

Provider selection works across all commands — `create`, `update`, `delete`, `diff`, `validate`, `replace`, and `patch` — and extends to the REST API server with a `provider` field in reconciliation request bodies.

---

## 🔄 New `replace` Command

If you've used Terraform's `taint` or `--replace` flag, you know the pattern: sometimes you don't want to update a resource in place — you want to tear it down and recreate it from scratch.

Jikkou now has its own version of this. The new `replace` command forces a full delete-then-create cycle on all targeted resources, regardless of whether an in-place update would be possible.

```bash
jikkou replace --values-files resources.yaml

# Preview first
jikkou replace --values-files resources.yaml --dry-run
```

A typical use case: development environments where you want to drop and recreate all your Kafka topics daily to start from a clean state. Rather than chaining `jikkou delete` and `jikkou create` in a script, `replace` handles it in one pass.

---

## 🛡️ Schema Registry — Major Improvements

This release brings five targeted fixes and features for the Schema Registry provider, most addressing specific community-reported issues.

### Subject Modes

Schema Registry subjects now support **mode management** directly from your resource definitions. This is essential for schema migration workflows — set a subject to `IMPORT` mode, register schemas with specific IDs, then switch back to `READWRITE`.

```yaml
apiVersion: "schemaregistry.jikkou.io/v1"
kind: "SchemaRegistrySubject"
metadata:
  name: "user-events"
spec:
  mode: "IMPORT"
  compatibilityLevel: "BACKWARD"
  schemaType: "AVRO"
  schema: |
    {
      "type": "record",
      "name": "User",
      "fields": [{"name": "id", "type": "int"}]
    }
```

Supported modes: `IMPORT`, `READONLY`, `READWRITE`, `FORWARD`.

### Schema ID and Version on Create

When migrating schemas between registries, you often need to preserve the original schema IDs and versions. Jikkou now lets you specify both via annotations:

```yaml
metadata:
  name: "events"
  annotations:
    schemaregistry.jikkou.io/schema-id: "100"
    schemaregistry.jikkou.io/schema-version: "1"
```

Combined with `IMPORT` mode, this gives you full control over registry migrations.

### Multiple URLs with Failover

The documentation promised comma-separated Schema Registry URLs for failover, but the raw string was passed directly to the underlying HTTP client. Failover simply didn't work.

Jikkou now properly parses comma-separated URLs. On connection failure, it automatically tries the next URL in the list.

```hocon
provider.schemaregistry {
  config = {
    url = "http://sr-primary:8081,http://sr-backup:8081,http://sr-dr:8081"
  }
}
```

### Subject Name Regex Validation

Enforce naming conventions on your Schema Registry subjects with a regex pattern. Jikkou will reject any subject that doesn't match — before it ever reaches the registry.

```yaml
validations:
  - name: "subjectMustHaveValidName"
    type: "io.streamthoughts.jikkou.schema.registry.validation.SubjectNameRegexValidation"
    config:
      subjectNameRegex: "[a-zA-Z0-9\\._\\-]+"
```

### Permanent Schema Deletion in One Step

Permanently deleting a schema used to require two separate Jikkou runs — first a soft delete, then a hard delete. In a CI/CD pipeline, that's impractical. You declare "delete this permanently" and expect it to happen in one operation.

When you set `jikkou.io/permanent-delete: true`, Jikkou now automatically performs the soft delete followed by the hard delete in a single reconciliation pass.

---

## ⚙️ Kafka Connect: KIP-980 Support

When creating Kafka connectors, the `state` field was silently ignored — every connector started in `RUNNING` state, even if you specified `STOPPED` or `PAUSED`. For production deployments, you often want to create a connector, verify its configuration, and only then start it manually.

Jikkou now uses the [KIP-980](https://cwiki.apache.org/confluence/display/KAFKA/KIP-980%3A+Allow+creating+connectors+in+a+stopped+state) API to pass `initial_state` when creating connectors:

```yaml
apiVersion: "kafkaconnect.jikkou.io/v1"
kind: "KafkaConnector"
metadata:
  name: "jdbc-source"
  annotations:
    kafkaconnect.jikkou.io/initial_state: "STOPPED"
spec:
  connectorClass: "io.confluent.connect.jdbc.JdbcSourceConnector"
  config:
    connection.url: "jdbc:mysql://db:3306/mydb"
```

Options: `RUNNING`, `STOPPED`, `PAUSED`.

---

## 📦 Directory Support for `--values-files`

Teams organizing Kafka configurations by team or environment — e.g., `configurations/cluster1/resources/teamA/values.yml` — couldn't pass a directory to `--values-files`. You'd get `java.io.IOException: Is a directory`, and wildcard expansion didn't work either.

`--values-files` now accepts directories. Jikkou recursively loads all matching files and deep-merges their values:

```bash
jikkou create --values-files config/environments/prod/
```

```
config/environments/prod/
├── teamA/
│   ├── topics.yml
│   └── acls.yml
├── teamB/
│   └── topics.yml
└── shared.yaml
```

All files are loaded and their values are merged recursively. No more listing every file individually.

---

## 📑 Jinja Template File Locations

Jinja's `{% include %}` directive only resolved templates from the Java classpath. If you wanted to split a large Jikkou YAML into reusable fragments, you had to mess with `CLASSPATH_PREFIX` or embed files into container images. For a tool that champions declarative configuration, this was a rough edge.

You can now configure filesystem directories where Jinja resolves template includes:

```hocon
jikkou {
  jinja {
    resourceLocations = [
      "/etc/jikkou/templates",
      "/opt/shared-templates"
    ]
  }
}
```

Your templates can reference local files naturally:

```jinja
{%- include "kafka/topic-defaults.jinja" -%}
{%- import "macros/common.jinja" as m -%}
{{ m.topic_config(name, partitions) }}
```

No classpath gymnastics required.

---

## 🔧 Under the Hood

- **Java 25**: The project now targets Java 25 with GraalVM 25.0.2. Native image metadata has been migrated to the newer reachability-metadata format for better compilation support.
- **REST client modernization**: Migrated from Jersey to RESTEasy proxy client, removing dependency on Jersey internals. OkHttp upgraded from 4.12.0 to 5.3.2.
- **Security fixes**: Addressed CVE-2024-47561, CVE-2025-12183, CVE-2025-55163, and CVE-2026-25526 (Jinjava).
- **Bug fixes**: Fixed repository double-loading on reconcile, GitHub repository file-pattern config, invalid supplier for Schema Registry basic auth, and comma-separated key-value pair parsing.
- **Server**: API resources now use a blocking executor for improved stability.

---

## ✅ Wrapping Up

A lot of what's in this release came directly from issues and feedback you opened on GitHub — so thank you. Keep it coming.

If you run into problems, open a [GitHub issue](https://github.com/streamthoughts/jikkou/issues).
Give us a ⭐️ on [GitHub](https://github.com/streamthoughts/jikkou) and join the conversation on [Slack](https://join.slack.com/t/jikkou-io/shared_invite/zt-27c0pt61j-F10NN7d7ZEppQeMMyvy3VA).
