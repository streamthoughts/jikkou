---
categories: [ ]
tags: [ "feature", "resources" ]
title: "KafkaConnectors"
linkTitle: "KafkaConnector"
weight: 10
description: >
  Learn how to manage Kafka Connectors.
---

{{% pageinfo color="info" %}}
This section describes the resource definition format for `KafkaConnector` entities, which can be used to define the 
configuration and status of connectors you plan to create and manage on specific Kafka Connect clusters.
{{% /pageinfo %}}

## Definition Format of `KafkaConnector`

Below is the overall structure of the `KafkaConnector` resource.

```yaml
---
apiVersion: "kafka.jikkou.io/v1beta1"  # The api version (required)
kind: "KafkaConnector"                 # The resource kind (required)
metadata:
  name: <string>                       # The name of the connector (required)
  labels:
    # Name of the Kafka Connect cluster to create the connector instance in (required).
    kafka.jikkou.io/connect-cluster: <string>
  annotations: { }
spec:
  connectorClass: <string>            # Name or alias of the class for this connector.
  tasksMax: <integer>                 # The maximum number of tasks for the Kafka Connector.
  config:                             # Configuration properties of the connector.
    <key>: <value>
  state: <string>                     # The state the connector should be in. Defaults to running.
```

See below for details about all these fields.

### Metadata

#### `metadata.name` [required]

The name of the connector.

#### `labels.kafka.jikkou.io/connect-cluster`  [required]

the name of the Kafka Connect cluster to create the connector instance in.
The cluster name must be configured through the `kafkaConnect.clusters[]` Jikkou's configuration setting (see: [Configuration]({{% relref "../Configuration" %}})).

### Specification

#### `spec.connectorClass` [required]

The name or alias of the class for this connector.

#### `spec.tasksMax` [optional]

The maximum number of tasks for the Kafka Connector. Default is `1`.

#### `spec.config` [required]

The connector's configuration properties.

#### `spec.state` [optional]

The state the connector should be in. Defaults to `running`.

Below are the valid values:

* `running`: Transition the connector and its tasks to RUNNING state. 
* `paused`: Pause the connector and its tasks, which stops message processing until the connector is resumed.
* `stopped`: Completely shut down the connector and its tasks. The connector config remains present in the config topic of the cluster (if running in distributed mode), unmodified.

### Examples

The following is an example of a resource describing a Kafka connector:

```yaml
---
# Example: file: kafka-connector-filestream-sink.yaml
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
  state: "RUNNING"
```

## Listing `KafkaConnector`

You can retrieve the state of Kafka Connector instances running on your Kafka Connect clusters using the `jikkou get kafkaconnectors` (or `jikkou get kc`) command.

### Usage

```bash
$jikkou get kc --help

Usage:

Get all 'KafkaConnector' resources.

jikkou get kafkaconnectors [-hV] [--expand-status] [-o=<format>]
                           [-s=<expressions>]...

Description:

Use jikkou get kafkaconnectors when you want to describe the state of all
resources of type 'KafkaConnector'.

Options:

      --expand-status     Retrieves additional information about the status of
                            the connector and its tasks.
  -h, --help              Show this help message and exit.
  -o, --output=<format>   Prints the output in the specified format. Allowed
                            values: json, yaml (default yaml).
  -s, --selector=<expressions>
                          The selector expression use for including or
                            excluding resources.
  -V, --version           Print version information and exit.
```

(The output from your current Jikkou version may be different from the above example.)

### Examples

(command)

```bash
$ jikkou get kc --expand-status 
```

(output)

```yaml
apiVersion: "kafka.jikkou.io/v1beta1"
kind: "KafkaConnector"
metadata:
  name: "local-file-sink"
  labels:
    kafka.jikkou.io/connect-cluster: "localhost"
spec:
  connectorClass: "FileStreamSink"
  tasksMax: 1
  config:
    file: "/tmp/test.sink.txt"
    topics: "connect-test"
  state: "RUNNING"
status:
  connectorStatus:
    name: "local-file-sink"
    connector:
      state: "RUNNING"
      worker_id: "localhost:8083"
    tasks:
      id: 1
      state: "RUNNING"
      worker_id: "localhost:8083"
```

The `status.connectorStatus` provides the connector status, as reported by the Kafka Connect REST API.

