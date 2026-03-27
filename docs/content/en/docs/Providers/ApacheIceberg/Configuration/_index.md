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

**Example (REST catalog):**

```hocon
jikkou {
  provider.iceberg {
    enabled = true
    config = {
      # Required — type of Iceberg catalog.
      # Accepted values: rest, hive, jdbc, glue, nessie, hadoop
      catalog-type = "rest"

      # The catalog name used to identify this catalog instance.
      catalog-name = "default"

      # The URI of the catalog endpoint (REST, Hive Metastore, JDBC, Nessie server, etc.)
      catalog-uri = "http://localhost:8181"

      # The warehouse root location (e.g., S3 bucket path, HDFS path, local path)
      warehouse = "s3://my-bucket/warehouse"

      # Extra catalog-specific properties passed directly to CatalogUtil.buildIcebergCatalog()
      catalog-properties {
        # s3.endpoint = "http://localhost:9000"
        # s3.path-style-access = "true"
      }

      # Enable verbose debug logging for catalog operations (default: false)
      debug-logging-enabled = false
    }
  }
}
```

### Configuration Properties

| Property | Type | Required | Default | Description |
|---|---|---|---|---|
| `catalog-type` | String | **yes** | — | Iceberg catalog type: `rest`, `hive`, `jdbc`, `glue`, `nessie`, `hadoop` |
| `catalog-name` | String | no | `default` | The catalog instance name |
| `catalog-uri` | String | no | — | Catalog endpoint URI (REST API URL, Hive Metastore thrift URI, JDBC URL, Nessie server URL) |
| `warehouse` | String | no | — | Warehouse root location (e.g., `s3://bucket/warehouse`) |
| `catalog-properties` | Map | no | — | Additional catalog properties forwarded verbatim to the Iceberg `CatalogUtil` |
| `debug-logging-enabled` | Boolean | no | `false` | Enable debug-level logging for catalog operations |

### Catalog Types

#### REST Catalog

Connects to any Iceberg REST Catalog API (e.g., Polaris, Gravitino, Unity Catalog):

```hocon
config = {
  catalog-type = "rest"
  catalog-uri  = "https://polaris.example.com/api/catalog"
  warehouse    = "s3://my-bucket/warehouse"
  catalog-properties {
    rest.signing-name   = "execute-api"
    rest.signing-region = "us-east-1"
  }
}
```

#### Hive Metastore

Connects to an Apache Hive Metastore:

```hocon
config = {
  catalog-type = "hive"
  catalog-uri  = "thrift://hive-metastore:9083"
  warehouse    = "hdfs://namenode:8020/user/hive/warehouse"
}
```

#### AWS Glue

Connects to AWS Glue Data Catalog (requires `iceberg-aws` on the classpath):

```hocon
config = {
  catalog-type = "glue"
  warehouse    = "s3://my-bucket/warehouse"
  catalog-properties {
    glue.region = "us-east-1"
  }
}
```

#### Nessie

Connects to a Project Nessie catalog (requires `iceberg-nessie` on the classpath):

```hocon
config = {
  catalog-type = "nessie"
  catalog-uri  = "http://nessie:19120/api/v1"
  warehouse    = "s3://my-bucket/warehouse"
  catalog-properties {
    nessie.ref              = "main"
    nessie.authentication.type = "BEARER"
    nessie.authentication.token = "${?NESSIE_TOKEN}"
  }
}
```

### Controller Settings

The `IcebergTableController` exposes additional options to control reconciliation behaviour.
These are set inside the provider `config` block.

| Property | Type | Default | Applies to | Description |
|---|---|---|---|---|
| `delete-orphans` | Boolean | `false` | Tables only | Drop tables that exist in the catalog but are not defined in any resource |
| `delete-orphan-columns` | Boolean | `false` | Tables only | Drop columns present in the live table but absent from the spec |
| `delete-purge` | Boolean | `false` | Tables only | Purge underlying data files when dropping a table (irreversible) |
| `tables.deletion.exclude` | List\<Pattern\> | `[]` | Tables only | Regex patterns — matching table names are never deleted |

{{% alert title="Note" color="info" %}}
The `IcebergNamespaceController` always uses `delete-orphans = false` to prevent accidental
deletion of namespaces that may still contain tables. To delete a namespace, use the `DELETE`
reconciliation mode explicitly.
{{% /alert %}}

**Example:**

```hocon
jikkou {
  provider.iceberg {
    enabled = true
    config = {
      catalog-type = "rest"
      catalog-uri  = "http://localhost:8181"
      warehouse    = "s3://my-bucket/warehouse"

      # Reconciliation safety settings
      delete-orphans        = false
      delete-orphan-columns = false
      delete-purge          = false

      # Never delete tables whose name starts with "audit_"
      tables.deletion.exclude = ["^audit_.*"]
    }
  }
}
```
