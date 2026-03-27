---
categories: [ ]
tags: [ "feature", "resources" ]
title: "Iceberg Table"
linkTitle: "Table"
weight: 20
description: >
  Learn how to manage Apache Iceberg Tables, including schema evolution.
---

{{% pageinfo color="info" %}}
`IcebergTable` resources are used to define the tables you want to manage in your Iceberg catalog.
An `IcebergTable` resource defines the schema, partition layout, sort order, and table-level
properties. Jikkou performs safe schema evolution — adding, renaming, updating, and dropping
columns — without data loss.
{{% /pageinfo %}}

## `IcebergTable`

### Specification

Here is the _resource definition file_ for defining an `IcebergTable`.

```yaml
apiVersion: "iceberg.jikkou.io/v1beta1"  # The api version (required)
kind: "IcebergTable"                      # The resource kind (required)
metadata:
  name: <namespace>.<table>              # Fully qualified table name (required)
  labels: { }
  annotations: { }
spec:
  location: <storage path>               # Override the default table location (optional)
  schema:
    identifierFields:                    # Primary-key columns for MERGE/UPSERT semantics (optional)
      - <column name>
    columns:                             # Ordered list of columns (required)
      - name: <column name>              # Column name (required)
        type: <type>                     # Column type (required) — see type reference below
        required: <true|false>           # Whether the column is non-nullable (default: false)
        doc: <description>              # Documentation string (optional)
        default: <value>                 # Initial default value, immutable after creation (optional)
        writeDefault: <value>            # Write-default for absent values, updatable (optional)
        previousName: <old name>         # Triggers a safe rename instead of drop+add (optional)
  partitionFields:                       # Partition layout (optional)
    - sourceColumn: <column>             # Source column to partition on (required)
      transform: <transform>             # Partition transform (required) — see transforms below
      name: <partition field name>       # Custom partition field name (optional)
  sortFields:                            # Default write sort order (optional)
    - column: <column>                   # Column name (mutually exclusive with term)
      term: <expression>                 # Sort expression e.g. bucket[16](user_id) (mutually exclusive with column)
      direction: <asc|desc>              # Sort direction (default: asc)
      nullOrder: <first|last>            # Null placement (default: last)
  properties:                            # Table-level metadata properties (optional)
    <key>: <value>
```

### Column Types

Jikkou maps a set of type strings to the underlying Iceberg types:

| Type string | Iceberg type | Notes |
|---|---|---|
| `boolean` | `BooleanType` | |
| `int` / `integer` | `IntegerType` | |
| `long` | `LongType` | |
| `float` | `FloatType` | |
| `double` | `DoubleType` | |
| `date` | `DateType` | |
| `time` | `TimeType` | |
| `timestamp` | `TimestampType` (without tz) | |
| `timestamptz` | `TimestampType` (with tz) | |
| `string` | `StringType` | |
| `uuid` | `UUIDType` | |
| `binary` | `BinaryType` | |
| `fixed[N]` | `FixedType(N)` | e.g. `fixed[16]` |
| `decimal(P,S)` | `DecimalType(P,S)` | e.g. `decimal(18,2)` |

Complex types (`struct`, `list`, `map`) can be expressed as nested objects using the following format:

**Struct:**

```yaml
type:
  type: "struct"
  fields:
    - name: "field_name"
      type: "string"       # Any type (primitive or nested)
      required: true        # default: false
      doc: "description"    # optional
```

**List:**

```yaml
type:
  type: "list"
  elementType: "string"     # Any type (primitive or nested)
  elementRequired: false    # default: false
```

**Map:**

```yaml
type:
  type: "map"
  keyType: "string"         # Any type (primitive or nested)
  valueType: "long"         # Any type (primitive or nested)
  valueRequired: false      # default: false
```

### Partition Transforms

| Transform | Example | Description |
|---|---|---|
| `identity` | `identity` | Partition by the exact column value |
| `year` | `year` | Extract year from a date/timestamp column |
| `month` | `month` | Extract year-month |
| `day` | `day` | Extract calendar date |
| `hour` | `hour` | Extract date-hour |
| `bucket[N]` | `bucket[16]` | Hash-bucket into N buckets |
| `truncate[W]` | `truncate[8]` | Truncate string or integer to width W |
| `void` | `void` | Always-null partition (marks dropped fields) — **not yet supported** |

### Schema Evolution

Jikkou applies schema changes in a safe, deterministic order:

1. **Incompatible change check** — verify annotation before proceeding
2. **Renames** — processed first to preserve Iceberg field IDs
3. **Column additions** — new columns appended
4. **Column updates** — type promotion, documentation, nullability, write-default changes
5. **Column deletions** — processed after updates to avoid conflicts
6. **Identifier field changes**
7. **Schema commit** — all schema changes are committed atomically
8. **Partition spec replacement**
9. **Sort order replacement**
10. **Properties update**

#### Safe Column Rename

To rename a column without losing its Iceberg field ID (which would break existing readers),
set the `previousName` field to the old column name:

```yaml
columns:
  - name: "user_identifier"   # new name
    previousName: "user_id"   # old name — triggers a rename, not drop+add
    type: "long"
    required: true
```

#### Incompatible Changes

By default, Jikkou rejects type changes that are not safe promotions (e.g., `int → long` is
safe; `string → int` is not). To allow incompatible changes on a specific resource, set the
annotation `iceberg.jikkou.io/allow-incompatible-changes: "true"`.

{{% alert title="Warning" color="warning" %}}
Incompatible schema changes may corrupt existing data readers. Use this annotation with caution
and only when you are certain all consumers can handle the new schema.
{{% /alert %}}

---

### Examples

#### Simple table with day partitioning

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
        doc: "Unique event identifier"
      - name: "user_id"
        type: "long"
        required: true
        doc: "The user who triggered the event"
      - name: "page_url"
        type: "string"
        required: true
        doc: "URL of the viewed page"
      - name: "event_time"
        type: "timestamptz"
        required: true
        doc: "Timestamp when the event occurred (UTC)"
      - name: "duration_ms"
        type: "long"
        doc: "Time spent on the page in milliseconds"
  partitionFields:
    - sourceColumn: "event_time"
      transform: "day"
  sortFields:
    - column: "event_time"
      direction: "asc"
      nullOrder: "last"
  properties:
    write.format.default: "parquet"
    write.parquet.compression-codec: "zstd"
```

#### Table with bucket partitioning and identifier fields

_`file: iceberg-orders.yaml`_

```yaml
---
apiVersion: "iceberg.jikkou.io/v1beta1"
kind: "IcebergTable"
metadata:
  name: "analytics.events.orders"
  annotations:
    iceberg.jikkou.io/allow-incompatible-changes: "false"
spec:
  schema:
    columns:
      - name: "order_id"
        type: "long"
        required: true
        doc: "Unique order identifier"
      - name: "customer_id"
        type: "long"
        required: true
        doc: "Customer who placed the order"
      - name: "order_date"
        type: "date"
        required: true
        doc: "Date the order was placed"
      - name: "status"
        type: "string"
        required: true
        doc: "Order status"
      - name: "total_amount"
        type: "decimal(18,2)"
        required: true
        doc: "Total order amount"
    identifierFields:
      - "order_id"
  partitionFields:
    - sourceColumn: "order_date"
      transform: "month"
    - sourceColumn: "customer_id"
      transform: "bucket[16]"
  sortFields:
    - column: "order_date"
      direction: "desc"
      nullOrder: "last"
    - column: "customer_id"
      direction: "asc"
      nullOrder: "last"
  properties:
    write.format.default: "parquet"
```

## `IcebergTableList`

If you need to define multiple tables (e.g., using a template), it may be easier to use an
`IcebergTableList` resource.

### Specification

```yaml
apiVersion: "iceberg.jikkou.io/v1beta1"  # The api version (required)
kind: "IcebergTableList"                  # The resource kind (required)
metadata: { }
items: [ ]                                # An array of IcebergTable
```

### Example

```yaml
---
apiVersion: "iceberg.jikkou.io/v1beta1"
kind: "IcebergTableList"
items:
  - metadata:
      name: "analytics.raw.clicks"
    spec:
      schema:
        columns:
          - name: "click_id"
            type: "uuid"
            required: true
          - name: "ts"
            type: "timestamptz"
            required: true
      partitionFields:
        - sourceColumn: "ts"
          transform: "day"

  - metadata:
      name: "analytics.raw.impressions"
    spec:
      schema:
        columns:
          - name: "impression_id"
            type: "uuid"
            required: true
          - name: "ts"
            type: "timestamptz"
            required: true
      partitionFields:
        - sourceColumn: "ts"
          transform: "day"
```
