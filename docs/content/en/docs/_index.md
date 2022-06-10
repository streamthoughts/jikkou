
---
title: "Documentation"
linkTitle: "Documentation"
weight: 20
menu:
  main:
    weight: 20
---

## Welcome to Jikkou

Welcome to the Jikkou documentation. Jikkou is the tool to automate the management of the configurations that live on your Apache Kafka clusters.
This guide shows you how to get started managing configurations for Topics, ACLS, Quotas, and more in the simplest and most seamless way possible.

## Is Jikkou for me ?

Jikkou is particularly useful for small development teams that want to quickly automate the creation and updating of their topics without having to implement complex solutions.
But, it can be also very effective in larger contexts, where the configuration of your topics for all your projects are managed by a single centralized administration team.

In addition, it is built on top of the Kafka's [Java AdminClient](https://kafka.apache.org/30/javadoc/org/apache/kafka/clients/admin/Admin.html). 
Thus, it works out-of-the-box with most the Apache Kafka distributions and cloud provider managed services (e.g., [Aiven](https://aiven.io/), [Confluent Cloud](https://confluent.cloud/), etc).
However, you may find some limitations with some managed services for Apache Kafka depending on which APIs are allowed to be used. Indeed, some providers limit for example some config properties to be overloaded or use own ACL management mechanisms.





