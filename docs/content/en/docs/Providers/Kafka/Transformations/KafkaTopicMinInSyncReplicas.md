---
title: "KafkaTopicMinInSyncReplicas"
linkTitle: "KafkaTopicMinInSyncReplicas"
description: "Enforce a minimum min.insync.replicas value for Kafka topics using this Jikkou transformation."
---

{{% pageinfo color="info" %}}
This transformation can be used to enforce a **minimum** value for the **`min.insync.replicas`** property of kafka topics.
{{% /pageinfo %}}

## Configuration

| Name                | Type | Description                                                        | Default |
|---------------------|------|--------------------------------------------------------------------|---------|
| `minInSyncReplicas` | Int  | Minimum value of `min.insync.replicas` to be used for Kafka Topics |         |

## Example

```hocon
jikkou {
  transformations: [
    {
      type = io.streamthoughts.jikkou.kafka.transform.KafkaTopicMinInSyncReplicasTransformation
      priority = 100
      config = {
        minInSyncReplicas = 2
      }
    }
  ]
}
```
