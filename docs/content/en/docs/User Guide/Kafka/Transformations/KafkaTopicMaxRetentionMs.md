---
title: "KafkaTopicMaxRetentionMs"
linkTitle: "KafkaTopicMaxRetentionMs"
---

{{% pageinfo color="info" %}}
This transformation can be used to enforce a **maximum** value for the **`retention.ms`** property of kafka topics.
{{% /pageinfo %}}

## Configuration

| Name             | Type | Description                                                 | Default |
|------------------|------|-------------------------------------------------------------|---------|
| `maxRetentionMs` | Int  | Minimum value of `retention.ms` to be used for Kafka Topics |         |

## Example

```hocon
jikkou {
  transformations: [
    {
      type = io.streamthoughts.jikkou.kafka.transform.KafkaTopicMinRetentionMsTransformation
      priority = 100
      config = {
        maxRetentionMs = 2592000000 # 30 days
      }
    }
  ]
}
```
