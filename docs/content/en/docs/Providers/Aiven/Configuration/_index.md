---
title: "Configuration"
linkTitle: "Configuration"
weight: 2
description: >
  Learn how to configure the extensions for Aiven.
---

{{% pageinfo %}}
Here, you will find the list of resources supported by the extension for Aiven.
{{% /pageinfo %}}

## Configuration

You can configure the properties to be used to connect the Aiven service
through the Jikkou client configuration property `jikkou.provider.aiven`.

**Example:**

```hocon
jikkou {
  provider.aiven {
    enabled = true
    type = io.streamthoughts.jikkou.extension.aiven.AivenExtensionProvider
    config = {
      # Aiven project name
      project = "http://localhost:8081"
      # Aiven service name
      service = generic
      # URL to the Aiven REST API.
      apiUrl = "https://api.aiven.io/v1/"
      # Aiven Bearer Token. Tokens can be obtained from your Aiven profile page
      tokenAuth = null
      # Enable debug logging
      debugLoggingEnabled = false
    }
  }
}
```

