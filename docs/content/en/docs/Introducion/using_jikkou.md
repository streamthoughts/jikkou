---
title: "Using Jikkou"
linkTitle: "Using Jikkou"
weight: 3
description: >
    This guide shows the basics of using Jikkou to manage resource entities on your Apache Kafka cluster.
---


{{% pageinfo %}}
This guide explains the basics of using Jikkou to manage resource entities on your Apache Kafka cluster. 
It assumes that you have already installed the [Jikkou](./_installation.md) client. If you are simply interested in running a few quick commands, you may wish to begin with the [Quickstart Guide](./_getting_started.md). This chapter covers the particulars of Jikkou commands, and explains how to configure and use Jikkou.
{{% /pageinfo %}}


## Jikkou Architecture

### Components

Jikkou is a tool which is implemented into two distinct parts:

* The Jikkou Client is a command-line client for end users.
* The Jikkou Java Library provides the logic for executing all operations the Apache Kafka cluster.

The Jikkou library is available on [Maven Central]( https://mvnrepository.com/artifact/io.streamthoughts/jikkou)

**For Maven:**

```xml
<dependency>
    <groupId>io.streamthoughts</groupId>
    <artifactId>jikkou</artifactId>
    <version>${jikkou.version}</version>
</dependency>
```

### Implementation

The Jikkou client and library are written in the Java programming language.

It is built on top of the Kafka's [Java AdminClient](https://kafka.apache.org/30/javadoc/org/apache/kafka/clients/admin/Admin.html).
Thus, it works out-of-the-box with most the Apache Kafka distributions and cloud provider managed services (e.g., [Aiven](https://aiven.io/), [Confluent Cloud](https://confluent.cloud/), etc).

The tool is completely **stateless** and thus does not store any state. Basically: _Your kafka cluster is the state of Jikkou_.

Configuration files are written in [YAML](https://yaml.org/).

## CLI Usage

```bash
$ jikkou help
```
Usage:
jikkou [-hV] [COMMAND]


Jikkou streamlines the management of the configurations that live on your data streams platform.

Find more information at: https://streamthoughts.github.io/jikkou/.

Options:

-h, --help      Show this help message and exit.
-V, --version   Print version information and exit.

Commands:

create      Create resources from the resource definition files (only non-existing resources will be created).
delete      Delete resources that are no longer described by the resource definition files.
update      Create or update resources from the resource definition files
apply       Update the resources as described by the resource definition files.
resources   List supported resources
extensions  List or describe the extensions of Jikkou
config      Sets or retrieves the configuration of this client
diff        Display all resource changes.
validate    Validate resource definition files.
health      Print or describe health indicators.
help        Display help information about the specified command.
get         List and describe all resources of a specific kind.
```

## Configuration

Jikkou uses the Java Admin client API for interacting with the target Apache Kafka cluster.

You can set the configs to be passed to Admin Client using the command-line arguments:
* `--command-config`: A property file containing configs to be passed to Admin Client
* `--command-property`: A KEY=VALUE property to be passed to the Admin Client.

Additionally, Jikkou will lookup for an [HOCON (Human-Optimized Config Object Notation)](https://github.com/lightbend/config) file named `application.conf` in the following locations:

* `./application.conf`
* `$HOME/.jikkou/application.conf`

Depending on the configuration properties you will override, Jikkou will always fallback to its reference default configuration (see [reference.conf](https://github.com/streamthoughts/jikkou/blob/main/jikkou-cli/src/main/resources/reference.conf)).

```hocon
jikkou {

  # Set what resource controllers are active (i.e., the fully class names)
  controllers {
    active: [
      io.streamthoughts.jikkou.kafka.control.AdminClientKafkaTopicController
      io.streamthoughts.jikkou.kafka.control.AdminClientKafkaQuotaController
      io.streamthoughts.jikkou.kafka.control.AdminClientKafkaAuthorizationController
    ]
  }

  # Set what resource descriptors are active (i.e., the fully class names)
  descriptors {
    active: [
      io.streamthoughts.jikkou.kafka.control.AdminClientKafkaBrokerCollector
    ]
  }

  # The paths from which to load extensions
  extension.paths = [${?JIKKOU_EXTENSION_PATH}]

  kafka {
    # The default Kafka AdminClient configuration
    client  {
      bootstrap.servers = "localhost:9092"
      bootstrap.servers = ${?JIKKOU_DEFAULT_KAFKA_BOOTSTRAP_SERVERS}
    }
    brokers {
      # If 'True' 
      wait-for-enabled = true
      wait-for-enabled = ${?JIKKOU_KAFKA_BROKERS_WAIT_FOR_ENABLED}
      # The minimal number of brokers that should be alive for the CLI stops waiting.
      wait-for-min-available = 1
      wait-for-min-available = ${?JIKKOU_KAFKA_BROKERS_WAIT_FOR_MIN_AVAILABLE}
      # The amount of time to wait before verifying that brokers are available.
      wait-for-retry-backoff-ms = 1000
      wait-for-retry-backoff-ms = ${?JIKKOU_KAFKA_BROKERS_WAIT_FOR_RETRY_BACKOFF_MS}
      # Wait until brokers are available or this timeout is reached.
      wait-for-timeout-ms = 60000
      wait-for-timeout-ms = ${?JIKKOU_KAFKA_BROKERS_WAIT_FOR_TIMEOUT_MS}
    }
  }

  # The regex patterns to use for including resources.
  include-resources = []
  # The regex patterns to use for excluding resources.
  exclude-resources = []

  # The default validation rules to apply on any specification files.
  validations = [
    {
      type = TopicConfigKeysValidation
      config = {}
    },
    {
      type = TopicNameRegexValidation
      config = {
        topic-name-regex = "[a-zA-Z0-9\\._\\-]+"
        topic-name-regex = ${?VALIDATION_DEFAULT_TOPIC_NAME_REGEX}
      }
    },
    {
      type = TopicMinNumPartitionsValidation
      config = {
        topic-min-num-partitions = 1
        topic-min-num-partitions = ${?VALIDATION_DEFAULT_TOPIC_MIN_NUM_PARTITIONS}
      }
    },
    {
      type = TopicMinReplicationFactorValidation
      config = {
        topic-min-replication-factor = 1
        topic-min-replication-factor = ${?VALIDATION_DEFAULT_TOPIC_MIN_REPLICATION_FACTOR}
      }
    }
  ]

  # The default transformations to apply on any specification files.
  transforms = []
}
```

{{% alert title="Tips" color="info" %}}
To debug the configuration use byu Jikkou, you can run the following command: `jikkou config get --comments`
{{% /alert %}}