---
title: "Manage Kafka ACLs"
linkTitle: "Manage Kafka ACLs"
weight: 10
description: >
  Declare and apply Access Control Lists (ACLs) for principals on your Apache Kafka cluster.
---

This guide shows how to manage Kafka Access Control Lists (ACLs) as code with Jikkou. For the full
resource specification, see the [Kafka Authorizations reference]({{% relref "/docs/Reference/Providers/Kafka/Resources/acls.md" %}}).

## Before you begin

* A running Apache Kafka cluster with an authorizer enabled (e.g. `StandardAuthorizer` or `AclAuthorizer`).
* A configured Jikkou context pointing at your cluster — see [Getting Started]({{% relref "/docs/Tutorials/get_started.md" %}}).
* A principal with permission to manage ACLs.

## 1. Describe the ACLs you want

Create a file describing the desired authorizations for each principal.

_`file: kafka-acls.yaml`_

```yaml
---
apiVersion: "kafka.jikkou.io/v1beta2"
kind: "KafkaPrincipalAuthorization"
metadata:
  name: "User:Alice"
spec:
  acls:
    - resource:
        type: 'topic'
        pattern: 'orders-'
        patternType: 'PREFIXED'
      type: "ALLOW"
      operations: [ 'READ', 'WRITE' ]
      host: "*"
```

## 2. Preview the changes

Always run in `--dry-run` first to review what Jikkou will do:

```bash
jikkou apply --files ./kafka-acls.yaml --dry-run
```

## 3. Apply the ACLs

```bash
jikkou apply --files ./kafka-acls.yaml
```

## Reuse permissions with roles

To avoid repeating the same ACLs for many principals, define a `KafkaPrincipalRole` once and reference
it from several principals:

```yaml
---
apiVersion: "kafka.jikkou.io/v1beta2"
kind: "KafkaPrincipalRole"
metadata:
  name: "OrdersReadWrite"
spec:
  acls:
    - type: "ALLOW"
      operations: [ 'READ', 'WRITE' ]
      resource:
        type: 'topic'
        pattern: 'orders-'
        patternType: 'PREFIXED'
      host: "*"
---
apiVersion: "kafka.jikkou.io/v1beta2"
kind: "KafkaPrincipalAuthorization"
metadata:
  name: "User:Alice"
spec:
  roles:
    - "OrdersReadWrite"
```

## Delete ACLs

Jikkou reconciles to the declared state. With `apply`, any ACL that exists on the cluster but is **not**
present in your resource files will be deleted for the principals you describe. To delete all ACLs for a
principal, add the delete annotation:

```yaml
apiVersion: "kafka.jikkou.io/v1beta2"
kind: "KafkaPrincipalAuthorization"
metadata:
  name: "User:Alice"
  annotations:
    jikkou.io/delete: true
spec:
  acls: []
```

{{% alert title="Recommendation" color="warning" %}}
In production, scope changes with `--selector` and always review them with `--dry-run` before applying.
{{% /alert %}}

## Related

* [Kafka Authorizations reference]({{% relref "/docs/Reference/Providers/Kafka/Resources/acls.md" %}})
* [Enforce governance policies]({{% relref "Enforce-Policies.md" %}})
