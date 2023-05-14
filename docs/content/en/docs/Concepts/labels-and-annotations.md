---
title: "Labels and annotations"
linkTitle: "Labels and annotations"
weight: 2
---

## Labels

You can use labels to attach arbitrary identifying metadata to objects.

Labels are key/value maps:

```yaml
metadata:
  labels:
    "key1": "value-1"
    "key2": "value-2"
```

{{% alert title="Note" color="info" %}}
The keys in the map must be string, but values can be any scalar types (_string_, _boolean_, or _numeric_).
{{% /alert %}}

{{% alert title="Labels are not persistent" color="warning" %}}
Jikkou is completely stateless. In other words, it will not store any state about the describe resources objects. Thus, when
retrieving objects from your system labels may not be reattached to the metadata objects.
{{% /alert %}}

#### Example

```yaml
metadata:
  labels:
    environment: "stating"
```

## Annotations

You can use annotations to attach arbitrary non-identifying metadata to objects.

Annotations are key/value maps:

```yaml
metadata:
  annotations:
    key1: "value-1"
    key2: "value-2"
```

{{% alert title="Note" color="info" %}}
The keys in the map must be string, but the values can be of any scalar types (_string_, _boolean_, or _numeric_).
{{% /alert %}}

### Built-in Annotations

#### `jikkou.io/ignore`

Used on: All Objects.

This annotation indicates whether the object should be ignored for reconciliation.

#### `jikkou.io/delete`

Used on: All Objects.

This annotation indicates (_when set to `true`_) that the object should be deleted from your system.

#### `jikkou.io/resource-location`

Used by jikkou.

This annotation is automatically added by Jikkou to an object when loaded from your local filesystem.

#### `jikkou.io/items-count`

Used by jikkou.

This annotation is automatically added by Jikkou to an object collection grouping several resources of homogeneous type.

#### `kafka.jikkou.io/cluster-id`

Used by jikkou.

The annotation is automatically added by Jikkou to a describe object part of an Apache Kafka cluster.