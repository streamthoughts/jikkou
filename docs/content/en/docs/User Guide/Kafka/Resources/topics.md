---
categories: [ ]
tags: [ "feature", "resources" ]
title: "Kafka Topics"
linkTitle: "Topics"
weight: 10
description: >
  Learn how to manage Kafka Topics.
---

{{% pageinfo color="info" %}}
KafkaTopic resources are used to define the topics you want to manage on your Kafka Cluster(s). A KafkaTopic resource
defines the number of partitions, the replication factor, and the configuration properties to be associated to a topics.
{{% /pageinfo %}}

## `KafkaTopic`

### Specification

Here is the _resource definition file_ for defining a `KafkaTopic`.

```yaml
apiVersion: "kafka.jikkou.io/v1beta2"  # The api version (required)
kind: "KafkaTopic"                     # The resource kind (required)
metadata:
  name: <The name of the topic>        # (required)
  labels: { }
  annotations: { }
spec:
  partitions: <Number of partitions>   # (optional)
  replicas: <Number of replicas>       # (optional)
  configs:
    <config_key>: <Config Value>       # The topic config properties keyed by name to override (optional)
  configMapRefs: [ ]                   # The list of ConfigMap to be applied to this topic (optional)
```

The `metadata.name` property is mandatory and specifies the name of the kafka topic.

To use the cluster default values for the number of `partitions` and `replicas` you can set the property
`spec.partitions` and `spec.replicas` to `-1`.

### Example

Here is a simple example that shows how to define a single YAML file containing two topic definition using
the `KafkaTopic` resource type.

_`file: kafka-topics.yaml`_

```yaml
---
apiVersion: "kafka.jikkou.io/v1beta2"
kind: KafkaTopic
metadata:
  name: 'my-topic-p1-r1'  # Name of the topic
  labels:
    environment: example
spec:
  partitions: 1           # Number of topic partitions (use -1 to use cluster default)
  replicas: 1             # Replication factor per partition (use -1 to use cluster default)
  configs:
    min.insync.replicas: 1
    cleanup.policy: 'delete'
---
apiVersion: "kafka.jikkou.io/v1beta2"
kind: KafkaTopic
metadata:
  name: 'my-topic-p2-r1'   # Name of the topic 
  labels:
    environment: example
spec:
  partitions: 2             # Number of topic partitions (use -1 to use cluster default)
  replicas: 1               # Replication factor per partition (use -1 to use cluster default)
  configs:
    min.insync.replicas: 1
    cleanup.policy: 'delete'
```

See official [Apache Kafka documentation](https://kafka.apache.org/documentation/#topicconfigs) for details about the topic-level configs.

{{% alert title="" color="tips" %}}
Tips: Multiple topics can be included in the same YAML file by using `---` lines.
{{% /alert %}}

## `KafkaTopicList`

If you need to define multiple topics (e.g. using a template), it may be easier to use a `KafkaTopicList` resource.

### Specification

Here the _resource definition file_ for defining a `KafkaTopicList`.

```yaml
apiVersion: "kafka.jikkou.io/v1beta2"  # The api version (required)
kind: "KafkaTopicList"                 # The resource kind (required)
metadata: # (optional)
  name: <The name of the topic>
  labels: { }
  annotations: { }
items: [ ]                             # An array of KafkaTopic
```

### Example

Here is a simple example that shows how to define a single YAML file containing two topic definition using
the `KafkaTopicList` resource type. In addition, the example uses a `ConfigMap` object to define the topic configuration
only once.

_`file: kafka-topics.yaml`_

```yaml
---
apiVersion: "kafka.jikkou.io/v1beta2"
kind: KafkaTopicList
metadata:
  labels:
    environment: example
items:
  - metadata:
      name: 'my-topic-p1-r1'
    spec:
      partitions: 1
      replicas: 1
      configMapRefs: [ "TopicConfig" ]

  - metadata:
      name: 'my-topic-p2-r1'
    spec:
      partitions: 2
      replicas: 1
      configMapRefs: [ "TopicConfig" ]
---
apiVersion: "core.jikkou.io/v1beta2"
kind: ConfigMap
metadata:
  name: 'TopicConfig'
data:
  min.insync.replicas: 1
  cleanup.policy: 'delete'
```