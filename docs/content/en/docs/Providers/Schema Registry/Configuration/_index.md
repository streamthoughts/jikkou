---
title: "Configuration"
linkTitle: "Configuration"
weight: 2
description: >
  Learn how to configure the extensions for SchemaRegistry.
---

{{% pageinfo %}}
Here, you will find the list of resources supported for SchemaRegistry.
{{% /pageinfo %}}

## Configuration

You can configure the properties to be used to connect the SchemaRegistry service
through the Jikkou client configuration property `jikkou.provider.schemaregistry`.

**Example:**

```hocon
jikkou {
  provider.schemaregistry {
    enabled = true
    type = io.jikkou.schema.registry.SchemaRegistryExtensionProvider
    config = {
      # Comma-separated list of URLs for schema registry instances that can be used to register or look up schemas
      url = "http://localhost:8081"
      # The name of the schema registry implementation vendor - can be any value
      vendor = generic
      # Method to use for authenticating on Schema Registry. Available values are: [none, basicauth, ssl]
      authMethod = none
      # Use when 'schemaRegistry.authMethod' is 'basicauth' to specify the username for Authorization Basic header
      basicAuthUser = null
      # Use when 'schemaRegistry.authMethod' is 'basicauth' to specify the password for Authorization Basic header
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

      # HTTP proxy: route requests to the Schema Registry through a forward proxy.
      # Proxy URL, e.g. 'http://proxy.example.com:3128'. When empty, JVM proxy system properties are used.
      proxyUrl = "http://proxy.example.com:3128"
      # Username for proxy Basic authentication (optional).
      proxyUsername = null
      # Password for proxy Basic authentication (optional).
      proxyPassword = null
      # Comma-separated hosts that bypass the proxy, e.g. 'localhost,127.0.0.1,*.internal'.
      nonProxyHosts = "localhost,127.0.0.1"
    }
  }
}
```

## HTTP proxy

Jikkou can reach the Schema Registry REST API through an HTTP/HTTPS forward proxy.

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
