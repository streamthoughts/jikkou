---
title: "Labels"
linkTitle: "Labels"
weight: 60
description: >
  Learn how to use the metadata labels provided by the Kafka Connect extension.
---

{{% pageinfo %}}
This section lists a number of well known labels, that have defined semantics. They can be attached
to `KafkaConnect` resources through the `metadata.labels` field and consumed as needed by extensions (i.e., validations, transformations, controller,
collector, etc.).
{{% /pageinfo %}}

## Labels

### `kafka.jikkou.io/connect-cluster`

```yaml
# Example
---
apiVersion: "kafka.jikkou.io/v1beta1"
kind: "KafkaConnector"
metadata:
  labels:
    kafka.jikkou.io/connect-cluster: 'my-connect-cluster'
```

The value of this label defined the name of the Kafka Connect cluster to create the connector instance in.
The cluster name must be configured through the `kafkaConnect.clusters[]` Jikkou's configuration setting (see: [Configuration]({{% relref "../Configuration" %}})).