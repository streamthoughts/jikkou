---
title: "KafkaTopicMaxReplicas"
linkTitle: "KafkaTopicMaxReplicas"
description: "Enforce a maximum replication factor for Kafka topics using this Jikkou transformation."
aliases:
  - /docs/providers/kafka/transformations/kafkatopicmaxreplicas/
---

{{% pageinfo color="info" %}}
This transformation can be used to enforce a maximum value for the replication factor of kafka topics.
{{% /pageinfo %}}

## Configuration

| Name                   | Type | Description                                                     | Default |
|------------------------|------|-----------------------------------------------------------------|---------|
| `maxReplicationFactor` | Int  | Maximum value of replication factor to be used for Kafka Topics |         |

## Example

```hocon
jikkou {
  transformations: [
    {
      type = io.jikkou.kafka.transform.KafkaTopicMaxReplicasTransformation
      priority = 100
      config = {
        maxReplicationFactor = 5
      }
    }
  ]
}
```
