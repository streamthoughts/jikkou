---
categories: [ ]
tags: [ "feature", "resources" ]
title: "Iceberg Namespace"
linkTitle: "Namespace"
weight: 10
description: >
  Learn how to manage Apache Iceberg Namespaces.
---

{{% pageinfo color="info" %}}
`IcebergNamespace` resources are used to define the namespaces (databases) you want to manage in your
Iceberg catalog. A namespace groups tables and carries metadata as key/value properties.
{{% /pageinfo %}}

## `IcebergNamespace`

### Specification

Here is the _resource definition file_ for defining an `IcebergNamespace`.

```yaml
apiVersion: "iceberg.jikkou.io/v1beta1"  # The api version (required)
kind: "IcebergNamespace"                  # The resource kind (required)
metadata:
  name: <namespace name>                  # Dot-separated namespace path (required)
  labels: { }
  annotations: { }
spec:
  properties:                             # Namespace-level metadata (optional)
    <key>: <value>
```

The `metadata.name` property is mandatory. Nested namespaces are expressed using dot notation —
for example, `analytics.events` creates a namespace `events` inside `analytics`.

### Example

_`file: iceberg-namespaces.yaml`_

```yaml
---
apiVersion: "iceberg.jikkou.io/v1beta1"
kind: "IcebergNamespace"
metadata:
  name: "analytics"
spec:
  properties:
    owner: "data-team"
    environment: "production"
---
apiVersion: "iceberg.jikkou.io/v1beta1"
kind: "IcebergNamespace"
metadata:
  name: "analytics.events"
spec:
  properties:
    owner: "data-team"
    team: "platform"
```

{{% alert title="" color="tips" %}}
Multiple namespaces can be included in the same YAML file by separating them with `---`.
{{% /alert %}}

## `IcebergNamespaceList`

If you need to define multiple namespaces (e.g., using a template), it may be easier to use
an `IcebergNamespaceList` resource.

### Specification

```yaml
apiVersion: "iceberg.jikkou.io/v1beta1"  # The api version (required)
kind: "IcebergNamespaceList"              # The resource kind (required)
metadata: { }
items: [ ]                                # An array of IcebergNamespace
```

### Example

```yaml
---
apiVersion: "iceberg.jikkou.io/v1beta1"
kind: "IcebergNamespaceList"
items:
  - metadata:
      name: "analytics"
    spec:
      properties:
        owner: "data-team"
  - metadata:
      name: "analytics.events"
    spec:
      properties:
        owner: "platform-team"
```
