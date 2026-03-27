---
title: "Annotations"
linkTitle: "Annotations"
weight: 60
description: >
  Learn how to use the metadata annotations provided by the extensions for Apache Iceberg.
---

{{% pageinfo %}}
Here, you will find information about the annotations provided by the Apache Iceberg extension for Jikkou.
{{% /pageinfo %}}

### List of built-in annotations

#### `iceberg.jikkou.io/allow-incompatible-changes`

Controls whether incompatible schema changes are allowed when reconciling an `IcebergTable`.

By default, Jikkou rejects type changes that cannot be safely promoted (for example, changing
a column from `string` to `int`). Setting this annotation to `"true"` on a specific table
resource lifts that restriction for that resource only.

```yaml
metadata:
  annotations:
    iceberg.jikkou.io/allow-incompatible-changes: "true"
```

{{% alert title="Warning" color="warning" %}}
Incompatible schema changes may break existing readers. Only enable this annotation when you
are certain all consumers of the table can handle the new schema.
{{% /alert %}}

#### `iceberg.jikkou.io/namespace-location`

Read-only annotation populated by the namespace collector. Contains the storage location
of a namespace as reported by the catalog. This annotation is set automatically when
collecting existing namespaces and does not need to be specified in resource files.

#### `iceberg.jikkou.io/table-location`

Reserved annotation for the storage location of a table. Currently, the table location
is stored in the `spec.location` field rather than as an annotation. This annotation key
is defined for future use.
