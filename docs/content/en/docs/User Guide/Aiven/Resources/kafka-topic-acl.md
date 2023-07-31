---
categories: [ ]
tags: [ "feature", "resources" ]
title: "ACL for Aiven Apache Kafka®"
linkTitle: "ACL for Apache Kafka® Topic"
weight: 10
description: >
  Learn how to manage Access Control Lists for Aiven Apache Kafka®
---

{{% pageinfo color="info" %}}
The `KafkaTopicAclEntry` resources are used to manage the Access Control Lists for Aiven Apache Kafka® service. A
`KafkaTopicAclEntry` resource defines the permission to be granted to a user for one or more kafka topics.
{{% /pageinfo %}}

## `KafkaTopicAclEntry`

### Specification

Here is the _resource definition file_ for defining a `KafkaTopicAclEntry`.

```yaml
---
apiVersion: "kafka.aiven.io/v1beta1"   # The api version (required)
kind: "KafkaTopicAclEntry"             # The resource kind (required)
metadata:
  labels: { }
  annotations: { }
spec:
  permission: <>               # The permission. Accepted values are: READ, WRITE, READWRITE, ADMIN
  username: <>                 # The username
  topic: <>                    # Topic name or glob pattern

```

### Example

Here is a simple example that shows how to define a single ACL entry using
the `KafkaTopicAclEntry` resource type.

_`file: kafka-topic-acl-entry.yaml`_

```yaml
---
apiVersion: "kafka.aiven.io/v1beta1"
kind: "KafkaTopicAclEntry"
metadata:
  labels: { }
  annotations: { }
spec:
  permission: "READWRITE"
  username: "alice"
  topic: "public-*"
```

## `KafkaTopicAclEntryList`

If you need to define multiple ACL entries (e.g. using a template), it may be easier to use a `KafkaTopicAclEntryList` resource.


### Specification

Here the _resource definition file_ for defining a `KafkaTopicList`.

```yaml
---
apiVersion: "kafka.aiven.io/v1beta1"    # The api version (required)
kind: "KafkaTopicAclEntryList"          # The resource kind (required)
metadata: # (optional)
  name: <The name of the topic>
  labels: { }
  annotations: { }
items: [ ]                             # An array of KafkaTopicAclEntry
```

### Example

Here is a simple example that shows how to define a single YAML file containing two ACL entry definitions using
the `KafkaTopicAclEntryList` resource type.

```yaml
---
apiVersion: "kafka.aiven.io/v1beta1"
kind: "KafkaTopicAclEntryList"
items:
  - spec:
      permission: "READWRITE"
      username: "alice"
      topic: "public-*"
  - spec:
      permission: "READ"
      username: "bob"
      topic: "public-*"
```