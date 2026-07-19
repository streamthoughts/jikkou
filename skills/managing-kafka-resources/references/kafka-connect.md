# Kafka Connect provider

## Kinds

| Kind | Get command | apiVersion |
|---|---|---|
| KafkaConnector | `jikkou get kafkaconnect connectors` | kafka.jikkou.io/v1 |

All get commands accept `-o JSON|YAML`, `--name <name>`, and selectors `-s '<expr>'`.

## Authoring examples

File sink connector:
```yaml
apiVersion: 'kafka.jikkou.io/v1'
kind: 'KafkaConnector'
metadata:
  name: 'local-file-sink'
  labels:
    # The name of the Kafka Connect cluster to connect to (must match a cluster
    # configured under jikkou.provider.kafkaconnect.config.clusters)
    kafka.jikkou.io/connect-cluster: 'my-connect-cluster'
spec:
  connectorClass: 'FileStreamSink'
  tasksMax: 1
  config:
    file: '/tmp/test.sink.txt'
    topics: 'connect-test'
  state: 'RUNNING'
```

`spec.state` accepts `RUNNING`, `PAUSED`, `STOPPED` (also reported: `UNASSIGNED`, `RESTARTING`, `FAILED`).

## Notes

- Requires at least one Kafka Connect cluster configured under `jikkou.provider.kafkaconnect.config.clusters` (an array — each entry has `name`, `url`, `authMethod` [`none`, `basicauth`, `ssl`], and optional proxy settings `proxyUrl`, `proxyUsername`, `proxyPassword`, `nonProxyHosts`). The `kafka.jikkou.io/connect-cluster` label on the resource selects which configured cluster a connector targets.
- Deleting: add `jikkou.io/delete: true` under `metadata.annotations`, then `jikkou apply`.
- The `status.connectorStatus` field is read-only, reported by the Kafka Connect REST API — don't set it when authoring.
- `jikkou api-resources schema --api-version=kafka.jikkou.io/v1 --kind=KafkaConnector` prints the JSON Schema for the `spec` field.
