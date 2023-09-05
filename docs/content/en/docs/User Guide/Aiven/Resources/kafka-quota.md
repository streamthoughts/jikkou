---
categories: [ ]
tags: [ "feature", "resources" ]
title: "Quotas for Aiven Apache Kafka®"
linkTitle: "Quotas for Aiven Apache Kafka®"
weight: 10
description: >
  Learn how to manage Quotas in Aiven for Apache Kafka®
---

{{% pageinfo color="info" %}}
The `KafkaQuota` resources are used to manage the Quotas in Aiven for Apache Kafka® service. 
For more details, see https://docs.aiven.io/docs/products/kafka/concepts/kafka-quotas
{{% /pageinfo %}}

## `KafkaQuota`

### Specification

Here is the _resource definition file_ for defining a `KafkaQuota`.

```yaml
---
apiVersion: "kafka.aiven.io/v1beta1"   # The api version (required)
kind: "KafkaQuota"                     # The resource kind (required)
metadata:
  labels: { }
  annotations: { }
spec:
  user: <string>                     # The username: (Optional: 'default' if null)
  clientId: <string>                 # The client-id
  consumerByteRate: <number>         # The quota in bytes for restricting data consumption
  producerByteRate: <number>         # The quota in bytes for restricting data production
  requestPercentage: <number>

```

### Example

Here is a simple example that shows how to define a single ACL entry using
the `KafkaQuota` resource type.

_`file: kafka-quotas.yaml`_

```yaml
---
apiVersion: "kafka.aiven.io/v1beta1"
kind: "KafkaQuota"
spec:
  user: "default"
  clientId: "default"
  consumerByteRate: 1048576
  producerByteRate: 1048576
  requestPercentage: 25
```

## `KafkaQuotaList`

If you need to define multiple Kafka quotas (e.g. using a template), it may be easier to use a `KafkaQuotaList` resource.


### Specification

Here the _resource definition file_ for defining a `KafkaTopicList`.

```yaml
---
apiVersion: "kafka.aiven.io/v1beta1"    # The api version (required)
kind: "KafkaQuotaList"                  # The resource kind (required)
metadata: # (optional)
  labels: { }
  annotations: { }
items: [ ]                             # An array of KafkaQuotaList
```

### Example

Here is a simple example that shows how to define a single YAML file containing two ACL entry definitions using
the `KafkaQuotaList` resource type.

```yaml
---
apiVersion: "kafka.aiven.io/v1beta1"
kind: "KafkaQuotaList"
items:
  - spec:
      user: "default"
      clientId: "default"
      consumerByteRate: 1048576
      producerByteRate: 1048576
      requestPercentage: 5
  - spec:
      user: "avnadmin"
      consumerByteRate: 5242880
      producerByteRate: 5242880
      requestPercentage: 25
```