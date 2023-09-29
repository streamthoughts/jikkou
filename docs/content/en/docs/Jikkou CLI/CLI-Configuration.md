---
title: "CLI Configuration"
linkTitle: "CLI Configuration"
weight: 2
description: >
  Learn how to configure Jikkou CLI.
---

{{% pageinfo %}}
**Hands-on:** Try the Jikkou: [Get Started tutorials]({{% relref "../Tutorials/get_started.md" %}}).
{{% /pageinfo %}}


## Configuration

To set up the configuration settings used by Jikkou CLI, you will need create a _jikkou config file_, which is created
automatically when you create a configuration context using:

```bash
jikkou config set-context <context-name> [--config-file=<config-gile>] [--config-props=<config-value>]
```

By default, the configuration of `jikkou` is located under the path `$HOME/.jikkou/config`.

This _jikkou config file_ defines all the contexts that can be used by jikkou CLI.

For example, below is the config file created during the [Getting Started]({{< relref "../Tutorials/get_started.md" >}}).

```json
{
  "currentContext": "localhost",
  "localhost": {
    "configFile": null,
    "configProps": {
      "kafka.client.bootstrap.servers": "localhost:9092"
    }
  }
}
```

Most of the time, a _context_ does not directly contain the configuration properties to be used, but rather points to a
specific [HOCON (Human-Optimized Config Object Notation)](https://github.com/lightbend/config) through the `configFile` property.

Then, the `configProps` allows you to override some of the property define by this file.

In addition, if no configuration file path is specified, Jikkou will lookup for an `application.conf` to
those following locations:

* `./application.conf`
* `$HOME/.jikkou/application.conf`

Finally, Jikkou always fallback 
to a [reference.conf](https://github.com/streamthoughts/jikkou/blob/main/jikkou-cli/src/main/resources/reference.conf) 
file that you can use as a template to define your own configuration.

**_reference.conf_**:
```hocon
jikkou {
  # The paths from which to load extensions
  extension.paths = [${?JIKKOU_EXTENSION_PATH}]

  # Kafka Extension
  kafka {
    # The default Kafka Client configuration
    client {
      bootstrap.servers = "localhost:9092"
      bootstrap.servers = ${?JIKKOU_DEFAULT_KAFKA_BOOTSTRAP_SERVERS}
    }
    brokers {
      # If 'True' 
      waitForEnabled = true
      waitForEnabled = ${?JIKKOU_KAFKA_BROKERS_WAIT_FOR_ENABLED}
      # The minimal number of brokers that should be alive for the CLI stops waiting.
      waitForMinAvailable = 1
      waitForMinAvailable = ${?JIKKOU_KAFKA_BROKERS_WAIT_FOR_MIN_AVAILABLE}
      # The amount of time to wait before verifying that brokers are available.
      waitForRetryBackoffMs = 1000
      waitForRetryBackoffMs = ${?JIKKOU_KAFKA_BROKERS_WAIT_FOR_RETRY_BACKOFF_MS}
      # Wait until brokers are available or this timeout is reached.
      waitForTimeoutMs = 60000
      waitForTimeoutMs = ${?JIKKOU_KAFKA_BROKERS_WAIT_FOR_TIMEOUT_MS}
    }
  }

  schemaRegistry {
    url = "http://localhost:8081"
    url = ${?JIKKOU_DEFAULT_SCHEMA_REGISTRY_URL}
  }

  # The default custom transformations to apply on any resources.
  transformations = []

  # The default custom validations to apply on any resources.
  validations = [
    {
      name = "topicMustHaveValidName"
      type = io.streamthoughts.jikkou.kafka.validation.TopicNameRegexValidation
      priority = 100
      config = {
        topicNameRegex = "[a-zA-Z0-9\\._\\-]+"
        topicNameRegex = ${?VALIDATION_DEFAULT_TOPIC_NAME_REGEX}
      }
    },
    {
      name = "topicMustHaveParitionsEqualsOrGreaterThanOne"
      type = io.streamthoughts.jikkou.kafka.validation.TopicMinNumPartitionsValidation
      priority = 100
      config = {
        topicMinNumPartitions = 1
        topicMinNumPartitions = ${?VALIDATION_DEFAULT_TOPIC_MIN_NUM_PARTITIONS}
      }
    },
    {
      name = "topicMustHaveReplicasEqualsOrGreaterThanOne"
      type = io.streamthoughts.jikkou.kafka.validation.TopicMinReplicationFactorValidation
      priority = 100
      config = {
        topicMinReplicationFactor = 1
        topicMinReplicationFactor = ${?VALIDATION_DEFAULT_TOPIC_MIN_REPLICATION_FACTOR}
      }
    }
  ]
  # The default custom reporters to report applied changes.
  reporters = [
    # Uncomment following lines to enable default kafka reporter
    #    {
    #     name = "default"
    #      type = io.streamthoughts.jikkou.kafka.reporter.KafkaChangeReporter
    #      config = {
    #        event.source = "jikkou/cli"
    #        kafka = {
    #          topic.creation.enabled = true
    #          topic.creation.defaultReplicationFactor = 1
    #          topic.name = "jikkou-resource-change-event"
    #          client = ${jikkou.kafka.client} {
    #            client.id = "jikkou-reporter-producer"
    #          }
    #        }
    #      }
    #    }
  ]
}
```

### Listing Contexts

```bash
$ jikkou config get-contexts 
 
 NAME         
 localhost *
 development
 staging
 production
```

### Verify Current Context

You can use `jikkou config current-context` command to show the context currently used by Jikkou CLI.

```bash
$ jikkou config current-context
Using context 'localhost'

 KEY          VALUE                                                                         
 ConfigFile   
 ConfigProps  {"kafka.client.bootstrap.servers": "localhost:9092"}  
```

### Verify Current Configuration

You can use `jikkou config view` command to show the configuration currently used by Jikkou CLI.

{{% alert title="Tips" color="info" %}}
To debug the configuration use by Jikkou, you can run the following command: `jikkou config view --comments`
or `jikkou config view --debug`
{{% /alert %}}