---
categories: [ ]
tags: [ "feature", "resources" ]
title: "Kafka Quotas"
linkTitle: "Quotas"
weight: 30
description: >
  Learn how to manage Kafka Client Quotas
---

{{% pageinfo color="info" %}}
KafkaClientQuota resources are used to define the quota limits to be applied on Kafka consumers and producers.
A KafkaClientQuota resource can be used to apply limit to consumers and/or producers identified by a `client-id` or a
user `principal`.
{{% /pageinfo %}}

## `KafkaClientQuota`

### Specification

Here is the _resource definition file_ for defining a `KafkaClientQuota`.

```yaml
apiVersion: "kafka.jikkou.io/v1beta2" # The api version (required)
kind: "KafkaClientQuota"              # The resource kind (required)
metadata: # (optional)
  labels: { }
  annotations: { }
spec:
  type: <The quota type> # (required)       
  entity:
    clientId: <The id of the client>    # (required depending on the quota type).
    user: <The principal of the user>   # (required depending on the quota type).
  configs:
    requestPercentage: <The quota in percentage (%) of total requests>      # (optional)
    producerByteRate: <The quota in bytes for restricting data production>  # (optional)
    consumerByteRate: <The quota in bytes for restricting data consumption> # (optional)
```

### Quota Types

The list below describes the supported quota types:

* `USERS_DEFAULT`: Set default quotas for all users.
* `USER`: Set quotas for a specific user principal.
* `USER_CLIENT`: Set quotas for a specific user principal and a specific client-id.
* `USER_ALL_CLIENTS`: Set default quotas for a specific user and all clients.
* `CLIENT`: Set default quotas for a specific client.
* `CLIENTS_DEFAULT`: Set default quotas for all clients.

### Example

Here is a simple example that shows how to define a single YAML file containing two quota definitions using
the `KafkaClientQuota` resource type.

_`file: kafka-quotas.yaml`_

```yaml
---
apiVersion: 'kafka.jikkou.io/v1beta2'
kind: 'KafkaClientQuota'
metadata:
  labels: { }
  annotations: { }
spec:
  type: 'CLIENT'
  entity:
    clientId: 'my-client'
  configs:
    requestPercentage: 10
    producerByteRate: 1024
    consumerByteRate: 1024
---
apiVersion: 'kafka.jikkou.io/v1beta2'
kind: 'KafkaClientQuota'
metadata:
  labels: { }
  annotations: { }
spec:
  type: 'USER'
  entity:
    user: 'my-user'
  configs:
    requestPercentage: 10
    producerByteRate: 1024
    consumerByteRate: 1024
```

## `KafkaClientQuotaList`

If you need to define multiple topics (e.g. using a template), it may be easier to use a `KafkaClientQuotaList`
resource.

### Specification

Here the _resource definition file_ for defining a `KafkaTopicList`.

```yaml
apiVersion: "kafka.jikkou.io/v1beta2"  # The api version (required)
kind: "KafkaClientQuotaList"           # The resource kind (required)
metadata: # (optional)
  name: <The name of the topic>
  labels: { }
  annotations: { }
items: [ ]                              # An array of KafkaClientQuota
```

### Example

Here is a simple example that shows how to define a single YAML file containing two `KafkaClientQuota` definition using
the `KafkaClientQuotaList` resource type.

```yaml
apiVersion: 'kafka.jikkou.io/v1beta2'
kind: 'KafkaClientQuotaList'
metadata:
  labels: { }
  annotations: { }
items:
  - spec:
    type: 'CLIENT'
    entity:
      clientId: 'my-client'
    configs:
      requestPercentage: 10
      producerByteRate: 1024
      consumerByteRate: 1024

  - spec:
      type: 'USER'
      entity:
        user: 'my-user'
      configs:
        requestPercentage: 10
        producerByteRate: 1024
        consumerByteRate: 1024
```