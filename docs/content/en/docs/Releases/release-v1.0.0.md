---
title: "Release v1.0.0"
linkTitle: "Release v1.0.0"
description: "Jikkou 1.0.0 — production milestone. Apache Iceberg, multi-cluster orchestration, Confluent Cloud RBAC."
weight: -100
---

## 🎉 Introducing Jikkou 1.0.0

We're pleased to announce [Jikkou 1.0.0](https://github.com/streamthoughts/jikkou/releases/tag/v1.0.0) — our first major release.

1.0.0 is a maturity milestone: we've canonicalised the package layout, tightened the public API contract, and shipped three substantial new capabilities. Highlights:

- 🧊 **Apache Iceberg provider** — declarative tables, namespaces, and views with schema evolution.
- 🌐 **Multi-provider orchestration** — provider groups, `--provider-all`, `--continue-on-error`.
- 🔐 **Confluent Cloud RBAC** — manage role bindings as code.
- 🧭 **Resource dependency ordering** — explicit dependencies honoured during reconciliation.
- 📦 **Coordinate & package rename** — `io.streamthoughts.jikkou` → `io.jikkou` (with a deprecation shim).
- 🛠 **CLI & API ergonomics** — JSON Schema export, `-o/--output` on list commands, `api-providers` group.

To install, see the [installation guide](https://www.jikkou.io/docs/install/).
For the full changelog, see the [GitHub release page](https://github.com/streamthoughts/jikkou/releases/tag/v1.0.0).
For step-by-step upgrade instructions, see [Migrating to Jikkou 1.0]({{< relref "/docs/Migration/migrate-to-1.0.md" >}}).

---

## 🚨 Breaking Changes

### Package & Coordinate Rename

All Java packages have moved from `io.streamthoughts.jikkou.*` to `io.jikkou.*`, and Maven coordinates from `io.streamthoughts:jikkou-*` to `io.jikkou:jikkou-*`. The change touches every module.

```xml
<!-- Before -->
<dependency>
  <groupId>io.streamthoughts</groupId>
  <artifactId>jikkou-core</artifactId>
  <version>0.37.3</version>
</dependency>

<!-- After -->
<dependency>
  <groupId>io.jikkou</groupId>
  <artifactId>jikkou-core</artifactId>
  <version>1.0.0</version>
</dependency>
```

**Backward compatibility.** Old class names (e.g. `io.streamthoughts.jikkou.kafka.KafkaExtensionProvider`) still resolve at runtime through a deprecation shim. You'll see a warning in the logs but your existing configurations and custom extensions keep working. The shim is scheduled for removal in 1.1.0.

---

## 🧊 Apache Iceberg Provider

A brand-new provider for declarative Apache Iceberg management. It introduces three resources — `IcebergNamespace`, `IcebergTable`, and `IcebergView` — covering the lifecycle of a catalog from namespaces down to schema evolution.

```yaml
apiVersion: "iceberg.jikkou.io/v1beta1"
kind: "IcebergTable"
metadata:
  name: "analytics.events.page_views"
spec:
  schema:
    columns:
      - { name: "event_id",    type: "uuid",        required: true }
      - { name: "user_id",     type: "long",        required: true }
      - { name: "event_time",  type: "timestamptz", required: true }
      - { name: "page_url",    type: "string",      required: true }
      - { name: "duration_ms", type: "long" }
  partitionFields:
    - { sourceColumn: "event_time", transform: "day" }
  sortFields:
    - { column: "event_time", direction: "asc", nullOrder: "nulls_last" }
  properties:
    write.format.default: "parquet"
    write.parquet.compression-codec: "zstd"
```

**Catalog backends supported:** REST, Hive, JDBC, AWS Glue, Project Nessie, Hadoop.

**Schema evolution.** Jikkou diffs the live table against the desired manifest in two passes — pass 1 resolves explicit renames declared via a `previousName` field on the column, pass 2 does standard add/drop/update on the rest. This keeps rename + type-promote safe in a single change.

**Safety controls.** Destructive operations are off by default. Three configuration properties on the table controller gate them:

| Property | Effect |
| --- | --- |
| `delete-orphans` | Allow Jikkou to drop tables present in the catalog but absent from your manifests. |
| `delete-purge` | When dropping a table, also delete its data files. |
| `delete-orphan-columns` | Allow column drops during schema evolution. |

A complete demo (Iceberg + Nessie + SeaweedFS) is available under [`demo/iceberg/`](https://github.com/streamthoughts/jikkou/tree/main/demo/iceberg).

> The Iceberg resources ship as `v1beta1` to give us room to refine the schema based on early feedback. They will be promoted to `v1` in a future release.

---

## 🌐 Multi-Provider & Multi-Cluster Orchestration

0.37.0 introduced multiple instances of the same provider type. 1.0.0 builds on that with **provider groups** and batch operations across them.

Define groups in your Jikkou configuration:

```hocon
jikkou {
  provider.kafka-prod-eu  { type = io.jikkou.kafka.KafkaExtensionProvider, config = { ... } }
  provider.kafka-prod-us  { type = io.jikkou.kafka.KafkaExtensionProvider, config = { ... } }
  provider.kafka-staging  { type = io.jikkou.kafka.KafkaExtensionProvider, config = { ... } }

  provider-groups {
    production = ["kafka-prod-eu", "kafka-prod-us"]
    nonprod    = ["kafka-staging"]
  }
}
```

New CLI options on every reconciliation command (`apply`, `create`, `update`, `patch`, `replace`, `delete`, `diff`):

| Flag | Purpose |
| --- | --- |
| `--provider-all` | Apply to every registered provider of the matching type. |
| `--provider-group <name>` | Apply to a named provider group. |
| `--continue-on-error` | Don't abort the batch when one provider fails. |

```bash
# Roll a topic change across both production regions
jikkou apply --files topics.yaml --provider-group production --continue-on-error

# Diff against every Kafka provider you have configured
jikkou diff --files topics.yaml --provider-all
```

The REST API mirrors this: `ResourceReconcileRequest` now accepts a `providers` list and a `continueOnError` flag. Existing clients that omit both fields are unaffected.

`--provider`, `--provider-all`, and `--provider-group` are mutually exclusive.

---

## 🔐 Confluent Cloud RBAC

A new provider — `jikkou-provider-confluent` — manages **Confluent Cloud role bindings** as code.

```yaml
apiVersion: "iam.confluent.cloud/v1"
kind: "RoleBinding"
metadata:
  name: "sa-analytics-cluster-admin"
spec:
  principal: "User:sa-abc123"
  roleName: "CloudClusterAdmin"
  crnPattern: "crn://confluent.cloud/organization=org-123/environment=env-456/cloud-cluster=lkc-789"
```

Configure it with a Confluent Cloud API key/secret in your Jikkou configuration. See the provider docs for the full role catalogue and CRN patterns.

---

## 🧭 Resource Dependency Ordering

Reconciliation now honours declared dependencies between resources. This matters when a manifest mixes resources whose creation order is not interchangeable — e.g. a Schema Registry subject that an Iceberg table depends on, or a namespace that must exist before its tables.

Jikkou builds a dependency graph from the manifest and processes resources in topological order, with parallel execution across independent branches.

---

## 🛠 CLI & API Improvements

- **JSON Schema export** — `jikkou api-resources schema --api-version <version> --kind <kind>` emits the JSON Schema for a resource. Useful for IDE integration and CI validation.
- **Output formats on list commands** — `-o/--output` now accepts `JSON`, `YAML`, or `TABLE` on every list/get command.
- **Provider introspection** — `jikkou api-providers list` and `jikkou api-providers get <name>` describe the providers available in your configuration.
- **Display metadata** — `ConfigProperty` and `ApiResourceSummary` carry `displayName` and `description` fields, surfaced in CLI help and API responses.
- **`get` reorganisation** — `jikkou get` subcommands are now grouped by provider (e.g. `jikkou get kafka topics`), so `--help` is navigable on installations with many providers. The old flat forms (`jikkou get topics`, `jikkou get kafkatopics`, …) keep working but are deprecated.

---

## 🔁 Migration

Upgrading from 0.37.x is straightforward thanks to the deprecation shim — most setups will run 1.0.0 unchanged with deprecation warnings, then clean up at their own pace.

See the dedicated [Migrating to Jikkou 1.0]({{< relref "/docs/Migration/migrate-to-1.0.md" >}}) guide for:

- Maven / Gradle coordinate updates
- An import-rename recipe for custom extensions
- CLI and REST API change notes

---

## 🙏 Thanks & Links

A huge thank you to everyone who filed issues, reviewed PRs, or tested release candidates — 1.0.0 reflects a year of community feedback.

- 📥 Install: <https://www.jikkou.io/docs/install/>
- 🐛 Issues: <https://github.com/streamthoughts/jikkou/issues>
- ⭐ GitHub: <https://github.com/streamthoughts/jikkou>
- 💬 Slack: <https://join.slack.com/t/jikkou-io/shared_invite/zt-27c0pt61j-F10NN7d7ZEppQeMMyvy3VA>
