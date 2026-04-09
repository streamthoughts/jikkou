---
title: "KafkaTopicMinReplicas"
linkTitle: "KafkaTopicMinReplicas"
description: "Enforce a minimum replication factor for Kafka topics using this Jikkou transformation."
---

{{% pageinfo color="info" %}}
This transformation can be used to enforce a minimum value for the replication factor of kafka topics.
{{% /pageinfo %}}

## Configuration

| Name                   | Type | Description                                                     | Default |
|------------------------|------|-----------------------------------------------------------------|---------|
| `minReplicationFactor` | Int  | Minimum value of replication factor to be used for Kafka Topics |         |

## Example

```hocon
jikkou {
  transformations: [
    {
      type = io.jikkou.kafka.transform.KafkaTopicMinReplicasTransformation
      priority = 100
      config = {
        minReplicationFactor = 3
      }
    }
  ]
}
```
