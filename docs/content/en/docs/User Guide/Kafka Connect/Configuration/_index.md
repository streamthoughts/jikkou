---
title: "Configuration"
linkTitle: "Configuration"
weight: 2
description: >
  Learn how to configure the extensions for Kafka Connect.
---

{{% pageinfo %}}
This section describes how to configure the Kafka Connect extension.
{{% /pageinfo %}}


## Extension

The Kafka Connect extension can be enabled/disabled via the configuration properties: 

```hocon
# Example
jikkou {
  extensions.provider.kafkaconnect.enabled = true
}
```

## Configuration

You can configure the properties to be used to connect the Kafka Connect cluster
through the Jikkou client configuration property: `jikkou.kafkaConnect`.

**Example:**

```hocon
jikkou {
  kafkaConnect {
    # Array of Kafka Connect clusters configurations.
    clusters = [
      {
        # Name of the cluster (e.g., dev, staging, production, etc.)
        name = "locahost"
        # URL of the Kafka Connect service
        url = "http://localhost:8083"
        # Method to use for authenticating on Kafka Connect. Available values are: [none, basicauth]
        authMethod = none
        # Use when 'authMethod' is 'basicauth' to specify the username for Authorization Basic header
        basicAuthUser = null
        # Use when 'authMethod' is 'basicauth' to specify the password for Authorization Basic header
        basicAuthPassword = null
        # Enable debug logging
        debugLoggingEnabled = false
      }
    ]
  }
}
```
