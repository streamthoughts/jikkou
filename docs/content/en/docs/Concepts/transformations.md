---
tags: [ "concept", "feature", "extension" ]
title: "Transformations"
linkTitle: "Transformations"
weight: 5
---

{{% pageinfo color="info" %}}
**_Transformations_** are applied to inbound resources. _Transformations_ are used to transform, enrich, or filter
resource entities
before they are validated and thus before the reconciliation process is executed on them.
{{% /pageinfo %}}

## Available Transformations

You can list all the available transformations using the Jikkou CLI command: 

```bash
jikkou extensions list --type=Transformation [-kinds <a resource kind to filter returned results>]
```

## Transformation chain

When using Jikkou CLI, you can configure a _transformation chain_ that will be applied to every resource.
This chain consists of multiple transformations, each designed to handle different types of resources. Jikkou ensures
that a transformation is executed only for the resource types it supports. In cases where a resource is not
accepted by a transformation, it is passed to the next transformation in the chain.
This process continues until a suitable transformation is found or until all transformations have been attempted.

### Configuration

```hocon
jikkou {
  # The list of transformations to execute
  transformations: [
    {
      # Simple or fully qualified class name of the transformation extension.
      type = ""
      # Priority to be used for executing this transformation extension.
      # The lowest value has the highest priority, so it's run first. Minimum value is -2^31 (highest) and a maximum value is 2^31-1 (lowest).
      # Usually, values under 0 should be reserved for internal transformation extensions.
      priority = 0
      config = {
        # Configuration properties for this transformation
      }
    }
  ]
}
```

{{% alert title="Tips" color="info" %}}
The `config` object of a _Transformation_ always fallback on the top-level `jikkou` config. This allows you to globally
declare some properties of the validation configuration.
{{% /alert %}}

### Example

```hocon
jikkou {
  # The list of transformations to execute
  transformations: [
    {
      # Enforce a minimum number of replicas for a kafka topic
      type = KafkaTopicMinReplicasTransformation
      priority = 100
      config = {
        minReplicationFactor = 4
      }
    },
    {
      # Enforce a {@code min.insync.replicas} for a kafka topic.
      type = KafkaTopicMinInSyncReplicasTransformation
      priority = 100
      config = {
        minInSyncReplicas = 2
      }
    }
  ]
}
```