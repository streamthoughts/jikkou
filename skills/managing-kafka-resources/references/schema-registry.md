# Schema Registry provider

## Kinds

| Kind | Get command | apiVersion |
|---|---|---|
| SchemaRegistrySubject | `jikkou get schemaregistry subjects` | schemaregistry.jikkou.io/v1 |

All get commands accept `-o JSON|YAML`, `--name <name>`, and selectors `-s '<expr>'`.

## Authoring examples

Avro subject (schema loaded from a sibling file via `$ref`):
```yaml
apiVersion: 'schemaregistry.jikkou.io/v1'
kind: 'SchemaRegistrySubject'
metadata:
  name: 'PersonAvro'
  annotations:
    schemaregistry.jikkou.io/normalize-schema: true
spec:
  compatibilityLevel: 'FULL_TRANSITIVE'
  schemaType: 'AVRO'
  schema:
    $ref: '{{ resource.directoryPath }}/avro-schema.avsc'
```

Inline JSON schema:
```yaml
apiVersion: 'schemaregistry.jikkou.io/v1'
kind: 'SchemaRegistrySubject'
metadata:
  name: 'order-value'
spec:
  compatibilityLevel: 'BACKWARD'
  schemaType: 'JSON'
  schema:
    $ref: 'order.schema.json'
```

`spec.schemaType` accepts `AVRO`, `PROTOBUF`, `JSON`. `spec.compatibilityLevel` accepts `BACKWARD`, `BACKWARD_TRANSITIVE`, `FORWARD`, `FORWARD_TRANSITIVE`, `FULL`, `FULL_TRANSITIVE`, `NONE`. `spec.mode` (rarely set) accepts `IMPORT`, `READONLY`, `READWRITE`, `FORWARD`.

## Notes

- Requires a reachable Schema Registry endpoint configured under `jikkou.provider.schemaregistry.config.url` in the Jikkou client configuration (property key `jikkou.provider.schemaregistry`); `vendor` (default `generic`), `authMethod` (`none`, `basicauth`, `ssl`), and proxy (`proxyUrl`, `proxyUsername`, `proxyPassword`, `nonProxyHosts`) settings live under the same `config` block. Without a working endpoint, `get`/`apply` commands fail to connect.
- Deleting: add `jikkou.io/delete: true` under `metadata.annotations`, then `jikkou apply`. The `schemaregistry.jikkou.io/permanante-delete` annotation forces a hard delete (must follow a prior soft delete).
- `schemaregistry.jikkou.io/normalize-schema` normalizes AVRO/JSON schemas server-side; `schemaregistry.jikkou.io/use-canonical-fingerprint` compares schemas by canonical fingerprint (AVRO only).
- `schemaregistry.jikkou.io/url`, `schemaregistry.jikkou.io/schema-version`, `schemaregistry.jikkou.io/schema-id` are read-only annotations that Jikkou attaches automatically when describing a subject.
- `jikkou api-resources schema --api-version=schemaregistry.jikkou.io/v1 --kind=SchemaRegistrySubject` prints the JSON Schema for the `spec` field.
