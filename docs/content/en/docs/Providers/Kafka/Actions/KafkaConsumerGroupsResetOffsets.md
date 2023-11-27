---
tags: [ "action", "apache kafka", "kafka" ]
title: "KafkaConsumerGroupsResetOffsets"
linkTitle: "KafkaConsumerGroupsResetOffsets"
weight: 20
description: >
  Learn how to use the KafkaConsumerGroupsResetOffsets action. 
---

{{% pageinfo color="info" %}}
The `KafkaConsumerGroupsResetOffsets` action allows resetting offsets of consumer group. 
It supports one consumer group at the time, and group should be in EMPTY state.
{{% /pageinfo %}}

## Usage (CLI)

```bash
Usage:

Execute the action.

jikkou action KafkaConsumerGroupsResetOffsets execute [-hV] [--dry-run]
[--to-earliest] [--to-latest] --group=PARAM [--logger-level=<level>]
[-o=<format>] [--to-datetime=PARAM] [--to-offset=PARAM] --topic=PARAM
[--topic=PARAM]...

DESCRIPTION:

Reset offsets of consumer group. Supports one consumer group at the time, and
group should be in EMPTY state.
You must choose one of the following reset specifications: to-datetime,
by-duration, to-earliest, to-latest, to-offset.


OPTIONS:

      --dry-run             Only show results without executing changes on
                              Consumer Groups.
      --group=PARAM         The consumer group to act on.
  -h, --help                Show this help message and exit.
      --logger-level=<level>
                            Specify the log level verbosity to be used while
                              running a command.
                            Valid level values are: TRACE, DEBUG, INFO, WARN,
                              ERROR.
                            For example, `--logger-level=INFO`
  -o, --output=<format>     Prints the output in the specified format. Allowed
                              values: JSON, YAML (default YAML).
      --to-datetime=PARAM   Reset offsets to offset from datetime. Format:
                              'YYYY-MM-DDTHH:mm:SS.sss'
      --to-earliest         Reset offsets to earliest offset.
      --to-latest           Reset offsets to latest offset.
      --to-offset=PARAM     Reset offsets to a specific offset.
      --topic=PARAM         The topic whose partitions must be included in the
                              reset-offset action.
  -V, --version             Print version information and exit.
```

### Examples

### Reset Consumer Group to the earliest offsets

```bash
jikkou action kafkaconsumergroupresetoffsets execute \
--group my-group \
--topic test \
--to-earliest
```

**(output)**

```text
---
kind: "ApiActionResultSet"
apiVersion: "core.jikkou.io/v1"
metadata:
  labels: {}
  annotations:
    configs.jikkou.io/to-earliest: "true"
    configs.jikkou.io/group: "my-group"
    configs.jikkou.io/dry-run: "false"
    configs.jikkou.io/topic: 
        - "test"
results:
- status: "SUCCEEDED"
  errors: []
  data:
    apiVersion: "kafka.jikkou.io/v1beta1"
    kind: "KafkaConsumerGroup"
    metadata:
      name: "my-group"
      labels:
        kafka.jikkou.io/is-simple-consumer: false
      annotations: {}
    status:
      state: "EMPTY"
      members: []
      offsets:
      - topic: "test"
        partition: 1
        offset: 0
      - topic: "test"
        partition: 0
        offset: 0
      - topic: "test"
        partition: 2
        offset: 0
      - topic: "--test"
        partition: 0
        offset: 0
      coordinator:
        id: "101"
        host: "localhost"
        port: 9092
```


