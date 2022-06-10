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

### Implementation

The Jikkou client and library are written in the Java programming language.

It is built on top of the Kafka's [Java AdminClient](https://kafka.apache.org/30/javadoc/org/apache/kafka/clients/admin/Admin.html).
Thus, it works out-of-the-box with most the Apache Kafka distributions and cloud provider managed services (e.g., [Aiven](https://aiven.io/), [Confluent Cloud](https://confluent.cloud/), etc).

The tool is completely **stateless** and thus does not store any state. Basically: _Your kafka cluster is the state of Jikkou_.

Configuration files are written in [YAML](https://yaml.org/).

## CLI Usage

```bash
$ jikkou help

Usage: 
jikkou [-hV] [--bootstrap-servers=<bootstrapServer>] [--command-config=<clientCommandConfig>] [--config-file=<configFile>]
       [--command-property=<String=String>]... [COMMAND]

Description:

CLI to ease and automate Apache Kafka cluster configuration management.

Options:

      --bootstrap-servers=<bootstrapServer>
                  A list of host/port pairs to use for establishing the initial connection to the Kafka cluster.
      --command-config=<clientCommandConfig>
                  A property file containing configs to be passed to Admin Client (warning: this option is only relevant if your are using the AdminClient
                    based KafkaResourceManager).
      --command-property=<String=String>
                  A KEY=VALUE property to be passed to the Admin Client (warning: this option is only relevant if your are using the AdminClient based
                    KafkaResourceManager).
      --config-file=<configFile>
                  The configuration file.
  -h, --help      Show this help message and exit.
  -V, --version   Print version information and exit.

Commands:

  validate    Validate your specification file.
  topics      Apply the Topic changes described by your specs-file against the Kafka cluster you are currently pointing at.
  acls        Apply the ACLs changes described by your specs-file against the Kafka cluster you are currently pointing at.
  brokers     Apply the broker configuration changes described by your specs-file against the Kafka cluster you are currently pointing at.
  quotas      Apply the quotas changes described by your specs-file against the Kafka cluster you are currently pointing at.
  extensions  List all extensions available.
  config      Sets or retrieves the configuration of this client.
  help        Displays help information about the specified command
```

## Configuration

Jikkou uses the Java Admin client API for interacting with the target Apache Kafka cluster.

You can set the configs to be passed to Admin Client using the command-line arguments:
* `--command-config`: A property file containing configs to be passed to Admin Client
* `--command-property`: A KEY=VALUE property to be passed to the Admin Client.

Additionally, Jikkou will lookup for an [HOCON (Human-Optimized Config Object Notation)](https://github.com/lightbend/config) file named `application.conf` in the following locations:

* `./application.conf`
* `$HOME/.jikkou/application.conf`

Depending on the configuration properties you will override, Jikkou will always fallback to its reference default configuration (see [reference.conf](https://github.com/streamthoughts/jikkou/blob/master/src/main/resources/reference.conf)).

```hocon
jikkou {

  # The KafkaResourceManager classes and configurations used for managing kafka resources
  managers {
    kafka {
      brokers {
        type = io.streamthoughts.jikkou.api.manager.kafka.AdminClientKafkaBrokerManager
        config = {
            adminClient = ${jikkou.adminClient}
            kafka.brokers = ${jikkou.kafka.brokers}
        }
      }
      topics {
        type = io.streamthoughts.jikkou.api.manager.kafka.AdminClientKafkaTopicManager
        config = {
            adminClient = ${jikkou.adminClient}
            kafka.brokers = ${jikkou.kafka.brokers}
        }
      }

      acls {
        type = io.streamthoughts.jikkou.api.manager.kafka.AdminClientKafkaAclsManager
        config = {
            adminClient = ${jikkou.adminClient}
            kafka.brokers = ${jikkou.kafka.brokers}
        }
      }

      quotas {
        type = io.streamthoughts.jikkou.api.manager.kafka.AdminClientKafkaQuotasManager
        config = {
            adminClient = ${jikkou.adminClient}
            kafka.brokers = ${jikkou.kafka.brokers}
        }
      }
    }
  }

  # The default Kafka AdminClient configuration
  adminClient {
    bootstrap.servers = ${?JIKKOU_DEFAULT_KAFKA_BOOTSTRAP_SERVERS}
  }

  # The paths from which to load extensions
  extension.paths = [${?JIKKOU_EXTENSION_PATH}]

  kafka {
    brokers {
      # If 'True' 
      wait-for-enabled = true
      wait-for-enabled = ${?JIKKOU_KAFKA_BROKERS_WAIT_FOR_ENABLED}
      # The minimal number of broker that should be alive for the interceptor stops waiting.
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

  template {
    values {

    }
  }

  # The default validation rules to apply on any specification files.
  validations = [
    {
        type = io.streamthoughts.jikkou.api.validations.TopicConfigKeysValidation
        config = {}
    },
    {
      type = io.streamthoughts.jikkou.api.validations.TopicNameRegexValidation
      config = {
        topic-name-regex = "[a-zA-Z0-9\\._\\-]+"
        topic-name-regex = ${?VALIDATION_DEFAULT_TOPIC_NAME_REGEX}
      }
    },
    {
      type = io.streamthoughts.jikkou.api.validations.TopicMinNumPartitionsValidation
      config = {
        topic-min-num-partitions = 1
        topic-min-num-partitions = ${?VALIDATION_DEFAULT_TOPIC_MIN_NUM_PARTITIONS}
      }
    },
    {
      type = io.streamthoughts.jikkou.api.validations.TopicMinReplicationFactorValidation
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