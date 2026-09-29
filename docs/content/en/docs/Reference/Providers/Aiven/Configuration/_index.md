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

      # Additional HTTP headers sent on every request to the Aiven REST API.
      # Applied last, so these override headers Jikkou sets itself, including 'Authorization'.
      clientHeaders {
        X-Api-Gateway-Key = "my-gateway-key"
        X-Tenant = "acme"
      }
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

## Custom HTTP headers

The `clientHeaders` property attaches arbitrary HTTP headers to every request Jikkou
sends to the Aiven REST API. It is useful for API gateway keys, tenant identifiers,
and tracing headers.

```hocon
clientHeaders {
  X-Api-Gateway-Key = "my-gateway-key"
  X-Tenant = "acme"
}
```

Custom headers are applied **last**, so a header set here replaces the one Jikkou would
otherwise send under the same name, including the `Authorization` header built from
`tokenAuth`. Header names are matched case-insensitively.

{{% alert title="Note" color="info" %}}
When debug logging is enabled, header values are redacted if the header name is
`Authorization`, `Proxy-Authorization`, `Cookie`, or `Set-Cookie`, or if it contains
`token`, `secret`, `key`, or `password`. Header names are always logged in full.
{{% /alert %}}

