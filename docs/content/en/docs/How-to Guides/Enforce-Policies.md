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

A policy selects the resources it applies to and defines rules. Each rule's `expression` fails when it
evaluates to `true`. The `failurePolicy` decides what happens on failure:

* `FAIL` — abort the operation with an error.
* `FILTER` — silently drop the invalid resource(s) and continue.

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
      expression: "resource.spec.partitions > 50"
      messageExpression: "'Topic partitions MUST be <= 50, but was: ' + string(resource.spec.partitions)"
    - name: MinTopicPartitions
      expression: "resource.spec.partitions < 3"
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
      expression: "resource.spec.op == 'DELETE'"
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

## Reuse policies across environments

Store policies in a [resource repository]({{% relref "/docs/Concepts/repositories.md" %}}) so they are injected
automatically and shared across teams and environments, instead of passing them on every command.

## Related

* [ValidatingResourcePolicy reference]({{% relref "/docs/Reference/Providers/Core/Resources/ValidatingResourcePolicy.md" %}})
* [Validations concept]({{% relref "/docs/Concepts/validations.md" %}})
* [Manage Kafka ACLs]({{% relref "Manage-Kafka-ACLs.md" %}})
