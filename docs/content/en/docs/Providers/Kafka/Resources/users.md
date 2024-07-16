---
categories: [ ]
tags: [ "feature", "resources" ]
title: "Kafka Users"
linkTitle: "Users"
weight: 10
description: >
  Learn how to manage Kafka Users.
---

{{% pageinfo color="info" %}}
This section describes the resource definition format for `KafkaUser` entities, which can be used to 
manage SCRAM Users for Apache Kafka.
{{% /pageinfo %}}

## Definition Format of `KafkaUser`

Below is the overall structure of the `KafkaUser` resource.

```yaml
---
apiVersion: kafka.jikkou.io/v1  # The api version (required)
kind: KafkaUser                 # The resource kind (required)
metadata:
  name: <string>
  annotations:
    # force update
    kafka.jikkou.io/force-password-renewal: <boolean>
spec:
  authentications:
    - type: <enum> # or 
      password: <string>  # leave empty to generate secure password
```

See below for details about all these fields.

### Metadata

#### `metadata.name` [required]

The name of the User.

#### `kafka.jikkou.io/force-password-renewal` [optional]

### Specification

#### `spec.authentications` [required]

The list of authentications to manage for the user.

#### `spec.authentications[].type` [required]

The authentication type:

* `scram-sha-256`
* `scram-sha-512`

#### `spec.authentications[].password` [required]

The password of the user.

### Examples

The following is an example of a resource describing a User:

```yaml
---
# Example: file: kafka-scram-users.yaml
apiVersion: "kafka.jikkou.io/v1"
kind: "User"
metadata:
  name: "Bob"
spec:
  authentications:
    - type: scram-sha-256
      password: null
    - type: scram-sha-512
      password: null
```

## Listing `Kafka Users`

You can retrieve the SCRAM users of a Kafka cluster using the `jikkou get kafkausers` (or `jikkou get ku`) command.

### Usage

```bash
$ jikkou get kc --help

Usage:

Get all 'KafkaUser' resources.

jikkou get kafkausers [-hV] [--list] [--logger-level=<level>] [--name=<name>]
                      [-o=<format>]
                      [--selector-match=<selectorMatchingStrategy>]
                      [-s=<expressions>]...

DESCRIPTION:

Use jikkou get kafkausers when you want to describe the state of all resources
of type 'KafkaUser'.

OPTIONS:

  -h, --help              Show this help message and exit.
      --list              Get resources as ResourceListObject (default: false).
      --logger-level=<level>
                          Specify the log level verbosity to be used while
                            running a command.
                          Valid level values are: TRACE, DEBUG, INFO, WARN,
                            ERROR.
                          For example, `--logger-level=INFO`
      --name=<name>       The name of the resource.
  -o, --output=<format>   Prints the output in the specified format. Valid
                            values: JSON, YAML (default: YAML).
  -s, --selector=<expressions>
                          The selector expression used for including or
                            excluding resources.
      --selector-match=<selectorMatchingStrategy>
                          The selector matching strategy. Valid values: NONE,
                            ALL, ANY (default: ALL)
  -V, --version           Print version information and exit.

```

(The output from your current Jikkou version may be different from the above example.)

### Examples

(command)

```bash
$ jikkou get ku
```

(output)

```yaml
apiVersion: "kafka.jikkou.io/v1"
kind: "KafkaUser"
metadata:
  name: "Bob"
  labels: {}
  annotations:
    kafka.jikkou.io/cluster-id: "xtzWWN4bTjitpL3kfd9s5g"
spec:
  authentications:
    - type: "scram-sha-256"
      iterations: 8192
    - type: "scram-sha-512"
      iterations: 8192
```