---
title: "Overview"
linkTitle: "Overview"
description: "What is Jikkou ?"
weight: 1
menu:
  main:
    weight: 10
ui.breadcrumb_disable: false
---

{{% pageinfo %}}
Welcome to the Jikkou documentation! Jikkou, means “execution (e.g. of a plan) or actual state (of things)” in Japanese.
{{% /pageinfo %}}

## What Is Jikkou ?

Jikkou is a powerful, flexible open-source framework that enables self-serve resource provisioning.
It allows developers and DevOps teams to easily manage, automate, and provision all the resources needed for their
**Apache Kafka&reg;** platform.

Jikkou was born with the aim to streamline day-to-day operations on Apache Kafka&reg;, ensuring that platform governance is
no longer a tedious and boring task for both developers and administrators.

## What Are The Use-Cases ?


Jikkou is primarily used as a GitOps solution for Kafka configuration management.

Here are some of the various use cases we've observed in different projects:

* **Topic as a Service**: Build a self-serve platform for managing Kafka topics.
* **ACL Management**: Centrally manage all ACLs of an Apache Kafka cluster.
* **Kafka Connectors Management**: Deploy and manage Kafka Connect connectors.
* **Ad Hoc Changes**: Apply ad hoc changes as needed.
* **Audit**: Easily check configurations of topics, brokers, or identify divergences between different environments.
* **Kafka Configuration Backup**: Periodically export all critical configurations of your Kafka cluster.
* **Configuration Replication**: Replicate the Kafka configuration from one cluster to another.

## How Does Jikkou Work ?

Jikkou offers flexibility in deployment, functioning either as a simple CLI (Command Line Interface) or as a REST
server, based on your requirements.

By adopting a stateless approach, Jikkou does not store any internal state. Instead, it leverages your platforms or
services as the source of truth. This design enables seamless integration with other solutions, such as Ansible and
Terraform, or allows for ad hoc use for specific tasks, making Jikkou incredibly flexible and versatile.

{{< figure src="./jikkou-architecture-overview.svg" width="80%" class="center" >}}

## Is Jikkou For Me ?

Jikkou can be implemented regardless of the size of your team or data platform.

### Small Development Team

Jikkou is particularly useful for small development teams looking to quickly automate the creation and
maintenance of their topics without having to implement a complex solution that requires learning a new technology
or language.

### Centralized Infrastructure (DevOps) Team

Jikkou can be very effective in larger contexts, where the configuration of your Kafka Topics, ACLs, and Quotas
for all your data platform is managed by a single and centralized devops team.

### Decentralized Data Product Teams

In an organization adopting Data Mesh principles, Jikkou can be leveraged in a decentralized way by each of your
Data Teams to manage all the assets (e.g. Topics, ACLs, Schemas, Connectors, etc.) necessary to expose and manage
their Data Products.

## Can I Use Jikkou with my Apache Kafka vendor ?

Jikkou can be used any Apache Kafka infrastructures, including:

* [Apache Kafka](https://kafka.apache.org/)
* [Aiven](https://aiven.io/kafka)
* [Amazon MSK](https://aws.amazon.com/fr/msk/)
* [Confluent Cloud](https://www.confluent.io/confluent-cloud/)
* [Redpanda](https://redpanda.com/)
