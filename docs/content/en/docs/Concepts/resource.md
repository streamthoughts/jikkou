---
title: "Resource"
linkTitle: "Resource"
weight: 1
---

{{% pageinfo color="info" %}}
_Jikkou Resources_ are entities that represent the state of a concrete instance of a concept that are part of the state
of your system, like a Topic on an Apache Kafka cluster.
{{% /pageinfo %}}

## Resource Objects

All resources can be distinguished between _persistent objects_, which are used to describe the desired state of your
system, and _transient objects_, which are only used to enrich or provide additional capabilities for the definition of
persistent objects.

A resource is an object with a type (called a _Kind_) and a concrete model that describe the associated data.
All resource are scoped by an API _Group_ and _Version_.

## Resource Definition

Resources are described in YAML format.

Here is a sample resource that described a Kafka Topic.

```yaml
apiVersion: "kafka.jikkou.io/v1beta2"
kind: KafkaTopic
metadata:
  name: 'my-topic'
  labels:
    environment: test
  annotations: {}
spec:
  partitions: 1
  replicas: 1
  configs:
    min.insync.replicas: 1
    cleanup.policy: 'delete'
```

## Resource Properties

The following are the properties that can be set to describe a resource:

| Property               | Description                                                                                                                                            |
|------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------|
| `apiVersion`           | The `group/version` of the resource type.                                                                                                              |
| `kind`                 | The type of the describe resource.                                                                                                                     |
| `metadata.name`        | An optional name to identify the resource.                                                                                                             |
| `metadata.labels`      | Arbitrary metadata to attach to the resource that can be handy when you have a lot of resources and you only need to identity or filters some objects. |
| `metadata.annotations` | Arbitrary non-identifying metadata to attach to the resource to mark them for a specific operation or to record some metadata.                         |
| `spec`                 | The object properties describing a desired state                                                                                                       |
|                        |                                                                                                                                                        |
