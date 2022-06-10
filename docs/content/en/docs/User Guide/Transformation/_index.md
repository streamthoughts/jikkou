---
categories: []
tags: ["feature", "extensions"] 
title: "Transformations"
linkTitle: "Transformations"
weight: 60
description: >
  Learn how to use the transformations for modifying resource entity configurations before being created and/or updated.
---

## Introduction

Jikkou allows you to plug custom transformations on declared resources (i.e. _Topics_, _Quotas_, _ACLs_, etc) before executing any action on your Kafka cluster and before executing validation rules.

## Configuration

To do that, you should implement the `io.streamthoughts.kafka.specs.transforms.Transformation` interface transformations.
Then, transformation must be configured in your `application.conf` file as follows:

```hocon
jikkou {
    transformations = [
        {
          # The fully-qualified name of the Transformation class, e.g.:
          type = ...
          # The config values that will be passed to the Transformation.
          config = {}
        }
   ]
}
```

## Built-in Transformations

