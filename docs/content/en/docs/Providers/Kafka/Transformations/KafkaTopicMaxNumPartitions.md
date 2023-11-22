---
title: "KafkaTopicMaxNumPartitions"
linkTitle: "KafkaTopicMaxNumPartitions"
---

{{% pageinfo color="info" %}}
This transformation can be used to enforce a **maximum** value for the number of partitions of kafka topics.
{{% /pageinfo %}}

## Configuration

| Name               | Type | Description                                                            | Default |
|--------------------|------|------------------------------------------------------------------------|---------|
| `maxNumPartitions` | Int  | maximum value for the number of partitions to be used for Kafka Topics |         |

## Example

```hocon
jikkou {
  transformations: [
    {
      type = io.streamthoughts.jikkou.kafka.transform.KafkaTopicMaxNumPartitions
      priority = 100
      config = {
        maxNumPartitions = 50
      }
    }
  ]
}
```
