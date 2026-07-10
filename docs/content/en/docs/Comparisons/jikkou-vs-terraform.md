---
title: "Jikkou vs Terraform for Apache Kafka"
linkTitle: "Jikkou vs Terraform"
weight: 1
description: >
  Should you manage Kafka topics, ACLs, and schemas with Terraform or with Jikkou? An honest comparison: state files vs stateless reconciliation, scale, coverage, and when to use both.
---

{{% alert title="TL;DR" color="primary" %}}
Use **Terraform** when Kafka is one of thirty resource types you provision (clusters, networks, service
accounts, API keys). Use **Jikkou** when you manage what lives *inside* Kafka: hundreds of topics, ACLs,
quotas, and schemas that must stay reconciled against the real cluster, not against a
state file. Many teams use both: Terraform provisions the cluster, Jikkou manages its contents.
{{% /alert %}}

## The state-file problem

Terraform trusts its state file. Kafka clusters do not stay in sync with state files.

A concrete scenario every Kafka team knows: during an incident, an engineer bumps
`retention.ms` on a production topic with a vendor UI or `kafka-configs.sh`. The incident ends,
the change stays. Terraform's state file still holds the old value, so `terraform plan` shows **no drift**
unless someone remembers to run a targeted `refresh`, and even then, resources created outside
Terraform are invisible until they are painstakingly imported.

Jikkou is **stateless by design**. There is no state file to protect, migrate, or unlock.
Every `jikkou diff` compares your Git-versioned YAML against the **actual cluster state**:

```bash
$ jikkou diff --files ./topics/
```

Out-of-band changes show up immediately as a diff. Reverting them is `jikkou apply`. Detecting them
continuously is a scheduled CI job.

## Managing 300 topics

Kafka estates are repetitive: hundreds of topics that differ by a name and one or two values.

In HCL, this becomes a `for_each` over a variables map: a second, more abstract configuration language
on top of the first, and every exception to the pattern makes the map grow warts.

Jikkou treats templating as a first-class concern with [Jinja templates](/docs/concepts/template/)
and per-environment values files:

```yaml
apiVersion: "kafka.jikkou.io/v1beta2"
kind: "KafkaTopicList"
items:
{% for domain in values.domains %}
  - metadata:
      name: "{{ domain.name }}-events"
    spec:
      partitions: {{ domain.partitions | default(6) }}
      replicas: 3
      configs:
        min.insync.replicas: 2
{% endfor %}
```

The same definitions apply to every environment (local Docker, ephemeral CI clusters, staging, production)
with different values files. And built-in [validations](/docs/concepts/validations/) enforce your
platform rules (naming conventions, minimum replication factor, partition limits) on every change before
it reaches a cluster.

## Coverage

| Resource | Jikkou | [`Mongey/kafka`](https://registry.terraform.io/providers/Mongey/kafka/latest) provider | [`confluentinc/confluent`](https://registry.terraform.io/providers/confluentinc/confluent/latest) provider |
|---|---|---|---|
| Topics & configs (on-prem / any Kafka) | ✓ | ✓ | Confluent Cloud only |
| ACLs | ✓ | ✓ | Confluent Cloud only |
| Quotas | ✓ | ✓ | ✓ |
| Consumer groups (offsets, state) | ✓ | ✗ | ✗ |
| Schema Registry subjects (Avro, JSON, Protobuf) | ✓ | separate provider | ✓ |
| Kafka Connect connectors | ✓ | separate provider | ✓ |
| Aiven (ACLs, quotas, schemas) | ✓ | ✗ | ✗ |
| AWS Glue schemas | ✓ | ✗ | ✗ |
| Apache Iceberg (tables, namespaces) | ✓ | ✗ | ✗ |
| Kafka **clusters**, networks, API keys | ✗ | ✗ | ✓ |

One Jikkou model covers on-premises Kafka, Confluent Cloud, Aiven, Amazon MSK, and Redpanda,
with the same YAML resources everywhere.

## When Terraform is the right choice

Honesty matters here:

- **Provisioning infrastructure.** Creating the Kafka cluster itself, VPC peering, service accounts,
  API keys: that is Terraform's home turf.
- **All-in on Confluent.** If you run exclusively on Confluent Platform or Confluent Cloud, the
  [`confluentinc/confluent`](https://registry.terraform.io/providers/confluentinc/confluent/latest) provider
  is mature and usually the natural first choice. Jikkou becomes
  more interesting when the problem is ongoing governance across clusters and providers.
- **Org-wide IaC standardization.** If your organization enforces every change through Terraform with
  policy-as-code tooling around it, adding a second tool has a real cost. Weigh it.
- **Everything-in-one-graph needs.** If topic creation must be atomically tied to the services that
  consume them inside one dependency graph, Terraform's model fits better.

## Better together

The pattern we see most often in production:

1. **Terraform** provisions the platform: clusters, networking, credentials.
2. **Jikkou** manages what lives inside: topics, ACLs, quotas, schemas, and connectors, owned by the
   platform team, changed through pull requests, applied by CI, drift-checked on a schedule.

## GitOps without handing out cluster credentials

A workflow used in production today: each team keeps its resource definitions (topics, schemas) in its
own Git repository. A pull request opens the change; `jikkou diff` generates a reviewable patch of
exactly what would be applied; once merged, a GitHub Action (see
[setup-jikkou](https://github.com/streamthoughts/setup-jikkou)) applies it.

And because Jikkou also ships as a [REST API server](/docs/how-to-guides/install/api-server/), your CI
never needs direct access to the Kafka cluster: the CLI talks to the Jikkou API (optionally behind your
API gateway), and only the server holds cluster credentials. Teams self-serve their resources through
pull requests; no central ticket queue, no shared admin credentials.

## Migrate in 10 minutes

Jikkou can export your existing cluster state as YAML. No manual imports, no state surgery:

```bash
# Export current topics into Git
jikkou get kafka topics -o YAML > topics.yaml

# Export ACLs and quotas too
jikkou get kafka acls -o YAML > acls.yaml
jikkou get kafka client-quotas -o YAML > quotas.yaml

# Review, commit, and from now on: preview every change
jikkou diff --files .
```

From there, wire `jikkou diff` into pull requests and `jikkou apply` into your main-branch pipeline.
See the [how-to guides](/docs/how-to-guides/).

## Next steps

- [Install Jikkou](/docs/install/)
- [Jikkou vs Strimzi Topic Operator]({{< relref "jikkou-vs-strimzi" >}})
- Questions? [Join the Slack community](https://join.slack.com/t/jikkou-io/shared_invite/zt-27c0pt61j-F10NN7d7ZEppQeMMyvy3VA)
