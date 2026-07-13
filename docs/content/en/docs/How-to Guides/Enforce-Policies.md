---
title: "Enforce Governance Policies"
linkTitle: "Enforce Policies"
weight: 60
description: >
  Use ValidatingResourcePolicy to enforce organizational rules on resources and changes with CEL.
---

A `ValidatingResourcePolicy` is a declarative, reusable way to enforce governance rules across *any*
resource using [Google CEL](https://cel.dev/) expressions. Use it to block destructive operations,
enforce limits (partitions, replication factor), or require naming and metadata conventions.

For the full specification, see the
[ValidatingResourcePolicy reference]({{% relref "/docs/Reference/Providers/Core/Resources/ValidatingResourcePolicy.md" %}}).
For the broader picture, see [Validations]({{% relref "/docs/Concepts/validations.md" %}}).

## Before you begin

* A Jikkou context configured for your platform — see [Getting Started]({{% relref "/docs/Tutorials/get_started.md" %}}).

## 1. Write a policy

A policy selects the resources it applies to and defines rules. Each rule's `expression` is a CEL
assertion that must hold for the resource to be valid: the rule fails when the expression evaluates
to `false` (the same convention as Kubernetes ValidatingAdmissionPolicy). The `failurePolicy` decides
what happens on failure:

* `FAIL`: abort the operation with an error.
* `FILTER`: silently drop the invalid resource(s) and continue.
* `CONTINUE`: report the violation but let the resource proceed. Useful to introduce a new rule in
  "warning mode" before making it blocking.

_`file: policy-topics.yaml`_

```yaml
---
apiVersion: core.jikkou.io/v1
kind: ValidatingResourcePolicy
metadata:
  name: KafkaTopicPolicy
spec:
  failurePolicy: FAIL
  selector:
    matchResources:
      - kind: KafkaTopic
  rules:
    - name: MaxTopicPartitions
      expression: "resource.spec.partitions <= 50"
      messageExpression: "'Topic partitions MUST be <= 50, but was: ' + string(resource.spec.partitions)"
    - name: MinTopicPartitions
      expression: "resource.spec.partitions >= 3"
      message: "Topic must have at least 3 partitions"
```

## 2. Apply resources with the policy

Policies are transient resources: pass the policy file alongside the resources being validated. Jikkou
evaluates the policy during reconciliation.

```bash
jikkou apply --files ./kafka-topics.yaml --files ./policy-topics.yaml --dry-run
```

If a topic violates a rule, a `FAIL` policy stops the run and prints the rule's message.

## Block destructive operations

Policies can match **change** resources (e.g. `KafkaTopicChange`) to control operations. This example
filters out delete operations on topics so they are never executed:

```yaml
---
apiVersion: core.jikkou.io/v1
kind: ValidatingResourcePolicy
metadata:
  name: BlockTopicDeletes
spec:
  failurePolicy: FILTER
  selector:
    matchResources:
      - kind: KafkaTopicChange
  rules:
    - name: FilterDeleteOperation
      expression: "resource.spec.op != 'DELETE'"
      messageExpression: "'Operation ' + resource.spec.op + ' on topics is not authorized'"
```

## Target a subset of resources

Combine `matchResources`, `matchLabels`, and `matchExpressions` (with `matchingStrategy: ALL` or `ANY`)
to scope a policy. For example, apply it only to topics in the `prod` environment:

```yaml
selector:
  matchingStrategy: ALL
  matchResources:
    - kind: KafkaTopic
  matchLabels:
    - key: environment
      operator: In
      values: ["prod"]
```

## Policy library

Ready-made policies you can copy and adapt live in
[`examples/policies/`](https://github.com/streamthoughts/jikkou/tree/main/examples/policies):

| Policy | What it enforces |
|--------|------------------|
| `topic-naming-convention.yaml` | Topic names follow a convention (kebab-case by default; adapt the regex) |
| `topic-min-insync-replicas.yaml` | `min.insync.replicas` is set explicitly and is at least 2 |
| `topic-min-replication-factor.yaml` | Replication factor is declared and at least 3 |
| `topic-partition-limits.yaml` | Partition count stays within platform bounds (3 to 50 by default) |
| `require-owner-label.yaml` | Every topic carries an `owner` label |
| `block-topic-deletes.yaml` | Reconciliation may never delete a topic (matches change resources) |

## Centrally enforce policies for all teams

Passing policy files on every command works for one team, but a platform team usually wants policies
applied to **every** run, no matter which files an application team passes. Configure a
[resource repository]({{% relref "/docs/Concepts/repositories.md" %}}) in the shared Jikkou context:
repository resources are injected automatically into each execution.

```hocon
jikkou {
  repositories = [
    {
      name = "platform-policies"
      type = io.jikkou.core.repository.GitHubResourceRepository
      config {
        repository = "my-org/kafka-platform-policies"
        branch = "main"
        paths = [ "policies/" ]
        # Access token for private repositories
        token = ${?GITHUB_TOKEN}
      }
    }
  ]
}
```

With this configuration in the context used by CI (or by the Jikkou API server), an application
team running `jikkou apply -f ./my-topics.yaml` gets the platform policies evaluated on every
resource and every change, without ever seeing the policy files. Updating a rule is a pull
request on the central policies repository, immediately effective for all teams.

## Related

* [ValidatingResourcePolicy reference]({{% relref "/docs/Reference/Providers/Core/Resources/ValidatingResourcePolicy.md" %}})
* [Validations concept]({{% relref "/docs/Concepts/validations.md" %}})
* [Manage Kafka ACLs]({{% relref "Manage-Kafka-ACLs.md" %}})
