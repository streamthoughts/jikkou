---
title: "API Server"
linkTitle: "API Server"
weight: 1
description: >
  Learn how to configure the Jikkou API server.
---


## Running Server on a Specific Port

By default, the server runs on port `28082`. However, you can set the server to run on a specific port:

```yaml
# ./etc/application.yaml
micronaut:
  server:
    port: 80  # Port used to access APIs

endpoints:
  all:
    port: 80  # Port used to access Health endpoints
```

## Enabling Specific Extension Providers

By default, the server is configured to run only with the `core` and  `kafka` extension providers.
However, you can enable (or disable) additional providers:

```yaml
jikkou:
  extensions.provider:
    # By default, disable all extension providers.
    default.enabled: false
    
    # Explicitly enabled/disable an extension provider
    #<provider_name>.enabled: <boolean>
    core.enabled: true
    kafka.enabled: true
    # schemaregistry.enabled: true
    # aiven.enabled: true
    # kafkaconnect.enabled: true
```