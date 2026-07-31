# Kafka provider

## Kinds

| Kind | Get command | apiVersion |
|---|---|---|
| KafkaTopic | `jikkou get kafka topics` | kafka.jikkou.io/v1 |
| KafkaPrincipalAuthorization (ACLs) | `jikkou get kafka acls` | kafka.jikkou.io/v1 |
| KafkaClientQuota | `jikkou get kafka client-quotas` | kafka.jikkou.io/v1 |
| KafkaConsumerGroup | `jikkou get kafka consumer-groups` | kafka.jikkou.io/v1 |
| KafkaUser | `jikkou get kafka users` | kafka.jikkou.io/v1 |
| KafkaBroker | `jikkou get kafka brokers` | kafka.jikkou.io/v1 |
| KafkaTableRecord | `jikkou get kafka table-records` | kafka.jikkou.io/v1 |

All get commands accept `-o JSON|YAML` and selectors `-s '<expr>'`; only `topics`, `client-quotas`, and `users` also accept `--name <name>` (filter the others client-side or with `-s`). `table-records` additionally requires `--topic-name`, `--key-type`, and `--value-type`, and only works against compacted topics.

## Authoring examples

Topic:
```yaml
apiVersion: 'kafka.jikkou.io/v1'
kind: 'KafkaTopic'
metadata:
  name: 'my-topic'
  labels: { team: 'payments' }
spec:
  partitions: 6
  replicas: 3
  configs:
    min.insync.replicas: 2
    cleanup.policy: 'compact'
    retention.ms: 604800000
```

User + ACL (one file, `---`-separated):
```yaml
apiVersion: 'kafka.jikkou.io/v1'
kind: 'KafkaUser'
metadata:
  name: 'payment-service'
spec:
  authentications:
    - type: 'scram-sha-512'
      iterations: 4096
---
apiVersion: 'kafka.jikkou.io/v1'
kind: 'KafkaPrincipalAuthorization'
metadata:
  name: 'User:payment-service'
spec:
  acls:
    - resource:
        type: 'topic'
        pattern: 'my-topic'
        patternType: 'literal'
      operations: ['READ', 'DESCRIBE']
      host: '*'
```

## Notes

- Deleting: add `jikkou.io/delete: true` under `metadata.annotations`, then `jikkou apply`.
- `min.insync.replicas` must be <= `replicas` or producers with acks=all fail.
- Topic config values are plain Kafka broker/topic configs — anything `kafka-configs.sh` accepts.
- All `kafka.jikkou.io` kinds share a single apiVersion (`v1`) in this CLI build; run `jikkou api-resources list` to confirm current versions before authoring, since this can change between releases.
- `jikkou api-resources schema --api-version=<v> --kind=<k>` prints the JSON Schema for a kind's `spec`, useful for checking field names before writing a resource by hand.
