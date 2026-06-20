---
title: "Manage Kafka Connect Connectors"
linkTitle: "Manage Connectors"
weight: 30
description: >
  Deploy, configure, and control the lifecycle of Kafka Connect connectors as code.
---

This guide shows how to manage Kafka Connect connectors with Jikkou. For the full resource
specification, see the [KafkaConnector reference]({{% relref "/docs/Reference/Providers/Kafka Connect/Resources/connector.md" %}}).

## Before you begin

* One or more reachable Kafka Connect clusters.
* Each cluster declared under the `kafkaConnect.clusters[]` setting in your Jikkou configuration —
  see the [Kafka Connect provider configuration]({{% relref "/docs/Reference/Providers/Kafka Connect/Configuration" %}}).

## 1. Describe a connector

The `kafka.jikkou.io/connect-cluster` label selects which configured cluster the connector is created in.

_`file: kafka-connector-filestream-sink.yaml`_

```yaml
---
apiVersion: "kafka.jikkou.io/v1beta1"
kind: "KafkaConnector"
metadata:
  name: "local-file-sink"
  labels:
    kafka.jikkou.io/connect-cluster: "my-connect-cluster"
spec:
  connectorClass: "FileStreamSink"
  tasksMax: 1
  config:
    file: "/tmp/test.sink.txt"
    topics: "connect-test"
  state: "running"
```

## 2. Preview and apply

```bash
jikkou apply --files ./kafka-connector-filestream-sink.yaml --dry-run   # review
jikkou apply --files ./kafka-connector-filestream-sink.yaml             # apply
```

## 3. Control the connector lifecycle

Set `spec.state` and re-apply to change the runtime state of a connector:

* `running` — connector and tasks are active (default).
* `paused` — message processing is paused until resumed.
* `stopped` — connector and tasks are shut down; the config is kept.

## 4. Inspect connector status

```bash
jikkou get kafkaconnectors --expand-status
```

The `status.connectorStatus` block reports the live state of the connector and its tasks as returned
by the Kafka Connect REST API.

## Related

* [KafkaConnector reference]({{% relref "/docs/Reference/Providers/Kafka Connect/Resources/connector.md" %}})
* [Restart connectors action]({{% relref "/docs/Reference/Providers/Kafka Connect/Actions" %}})
