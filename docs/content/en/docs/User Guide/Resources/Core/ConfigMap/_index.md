---
title: "ConfigMap"
linkTitle: "ConfigMap"
weight: 1
description: >
  Learn how to use ConfigMap objects.
---

You can use a `ConfigMap` to define reusable data in the form of key/value pairs that can then be referenced and used by
other resources.

## Specification

```yaml
---
apiVersion: "core.jikkou.io/v1beta2"
kind: ConfigMap
metadata:
  name: '<CONFIG-MAP-NAME>'   # Name of the ConfigMap (required)
data:                         # Map of key-value pairs (required)
  <KEY_1>: "<VALUE_1>" 
```

## Example

For example, the below `ConfigMap` show how to define default config properties namedc`KafkaTopicConfig` that can then
reference and used to define multiple [KafkaTopic]({{< relref "../../Kafka/Topics" >}}). resources.

```yaml
---
apiVersion: "core.jikkou.io/v1beta2"
kind: ConfigMap
metadata:
name: 'KafkaTopicConfig'
data:
  cleanup.policy: 'delete'
  min.insync.replicas: 2
  retention.ms: 86400000 # (1 day)
```
```