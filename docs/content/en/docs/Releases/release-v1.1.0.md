---
title: "Release v1.1.0"
linkTitle: "Release v1.1.0"
description: "Jikkou 1.1.0 release notes: CI drift detection, Kafka 4.x share groups, HTTP proxy and custom headers for REST providers."
weight: -110
---

## 🚀 Jikkou 1.1.0

[Jikkou 1.1.0](https://github.com/streamthoughts/jikkou/releases/tag/v1.1.0) is out.

Where 1.0.0 was about the shape of the project (package rename, API contract, new providers), this
one is about running Jikkou where it actually lives: in a CI pipeline, behind a corporate proxy, and
against Kafka 4.x clusters. Highlights:

- 🔎 `jikkou diff --fail-on-changes` for scheduled drift checks, with a dedicated exit code.
- 🧾 A ready-made policy pack under `examples/policies`.
- 🧵 Kafka 4.x share groups (KIP-932) and declarative group configuration.
- 🌐 HTTP proxy support and custom request headers on all REST-based providers.
- 🧩 Schema Registry subjects are now updated when only their references change.
- 🤖 An official agent skill, so coding assistants drive Jikkou safely instead of guessing.

Upgrading from 1.0.x needs no configuration changes. One behaviour change is worth knowing about
before you apply: see the note on consumer group configs below.

To install, see the [installation guide](https://www.jikkou.io/docs/install/).
For the full changelog, see the [GitHub release page](https://github.com/streamthoughts/jikkou/releases/tag/v1.1.0).

---

## 🔎 Drift Detection in CI

Someone bumps `retention.ms` from a vendor UI during an incident. A topic gets created by hand and
never makes it back into Git. Because Jikkou is stateless and compares your definitions against the
live cluster on every run, finding that kind of drift was already possible. What was missing was a
way for a CI job to act on it.

`jikkou diff` now takes `--fail-on-changes`:

```bash
jikkou diff --files ./resources --fail-on-changes
```

Exit code `3` means drift was detected. `0` means the cluster matches Git, and `1` / `2` keep their
usual meanings (execution error and usage error), so a scheduled job can tell a broken run apart
from a drifted cluster:

| Code | Meaning |
| --- | --- |
| `0` | No changes. The cluster matches Git. |
| `1` | Validation or execution error. |
| `2` | Usage error (invalid flags or arguments). |
| `3` | Drift detected: at least one pending change. |

A one-line summary goes to stderr (`3 changes detected: 1 CREATE, 2 UPDATE`), while stdout carries
the full diff, so you can pipe the machine-readable output somewhere and still read the summary in
the job log.

The check runs on the filtered result. `--fail-on-changes --filter-change-op UPDATE` counts only
updates as drift and ignores creations and deletions. Without filters, every operation other than
`NONE` counts.

The [Detect Configuration Drift](/docs/how-to-guides/automating/detect-configuration-drift/) guide
walks through an hourly GitHub Actions workflow that uploads the diff and opens an issue.

---

## 🌍 Fleet Output You Can Read

Running `apply` or `diff` across a provider group used to produce one flat stream of changes with no
indication of which cluster each one came from. Change results are now tagged with a
`jikkou.io/provider` annotation, and the text printer groups its output under a header per provider:

```
PROVIDER [kafka-prod-eu]
  ...
PROVIDER [kafka-prod-us]
  ...
```

This release also fixes a bug that made the feature much less useful than it looked: the provider
annotation was written onto the shared input resources, so after the first provider in a batch, every
subsequent one saw resources already tagged for someone else and skipped them. If you tried
`--provider-all` or `--provider-group` on 1.0.x and only the first cluster was reconciled, that was
this bug.

See [Manage a Fleet of Kafka Clusters](/docs/how-to-guides/manage-kafka-fleet/) for the full setup.

---

## 🧾 A Policy Pack to Start From

`ValidatingResourcePolicy` has been available since 0.36, but every user had to write their first
rules from a blank file. [`examples/policies`](https://github.com/streamthoughts/jikkou/tree/main/examples/policies)
now ships policies covering the rules most teams write anyway:

- `topic-min-replication-factor.yaml`
- `topic-min-insync-replicas.yaml`
- `topic-partition-limits.yaml`
- `topic-naming-convention.yaml`
- `require-owner-label.yaml`
- `block-topic-deletes.yaml`

While writing them, we found that the docs and the older examples had the rule semantics backwards.
The engine treats a rule expression as an *assertion*: the rule fails when the expression evaluates
to `false`. Several examples were written as if the expression described the violation, which means
they asserted the opposite of what their name claimed. Everything under `examples/` and in the
[Enforce Policies](/docs/how-to-guides/enforce-policies/) guide has been rewritten to match the
engine. If you copied an example policy before 1.1.0, please re-read it.

A CEL bug is fixed alongside this. Expressions touching scalars that are not JSON-native, such as
`spec.replicas` (a `Short`), failed to evaluate. Resources are now normalised through a JSON
round-trip before evaluation, so a policy on replication factor behaves like any other.

The [`KafkaTopicMaxReplicas`](/docs/reference/providers/kafka/transformations/kafkatopicmaxreplicas/)
transformation, which caps replication factor at reconciliation time rather than rejecting the
manifest, finally has a reference page. It has shipped for a while but was undocumented.

---

## 🧵 Kafka 4.x Share Groups

Kafka 4.x introduced share groups ([KIP-932](https://cwiki.apache.org/confluence/display/KAFKA/KIP-932%3A+Queues+for+Kafka)),
the queue-style consumption model where several consumers cooperatively read the same partition.
Jikkou now manages them with a new `KafkaShareGroup` resource.

```bash
$ jikkou get kafkasharegroups --in-states STABLE --offsets
```

```yaml
apiVersion: "kafka.jikkou.io/v1"
kind: "KafkaShareGroup"
metadata:
  name: "my-share-group"
spec:
  configs:
    share.auto.offset.reset: "earliest"
status:
  state: "STABLE"
  members:
    - memberId: "share-consumer-b103994e-bcd5-4236-9d03-97065057e594"
      clientId: "share-consumer"
      host: "/127.0.0.1"
      rackId: "rack-1"
      assignments:
        - "my-topic-0"
  offsets:
    - topic: "my-topic"
      partition: 0
      offset: 0
  coordinator:
    id: "101"
    host: "localhost"
    port: 9092
```

`spec.configs` is reconciled, not just reported. Group-level settings such as
`share.record.lock.duration.ms` or `share.isolation.level` are diffed against the Kafka `GROUP`
configuration resource and applied incrementally, the same way topic configs have always been:

```yaml
apiVersion: "kafka.jikkou.io/v1"
kind: "KafkaShareGroup"
metadata:
  name: "orders-queue"
spec:
  configs:
    share.record.lock.duration.ms: 30000
    share.auto.offset.reset: "earliest"
```

The same `spec.configs` block now exists on `KafkaConsumerGroup`, since Kafka 4.x made
consumer groups dynamically configurable too:

```yaml
apiVersion: "kafka.jikkou.io/v1"
kind: "KafkaConsumerGroup"
metadata:
  name: "my-group"
spec:
  configs:
    consumer.session.timeout.ms: 50000
```

{{% alert title="Check this before upgrading" color="warning" %}}
For both group types, `config-delete-orphans` defaults to `true`: group configs present on the
cluster but absent from the resource are removed. `KafkaConsumerGroup` had no `spec` before 1.1.0,
so if you apply existing consumer group manifests against a Kafka 4.x cluster whose group configs
were set outside Jikkou, those configs will be deleted. Run `jikkou diff` first, and set
`config-delete-orphans = false` on the controller to leave externally-managed configs alone.
{{% /alert %}}

Share groups can be deleted through `jikkou delete`, and a new
[`KafkaShareGroupsResetOffsets`](/docs/reference/providers/kafka/actions/kafkasharegroupsresetoffsets/)
action resets the Share-Partition Start Offset:

```bash
$ jikkou action KafkaShareGroupsResetOffsets execute \
    --group my-share-group \
    --topic my-topic \
    --to-earliest
```

Share groups require a Kafka 4.x cluster with `group.share.enable=true` and the `share.version`
feature enabled on the brokers.

### Partition-level offset selectors

Offset reset actions accept `topic:partition` in `--topic`, not only a bare topic name
([#770](https://github.com/streamthoughts/jikkou/issues/770)):

```bash
$ jikkou action KafkaConsumerGroupsResetOffsets execute \
    --group my-group \
    --topic orders:3 \
    --topic orders:7 \
    --to-earliest
```

Bare topic names are expanded to all partitions exactly as before, so nothing changes for existing
usage. A `topic:partition` entry skips the `describeTopics` round-trip entirely.

---

## 🌐 REST Providers Behind a Proxy

Reported in [#773](https://github.com/streamthoughts/jikkou/issues/773): Jikkou could not reach a
Schema Registry through a corporate proxy. The default RESTEasy Apache engine ignores the JVM proxy
system properties, and the JVM itself never reads the OS `http_proxy` / `https_proxy` variables, so
neither of the two things you would naturally try had any effect.

Proxy configuration is now a first-class option on all four REST-based providers (Kafka Connect,
Schema Registry, Confluent Cloud, Aiven):

```hocon
jikkou {
  provider.schemaregistry {
    type = io.jikkou.schema.registry.SchemaRegistryExtensionProvider
    config {
      url = "https://schema-registry.internal:8081"

      proxyUrl = "http://proxy.corp.internal:3128"
      proxyUsername = "svc-jikkou"
      proxyPassword = ${?PROXY_PASSWORD}
      nonProxyHosts = "localhost,127.0.0.1,*.internal"
    }
  }
}
```

`nonProxyHosts` follows the usual JVM wildcard syntax. Credentials are scoped to the proxy host so
that authentication works over a `CONNECT` tunnel for HTTPS targets. When no proxy is configured,
the client is built exactly as before.

### Custom request headers

The same four providers accept a `clientHeaders` map, which attaches arbitrary headers to every
outgoing request. API gateway keys, tenant identifiers, and tracing headers were the driving cases:

```hocon
clientHeaders {
  X-Api-Gateway-Key = "my-gateway-key"
  X-Tenant = "acme"
}
```

Headers are applied last and matched case-insensitively, so a header set here replaces the one
Jikkou would otherwise send under the same name. That includes `Authorization`, which is deliberate:
it lets you front a provider with a gateway that expects its own token scheme.

Debug logging redacts values for `Authorization`, `Proxy-Authorization`, `Cookie` and `Set-Cookie`,
and for any header whose name contains `token`, `secret`, `key` or `password`. Header names are
always logged in full.

---

## 🧩 Schema Registry: References Now Trigger an Update

A subject whose schema text was unchanged but whose `references` had been edited was considered
up to date, so the new references were never pushed
([#792](https://github.com/streamthoughts/jikkou/issues/792), contributed by
[@ihaupe](https://github.com/ihaupe)). References are now part of the comparison and of the
registration payload.

---

## 🤖 An Official Agent Skill

Give a coding agent a Kafka cluster and a Jikkou binary, and it will happily use them imperatively.
The recurring mistakes are predictable enough that we wrote them down: guessing YAML field names
rather than reading the resource schema, reaching for `kafka-topics.sh`, editing a live resource
without fetching it first, and running `apply` on a change that looked too trivial to preview. Each
one is a real cluster mutation with no diff in front of it.

This release ships an official skill, [`managing-kafka-resources`](https://github.com/streamthoughts/jikkou/tree/main/skills/managing-kafka-resources),
that pins the agent to the workflow you would use yourself: discover, author, validate, diff, apply.
It carries per-provider reference files for Kafka, Schema Registry, Kafka Connect, Aiven, Confluent
Cloud, AWS Glue and Iceberg, so the agent reads the resource schema instead of inventing it, plus a
diagnostics reference for consumer lag, ACL audits and misconfiguration checks.

The safety rules matter more than the reference material:

- Never run `apply` without first showing you `diff` or `--dry-run` output.
- Deleting requires the explicit `jikkou.io/delete: true` annotation, never any other route.
- Contexts named `prod` or `production` get an extra confirmation before any non-dry-run apply.
- No casual `jikkou config use-context`, because Jikkou eagerly loads the current context on every
  invocation and a broken one breaks every subsequent command, not just the one being tried.
- Ask before installing anything.

On Claude Code, install it as a plugin:

```
/plugin marketplace add streamthoughts/jikkou
/plugin install jikkou
```

For any other agent, copy the [`skills/managing-kafka-resources`](https://github.com/streamthoughts/jikkou/tree/main/skills/managing-kafka-resources)
directory into that agent's skills directory. It is plain Markdown and reference files, with no code
of its own to execute, so you can read the whole thing in a few minutes before you trust it with a
cluster.

This is a first pass and we expect to iterate on it. If your agent does something with Jikkou that
it should not have, that is a bug in the skill, so please open an issue.

---

## 🐛 Other Fixes

- **API server**: Kafka admin calls no longer block the Netty event loop. Under concurrent requests
  this could stall unrelated endpoints.
- **API server**: the health endpoint only reports extension providers that are actually enabled.
- **CLI**: an empty Jikkou configuration file is tolerated instead of failing at startup.
- **Aiven**: partition offsets and topic sizes are now read as 64-bit values. A partition over 2 GiB
  reported a size that did not fit in an `int`, and deserialisation aborted with
  `JsonMappingException: Numeric value (3221225472) out of range of int`, failing the whole topic
  listing and diff.
- **Aiven**: planning now describes only the Schema Registry subjects under management, instead of
  every subject in the service. On services with many subjects this is a large reduction in API
  calls.
- **Build**: the `Multi-Release` manifest entry is restored in the shaded runner jar.

### Dependencies

Jackson `2.21.4`, Netty `4.2.16.Final`, PostgreSQL JDBC `42.7.12` and Logback `1.5.34`, clearing the
HIGH findings from the Trivy scan. The CLI and API server modules import their Jackson and pgjdbc
pins ahead of the Micronaut BOMs, which previously won and pulled older versions back in regardless
of what the parent POM declared.

---

## 📎 Also in This Release

The repository now has an [ADOPTERS.md](https://github.com/streamthoughts/jikkou/blob/main/ADOPTERS.md)
and a [SECURITY.md](https://github.com/streamthoughts/jikkou/blob/main/SECURITY.md). If you run
Jikkou in production, a PR adding your organisation to the first one is very welcome.

---

## 🙏 Thanks

Thanks to [@ihaupe](https://github.com/ihaupe), [@timBorelle](https://github.com/timBorelle),
[@dastokes273](https://github.com/dastokes273), Rui Loureiro and Jeroen Schutrup for the code and
documentation contributions in this release, and to everyone who reported the issues behind it.

- 📥 Install: <https://www.jikkou.io/docs/install/>
- 🐛 Issues: <https://github.com/streamthoughts/jikkou/issues>
- ⭐ GitHub: <https://github.com/streamthoughts/jikkou>
- 💬 Slack: <https://join.slack.com/t/jikkou-io/shared_invite/zt-27c0pt61j-F10NN7d7ZEppQeMMyvy3VA>
