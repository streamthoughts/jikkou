---
title: "KafkaTopicMinReplicas"
linkTitle: "KafkaTopicMinReplicas"
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
      type = io.streamthoughts.jikkou.kafka.transform.KafkaTopicMinReplicasTransformation
      priority = 100
      config = {
        minReplicationFactor = 3
      }
    }
  ]
}
```
