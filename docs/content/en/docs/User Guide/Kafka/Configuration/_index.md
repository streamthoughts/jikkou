---
title: "Configuration"
linkTitle: "Configuration"
weight: 2
description: >
  Learn how to configure the extensions for Apache Kafka.
---

{{% pageinfo %}}
Here, you will find the list of resources supported for Apache Kafka.
{{% /pageinfo %}}

## Configuration

The Apache Kafka extension is built on top of the Kafka Admin Client. You can configure the properties to be passed to
kafka client
through the Jikkou client configuration property `jikkou.kafka.client`.

**Example:**

```hocon
jikkou {
  kafka {
    client {
      bootstrap.servers = "localhost:9092"
      security.protocol = "SSL"
      ssl.keystore.location = "/tmp/client.keystore.p12"
      ssl.keystore.password = "password"
      ssl.keystore.type = "PKCS12"
      ssl.truststore.location = "/tmp/client.truststore.jks"
      ssl.truststore.password = "password"
      ssl.key.password = "password"
    }
  }
}
```

In addition, the extension support configuration settings to wait for at least a minimal number of brokers before
processing.

```hocon
jikkou {
  kafka {
    brokers {
      # If 'True' 
      waitForEnabled = true
      waitForEnabled = ${?JIKKOU_KAFKA_BROKERS_WAIT_FOR_ENABLED}
      # The minimal number of brokers that should be alive for the CLI stops waiting.
      waitForMinAvailable = 1
      waitForMinAvailable = ${?JIKKOU_KAFKA_BROKERS_WAIT_FOR_MIN_AVAILABLE}
      # The amount of time to wait before verifying that brokers are available.
      waitForRetryBackoffMs = 1000
      waitForRetryBackoffMs = ${?JIKKOU_KAFKA_BROKERS_WAIT_FOR_RETRY_BACKOFF_MS}
      # Wait until brokers are available or this timeout is reached.
      waitForTimeoutMs = 60000
      waitForTimeoutMs = ${?JIKKOU_KAFKA_BROKERS_WAIT_FOR_TIMEOUT_MS}
    }
  }
}
```
