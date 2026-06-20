---
title: "Manage Apache Iceberg Tables"
linkTitle: "Manage Iceberg Tables"
weight: 40
description: >
  Create Iceberg namespaces and tables, and evolve table schemas safely, as code.
---

This guide shows how to manage Apache Iceberg namespaces and tables with Jikkou, including safe schema
evolution. For the full resource specification, see the
[Iceberg Table reference]({{% relref "/docs/Reference/Providers/ApacheIceberg/Resources/iceberg-table.md" %}}).

## Before you begin

* Access to an Iceberg catalog (REST, Hive, JDBC, Glue, …) and its backing storage.
* A Jikkou context configured with the Iceberg provider — see the
  [Apache Iceberg provider configuration]({{% relref "/docs/Reference/Providers/ApacheIceberg/Configuration" %}}).

## 1. Create a namespace

_`file: iceberg-namespace.yaml`_

```yaml
---
apiVersion: "iceberg.jikkou.io/v1beta1"
kind: "IcebergNamespace"
metadata:
  name: "analytics.events"
```

```bash
jikkou apply --files ./iceberg-namespace.yaml --dry-run   # review
jikkou apply --files ./iceberg-namespace.yaml             # apply
```

## 2. Create a table

_`file: iceberg-page-views.yaml`_

```yaml
---
apiVersion: "iceberg.jikkou.io/v1beta1"
kind: "IcebergTable"
metadata:
  name: "analytics.events.page_views"
spec:
  schema:
    columns:
      - name: "event_id"
        type: "uuid"
        required: true
      - name: "user_id"
        type: "long"
        required: true
      - name: "page_url"
        type: "string"
        required: true
      - name: "event_time"
        type: "timestamptz"
        required: true
  partitionFields:
    - sourceColumn: "event_time"
      transform: "day"
  sortFields:
    - column: "event_time"
      direction: "asc"
  properties:
    write.format.default: "parquet"
    write.parquet.compression-codec: "zstd"
```

```bash
jikkou apply --files ./iceberg-page-views.yaml
```

## 3. Evolve the schema safely

To add, update, or drop columns, edit the resource and re-apply. Jikkou performs safe schema evolution
and preserves Iceberg field IDs.

To **rename** a column without breaking existing readers, set `previousName` to the old name instead of
dropping and re-adding it:

```yaml
columns:
  - name: "user_identifier"   # new name
    previousName: "user_id"   # triggers a rename, not drop+add
    type: "long"
    required: true
```

By default Jikkou rejects unsafe type changes (e.g. `string → int`). Safe promotions such as
`int → long` are allowed. To force an incompatible change on a single resource, set the annotation
`iceberg.jikkou.io/allow-incompatible-changes: "true"`.

{{% alert title="Warning" color="warning" %}}
Incompatible schema changes may break existing data readers. Use the override annotation only when you
are certain all consumers can handle the new schema.
{{% /alert %}}

## Manage many tables at once

Use an `IcebergTableList` (handy with [templating]({{% relref "/docs/Concepts/template.md" %}})) to define multiple
tables in a single file.

## Related

* [Iceberg Table reference]({{% relref "/docs/Reference/Providers/ApacheIceberg/Resources/iceberg-table.md" %}})
* [Iceberg Namespace reference]({{% relref "/docs/Reference/Providers/ApacheIceberg/Resources/iceberg-namespace.md" %}})
* [Iceberg View reference]({{% relref "/docs/Reference/Providers/ApacheIceberg/Resources/iceberg-view.md" %}})
