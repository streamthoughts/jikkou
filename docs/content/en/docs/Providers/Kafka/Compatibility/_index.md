---
title: "Compatibility"
linkTitle: "Compatibility"
weight: 100
description: >
  Compatibility for Apache Kafka.
---

The Apache Kafka extension for Jikkou utilizes the [Kafka Admin Client](https://kafka.apache.org/documentation/#adminapi)
which is compatible with any Kafka infrastructure, such as :

* Aiven
* Apache Kafka
* Confluent Cloud
* MSK
* Redpanda
* etc.

In addition, [Kafka Protocol](https://kafka.apache.org/protocol.html) has a "bidirectional" client compatibility policy. 
In other words, new clients can talk to old servers, and old clients can talk to new servers.

