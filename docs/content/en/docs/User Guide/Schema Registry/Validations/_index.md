---
categories: [ ]
tags: [ "feature", "extensions" ]
title: "Validations"
linkTitle: "Validations"
weight: 50
description: >
  Learn how to use the built-in validations provided by the extensions for Schema Registry.
---

Jikkou ships with the following built-in _validations_:

## Subject

### `SchemaCompatibilityValidation`

```hocon
type = io.streamthoughts.jikkou.schema.registry.validation.SchemaCompatibilityValidation
```

The `SchemaCompatibilityValidation` allows testing the compatibility of the schema with the latest 
version already registered in the Schema Registry using the provided compatibility-level.

### `AvroSchemaValidation`

The `AvroSchemaValidation` allows checking if the specified Avro schema matches some specific avro schema definition
rules;

```hocon
type = io.streamthoughts.jikkou.schema.registry.validation.AvroSchemaValidation
```

**Configuration**

| Name                   | Type    | Description                                         | Default |
|------------------------|---------|-----------------------------------------------------|---------|
| `fieldsMustHaveDoc`    | Boolean | Verify that all record fields have a `doc` property | `false` |
| `fieldsMustBeNullable` | Boolean | Verify that all record fields are nullable          | `false` |
| `fieldsMustBeOptional` | Boolean | Verify that all record fields are optional          | `false` |




