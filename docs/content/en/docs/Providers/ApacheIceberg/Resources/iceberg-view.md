---
categories: [ ]
tags: [ "feature", "resources" ]
title: "Iceberg View"
linkTitle: "View"
weight: 30
description: >
  Learn how to manage Apache Iceberg Views.
---

{{% pageinfo color="info" %}}
`IcebergView` resources are used to define SQL views in your Iceberg catalog.
A view is a logical definition backed by one or more SQL queries — the output schema
is inferred by the engine and populated on collect.
{{% /pageinfo %}}

## `IcebergView`

### Specification

Here is the _resource definition file_ for defining an `IcebergView`.

```yaml
apiVersion: "iceberg.jikkou.io/v1beta1"  # The api version (required)
kind: "IcebergView"                      # The resource kind (required)
metadata:
  name: <namespace>.<view>              # Fully qualified view name (required)
  labels: { }
  annotations: { }
spec:
  schema:                                # Output schema (read-only, inferred by the engine)
    columns:
      - name: <column name>
        type: <type>
        required: <true|false>
        doc: <description>
  queries:                               # SQL query definitions (at least one required)
    - sql: <SQL SELECT statement>        # The SQL defining the view (required)
      dialect: <dialect>                 # SQL dialect e.g. 'spark', 'trino', 'presto', 'hive' (required)
  defaultNamespace: <namespace>          # Default namespace for unqualified table references (optional)
  defaultCatalog: <catalog>              # Default catalog for unqualified table references (optional)
  properties:                            # View-level metadata properties (optional)
    <key>: <value>
```

### Examples

#### Simple view with daily aggregation

_`file: iceberg-daily-page-stats.yaml`_

```yaml
---
apiVersion: "iceberg.jikkou.io/v1beta1"
kind: "IcebergView"
metadata:
  name: "analytics.events.daily_page_stats"
spec:
  queries:
    - sql: >-
        SELECT
            CAST(event_time AS DATE) AS view_date,
            page_url,
            COUNT(*) AS view_count,
            COUNT(DISTINCT user_id) AS unique_users
        FROM analytics.events.page_views
        GROUP BY CAST(event_time AS DATE), page_url
      dialect: "spark"
  defaultNamespace: "analytics.events"
  properties:
    comment: "Daily page view statistics aggregated from raw events"
```

#### View with multiple SQL dialects

_`file: iceberg-multi-dialect-view.yaml`_

```yaml
---
apiVersion: "iceberg.jikkou.io/v1beta1"
kind: "IcebergView"
metadata:
  name: "analytics.events.active_users"
spec:
  queries:
    - sql: >-
        SELECT user_id, COUNT(*) AS event_count
        FROM analytics.events.page_views
        WHERE event_time >= current_date() - INTERVAL 30 DAYS
        GROUP BY user_id
      dialect: "spark"
    - sql: >-
        SELECT user_id, COUNT(*) AS event_count
        FROM analytics.events.page_views
        WHERE event_time >= current_date - INTERVAL '30' DAY
        GROUP BY user_id
      dialect: "trino"
  defaultNamespace: "analytics.events"
  properties:
    comment: "Users active in the last 30 days"
```

---

## `IcebergViewList`

If you need to define multiple views (e.g., using a template), it may be easier to use an
`IcebergViewList` resource.

### Specification

```yaml
apiVersion: "iceberg.jikkou.io/v1beta1"  # The api version (required)
kind: "IcebergViewList"                  # The resource kind (required)
metadata: { }
items: [ ]                               # An array of IcebergView
```

### Example

```yaml
---
apiVersion: "iceberg.jikkou.io/v1beta1"
kind: "IcebergViewList"
items:
  - metadata:
      name: "analytics.events.daily_page_stats"
    spec:
      queries:
        - sql: >-
            SELECT CAST(event_time AS DATE) AS view_date, page_url, COUNT(*) AS view_count
            FROM analytics.events.page_views
            GROUP BY CAST(event_time AS DATE), page_url
          dialect: "spark"
      defaultNamespace: "analytics.events"

  - metadata:
      name: "analytics.events.active_users"
    spec:
      queries:
        - sql: >-
            SELECT user_id, COUNT(*) AS event_count
            FROM analytics.events.page_views
            WHERE event_time >= current_date() - INTERVAL 30 DAYS
            GROUP BY user_id
          dialect: "spark"
      defaultNamespace: "analytics.events"
```
