---
categories: [ ]
tags: [ "feature", "resources" ]
title: "Kafka Table Records"
linkTitle: "Records"
weight: 30
description: >
  Learn how to manage a KTable Topic Records
---

{{% pageinfo color="info" %}}
A KafkaTableRecord resource can be used to produce a key/value record into a given _compacted_ topic, i.e., a topic
with `cleanup.policy=compact` (a.k.a. _KTable_).
{{% /pageinfo %}}

## `KafkaTableRecord`

### Specification

Here is the _resource definition file_ for defining a `KafkaTableRecord`.

```yaml
apiVersion: "kafka.jikkou.io/v1beta1" # The api version (required)
kind: "KafkaTableRecord"              # The resource kind (required)
metadata:
  name: <string>                      # The topic name (required)        
  labels: { }
  annotations: { }
spec:
  headers: # The list of headers
    - name: <string>
      value: <string>
  key:    # The record-key (required)
    type: <string>                   # The record-key type. Must be one of: BINARY, STRING, JSON (required)
    data:                            # The record-key in JSON serialized form.
      $ref: <url or path>            # Or an url to a local file containing the JSON string value.
  value:  # The record-value (required)
    type: <string>                   # The record-value type. Must be one of: BINARY, STRING, JSON (required)
    data:                            # The record-value in JSON serialized form.
      $ref: <url or path>            # Or an url to a local file containing the JSON string value.
```

### Usage

The `KafkaTableRecord` resource has been designed primarily to manage reference data published and shared via Kafka. Therefore, it
is highly recommended to use this resource only with compacted Kafka topics containing a small amount of data.

### Examples

Here are some examples that show how to a `KafkaTableRecord` using the different supported data type.

**STRING:**

```yaml
---
apiVersion: "kafka.jikkou.io/v1beta1"
kind: "KafkaTableRecord"
metadata:
  # The name of the kafka table topic.
  name: "my-topic"
spec:
  headers:
    - name: "content-type"
      value: "application/text"
  key:
    type: STRING
    data: |
      "bar"
  value:
    type: STRING
    data: |
      "foo"
```

**JSON:**

```yaml
---
apiVersion: "kafka.jikkou.io/v1beta1"
kind: "KafkaTableRecord"
metadata:
  # The name of the kafka table topic.
  name: "my-topic"
spec:
  headers:
    - name: "content-type"
      value: "application/text"
  key:
    type: STRING
    data: |
      "bar"
  value:
    type: JSON
    data: |
      {
        "foo": "bar"
      }
```

**BINARY:**

```yaml
---
apiVersion: "kafka.jikkou.io/v1beta1"
kind: "KafkaTableRecord"
metadata:
  # The name of the kafka table topic.
  name: "my-topic"
spec:
  headers:
    - name: "content-type"
      value: "application/text"
  key:
    type: STRING
    data: |
      "bar"
  value:
    type: JSON
    data: |
      "eyJmb28iOiAiYmFyIn0K"
```