---
title: "Release v0.34.0"
linkTitle: "Release v0.34.0"
weight: -34
---

## Introducing Jikkou 0.34.0

We're excited to unveil the latest release of
Jikkou [0.34.0](https://github.com/streamthoughts/jikkou/releases/tag/v0.34.0). 🎉

To install the new version, please visit the [installation guide](https://www.jikkou.io/docs/install/). For detailed
release notes, check out the [GitHub page](https://github.com/streamthoughts/jikkou/releases/tag/v0.34.0).

### What's New in Jikkou 0.34.0?

* Enhanced Aiven provider with support for Kafka topics.
* Added SSL support for Kafka Connect and Schema Registry
* Introduced dynamic connection for Kafka Connect clusters

Below is a summary of these new features with examples.

## Topic Aiven for Apache Kafka

Jikkou 0.34.0 adds a new `KafkaTopic` kind
that can be used to manage kafka Topics directly though the [Aiven](https://aiven.io/) API.

You can list kafka topics using the new command below:

```bash
jikkou get avn-kafkatopics
```

In addition, topics can be described, created and updated using the same resource model as the Apache Kafka provider.

```yaml
# file:./aiven-kafkat-topics.yaml
---
apiVersion: "kafka.aiven.io/v1beta2"
kind: "KafkaTopic"
metadata:
  name: "test"
  labels:
    tag.aiven.io/my-tag: "test-tag"
spec:
  partitions: 1
  replicas: 3
  configs:
    cleanup.policy: "delete"
    compression.type: "producer"
```

The main advantages of using this new resource kind are the use of the Aiven Token API to authenticate to the Aiven API
and the ability to manage tags for kafka topics.

## SSL support for Kafka Connect and Schema Registry

Jikkou 0.34.0 also brings SSL support for the Kafka Connect and Schema Registry providers.
Therefore, it's now possible to configure the providers to authenticate using SSL certificate.

Example for the Schema Registry:

````hocon
jikkou {
  schemaRegistry {
    url = "https://localhost:8081"
    authMethod = "SSL"
    sslKeyStoreLocation = "/certs/registry.keystore.jks"
    sslKeyStoreType = "JKS"
    sslKeyStorePassword = "password"
    sslKeyPassword = "password"
    sslTrustStoreLocation = "/certs/registry.truststore.jks"
    sslTrustStoreType = "JKS"
    sslTrustStorePassword = "password"
    sslIgnoreHostnameVerification = true
  }
}
````

## Dynamically connection for Kafka Connect clusters

Before Jikkou 0.34.0, to deploy a Kafka Connect connector, it was mandatory to configure a connection to a target
cluster:

```hocon
jikkou {
  extensions.provider.kafkaconnect.enabled = true
  kafkaConnect {
    clusters = [
      {
        name = "my-connect-cluster"
        url = "http://localhost:8083"
      }
    ]
  }
}
```

This connection could then be referenced in a connector resource definition via the
annotation `kafka.jikkou.io/connect-cluster`.

```yaml
apiVersion: "kafka.jikkou.io/v1beta1"
kind: "KafkaConnector"
metadata:
  name: "mu-connector"
  labels:
    kafka.jikkou.io/connect-cluster: "my-connect-cluster"
```

This approach is suitable for most use cases, but can be challenging if you need to manage a large and dynamic number of
Kafka Connect clusters.

To meet this need, it is now possible to provide connection information for the cluster to connect to directly, through
the new metadata annotation new metadata annotation: `jikkou.io/config-override`.

Here is a simple example showing the use of the new annotation:

```yaml
apiVersion: "kafka.jikkou.io/v1beta1"
kind: "KafkaConnector"
metadata:
  name: "mu-connector"
  labels:
    kafka.jikkou.io/connect-cluster: "my-connect-cluster"
  annotations:
    jikkou.io/config-override: |
      { "url": "http://localhost:8083" }
```
This new annotation can be used with the Jikkou's Jinja template creation mechanism 
to define dynamic configuration.

## Wrapping Up

We hope you enjoy these new features. If you encounter any issues with Jikkou v0.34.0, please feel free to open a GitHub
issue on our [project page](https://github.com/streamthoughts/jikkou/issues). Don't forget to give us a ⭐️
on [Github](https://github.com/streamthoughts/jikkou) to support
the team, and join us on [Slack](https://join.slack.com/t/jikkou-io/shared_invite/zt-27c0pt61j-F10NN7d7ZEppQeMMyvy3VA).