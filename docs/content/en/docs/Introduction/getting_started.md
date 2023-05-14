---
title: "Getting Started"
weight: 1
description: >
    This guide covers how you can quickly get started using Jikkou.
---

This document will guide you through setting up Jikkou in a few minutes and managing your first resources
with Jikkou.

## Prerequisites
The following prerequisites are required for a successful and properly use of Jikkou.

Make sure the following is installed:

* An Apache Kafka cluster.
  * Using Docker, [Docker Compose](https://docs.docker.com/compose/) is the easiest way to use it.
* Java 17

## Start your local Apache Kafka Cluster 

You must have access to an Apache Kafka cluster for using Jikkou.
Most of the time, the latest version of Jikkou is always built for working with the most recent version of Apache Kafka.

Make sure the Docker is up and running.

Then, run the following commands:

```bash
$ git clone https://github.com/streamthoughts/jikkou
$ cd jikkou
$ ./up              # use ./down for stopping the docker-compose stack
```

## Run Jikkou

### Download the latest distribution

Run the following commands to install the latest version:

```bash
wget https://github.com/streamthoughts/jikkou/releases/download/0.17.0/jikkou.deb && \
sudo dpkg -i jikkou.deb && \
source <(jikkou generate-completion) && \
jikkou --version
```

For more details, or for other options, see the [installation](./installation.md) guide.

## Configure Jikkou for your local Apache Kafka cluster

Set configuration context for localhost

```bash
jikkou config set-context localhost --config=kafka.client.bootstrap.servers=localhost:9092
```
Show the complete configuration.

```bash
jikkou config view --name localhost
```

Finally, let's check if your cluster is accessible: 

```bash
jikkou health get kafkabroker
```

_(output_)

If OK, you should get an output similar to : 

```yaml
---
name: "kafka"
status: "UP"
details:
  resource: "urn:kafka:cluster:id:KRzY-7iRTHy4d1UVyNlcuw"
  brokers:
    - id: "1"
      host: "localhost"
      port: 9092
```

## Create your first topics

First, create a resource YAML file describing the topics you want to create on your cluster:

_file: kafka-topics.yaml_
```yaml
apiVersion: "kafka.jikkou.io/v1beta2"
kind: "KafkaTopicList"
items:
  - metadata:
      name: 'my-first-topic'
    spec:
      partitions: 5
      replicationFactor: 1
      configs:
        cleanup.policy: 'compact'

  - metadata:
      name: 'my-second-topic'
    spec:
      partitions: 4
      replicationFactor: 1
      configs:
        cleanup.policy: 'delete'
```

Then, run the following Jikkou command to trigger the topic creation on the cluster:

```bash
jikkou create -f ./kafka-topics.yaml
```

(output)
```yaml
TASK [ADD] Add topic 'my-first-topic' (partitions=5, replicas=-1, configs=[cleanup.policy=compact]) - CHANGED 
{
  "changed" : true,
  "end" : 1683986528117,
  "resource" : {
    "name" : "my-first-topic",
    "partitions" : {
      "after" : 5
    },
    "replicas" : {
      "after" : -1
    },
    "configs" : {
      "cleanup.policy" : {
        "after" : "compact",
        "operation" : "ADD"
      }
    },
    "operation" : "ADD"
  },
  "failed" : false,
  "status" : "CHANGED"
}
TASK [ADD] Add topic 'my-second-topic' (partitions=4, replicas=-1, configs=[cleanup.policy=delete]) - CHANGED 
{
  "changed" : true,
  "end" : 1683986528117,
  "resource" : {
    "name" : "my-second-topic",
    "partitions" : {
      "after" : 4
    },
    "replicas" : {
      "after" : -1
    },
    "configs" : {
      "cleanup.policy" : {
        "after" : "delete",
        "operation" : "ADD"
      }
    },
    "operation" : "ADD"
  },
  "failed" : false,
  "status" : "CHANGED"
}
EXECUTION in 772ms 
ok : 0, created : 2, altered : 0, deleted : 0 failed : 0
```

{{% alert title="Tips" color="info" %}}
In the above command, we chose to use the `create` command to create the new topics.
But we could just as easily use the `update` or `apply` command to get the same result depending on our needs.
{{% /alert %}}

Finally, you can verify that topics are created on the cluster

```yaml
jikkou get kafkatopics --describe-default-configs
```

{{% alert title="Tips" color="info" %}}
We use the `--describe-default-configs` to export built-in default configuration for configs that have a default value.
{{% /alert %}}

## Update Kafka Topics

Edit your `kafka-topics.yaml` to add a `retention.ms: 86400000` property to the defined topics.

Then, run the following command.

```bash
jikkou update -f ./kafka-topics.yaml
```

## Delete Kafka Topics

To delete all topics defines in the `topics.yaml`, add an annotation `jikkou.io/delete: true` as follows:

```yaml
apiVersion: "kafka.jikkou.io/v1beta2"
kind: "KafkaTopicList"
metadata:
  annotations:
    # Annotation to specify that all resources must be deleted.
    jikkou.io/delete: true
items:
  - metadata:
      name: 'my-first-topic'
    spec:
      partitions: 5
      replicationFactor: 1
      configs:
        cleanup.policy: 'compact'

  - metadata:
      name: 'my-second-topic'
    spec:
      partitions: 4
      replicationFactor: 1
      configs:
        cleanup.policy: 'delete'
```

Then, run the following command: 

```bash
$ jikkou apply \
    --files ./kafka-topics.yaml \
    --selector "metadata.name MATCHES (my-.*-topic)" \
    --dry-run
```

Using the `dry-run` option, give you the possibility to check the changes that will be made before applying them.

Now, rerun the above command without the `--dry-run` option to definitively delete the topics.

{{% alert title="Recommendation" color="warning" %}}
When working in a production environment, we strongly recommend running commands with a `--selector` option to ensure 
that changes are only applied to a specific set of resources. Also, always run your command in `--dry-run` mode to verify the changes
that will be executed by Jikkou before continuing.
{{% /alert %}}

## Reading the Help

To learn more about the available Jikkou commands, use `jikkou help` or type a command followed by the `-h` flag:

```bash
$ jikkou help get
```

## Next Steps

Now, you're ready to use Jikkou!🚀

As next steps, we suggest reading the following documentation in this order:
* Learn Jikkou [concepts]({{% relref "../Concepts" %}})
* Read the Developer Guide to understand how to use the Jikkou API for Java
* Look at the [examples](https://github.com/streamthoughts/jikkou/tree/main/examples)