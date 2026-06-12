---
title: "Configuration"
linkTitle: "Configuration"
weight: 2
description: >
  Learn how to configure the extensions for Aiven.
aliases:
  - /docs/providers/aiven/configuration/
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
    type = io.jikkou.extension.aiven.AivenExtensionProvider
    config = {
      # Aiven project name
      project = "http://localhost:8081"
      # Aiven service name
      service = generic
      # URL to the Aiven REST API.
      apiUrl = "https://api.aiven.io/v1/"
      # Aiven Bearer Token. Tokens can be obtained from your Aiven profile page
      tokenAuth = null
      # HTTP proxy URL, e.g. 'http://proxy.example.com:3128'. When empty, JVM proxy system properties are used.
      proxyUrl = "http://proxy.example.com:3128"
      # Username for proxy Basic authentication (optional).
      proxyUsername = null
      # Password for proxy Basic authentication (optional).
      proxyPassword = null
      # Comma-separated hosts that bypass the proxy, e.g. 'localhost,127.0.0.1,*.internal'.
      nonProxyHosts = "localhost,127.0.0.1"
      # Enable debug logging
      debugLoggingEnabled = false
    }
  }
}
```

## HTTP proxy

Jikkou can reach the Aiven REST API through an HTTP/HTTPS forward proxy.

| Property         | Description                                                                                        |
|------------------|----------------------------------------------------------------------------------------------------|
| `proxyUrl`       | Proxy URL, e.g. `http://proxy.example.com:3128`. When empty, JVM proxy system properties are used.  |
| `proxyUsername`  | Username for proxy Basic authentication (optional).                                                |
| `proxyPassword`  | Password for proxy Basic authentication (optional).                                                |
| `nonProxyHosts`  | Comma-separated hosts that bypass the proxy, e.g. `localhost,127.0.0.1,*.internal`.                |

If `proxyUrl` is not set, Jikkou honors the standard JVM proxy system properties
(`-Dhttps.proxyHost`, `-Dhttp.proxyHost`, `-Dhttp.proxyUser`, `-Dhttp.proxyPassword`,
`-Dhttp.nonProxyHosts`), which can be supplied via `JAVA_TOOL_OPTIONS`.

{{% alert title="Note" color="info" %}}
The OS-level `http_proxy` / `https_proxy` environment variables are **not** read by the JVM
and have no effect. Use `proxyUrl` or the JVM system properties above.
{{% /alert %}}

