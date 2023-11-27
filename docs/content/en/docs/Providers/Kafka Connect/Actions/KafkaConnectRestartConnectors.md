---
tags: [ "action", "apache kafka", "kafka connect" ]
title: "KafkaConnectRestartConnectors"
linkTitle: "KafkaConnectRestartConnectors"
weight: 20
description: >
  Learn how to use the KafkaConnectRestartConnector action. 
---

{{% pageinfo color="info" %}}
The `KafkaConnectRestartConnectors` action allows  a user to restart all or just the
failed Connector and Task instances for one or multiple named connectors.
{{% /pageinfo %}}

## Usage (CLI)

```bash
Usage:

Execute the action.

jikkou action KafkaConnectRestartConnectors execute [-hV] [--include-tasks]
[--only-failed] [--connect-cluster=PARAM] [--logger-level=<level>]
[-o=<format>] [--connector-name=PARAM]...

DESCRIPTION:

The KafkaConnectRestartConnectors action a user to restart all or just the
failed Connector and Task instances for one or multiple named connectors.

OPTIONS:

      --connect-cluster=PARAM
                          The name of the connect cluster.
      --connector-name=PARAM
                          The connector's name.
  -h, --help              Show this help message and exit.
      --include-tasks     Specifies whether to restart the connector instance
                            and task instances (includeTasks=true) or just the
                            connector instance (includeTasks=false)
      --logger-level=<level>
                          Specify the log level verbosity to be used while
                            running a command.
                          Valid level values are: TRACE, DEBUG, INFO, WARN,
                            ERROR.
                          For example, `--logger-level=INFO`
  -o, --output=<format>   Prints the output in the specified format. Allowed
                            values: JSON, YAML (default YAML).
      --only-failed       Specifies whether to restart just the instances with
                            a FAILED status (onlyFailed=true) or all instances
                            (onlyFailed=false)
  -V, --version           Print version information and exit.
```

### Examples

### Restart all connectors for all Kafka Connect clusters.

```bash
jikkou action kafkaconnectrestartconnectors execute
```

**(output)**

```text
---
kind: "ApiActionResultSet"
apiVersion: "core.jikkou.io/v1"
metadata:
  labels: {}
  annotations: {}
results:
- status: "SUCCEEDED"
  data:
    apiVersion: "kafka.jikkou.io/v1beta1"
    kind: "KafkaConnector"
    metadata:
      name: "local-file-sink"
      labels:
        kafka.jikkou.io/connect-cluster: "my-connect-cluster"
      annotations: {}
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
          workerId: "connect:8083"
        tasks:
        - id: 0
          state: "RUNNING"
          workerId: "connect:8083"
```

### Restart all connectors with a FAILED status on all Kafka Connect clusters.

```bash
jikkou action kafkaconnectrestartconnectors execute \
--only-failed
```

### Restart specific connector and tasks for on Kafka Connect cluster

```bash
jikkou action kafkaconnectrestartconnectors execute \
--cluster-name my-connect-cluster
--connector-name local-file-sink \
--include-tasks
```


