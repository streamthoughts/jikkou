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
  # The providers
  provider:
    # Core
    core:
      enabled: true
      type: io.streamthoughts.jikkou.core.CoreExtensionProvider
      
    # Default configuration for Apache Kafka
    kafka:
      enabled: true
      type: io.streamthoughts.jikkou.kafka.KafkaExtensionProvider
      config:
        client:
          bootstrap.servers: localhost:9092
          
    # Default configuration for Schema Registry
    schemaregistry:
      enabled: true
      type: io.streamthoughts.jikkou.schema.registry.SchemaRegistryExtensionProvider
      config:
        url: http://localhost:8081
        
    # Default configuration for Kafka Connect
    kafkaconnect:
      enabled: true
      type: io.streamthoughts.jikkou.kafka.connect.KafkaConnectExtensionProvider
      config:
        clusters:
          - name: localhost
            url: http://localhost:8083
```