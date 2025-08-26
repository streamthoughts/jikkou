---
title: "ValidatingResourcePolicy"
description: "The ValidatingResourcePolicy resource defines validation rules and selection strategies for applying policies to resources in Jikkou."
weight: 20
---

The `ValidatingResourcePolicy` resource is used to define **validation rules** applied to resources or resource changes before they are applied by Jikkou.  
It allows enforcing organizational policies, validating constraints, or filtering out undesired operations.

Each policy can select one or more resource kinds and define **rules** expressed in [Google CEL (Common Expression Language)](https://opensource.google/projects/cel).  
Rules can either **fail** the execution or **filter** the invalid resources, depending on the configured `failurePolicy`.

---

## Specification

```yaml
apiVersion: core.jikkou.io/v1
kind: ValidatingResourcePolicy
metadata:
  name: <string> # Required. Unique policy name.
spec:
  failurePolicy: <string> # Required. One of: FAIL | FILTER
  selector:
    matchingStrategy: <string> # Optional. One of: ALL | ANY (default: ALL)
    matchResources:
      - apiVersion: <string> # Optional. API version to match (e.g., core.jikkou.io/v1)
        kind: <string>       # Required. Resource kind (e.g., KafkaTopic)
    matchLabels:
      - key: <string>       # Label key to match
        operator: <string>  # One of: In | NotIn | Exists | DoesNotExist
        values: [<string>]  # Optional list of values
    matchExpressions:
      - <string> # CEL expression
  rules:
    - name: <string> # Required. Rule identifier.
      expression: <string> # Required. A CEL expression evaluated against the resource.
      message: <string> # Optional. Static message returned when the rule fails.
      messageExpression: <string> # Optional. CEL expression to generate a dynamic error message.
```

---

## Fields

| Field                            | Type     | Required | Description                                                                                                                                                                                         |
|----------------------------------|----------|----------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `spec.failurePolicy`             | `string` | Yes      | Defines the policy behavior when validation fails. Possible values:<br/>• `FAIL` → stop execution with an error.<br/>• `FILTER` → skip the invalid resource(s) but continue processing others.      |
| `spec.selector.matchingStrategy` | `string` | No       | Strategy for combining multiple selectors. Possible values:<br/>• `ALL` → resource must match **all** conditions.<br/>• `ANY` → resource must match **at least one** condition.<br/>Default: `ALL`. |
| `spec.selector.matchResources`   | `list`   | No       | Selects resources by API version and kind.                                                                                                                                                          |
| `spec.selector.matchLabels`      | `list`   | No       | Selects resources based on labels, using operators (`In`, `NotIn`, `Exists`, `DoesNotExist`).                                                                                                       |
| `spec.selector.matchExpressions` | `list`   | No       | Selects resources using CEL expressions for advanced filtering.                                                                                                                                     |
| `spec.rules`                     | `list`   | Yes      | A list of validation rules.                                                                                                                                                                         |
| `spec.rules[].name`              | `string` | Yes      | A unique identifier for the rule.                                                                                                                                                                   |
| `spec.rules[].expression`        | `string` | Yes      | A CEL expression evaluated against the resource. The rule fails when the expression evaluates to `true`.                                                                                            |
| `spec.rules[].message`           | `string` | No       | Static error message returned when validation fails.                                                                                                                                                |
| `spec.rules[].messageExpression` | `string` | No       | CEL expression returning a dynamic error message string.                                                                                                                                            |

---

## Resource Selection

Policies define which resources they apply to using a **selector**.  
A selector can combine multiple strategies to target resources based on:

- **Resource metadata** (kind, apiVersion).
- **Labels** (with operators like `In`, `NotIn`, `Exists`, `DoesNotExist`).
- **CEL expressions** (arbitrary conditions on resource content).

### Matching Strategy

| Strategy | Description                                                                                                    |
|----------|----------------------------------------------------------------------------------------------------------------|
| `ALL`    | The resource must match **all** specified selectors (`matchResources`, `matchLabels`, and `matchExpressions`). |
| `ANY`    | The resource is selected if it matches **at least one** of the specified selectors.                            |

Default: `ALL`

---

### `matchResources`

Selects resources by API version and/or kind.

```yaml
matchResources:
  - apiVersion: core.jikkou.io/v1
    kind: KafkaTopic
```

- `apiVersion` → Optional. Restricts matching to a specific API group/version.
- `kind` → Required. Matches the resource kind (e.g. `KafkaTopic`, `KafkaTopicChange`).

---

### `matchLabels`

Selects resources based on their **metadata labels** using operators.

```yaml
matchLabels:
  - key: environment
    operator: In
    values: ["prod", "staging"]
  - key: team
    operator: NotIn
    values: ["test"]
  - key: critical
    operator: Exists
```

Supported operators:

| Operator       | Description                                                  |
|----------------|--------------------------------------------------------------|
| `In`           | Matches if the label value is in the list of values.         |
| `NotIn`        | Matches if the label value is **not** in the list of values. |
| `Exists`       | Matches if the label key is defined (value doesn’t matter).  |
| `DoesNotExist` | Matches if the label key is **not** defined.                 |

---

### `matchExpressions`

Selects resources using **CEL expressions** for maximum flexibility.

```yaml
matchExpressions:
  - "resource.metadata.name.startsWith('topic-')"
  - "resource.spec.partitions > 10"
```

Examples:
- Match resources with names starting with `topic-`.
- Match topics with more than 10 partitions.

---

## Examples

### Example 1: Filtering `DELETE` operations on `KafkaTopic` resources

```yaml
apiVersion: core.jikkou.io/v1
kind: ValidatingResourcePolicy
metadata:
  name: KafkaTopicPolicy
spec:
  failurePolicy: FILTER
  selector:
    matchResources:
      - kind: KafkaTopicChange
  rules:
    - name: FilterDeleteOperation
      expression: "size(resource.spec.changes) > 0 && resource.spec.op == 'DELETE'"
      messageExpression: "'Operation ' + resource.spec.op + ' on topics is not authorized'"
```

This policy prevents **delete operations** on Kafka topics from being executed by filtering them out.

---

### Example 2: Validating partitions count for `KafkaTopic`

```yaml
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
      expression: "resource.spec.partitions >= 50"
      messageExpression: "'Topic partition MUST be inferior to 50, but was: ' + string(resource.spec.partitions)"

    - name: MinTopicPartitions
      expression: "resource.spec.partitions < 3"
      message: "Topic must have at-least 3 partitions"
```

This policy enforces a **minimum of 3 partitions** and a **maximum of 49 partitions** for Kafka topics.

---

### Example 3: Match only KafkaTopic in `prod` environment

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

---

### Example 4: Match any KafkaTopic OR resources with label `critical=true`

```yaml
selector:
  matchingStrategy: ANY
  matchResources:
    - kind: KafkaTopic
  matchLabels:
    - key: critical
      operator: In
      values: ["true"]
```

---

### Example 5: Match using CEL expression

```yaml
selector:
  matchExpressions:
    - "resource.spec.replicationFactor < 3"
```

---

## Use cases

- Preventing destructive operations (e.g., deleting topics, removing configs).
- Enforcing resource limits (e.g., partition count, replication factor).
- Ensuring naming conventions or metadata compliance.
- Dynamically generating error messages with contextual information.  