---
categories: [ ]
tags: [ "feature", "resources" ]
title: "Schema Registry Subjects"
linkTitle: "Subjects"
weight: 10
description: >
  Learn how to manage SchemaRegistry Subjects.
---

{{% pageinfo color="info" %}}
_SchemaRegistrySubject_ resources are used to define the subject schemas you want to manage on your SchemaRegistry. A
SchemaRegistrySubject resource defines the schema, the compatibility level, and the references to be associated with a
subject version.
{{% /pageinfo %}}

## `SchemaRegistrySubject`

### Specification

Here is the _resource definition file_ for defining a `SchemaRegistrySubject`.

```yaml
apiVersion: "schemaregistry.jikkou.io/v1beta2"  # The api version (required)
kind: "SchemaRegistrySubject"                   # The resource kind (required)
metadata:
  name: <The name of the subject>               # (required)
  labels: { }
  annotations: { }
spec:
  schemaRegistry:
    vendor: <vendor_name>                       # (optional) The vendor of the SchemaRegistry, e.g., Confluent, Karapace, etc
  compatibilityLevel: <compatibility_level>     # (optional) The schema compatibility level for this subject.
  schemaType: <The schema format>               # (required) Accepted values are: AVRO, PROTOBUF, JSON
  schema:
    $ref: <url or path>  # 
  references: # Specifies the names of referenced schemas (optional array).
    - name: <>                                  # The name for the reference.
      subject: <>                               # The subject under which the referenced schema is registered.
      version: <>                               # The exact version of the schema under the registered subject.
]
```

The `metadata.name` property is mandatory and specifies the name of the _Subject_.

To use the SchemaRegistry default values for the `compatibilityLevel` you can omit the property.

### Example

Here is a simple example that shows how to define a single subject AVRO schema for type using
the `SchemaRegistrySubject` resource type.

_`file: subject-user.yaml`_

```yaml
---
apiVersion: "schemaregistry.jikkou.io/v1beta2"
kind: "SchemaRegistrySubject"
metadata:
  name: "User"
  labels: { }
  annotations:
    schemaregistry.jikkou.io/normalize-schema: true
spec:
  compatibilityLevel: "FULL_TRANSITIVE"
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
apiVersion: "schemaregistry.jikkou.io/v1beta2"
kind: "SchemaRegistrySubject"
metadata:
  name: "User"
  labels: { }
  annotations:
    schemaregistry.jikkou.io/normalize-schema: true
spec:
  compatibilityLevel: "FULL_TRANSITIVE"
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