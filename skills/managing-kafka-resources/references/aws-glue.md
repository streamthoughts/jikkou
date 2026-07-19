# AWS Glue provider

## Kinds

| Kind | Get command | apiVersion |
|---|---|---|
| AwsGlueSchema | `jikkou get aws glue-schemas` | aws.jikkou.io/v1 |

All get commands accept `-o JSON|YAML`, `--name <name>`, and selectors `-s '<expr>'`.

## Authoring examples

Avro schema on the Glue Schema Registry:
```yaml
apiVersion: 'aws.jikkou.io/v1'
kind: 'AwsGlueSchema'
metadata:
  name: 'PersonAvro'
  labels:
    glue.aws.amazon.com/registry-name: 'Test'
  annotations:
    glue.aws.amazon.com/use-canonical-fingerprint: true
spec:
  compatibility: 'BACKWARD'
  dataFormat: 'AVRO'
  schemaDefinition: |
    {
      "namespace": "example",
      "type": "record",
      "name": "Person",
      "fields": [
        { "name": "id", "type": "int", "doc": "The person's unique ID (required)" },
        { "name": "firstname", "type": "string", "doc": "The person's legal firstname (required)" },
        { "name": "lastname", "type": "string", "doc": "The person's legal lastname (required)" },
        { "name": "age", "type": ["null", "int"], "default": null, "doc": "The person's age (optional)" }
      ]
    }
```

`spec.dataFormat` accepts `AVRO`, `PROTOBUF`, `JSON`. `spec.compatibility` accepts `NONE`, `DISABLED`, `BACKWARD`, `BACKWARD_ALL`, `FORWARD`, `FORWARD_ALL`, `FULL`, `FULL_ALL` (applies to all versions of the Glue schema).

## Notes

- Requires AWS credentials and region configured under `jikkou.provider.aws.config`: `aws.client.region`, `aws.client.accessKeyId`, `aws.client.secretAccessKey`, optional `aws.client.sessionToken`, and optional `aws.client.endpointOverride` (for S3-compatible/Glue-compatible endpoints). `aws.glue.registryNames` restricts lookups to a given set of registry names.
- The Glue registry a schema belongs to is set via the `glue.aws.amazon.com/registry-name` label, not a spec field.
- `glue.aws.amazon.com/created-time`, `updated-time`, `registry-arn`, `schema-arn`, `schema-version-id` are read-only annotations Jikkou attaches automatically. `glue.aws.amazon.com/use-canonical-fingerprint` compares schemas by canonical fingerprint (AVRO only).
- Deleting: add `jikkou.io/delete: true` under `metadata.annotations`, then `jikkou apply`.
- `jikkou api-resources schema --api-version=aws.jikkou.io/v1 --kind=AwsGlueSchema` prints the JSON Schema for the `spec` field.
