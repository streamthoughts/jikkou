---
title: "Configuration"
linkTitle: "Configuration"
weight: 2
description: >
  Learn how to configure the extensions for Apache Iceberg.
---

{{% pageinfo %}}
Here, you will find the configuration properties for the Apache Iceberg extension.
{{% /pageinfo %}}

## Configuration

The Apache Iceberg extension connects to an Iceberg catalog. You configure it through the Jikkou
client configuration property `jikkou.provider.iceberg`.

**Example (JDBC catalog with PostgreSQL):**

```hocon
jikkou {
  provider.iceberg {
    enabled = true
    type = io.jikkou.iceberg.IcebergExtensionProvider
    config = {
      # Required — type of Iceberg catalog.
      # Accepted values: rest, hive, jdbc, glue, nessie, hadoop
      catalogType = "jdbc"

      # The catalog name used to identify this catalog instance.
      catalogName = "default"

      # The URI of the catalog endpoint (REST URL, Hive Metastore URI, JDBC URL, etc.)
      catalogUri = "jdbc:postgresql://localhost:5432/iceberg"

      # The warehouse root location (e.g., local path, S3 bucket path, HDFS path)
      warehouse = "/tmp/iceberg-warehouse"

      # Extra catalog-specific properties passed directly to CatalogUtil.buildIcebergCatalog()
      catalogProperties {
        jdbc.user     = "iceberg"
        jdbc.password = "iceberg"
      }

      # Enable verbose debug logging for catalog operations (default: false)
      debugLoggingEnabled = false
    }
  }
}
```

### Configuration Properties

| Property | Type | Required | Default | Description |
|---|---|---|---|---|
| `catalogType` | String | **yes** | — | Iceberg catalog type: `rest`, `hive`, `jdbc`, `glue`, `nessie`, `hadoop` |
| `catalogName` | String | no | `default` | The catalog instance name |
| `catalogUri` | String | no | — | Catalog endpoint URI (REST API URL, Hive Metastore thrift URI, JDBC URL, Nessie server URL) |
| `warehouse` | String | no | — | Warehouse root location (e.g., `s3://bucket/warehouse`, `/tmp/iceberg`) |
| `catalogProperties` | Map | no | — | Additional catalog properties forwarded verbatim to the Iceberg `CatalogUtil` |
| `debugLoggingEnabled` | Boolean | no | `false` | Enable debug-level logging for catalog operations |

### Catalog Types

#### JDBC Catalog (PostgreSQL)

Stores catalog metadata (namespaces, table specs) in a relational database.
The PostgreSQL JDBC driver is bundled in the Jikkou CLI distribution.

```hocon
config = {
  catalogType = "jdbc"
  catalogUri  = "jdbc:postgresql://localhost:5432/iceberg"
  warehouse   = "/tmp/iceberg-warehouse"
  catalogProperties {
    jdbc.user     = "iceberg"
    jdbc.password = "iceberg"
  }
}
```

#### REST Catalog

Connects to any Iceberg REST Catalog API (e.g., Polaris, Gravitino, Unity Catalog):

```hocon
config = {
  catalogType = "rest"
  catalogUri  = "https://polaris.example.com/api/catalog"
  warehouse   = "s3://my-bucket/warehouse"
  catalogProperties {
    rest.signing-name   = "execute-api"
    rest.signing-region = "us-east-1"
  }
}
```

#### Hive Metastore

Connects to an Apache Hive Metastore (requires `iceberg-hive-metastore` on the classpath):

```hocon
config = {
  catalogType = "hive"
  catalogUri  = "thrift://hive-metastore:9083"
  warehouse   = "hdfs://namenode:8020/user/hive/warehouse"
}
```

#### AWS Glue

Connects to AWS Glue Data Catalog (requires `iceberg-aws` on the classpath):

```hocon
config = {
  catalogType = "glue"
  warehouse   = "s3://my-bucket/warehouse"
  catalogProperties {
    glue.region = "us-east-1"
  }
}
```

#### Nessie

Nessie exposes a standard **Iceberg REST catalog** endpoint at `/iceberg`. Using
`catalogType = "rest"` is recommended because it relies only on `iceberg-core`
(always bundled in the Jikkou CLI). The `catalogType = "nessie"` variant requires
the optional `iceberg-nessie` JAR on the classpath.

```hocon
# Recommended: use Nessie's built-in Iceberg REST endpoint
config = {
  catalogType = "rest"
  catalogUri  = "http://nessie:19120/iceberg"
  warehouse   = "s3://my-bucket/warehouse"
  catalogProperties {
    prefix = "main"   # Nessie branch
  }
}
```

### Controller Settings

The table and view controllers expose additional options to control reconciliation behaviour.
These are set inside the provider `config` block.

#### Table Controller

| Property | Type | Default | Description |
|---|---|---|---|
| `delete-orphans` | Boolean | `false` | Drop tables that exist in the catalog but are not defined in any resource |
| `delete-orphan-columns` | Boolean | `false` | Drop columns present in the live table but absent from the spec |
| `delete-purge` | Boolean | `false` | Purge underlying data files when dropping a table (irreversible) |
| `tables.deletion.exclude` | List\<Pattern\> | `[]` | Regex patterns — matching table names are never deleted |

#### View Controller

| Property | Type | Default | Description |
|---|---|---|---|
| `delete-orphans` | Boolean | `false` | Drop views that exist in the catalog but are not defined in any resource |
| `views.deletion.exclude` | List\<Pattern\> | `[]` | Regex patterns — matching view names are never deleted |

{{% alert title="Note" color="info" %}}
The `delete-orphans` property applies independently to tables and views.
The `IcebergNamespaceController` always uses `delete-orphans = false` to prevent accidental
deletion of namespaces that may still contain tables. To delete a namespace, use the `DELETE`
reconciliation mode explicitly.
{{% /alert %}}

**Example:**

```hocon
jikkou {
  provider.iceberg {
    enabled = true
    type = io.jikkou.iceberg.IcebergExtensionProvider
    config = {
      catalogType = "rest"
      catalogUri  = "http://localhost:8181"
      warehouse   = "s3://my-bucket/warehouse"

      # Table reconciliation safety settings
      delete-orphans        = false
      delete-orphan-columns = false
      delete-purge          = false

      # Never delete tables whose name starts with "audit_"
      tables.deletion.exclude = ["^audit_.*"]

      # Never delete views whose name starts with "v_core_"
      views.deletion.exclude = ["^v_core_.*"]
    }
  }
}
```
