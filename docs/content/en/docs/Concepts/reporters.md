---
categories: [ ]
tags: [ "feature", "extensions" ]
title: "Reporters"
linkTitle: "Reporters"
weight: 11
---

{{% pageinfo color="info" %}}
**_Reporters_** can be used to report changes applied by Jikkou to a third-party system.
{{% /pageinfo %}}

## Configuration

Jikkou allows you to configure multiple reporters as follows:

```hocon
jikkou {
  # The list of reporters to execute
  reporters: [
    {
      # Custom name for the reporter
      name = ""
      # Simple or fully qualified class name of the transformation extension.
      type = ""
      config = {
        # Configuration properties for this reporter
      }
    }
  ]
}
```

{{% alert title="Tips" color="info" %}}
The `config` object passed to a _reporter_ will fallback on the top-level `jikkou` config. 
This allows you to globally declare some configuration settings.
{{% /alert %}}

### Built-in implementations

Jikkou packs with some built-in `ChangeReporter` implementations: 

### `KafkaChangeReporter`

The `KafkaChangeReporter` can be used to send change results into a given kafka topic. Changes will be published
as Cloud Events.

#### Configuration

The below example shows how to configure the `KafkaChangeReporter`.

```hocon
jikkou {
  # The default custom reporters to report applied changes.
  reporters = [
    {
      name = "kafka-reporter"
      type = io.streamthoughts.jikkou.kafka.reporter.KafkaChangeReporter
      config = {
        # The 'source' of the event that will be generated.
        event.source = "jikkou/cli"
        kafka = {
          # If 'true', topic will be automatically created if it does not already exist.
          topic.creation.enabled = true
          # The default replication factor used for creating topic.
          topic.creation.defaultReplicationFactor = 1
          # The name of the topic the events will be sent.
          topic.name = "jikkou-resource-change-event"
          # The configuration settings for Kafka Producer and AdminClient
          client = ${jikkou.kafka.client} {
            client.id = "jikkou-reporter-producer"
          }
        }
      }
    }
  ]
}
```
