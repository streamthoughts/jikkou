# Apache Iceberg provider

## Kinds

| Kind | Get command | apiVersion |
|---|---|---|
| IcebergNamespace | `jikkou get iceberg namespaces` | iceberg.jikkou.io/v1beta1 |
| IcebergTable | `jikkou get iceberg tables` | iceberg.jikkou.io/v1beta1 |
| IcebergView | `jikkou get iceberg views` | iceberg.jikkou.io/v1beta1 |

All get commands accept `-o JSON|YAML`, `--name <name>`, and selectors `-s '<expr>'`.

## Authoring examples

Namespace (one file, `---`-separated for a nested namespace):
```yaml
apiVersion: 'iceberg.jikkou.io/v1beta1'
kind: 'IcebergNamespace'
metadata:
  name: 'analytics'
spec:
  properties:
    owner: 'data-team'
    environment: 'production'
---
apiVersion: 'iceberg.jikkou.io/v1beta1'
kind: 'IcebergNamespace'
metadata:
  name: 'analytics.events'
spec:
  properties:
    owner: 'data-team'
    team: 'platform'
```

Table:
```yaml
apiVersion: 'iceberg.jikkou.io/v1beta1'
kind: 'IcebergTable'
metadata:
  name: 'analytics.events.page_views'
spec:
  schema:
    columns:
      - name: 'event_id'
        type: 'uuid'
        required: true
        doc: 'Unique event identifier'
      - name: 'user_id'
        type: 'long'
        required: true
        doc: 'The user who triggered the event'
      - name: 'event_time'
        type: 'timestamptz'
        required: true
        doc: 'Timestamp when the event occurred (UTC)'
  partitionFields:
    - sourceColumn: 'event_time'
      transform: 'day'
  sortFields:
    - column: 'event_time'
      direction: 'asc'
      nullOrder: 'last'
  properties:
    write.format.default: 'parquet'
    write.parquet.compression-codec: 'zstd'
```

`spec.partitionFields[].transform` accepts `identity`, `year`, `month`, `day`, `hour`, `bucket[N]`, `truncate[W]`, `void`. `spec.sortFields[].nullOrder` accepts only `first` or `last`; anything else silently falls back to `last`.

View (author `spec.queries`, not `spec.schema` — the view's `schema` is read-only, inferred by the engine on collect):
```yaml
apiVersion: 'iceberg.jikkou.io/v1beta1'
kind: 'IcebergView'
metadata:
  name: 'analytics.events.daily_page_views'
spec:
  defaultCatalog: 'default'
  defaultNamespace: 'analytics.events'
  queries:
    - dialect: 'spark'
      sql: 'SELECT date(event_time) AS day, count(*) AS views FROM page_views GROUP BY date(event_time)'
  properties:
    comment: 'Daily page-view counts'
```

At least one `queries` entry is required; `defaultNamespace`/`defaultCatalog` resolve unqualified table references in the SQL.

## Notes

- Requires a catalog configured under `jikkou.provider.iceberg.config`: `catalogType` (required — `rest`, `hive`, `jdbc`, `glue`, `nessie`, `hadoop`), `catalogName` (default `default`), `catalogUri`, `warehouse`, and free-form `catalogProperties` forwarded to Iceberg's `CatalogUtil`. Nessie is best reached via `catalogType: 'rest'` pointed at its built-in Iceberg REST endpoint (`.../iceberg`), since the dedicated `nessie` catalog type needs an optional JAR not always bundled.
- Table/view controller safety switches (also under the provider `config` block): `delete-orphans` (default `false`), `delete-orphan-columns` (default `false`), `delete-purge` (default `false`, irreversible), `tables.deletion.exclude` / `views.deletion.exclude` (regex allow-lists that are never deleted). The `IcebergNamespaceController` always uses `delete-orphans = false`; delete a namespace explicitly with the `DELETE` reconciliation mode instead.
- Deleting a single resource: add `jikkou.io/delete: true` under `metadata.annotations`, then `jikkou apply`.
- `jikkou api-resources schema --api-version=iceberg.jikkou.io/v1beta1 --kind=<Kind>` prints the JSON Schema for a kind's `spec`.
