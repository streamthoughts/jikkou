---
title: "Jikkou vs Strimzi Topic Operator"
linkTitle: "Jikkou vs Strimzi"
weight: 2
description: >
  Running Kafka on Kubernetes with Strimzi? Where the Topic Operator stops, and how Jikkou complements it: Schema Registry, Kafka Connect, ACLs, quotas, and clusters outside Kubernetes.
---

{{% alert title="TL;DR" color="primary" %}}
If your Kafka clusters run on Kubernetes under **Strimzi**, keep the **Topic Operator** for topics; it is
the right tool there. Use **Jikkou** for everything it cannot reach: Schema Registry subjects, Kafka Connect
connectors, ACLs and quotas at breadth, Confluent Cloud, Aiven, Amazon MSK, and any cluster that does not
live in your Kubernetes estate. If you are **not** on Kubernetes, the Topic Operator is not an option at all,
and Jikkou covers the whole surface on its own.
{{% /alert %}}

## Two different jobs

The cleanest way to see the difference: **Strimzi runs Kafka; Jikkou manages what is inside it.**

Strimzi manages the cluster lifecycle itself on Kubernetes: brokers, KRaft/ZooKeeper, listeners and
security, users, and companion tooling like Kafka Connect and MirrorMaker. Jikkou never touches the
cluster lifecycle. It manages Kafka *resources* (topics, ACLs, quotas, consumer groups, schemas,
connectors) declaratively, regardless of where Kafka runs.

## What the Topic Operator does well

Credit where due. Inside a Strimzi-managed cluster, `KafkaTopic` and `KafkaUser` custom resources are
excellent: Kubernetes-native, reconciled continuously by an operator, GitOps-ready through Argo CD or Flux
with zero extra tooling, and backed by a large CNCF community. If topics and users on Strimzi clusters are
your entire need, you do not need Jikkou.

## Where it stops

Most Kafka estates are bigger than that. The Topic Operator manages exactly two resource kinds, on exactly
one class of cluster:

| Resource | Strimzi Topic/User Operator | Jikkou |
|---|---|---|
| Topics on Strimzi-managed clusters | ✓ | ✓ |
| Users / ACLs on Strimzi clusters | ✓ (KafkaUser) | ✓ |
| Topics on **non-Strimzi** clusters (on-prem, MSK, Confluent Cloud, Aiven, Redpanda) | Standalone mode only; still requires Kubernetes and a running operator | ✓ |
| Schema Registry subjects (Avro, JSON, Protobuf) | ✗ | ✓ |
| Kafka Connect connectors | ✗ (KafkaConnector CRD exists, but only for Strimzi-run Connect) | ✓ |
| Quotas | ✗ | ✓ |
| Consumer group management | ✗ | ✓ |
| Aiven / Confluent Cloud / AWS Glue resources | ✗ | ✓ |
| Templating (one definition, many environments) | ✗ (raw CRDs) | ✓ (Jinja + values files) |
| Validation policies (naming, min ISR, partition limits) | ✗ | ✓ |
| Works without Kubernetes | ✗ | ✓ |

Two structural differences matter beyond the table:

- **Kubernetes gravity.** The Topic Operator can technically target an external cluster (standalone
  deployment), but you are still tied to Kubernetes and its reconciliation cycles. Running an operator
  plus the Kubernetes overhead just to manage resources on a managed service like MSK, Confluent Cloud,
  or Aiven is complexity most teams prefer to avoid. Jikkou is a stateless CLI: it runs anywhere your CI
  runs, and applies one model across the whole fleet.
- **Policy.** CRDs are raw desired state. There is no place to say "every topic must have at least
  `min.insync.replicas: 2` and follow the `<domain>-<name>` convention". Jikkou validations enforce
  platform rules on every resource before it reaches any cluster.

## Better together

The pattern that works well for Strimzi shops:

1. **Argo CD / Flux + Topic Operator** own topics and users on the Strimzi clusters, same as today.
2. **Jikkou in CI** owns the rest from the same Git repository: Schema Registry subjects, Connect
   connectors, quotas, and every resource on managed-service clusters (Confluent Cloud, Aiven, MSK).

One repository, one review process, two reconcilers, each on the surface where it is strongest.

## When Strimzi alone is enough

If **all** of the following hold, stay with the Topic Operator and skip Jikkou:

- Every Kafka cluster you manage runs on Kubernetes under Strimzi.
- Topics and users are the entire resource surface: no Schema Registry, no Connect, no quotas.
- Your team is comfortable expressing everything as raw CRDs without templating or policy checks.

That is a real profile, and small K8s-only estates fit it. Most platform teams outgrow it.

## Next steps

- [Install Jikkou](/docs/install/)
- [Jikkou vs Terraform]({{< relref "jikkou-vs-terraform" >}})
- Questions? [Join the Slack community](https://join.slack.com/t/jikkou-io/shared_invite/zt-27c0pt61j-F10NN7d7ZEppQeMMyvy3VA)
