---
categories: [ ]
tags: [ "feature", "resources" ]
title: "Kafka Consumer Groups"
linkTitle: "Topics"
weight: 10
description: >
  Learn how to manage Kafka Consumer Groups.
---

{{% pageinfo color="info" %}}
This section describes the resource definition format for `KafkaConsumerGroup` entities, which can be used to define the
consumer groups you plan to manage on a specific Kafka cluster.
{{% /pageinfo %}}

## Listing `KafkaConsumerGroup`

You can retrieve the state of Kafka Consumer Groups using the `jikkou get kafkaconsumergroups` (or `jikkou get kcg`) command.

### Usage

```bash
$ jikkou get kafkaconsumergroups --help

Usage:

Get all 'KafkaConsumerGroup' resources.

jikkou get kafkaconsumergroups [-hV] [--list] [--offsets]
                               [--logger-level=<level>] [-o=<format>]
                               [--in-states=PARAM]... [-s=<expressions>]...

DESCRIPTION:

Use jikkou get kafkaconsumergroups when you want to describe the state of all
resources of type 'KafkaConsumerGroup'.

OPTIONS:

  -h, --help              Show this help message and exit.
      --in-states=PARAM   If states is set, only groups in these states will be
                            returned. Otherwise, all groups are returned. This
                            operation is supported by brokers with version
                            2.6.0 or later
      --list              Get resources as ResourceListObject.
      --logger-level=<level>
                          Specify the log level verbosity to be used while
                            running a command.
                          Valid level values are: TRACE, DEBUG, INFO, WARN,
                            ERROR.
                          For example, `--logger-level=INFO
  -o, --output=<format>   Prints the output in the specified format. Allowed
                            values: json, yaml (default yaml).
      --offsets           Specify whether consumer group offsets should be
                            described.
  -s, --selector=<expressions>
                          The selector expression used for including or
                            excluding resources.
  -V, --version           Print version information and exit
```

(The output from your current Jikkou version may be different from the above example.)

### Examples

(command)

```bash
$ jikkou get kafkaconsumergroups --in-states STABLE --offsets
```

(output)

```yaml
---
apiVersion: "kafka.jikkou.io/v1beta1"
kind: "KafkaConsumerGroup"
metadata:
  name: "my-group"
  labels:
    kafka.jikkou.io/is-simple-consumer: false
spec:
  state: "STABLE"
  members:
    - memberId: "console-consumer-b103994e-bcd5-4236-9d03-97065057e594"
      clientId: "console-consumer"
      host: "/127.0.0.1"
      assignments:
        - "my-topic-0"
      offsets:
        - topic: "my-topic"
          partition: 0
          offset: 0
  coordinator:
    id: "101"
    host: "localhost"
    port: 9092
```