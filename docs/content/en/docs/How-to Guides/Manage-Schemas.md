---
title: "Manage Schema Registry Subjects"
linkTitle: "Manage Schemas"
weight: 20
description: >
  Register and evolve Avro, Protobuf, and JSON schemas in your Schema Registry as code.
---

This guide shows how to manage Schema Registry subjects with Jikkou. For the full resource
specification, see the [Schema Registry Subjects reference]({{% relref "/docs/Reference/Providers/Schema Registry/Resources/subject.md" %}}).

## Before you begin

* A reachable Schema Registry (Confluent, Karapace, Apicurio, …).
* A Jikkou context configured with the `schemaregistry` provider — see the
  [Schema Registry provider configuration]({{% relref "/docs/Reference/Providers/Schema Registry/Configuration" %}}).

## 1. Describe a subject

Keep your schema in its own file and reference it from the resource with `$ref`.

_`file: subject-user.yaml`_

```yaml
---
apiVersion: "schemaregistry.jikkou.io/v1beta2"
kind: "SchemaRegistrySubject"
metadata:
  name: "User"
  annotations:
    schemaregistry.jikkou.io/normalize-schema: true
spec:
  compatibilityLevel: "FULL_TRANSITIVE"
  schemaType: "AVRO"
  schema:
    $ref: ./user-schema.avsc
```

_`file: user-schema.avsc`_

```json
{
  "namespace": "example.avro",
  "type": "record",
  "name": "User",
  "fields": [
    { "name": "name", "type": ["null", "string"], "default": null },
    { "name": "favorite_number", "type": ["null", "int"], "default": null }
  ]
}
```

## 2. Preview and apply

```bash
jikkou apply --files ./subject-user.yaml --dry-run   # review
jikkou apply --files ./subject-user.yaml             # apply
```

## 3. Evolve the schema safely

To evolve a schema, edit the `.avsc` file and re-apply. Jikkou registers a new version only when the
schema actually changes, and the registry rejects changes that violate the subject's
`compatibilityLevel`. Run with `--dry-run` first to confirm the diff is what you expect.

## Manage many subjects at once

Use a `SchemaRegistrySubjectList` (handy with [templating]({{% relref "/docs/Concepts/template.md" %}})) to manage
multiple subjects in a single file:

```yaml
apiVersion: "schemaregistry.jikkou.io/v1beta2"
kind: "SchemaRegistrySubjectList"
items: []   # an array of SchemaRegistrySubject
```

## Related

* [Schema Registry Subjects reference]({{% relref "/docs/Reference/Providers/Schema Registry/Resources/subject.md" %}})
* [Back up and restore configuration]({{% relref "Backup-and-Restore.md" %}})
