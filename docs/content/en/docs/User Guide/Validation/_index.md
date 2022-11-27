---
categories: []
tags: ["feature", "extensions"] 
title: "Validations"
linkTitle: "Validations"
weight: 50
description: >
  Learn how to use the validations for ensuring resource entity configurations meets your requirements before being created and/or updated.
---

## Introduction

Jikkou allows running validation rules on declared resources (i.e. _Topics_, _Quotas_, _ACLs_, etc) before executing any action on your Kafka cluster.

To do that, the validations to be applied must be configured in your `application.conf`.

For example, the below configuration shows how to use the validation `io.streamthoughts.jikkou.kafka.validations.TopicNameRegexValidation` class 
to verify that all topic names match a given regex.

```hocon
jikkou {
    validations = [
        {
          # The fully-qualified name of the Validation class, e.g.:
          type = io.streamthoughts.jikkou.kafka.validations.TopicNameRegexValidation
          # The config values that will be passed to the Validation.
          config = {
            topic-regex = "[a-zA-Z0-9\\._\\-]+"
          }
        }
   ]
}
```

{{% alert title="About validation's configuration" color="info" %}}
The `config` object of a validation will always fallback on the top-level `jikkou` config. This allowed to declare some validation config properties globally.
{{% /alert %}}

## Built-in Validations

Jikkou ships with the following built-in _validations_:

### Topics

#### `NoDuplicateTopicsAllowedValidation`

#### `TopicConfigKeysValidation`

The `TopicConfigKeysValidation` allows checking if the specified topic configs are all valid.

**Configuration**

* `type`: `io.streamthoughts.jikkou.kafka.validations.TopicConfigKeysValidation`

#### `TopicMinNumPartitions`

The `TopicMinNumPartitions` allows checking if the specified number of partitions for a topic is not less than the minimum required.

**Configuration**

* `type`: `io.streamthoughts.jikkou.kafka.validations.TopicMinNumPartitionsValidation`
* `config`:
  *  `topic-min-num-partitions`: (default: `1`)

#### `TopicMaxNumPartitions`

The `TopicMaxNumPartitions` allows checking if the number of partitions for a topic is not greater than the maximum configured.

**Configuration**

* `type`: `io.streamthoughts.jikkou.kafka.validations.TopicMaxNumPartitions`
* `config`:
  *  `topic-max-num-partitions`:

#### `TopicMinReplicationFactor`

The `TopicMinReplicationFactor` allows checking if the specified replication factor for a topic is not less than the minimum required.

**Configuration**

* `type`: `io.streamthoughts.jikkou.kafka.validations.TopicMinReplicationFactor`
* `config`:
  * `topic-min-replication-factor`: (default: `1`)

#### `TopicMaxReplicationFactor`

The `TopicMaxReplicationFactor` allows checking if the specified replication factor for a topic is not greater than the maximum configured.

**Configuration**

* `type`: `io.streamthoughts.jikkou.kafka.validations.TopicMaxReplicationFactor`
* `config`:
  * `topic-max-replication-factor`: (default: `1`)

#### `TopicNamePrefix`

The `TopicNamePrefix` allows checking if the specified name for a topic starts with one of the configured suffixes.

**Configuration**

* `type`: `io.streamthoughts.jikkou.kafka.validations.TopicNamePrefix`
* `config`:
  * `topic-name-prefixes-allowed`: 

#### `TopicNameRegex`

The `TopicNameRegex` allows checking if the specified name for a topic matches the configured regex.

**Configuration**

#### `TopicNameSuffix`

The `TopicNameSuffix` allows checking  if the specified name for a topic ends with one of the configured suffixes.


* `type`: `io.streamthoughts.jikkou.kafka.validations.TopicNameSuffix`
* `config`:
  * `topic-name-suffixes-allowed`:

### ACLs

#### `NoDuplicateUsersAllowedValidation`
#### `NoDuplicateRolesAllowedValidation`

### Quotas

#### `QuotasEntityValidation`

## Default configuration

By default, Jikkou is configured with the following _validations_:

* `TopicConfigKeysValidation`
* `TopicMinNumPartitionsValidation`
* `TopicMinReplicationFactorValidation`
* `TopicNameRegexValidation`

You can easily override the config values for those validations by setting the following environment variables:

* `VALIDATION_DEFAULT_TOPIC_NAME_REGEX`
* `VALIDATION_DEFAULT_TOPIC_MIN_NUM_PARTITIONS`
* `VALIDATION_DEFAULT_TOPIC_MIN_REPLICATION_FACTOR`

## Validation Report

All validation rule errors are reported in the following form:

```text
Validation rule violations:
- [TopicNameRegex]: Name for topic 'my_topic' does not match the configured regex: [a-zA-Z0-9\.\-]+
- [TopicMinNumPartitions]: Number of partitions for topic 'my_topic' is less than the minimum required: 1 < 3
- [TopicMinReplicationFactor]: Replication factor for topic 'my-topic' is less than the minimum required: 1 < 3
```