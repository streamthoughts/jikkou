---
title: "How Jikkou Compares"
linkTitle: "Comparisons"
weight: 55
menu:
  main:
    weight: 25
description: >
  How Jikkou compares to Terraform, the Strimzi Topic Operator, and vendor consoles for managing Apache Kafka resources.
---

Choosing a tool to manage Kafka resources usually comes down to four candidates: **Terraform**,
the **Strimzi Topic Operator**, a **vendor console** (Conduktor, Confluent, Aiven, Redpanda), or **Jikkou**.
They are not interchangeable; they solve different problems. Here is the honest map.

## At a glance

| Dimension | Jikkou | Terraform | Strimzi Topic Operator | Vendor consoles |
|---|---|---|---|---|
| **State model** | Stateless: your cluster is the source of truth | State file must stay in sync | Kubernetes CRDs are the source of truth | Internal database |
| **Resource coverage** | Topics, ACLs, quotas, Schema Registry, Connect, consumer groups, Aiven/Confluent Cloud/MSK/Glue, Iceberg | Depends on provider; strongest for Confluent Cloud infra | Topics and users only | Broad, UI-driven |
| **Requires Kubernetes** | No | No | Yes (Strimzi-managed clusters) | No |
| **GitOps fit** | Native: YAML in Git, diff and apply in CI | Good, with state management overhead | Native on Kubernetes | Weak: changes live in the UI |
| **Multi-platform (on-prem + cloud)** | Yes, one model across all of them | Provider-by-provider | Strimzi clusters only | Vendor-scoped |
| **Cost** | Free, Apache 2.0 | Free core; state/collaboration features are commercial | Free, CNCF | Commercial (or tied to the vendor) |

## Which page do you need?

- Your team already manages infrastructure with Terraform → read **[Jikkou vs Terraform]({{< relref "jikkou-vs-terraform" >}})**.
- Your Kafka clusters run on Kubernetes with Strimzi → read **[Jikkou vs Strimzi Topic Operator]({{< relref "jikkou-vs-strimzi" >}})**.
- You are evaluating a console: consoles are complements, not alternatives. They give you visibility, Jikkou gives you
  reviewable, versioned, automated change management. Many teams run both.
