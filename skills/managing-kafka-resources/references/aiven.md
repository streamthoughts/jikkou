# Aiven provider

## Kinds

| Kind | Get command | apiVersion |
|---|---|---|
| KafkaTopic | `jikkou get aiven topics` | kafka.aiven.io/v1 |
| KafkaTopicAclEntry | `jikkou get aiven kafka-acls` | kafka.aiven.io/v1 |
| KafkaQuota | `jikkou get aiven kafka-quotas` | kafka.aiven.io/v1 |
| SchemaRegistrySubject | `jikkou get aiven subjects` | kafka.aiven.io/v1 |
| SchemaRegistryAclEntry | `jikkou get aiven schemaregistry-acls` | kafka.aiven.io/v1 |

All get commands accept `-o JSON|YAML` and selectors `-s '<expr>'`. `topics` and `subjects` additionally accept `--name <name>`; the ACL and quota get commands do not.

## Authoring examples

Kafka topic ACL entries (one file, `---`-separated):
```yaml
apiVersion: 'kafka.aiven.io/v1'
kind: 'KafkaTopicAclEntry'
metadata:
  labels: {}
spec:
  permission: 'ADMIN'
  username: 'avnadmin'
  topic: '*'
---
apiVersion: 'kafka.aiven.io/v1'
kind: 'KafkaTopicAclEntry'
metadata:
  labels: {}
spec:
  permission: 'READWRITE'
  username: 'alice'
  topic: '*alice*'
```

`spec.permission` accepts `ADMIN`, `READ`, `READWRITE`, `WRITE`. The same enum applies to `SchemaRegistryAclEntry.spec.permission`.

Kafka quota:
```yaml
apiVersion: 'kafka.aiven.io/v1'
kind: 'KafkaQuota'
spec:
  user: 'default'
  clientId: 'default'
  consumerByteRate: 1048576
  producerByteRate: 1048576
  requestPercentage: 25
```

## Notes

- Requires Aiven project/service and API token configured under `jikkou.provider.aiven.config`: `project`, `service`, `apiUrl` (default `https://api.aiven.io/v1/`), `tokenAuth` (Aiven Bearer Token from your Aiven profile page), plus optional proxy settings (`proxyUrl`, `proxyUsername`, `proxyPassword`, `nonProxyHosts`).
- ACL entries are identified by an auto-assigned ID; Jikkou records it in the `kafka.aiven.io/acl-entry-id` annotation once created (source: `MetadataAnnotations.AIVEN_IO_KAFKA_ACL_ID` in `jikkou-provider-aiven` — some older docs pages show a different key, trust this one).
- Deleting: add `jikkou.io/delete: true` under `metadata.annotations`, then `jikkou apply`.
- `jikkou api-resources schema --api-version=kafka.aiven.io/v1 --kind=<Kind>` prints the JSON Schema for a kind's `spec`.
