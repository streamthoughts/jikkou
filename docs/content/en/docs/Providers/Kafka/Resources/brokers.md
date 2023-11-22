---
categories: [ ]
tags: [ "feature", "resources" ]
title: "Kafka Brokers"
linkTitle: "Brokers"
weight: 10
description: >
  Learn how to manage Kafka Brokers.
---

{{% pageinfo color="info" %}}
This section describes the resource definition format for `kafkabrokers` entities, which can be used to define the
brokers you plan to manage on a specific Kafka cluster.
{{% /pageinfo %}}

## Listing `KafkaBroker`

You can retrieve the state of Kafka Consumer Groups using the `jikkou get kafkabrokers` (or `jikkou get kb`) command.

### Usage

```bash
Usage:

Get all 'KafkaBroker' resources.

jikkou get kafkabrokers [-hV] [--default-configs] [--dynamic-broker-configs]
                        [--list] [--static-broker-configs]
                        [--logger-level=<level>] [-o=<format>]
                        [-s=<expressions>]...

DESCRIPTION:

Use jikkou get kafkabrokers when you want to describe the state of all
resources of type 'KafkaBroker'.

OPTIONS:

      --default-configs   Describe built-in default configuration for configs
                            that have a default value.
      --dynamic-broker-configs
                          Describe dynamic configs that are configured as
                            default for all brokers or for specific broker in
                            the cluster.
  -h, --help              Show this help message and exit.
      --list              Get resources as ResourceListObject.
      --logger-level=<level>
                          Specify the log level verbosity to be used while
                            running a command.
                          Valid level values are: TRACE, DEBUG, INFO, WARN,
                            ERROR.
                          For example, `--logger-level=INFO`
  -o, --output=<format>   Prints the output in the specified format. Allowed
                            values: json, yaml (default yaml).
  -s, --selector=<expressions>
                          The selector expression used for including or
                            excluding resources.
      --static-broker-configs
                          Describe static configs provided as broker properties
                            at start up (e.g. server.properties file).
  -V, --version           Print version information and exit.

```

(The output from your current Jikkou version may be different from the above example.)

### Examples

(command)

```bash
$ jikkou get kafkabrokers --static-broker-configs
```

(output)

```yaml
---
apiVersion: "kafka.jikkou.io/v1beta2"
kind: "KafkaBroker"
metadata:
  name: "101"
  labels: {}
  annotations:
    kafka.jikkou.io/cluster-id: "xtzWWN4bTjitpL3kfd9s5g"
spec:
  id: "101"
  host: "localhost"
  port: 9092
  configs:
    advertised.listeners: "PLAINTEXT://kafka:29092,PLAINTEXT_HOST://localhost:9092"
    authorizer.class.name: "org.apache.kafka.metadata.authorizer.StandardAuthorizer"
    broker.id: "101"
    controller.listener.names: "CONTROLLER"
    controller.quorum.voters: "101@kafka:29093"
    inter.broker.listener.name: "PLAINTEXT"
    listener.security.protocol.map: "CONTROLLER:PLAINTEXT,PLAINTEXT:PLAINTEXT,PLAINTEXT_HOST:PLAINTEXT"
    listeners: "PLAINTEXT://kafka:29092,PLAINTEXT_HOST://0.0.0.0:9092,CONTROLLER://kafka:29093"
    log.dirs: "/var/lib/kafka/data"
    node.id: "101"
    offsets.topic.replication.factor: "1"
    process.roles: "broker,controller"
    transaction.state.log.replication.factor: "1"
    zookeeper.connect: ""
```