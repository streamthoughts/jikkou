---
categories: []
tags: ["feature", "resources"] 
title: "Topics"
linkTitle: "Topics"
weight: 10
description: >
  Learn how to use validation rules for ensuring resource entity configurations meets your requirements before being created and/or updated.
---

## The Resource Specification File

The _resource specification file_ for defining `topics` contains the following fields:

```yaml
apiVersion: "kafka.jikkou.io/v1beta2" # The api version (required)
kind: "KafkaTopicList"  # The resource kind (required)
metadata: # (optional)
  labels: {}
  annotations: {}
spec:
    topics:  # A list of the topics to manage (optional)
    - name: The name of the topic
      partitions: The number of partitions
      replication_factor: The number of replications
      configs: 
        example: # A list of the topic config properties keyed by name to override (optional).
      config_map_refs: A list of configs to apply to this topic (optional).
```

## Usage

```bash
$ jikkou topics -h   
```

```bash
Apply the Topic changes described by your specs-file against the Kafka cluster you are currently pointing at.

jikkou topics [-hV] [COMMAND]

Description:

This command can be used to create, alter, delete or describe Topics on a remote Kafka cluster

Options:

  -h, --help      Show this help message and exit.
  -V, --version   Print version information and exit.

Commands:

  alter     Alter the topic configurations as describe in the specification file.
  apply     Apply all changes to the Kafka topics.
  create    Create the topics missing on the cluster as describe in the specification file.
  delete    Delete all topics not described in the specification file.
  describe  Describe all the topics that currently exist on the remote Kafka cluster.
  help      Displays help information about the specified command
```

## Example

**Resource Specification File**

Create a file named `kafka-topics.yaml` with the following content:

```yaml
apiVersion: 1
spec:
    topics:
    - name: "{{ values.topic_prefix | default('') }}my-topic"
      partitions: 6
      replication_factor: 3
      configs:
        min.insync.replicas: 2
```

You can notice that in the above file we have used variable substitution, written in double braces, to dynamically define a prefix for our topic.

**Command**

Run the following command to create and/or update the topics declared in the resource file.

```bash
$ jikkou --bootstrap-servers localhost:9092 topics apply \
--files kafka-topics.yaml \
--set-value topic_prefix=dev- \
--verbose
```

In command above, the `--set-value` is used to pass the value of the `topic_prefix` variable. 

In addition, we use the `--verbose` flag to display all the details about the changes that have been applied to the target cluster.

**Output**

```
TASK [CREATE] Create a new topic dev-my-topic (partitions=6, replicas=3) - CHANGED **********************
{
  "changed" : true,
  "end" : 1634071489773,
  "resource" : {
    "name" : "dev-my-topic",
    "operation" : "ADD",
    "partitions" : {
      "after" : 6,
      "operation" : "ADD"
    },
    "replication_factor" : {
      "after" : 3,
      "operation" : "ADD"
    },
    "configs" : {
      "min.insync.replicas" : {
        "after" : "2",
        "operation" : "ADD"
      }
    }
  },
  "failed" : false,
  "status" : "CHANGED"
}
EXECUTION in 2s 661ms
ok : 0, created : 1, altered : 0, deleted : 0 failed : 0
```

