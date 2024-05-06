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
through the Jikkou client configuration property `jikkou.schemaRegistry`.

**Example:**

```hocon
jikkou {
  schemaRegistry {
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
  }
}
```
