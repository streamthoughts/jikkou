---
categories: []
tags: ["feature", "extensions"] 
title: "Validations"
linkTitle: "Validations"
weight: 50
description: >
  Learn how to use the built-in validations provided by the extensions for Apache Kafka.
---

Jikkou ships with the following built-in _validations_:

## Topics

### `NoDuplicateTopicsAllowedValidation`

(_auto registered_)

### `TopicConfigKeysValidation`

(_auto registered_)

```hocon
type = io.streamthoughts.jikkou.kafka.validation.TopicConfigKeysValidation
```

The `TopicConfigKeysValidation` allows checking if the specified topic configs are all valid.

### `TopicMinNumPartitions`

```hocon
type = io.streamthoughts.jikkou.kafka.validation.TopicMinNumPartitionsValidation
```

The `TopicMinNumPartitions` allows checking if the specified number of partitions for a topic is not less than the minimum required.

**Configuration**

| Name                    | Type | Description                          | Default |
|-------------------------|------|--------------------------------------|---------|
| `topicMinNumPartitions` | Int  | Minimum number of partitions allowed |         |


### `TopicMaxNumPartitions`

```hocon
type = io.streamthoughts.jikkou.kafka.validation.TopicMaxNumPartitions
```

The `TopicMaxNumPartitions` allows checking if the number of partitions for a topic is not greater than the maximum configured.

**Configuration**

| Name                    | Type | Description                          | Default |
|-------------------------|------|--------------------------------------|---------|
| `topicMaxNumPartitions` | Int  | Maximum number of partitions allowed |         |

### `TopicMinReplicationFactor`

```hocon
type = io.streamthoughts.jikkou.kafka.validation.TopicMinReplicationFactor
```

The `TopicMinReplicationFactor` allows checking if the specified replication factor for a topic is not less than the minimum required.

**Configuration**

| Name                        | Type | Description                        | Default |
|-----------------------------|------|------------------------------------|---------|
| `topicMinReplicationFactor` | Int  | Minimum replication factor allowed |         |

### `TopicMaxReplicationFactor`

```hocon
type = io.streamthoughts.jikkou.kafka.validation.TopicMaxReplicationFactor
```

The `TopicMaxReplicationFactor` allows checking if the specified replication factor for a topic is not greater than the maximum configured.

**Configuration**

| Name                        | Type | Description                        | Default |
|-----------------------------|------|------------------------------------|---------|
| `topicMaxReplicationFactor` | Int  | Maximum replication factor allowed |         |


### `TopicNamePrefix`

```hocon
type = io.streamthoughts.jikkou.kafka.validation.TopicNamePrefix
```

The `TopicNamePrefix` allows checking if the specified name for a topic starts with one of the configured suffixes.

**Configuration**

| Name                | Type | Description                        | Default |
|---------------------|------|------------------------------------|---------|
| `topicNamePrefixes` | List | List of topic name prefixes allows |         |


#### `TopicNameSuffix`

```hocon
type = io.streamthoughts.jikkou.kafka.validation.TopicNameSuffix
```

The `TopicNameSuffix` allows checking  if the specified name for a topic ends with one of the configured suffixes.

**Configuration**

| Name                | Type | Description                        | Default |
|---------------------|------|------------------------------------|---------|
| `topicNameSuffixes` | List | List of topic name suffixes allows |         |


## ACLs

### `NoDuplicateUsersAllowedValidation`
(auto registered)

### `NoDuplicateRolesAllowedValidation`
(auto registered)

## Quotas

### `QuotasEntityValidation`
(auto registered)
