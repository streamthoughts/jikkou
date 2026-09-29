---
title: "Configuration"
linkTitle: "Configuration"
weight: 2
description: >
  Learn how to configure the extensions for Kafka Connect.
aliases:
  - /docs/providers/kafka-connect/configuration/
---

{{% pageinfo %}}
This section describes how to configure the Kafka Connect extension.
{{% /pageinfo %}}


## Configuration

You can configure the properties to be used to connect the Kafka Connect cluster
through the Jikkou client configuration property: `jikkou.provider.kafkaconnect`.

**Example:**

```hocon
jikkou {
  provider.kafkaconnect {
    enabled = true
    type = io.jikkou.kafka.connect.KafkaConnectExtensionProvider
    config = {
      # Array of Kafka Connect clusters configurations.
      clusters = [
        {
          # Name of the cluster (e.g., dev, staging, production, etc.)
          name = "localhost"
          # URL of the Kafka Connect service
          url = "http://localhost:8083"
          # Method to use for authenticating on Kafka Connect. Available values are: [none, basicauth, ssl]
          authMethod = none
          # Use when 'authMethod' is 'basicauth' to specify the username for Authorization Basic header
          basicAuthUser = null
          # Use when 'authMethod' is 'basicauth' to specify the password for Authorization Basic header
          basicAuthPassword = null
          # Enable debug logging
          debugLoggingEnabled = false

          # Ssl Config: Use when 'authMethod' is 'ssl'
          # The location of the key store file.
          sslKeyStoreLocation = "/certs/registry.keystore.jks"
          # The file format of the key store file.
          sslKeyStoreType = "JKS"
          # The password for the key store file.
          sslKeyStorePassword = "password"
          # The password of the private key in the key store file.
          sslKeyPassword = "password"
          # The location of the trust store file.
          sslTrustStoreLocation = "/certs/registry.truststore.jks"
          # The file format of the trust store file.
          sslTrustStoreType = "JKS"
          # The password for the trust store file.
          sslTrustStorePassword = "password"
          # Specifies whether to ignore the hostname verification.
          sslIgnoreHostnameVerification = true

          # HTTP proxy: route requests to the Kafka Connect REST API through a forward proxy.
          # Proxy URL, e.g. 'http://proxy.example.com:3128'. When empty, JVM proxy system properties are used.
          proxyUrl = "http://proxy.example.com:3128"
          # Username for proxy Basic authentication (optional).
          proxyUsername = null
          # Password for proxy Basic authentication (optional).
          proxyPassword = null
          # Comma-separated hosts that bypass the proxy, e.g. 'localhost,127.0.0.1,*.internal'.
          nonProxyHosts = "localhost,127.0.0.1"

          # Additional HTTP headers sent on every request to this Kafka Connect cluster.
          # Applied last, so these override headers Jikkou sets itself, including 'Authorization'.
          clientHeaders {
            X-Api-Gateway-Key = "my-gateway-key"
            X-Tenant = "acme"
          }
        }
      ]
    }
  }
}
```

## HTTP proxy

Jikkou can reach the Kafka Connect REST API through an HTTP/HTTPS forward proxy.

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
sends to a Kafka Connect cluster. It is useful for API gateway keys, tenant identifiers,
and tracing headers. It is set per cluster, inside a `clusters` entry.

```hocon
clientHeaders {
  X-Api-Gateway-Key = "my-gateway-key"
  X-Tenant = "acme"
}
```

Custom headers are applied **last**, so a header set here replaces the one Jikkou would
otherwise send under the same name, including `Authorization`. Setting `Authorization`
through `clientHeaders` is the supported way to use a bearer token or another
authentication scheme that `authMethod` does not cover. Header names are matched
case-insensitively.

{{% alert title="Note" color="info" %}}
When debug logging is enabled, header values are redacted if the header name is
`Authorization`, `Proxy-Authorization`, `Cookie`, or `Set-Cookie`, or if it contains
`token`, `secret`, `key`, or `password`. Header names are always logged in full.
{{% /alert %}}
