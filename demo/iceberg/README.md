# Jikkou — Apache Iceberg Demo

> Manage Apache Iceberg namespaces and tables as code with Jikkou,
> targeting multiple catalog backends from a single configuration.

---

## Overview

This demo configures **two Iceberg catalog providers** side by side:

| Provider name        | Catalog | Backend                                      | `--provider` flag           |
|----------------------|---------|----------------------------------------------|-----------------------------|
| **`iceberg-jdbc`**   | JDBC    | PostgreSQL `:5432`                           | not needed (default)        |
| **`iceberg-nessie`** | REST    | [Nessie](https://projectnessie.org) `:19120` | `--provider iceberg-nessie` |

The same YAML resource files are applied to either catalog — only the `--provider`
flag changes.

## Architecture

```
                            ┌──────────────────────────────────────────────┐
                            │           Docker Compose stack               │
                            │                                              │
Jikkou CLI ──(JDBC)───────▶ │  PostgreSQL :5432    (iceberg-jdbc)          │
     │                      │  warehouse → /tmp/iceberg-demo               │
     │                      │                                              │
     └───────(REST)───────▶ │  Nessie :19120       (iceberg-nessie)        │
                            │                                              │
                            └──────────────────────────────────────────────┘
```

## Prerequisites

| Requirement                | Link                                                     |
|----------------------------|----------------------------------------------------------|
| Docker & Docker Compose v2 | https://docs.docker.com/get-docker/                      |
| Jikkou CLI                 | https://github.com/streamthoughts/jikkou/releases/latest |
| GitHub account             | Required to pull the Nessie image from `ghcr.io`         |

---

## Getting Started

### Step 0 — Authenticate to GitHub Container Registry

The Nessie Docker image is hosted on **GitHub Container Registry** (`ghcr.io`).
Create a [Personal Access Token](https://github.com/settings/tokens) with the
`read:packages` scope, then log in:

```bash
export CR_PAT=<your-github-token>
echo $CR_PAT | docker login ghcr.io -u USERNAME --password-stdin
```

> You only need to do this once per machine — Docker caches the credentials.

---

### Step 1 — Start the stack

```bash
cd demo/iceberg
chmod +x ./up ./down
./up
```

Verify both services are healthy:

```bash
docker compose ps
```

---

### Step 2 — Configure the Jikkou context

```bash
jikkou config set-context iceberg-demo --config-file=$(pwd)/jikkou-iceberg.conf
jikkou config use-context iceberg-demo
```

The configuration declares two providers for the same Iceberg extension:

```
provider.iceberg-jdbc    → default = true   (no flag needed)
provider.iceberg-nessie  →                  (use --provider iceberg-nessie)
```

Check the health of both catalogs:

```bash
# JDBC catalog (default)
jikkou health get iceberg | yq

# Nessie catalog
jikkou health get iceberg --provider iceberg-nessie | yq
```

<details>
<summary>Expected output — JDBC</summary>

```yaml
---
apiVersion: "core.jikkou.io/v1"
kind: "ApiHealthResult"
name: "iceberg"
status:
  name: "UP"
details:
  catalog.name: "demo"
  catalog.type: "jdbc"
```
</details>

<details>
<summary>Expected output — Nessie</summary>

```yaml
---
apiVersion: "core.jikkou.io/v1"
kind: "ApiHealthResult"
name: "iceberg"
status:
  name: "UP"
details:
  catalog.name: "nessie"
  catalog.type: "rest"
```
</details>

---

## Part A — JDBC Catalog (default provider)

> `iceberg-jdbc` is the **default** provider — no `--provider` flag needed.

### Step 3 — Create namespaces

```bash
cat ./resources/01-namespaces.yaml | yq          # inspect the definition
jikkou apply --files ./resources/01-namespaces.yaml
jikkou get icebergnamespaces | yq                 # verify
```

Run `apply` a second time — Jikkou detects no drift and makes **zero changes**.

---

### Step 4 — Create a table

```bash
cat ./resources/02-table-initial.yaml | yq        # inspect
jikkou apply --files ./resources/02-table-initial.yaml
jikkou get icebergtables --name analytics.events.page_views | yq
```

The `page_views` table ships with:

- 4 required columns — `event_id`, `user_id`, `page_url`, `event_time`
- Day partitioning on `event_time`
- Sort by `event_time` ascending

---

### Step 5 — Schema evolution (add columns)

```bash
jikkou diff  --files ./resources/03-table-evolved.yaml | yq   # preview
jikkou apply --files ./resources/03-table-evolved.yaml         # apply
jikkou get icebergtables --name analytics.events.page_views | yq
```

Two new **optional** columns (`duration_ms`, `referrer`) appear as `ADD` operations.
Iceberg increments the schema version — existing data files remain readable without
any rewrite.

---

### Step 6 — Safe column rename

```bash
jikkou diff  --files ./resources/04-table-renamed.yaml | yq   # preview
jikkou apply --files ./resources/04-table-renamed.yaml         # apply
```

`page_url` is renamed to `url` via the `previousName` field. Jikkou issues an Iceberg
**rename** (not drop + add), preserving the internal field ID so existing Parquet files
stay readable.

---

### Step 7 — Idempotency check

```bash
jikkou apply --files ./resources/04-table-renamed.yaml
```

No changes — live state already matches the desired state.

---

### Step 8 — Delete a table

```bash
jikkou diff  --files ./resources/05-table-delete.yaml | yq    # preview
jikkou apply --files ./resources/05-table-delete.yaml          # apply
jikkou get icebergtables | yq                                  # verify
```

The `jikkou.io/delete: true` annotation drops the table from the catalog.
Data files are kept unless `delete-purge` is enabled.

---

### Step 9 — Inspect the JDBC catalog in PostgreSQL

```bash
# List Iceberg catalog tables
docker exec -it iceberg-postgres \
  psql -U iceberg -d iceberg -c "\dt"

# Tracked tables
docker exec -it iceberg-postgres \
  psql -U iceberg -d iceberg \
  -c "SELECT catalog_name, table_namespace, table_name FROM iceberg_tables;"

# Namespace properties
docker exec -it iceberg-postgres \
  psql -U iceberg -d iceberg \
  -c "SELECT * FROM iceberg_namespace_properties;"
```

---

### Step 10 — Inspect metadata files

```bash
find /tmp/iceberg-demo -name "*.json" | sort
cat $(find /tmp/iceberg-demo -name "*.json" | head -1) | jq .
```

---

## Part B — Nessie Catalog (`--provider iceberg-nessie`)

> The same resource files work against Nessie — just add
> **`--provider iceberg-nessie`** to every command.

### Step 11 — Create namespaces

```bash
jikkou apply --provider iceberg-nessie --files ./resources/01-namespaces.yaml
jikkou get icebergnamespaces --provider iceberg-nessie | yq
```

---

### Step 12 — Create a table

```bash
jikkou apply --provider iceberg-nessie --files ./resources/02-table-initial.yaml
jikkou get icebergtables --provider iceberg-nessie | yq
```

---

### Step 13 — Schema evolution

```bash
jikkou diff  --provider iceberg-nessie --files ./resources/03-table-evolved.yaml | yq
jikkou apply --provider iceberg-nessie --files ./resources/03-table-evolved.yaml
```

---

### Step 14 — Safe column rename

```bash
jikkou apply --provider iceberg-nessie --files ./resources/04-table-renamed.yaml
```

---

### Step 15 — Delete a table

```bash
jikkou apply --provider iceberg-nessie --files ./resources/05-table-delete.yaml
jikkou get icebergtables --provider iceberg-nessie | yq
```

---

### Step 16 — Explore the Nessie catalog

Nessie provides a REST API with **Git-like** versioning — every schema change is a commit:

```bash
# List all entries on the main branch
curl -s http://localhost:19120/api/v2/trees/main/entries | jq .

# View the commit log
curl -s http://localhost:19120/api/v2/trees/main/log | jq .
```

---

## Tear down

```bash
./down
rm -rf /tmp/iceberg-demo
```

---

## Going further

| Topic                        | Reference                                                                                              |
|------------------------------|--------------------------------------------------------------------------------------------------------|
| Full resource reference      | [`docs/content/en/docs/Providers/ApacheIceberg/`](../../docs/content/en/docs/Providers/ApacheIceberg/) |
| More YAML examples           | [`examples/iceberg/`](../../examples/iceberg/)                                                         |
| Partition transforms         | `iceberg-table.md` — Partition Transforms table                                                        |
| Incompatible type changes    | Annotation `iceberg.jikkou.io/allow-incompatible-changes`                                              |
| Controller safety flags      | `Configuration/_index.md` — `delete-orphans`, `delete-purge`                                           |
| Multi-provider configuration | [`docs/content/en/docs/Concepts/providers.md`](../../docs/content/en/docs/Concepts/providers.md)       |
| Nessie documentation         | https://projectnessie.org/docs/                                                                        |
