---
categories: [ ]
tags: [ "feature", "resources" ]
title: "Schema for AWS Glue Schema Registry"
linkTitle: "Schema for AWS Glue Schema Registry"
weight: 40
description: >
  Learn how to manage Schema in AWS Glue Schema Registry.
---

{{% pageinfo color="info" %}}
_AwsGlueSchema_ resources are used to define the schemas you want to manage on your AWS Glue Schema registry. A
AwsGlueSchema resource defines the schema, and the compatibility mode to be associated with a subject definition.
{{% /pageinfo %}}

## `AwsGlueSchema`

### Specification

Here is the _resource definition file_ for defining a `AwsGlueSchema`.

```yaml
apiVersion: "aws.jikkou.io/v1"                  # The api version (required)
kind: "AwsGlueSchema"                           # The resource kind (required)    
metadata:
  name: <The name of the subject>               # The schema name (required)
  labels:
    glue.aws.amazon.com/registry-name:          # The registry name (required)
  annotations: { }
spec:
  compatibility: <compatibility>                # The schema compatibility level for this subject (required).
  dataFormat: <The data format>                 # Accepted values are: AVRO, PROTOBUF, JSON (required).
  schemaDefinition:
    $ref: <url or path>  # 
]
```

The `metadata.name` property is mandatory and specifies the name of the _Subject_.

### Compatibility

Supported compatibility mode are:

* `BACKWARD` (recommended) — Consumer can read both current and previous version.
* `BACKWARD_ALL` — Consumer can read current and all previous versions.
* `FORWARD` — Consumer can read both current and subsequent version.
* `FORWARD_ALL` — Consumer can read both current and all subsequent versions.
* `FULL` — Combination of Backward and Forward.
* `FULL_ALL` — Combination of Backward all and Forward all.
* `NONE` — No compatibility checks are performed.
* `DISABLED` — Prevent any versioning for this schema

### Example

Here is a simple example that shows how to define a single subject AVRO schema for type using
the `AwsGlueSchema` resource type.

_`file: subject-user.yaml`_

```yaml
---
apiVersion: "aws.jikkou.io/v1"
kind: "AwsGlueSchema"
metadata:
  name: "User"
  labels:
    glue.aws.amazon.com/registry-name: Test
  annotations:
    glue.aws.amazon.com/normalize-schema: true
spec:
  compatibility: "FULL_TRANSITIVE"
  schemaType: "AVRO"
  schema:
    $ref: ./user-schema.avsc
```

_`file: user-schema.avsc`_

```yaml
---
{
  "namespace": "example.avro",
  "type": "record",
  "name": "User",
  "fields": [
    {
      "name": "name",
      "type": [ "null", "string" ],
      "default": null,
    },
    {
      "name": "favorite_number",
      "type": [ "null", "int" ],
      "default": null
    },
    {
      "name": "favorite_color",
      "type": [ "null", "string" ],
      "default": null
    }
  ]
}
```

Alternatively, we can directly pass the Avro schema as follows :

_`file: subject-user.yaml`_

```yaml
---
apiVersion: "aws.jikkou.io/v1"
kind: "AwsGlueSchema"
metadata:
  name: "User"
  labels: { }
  annotations:
    glue.aws.amazon.com/normalize-schema: true
spec:
  compatibility: "FULL_TRANSITIVE"
  schemaType: "AVRO"
  schema: |
    {
      "namespace": "example.avro",
      "type": "record",
      "name": "User",
      "fields": [
        {
          "name": "name",
          "type": [ "null", "string" ],
          "default": null
        },
        {
          "name": "favorite_number",
          "type": [ "null", "int" ],
          "default": null
        },
        {
          "name": "favorite_color",
          "type": [ "null", "string"],
          "default": null
        }
      ]
    }
```