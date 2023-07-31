---
title: "Annotations"
linkTitle: "Annotations"
weight: 60
description: >
  Learn how to use the metadata annotations provided by the extensions for Schema Registry.
---

{{% pageinfo %}}
Here, you will find information about the annotations provided by the Schema Registry extension for Jikkou.
{{% /pageinfo %}}

### List of built-in annotations

#### `schemaregistry.jikkou.io/url`

Used by jikkou.

The annotation is automatically added by Jikkou to describe the SchemaRegistry URL from which a subject schema is
retrieved.

#### `schemaregistry.jikkou.io/schema-version`

Used by jikkou.

The annotation is automatically added by Jikkou to describe the version of a subject schema.

#### `schemaregistry.jikkou.io/schema-id`

Used by jikkou.

The annotation is automatically added by Jikkou to describe the version of a subject id.

#### `schemaregistry.jikkou.io/normalize-schema`

Used on: `schemaregistry.jikkou.io/v1beta2:SchemaRegistrySubject`

This annotation can be used to normalize the schema on SchemaRegistry server side. Note, that Jikkou will attempt
to normalize AVRO and JSON schema.


See: [Confluent SchemaRegistry API Reference](https://docs.confluent.io/platform/current/schema-registry/develop/api.html#post--subjects-(string-%20subject)-versions)

#### `schemaregistry.jikkou.io/permanante-delete`

Used on: `schemaregistry.jikkou.io/v1beta2:SchemaRegistrySubject`

The annotation can be used to specify a hard delete of the subject, which removes all associated metadata including
the schema ID. The default is false. If the flag is not included, a soft delete is performed. You must perform a soft
delete first, then the hard delete.

See: [Confluent SchemaRegistry API Reference](https://docs.confluent.io/platform/current/schema-registry/develop/api.html#delete--subjects-(string-%20subject))