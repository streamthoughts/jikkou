---
title: "Quickstart Guide"
linkTitle: "Quickstart Guide"
weight: 1
description: >
    This guide covers how you can quickly get started using Jikkou.
---


## Prerequisites
The following prerequisites are required for a successful and properly use of Jikkou.

* An Apache Kafka cluster.
* Installing and configuring Jikkou.
* Java

## Deploy a local Apache Kafka broker or have access to a cluster

You must have access to an Apache Kafka cluster for using Jikkou. 
Most of the time, the latest version of Jikkou is always built for working with the most recent version of Apache Kafka.

To quickly deploy a local Apache Kafka cluster : 

```bash
$ git clone https://github.com/streamthoughts/jikkou
$ cd jikkou
$ ./up              # use ./down for stopping the docker-compose stack
```

## Install Jikkou
Download the latest distribution version of the Jikkou client. You can look at the official [releases page](https://github.com/streamthoughts/jikkou/releases).

For more details, or for other options, see the [installation](./_installation.md) guide.

## Describe Broker Settings

* First, let's run the following command to describe the configuration of brokers :

```bash
$ jikkou --bootstrap-servers localhost:9092 brokers describe
```

* If you succeed to connect to your cluster, then the command should return an output similar to:

__(output)__

```yaml
---
apiVersion: "kafka.jikkou.io/v1beta2"
kind: "KafkaBrokerList"
metadata:
  labels: {}
  annotations:
    jikkou.io/kafka-cluster-id: "w1D7-dwKQIC5xb21CSSD3g"
    jikkou.io/resource-generated: "2022-01-01T00:00:00.463931011Z"
spec:
  brokers:
    - id: "1"
      host: "localhost"
      port: 9092
      configs:
        advertised.listeners: "PLAINTEXT://kafka-broker:29092,PLAINTEXT_HOST://localhost:9092"
        authorizer.class.name: "kafka.security.authorizer.AclAuthorizer"
        broker.id: "1"
        group.initial.rebalance.delay.ms: "0"
        listener.security.protocol.map: "PLAINTEXT:PLAINTEXT,PLAINTEXT_HOST:PLAINTEXT"
        listeners: "PLAINTEXT://0.0.0.0:29092,PLAINTEXT_HOST://0.0.0.0:9092"
        log.dirs: "/var/lib/kafka/data"
        offsets.topic.replication.factor: "1"
        zookeeper.connect: "zookeeper:2181"

```

## Create Topics

Now, let's create some topics on our Apache Kafka cluster. To do this, create a `topics.yaml` file with the following content:

```yaml
apiVersion: "kafka.jikkou.io/v1beta2"
kind: "KafkaTopicList"
metadata: {}
spec:
  topics:
  - name: 'jikkou-demo-my-first-topic'
    partitions: 1
    replication_factor: 1
    configs:
      cleanup.policy: 'compact'

  - name: 'jikkou-demo-my-second-topic'
    partitions: 5
    replication_factor: 1
    configs:
      cleanup.policy: 'delete'
```

Then, run the following Jikkou command to trigger the topic creation on the cluster: 

```bash
$ jikkou --bootstrap-servers localhost:9092 \
  topics \
  create \
  --files ./topics.yaml \
  --include "jikkou-demo-.*" \
  --yes
```

In the above command, we choose to use the `create` subcommand to create the new topics.
But we could just as well use the `apply` subcommand to obtain the same result.

In addition, we use the `--includes` option to limit the resources that will be selected by Jikkou to determine the changes to apply.

__(output)__

```text
TASK [CREATE] Create a new topic jikkou-demo-my-first-topic (partitions=1, replicas=1) - CHANGED ********
{
  "changed" : true,
  "end" : 1655125593974,
  "resource" : {
    "name" : "jikkou-demo-my-first-topic",
    "operation" : "ADD",
    "partitions" : {
      "after" : 1,
      "operation" : "ADD"
    },
    "replication_factor" : {
      "after" : 1,
      "operation" : "ADD"
    },
    "configs" : {
      "cleanup.policy" : {
        "after" : "compact",
        "operation" : "ADD"
      }
    }
  },
  "failed" : false,
  "status" : "CHANGED"
}
TASK [CREATE] Create a new topic jikkou-demo-my-second-topic (partitions=5, replicas=1) - CHANGED *******
{
  "changed" : true,
  "end" : 1655125593974,
  "resource" : {
    "name" : "jikkou-demo-my-second-topic",
    "operation" : "ADD",
    "partitions" : {
      "after" : 5,
      "operation" : "ADD"
    },
    "replication_factor" : {
      "after" : 1,
      "operation" : "ADD"
    },
    "configs" : {
      "cleanup.policy" : {
        "after" : "delete",
        "operation" : "ADD"
      }
    }
  },
  "failed" : false,
  "status" : "CHANGED"
}
EXECUTION in 2s 301ms 
ok : 0, created : 2, altered : 0, deleted : 0 failed : 0
```

## Describe Topics

After that, you can describe the topics previously created using the `describe` sub-command:

```bash
$ jikkou --bootstrap-servers localhost:9092 \
  topics \
  describe \
  --include "jikkou-demo-.*" \
  --default-configs
```

In the command above, we use the `-default-configs` to export built-in default configuration for configs that have a default value. 

## Remove Topics

Finally, let's empty the `topics.yaml` file to remove all the topics previously created :

```yaml
apiVersion: "kafka.jikkou.io/v1beta2"
kind: "KafkaTopicList"
metadata: {}
spec:
  topics: []

```

And run the following command using `dry-run`: 

```bash
$ jikkou --bootstrap-servers localhost:9092 \
    topics \
    apply \
    --files ./topics.yaml \
    --include "jikkou-demo-.*" \
    --delete-topic-orphans \
    --dry-run
```

The command should prompt that the previously created topics should be deleted. Indeed, Jikkou will detect that some topics that exist on our cluster and that match our predicate are no longer defined in our input files.

You now re-execute the above command without the dry-run to definitively delete the topics.

{{% alert title="Recommendation" color="warning" %}}
When working on a production environment, we highly recommend to always run the `apply` or `delete` command with the `--include` or `--exclude` options to make sure to not remove any topics by accident.
Furthermore, always run your command in `--dry-run` mode to check for changes that will be executed by Jikkou before proceeding.
{{% /alert %}}

## Reading the Help

To learn more about the available Jikkou commands, use `jikkou help` or type a command followed by the `-h` flag:

```bash
$ jikkou topics -h
```