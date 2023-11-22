---
title: "KafkaTopicMinRetentionMs"
linkTitle: "KafkaTopicMinRetentionMs"
---

{{% pageinfo color="info" %}}
This transformation can be used to enforce a **minimum** value for the **`retention.ms`** property of kafka topics.
{{% /pageinfo %}}

## Configuration

| Name             | Type | Description                                                 | Default |
|------------------|------|-------------------------------------------------------------|---------|
| `minRetentionMs` | Int  | Minimum value of `retention.ms` to be used for Kafka Topics |         |

## Example

```hocon
jikkou {
  transformations: [
    {
      type = io.streamthoughts.jikkou.kafka.transform.KafkaTopicMinRetentionMsTransformation
      priority = 100
      config = {
        minRetentionMs = 604800000 # 7 days
      }
    }
  ]
}
```
