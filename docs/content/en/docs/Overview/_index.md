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

## What is Jikkou ?

Jikkou is a lightweight open-source tool designed to provide an efficient and easy way to manage, automate and provision
resources on **Event-Driven Data Mesh platforms** (or, more simply, on any **Apache Kafka Infrastructures**).

## How does Jikkou work ?

Jikkou adopts a stateless approach and thus does not store any state internally. Instead, it leverages your platforms 
or services as the source of truth. This design allows you to seamlessly integrate Jikkou with other solutions 
(such as Ansible, Terraform, etc.) or use it on an ad hoc basis for specific needs, making it incredibly flexible and versatile.

{{< figure src="./jikkou-how-it-works.svg" width="80%" class="center" >}}

## Why Jikkou ? The Story Behind Jikkou.

Jikkou was originally created as a side project to help development teams quickly recreate topics on Apache Kafka
clusters used for testing purposes (the project was called _Kafka Specs_). At the time, the aim was to ensure
that environments were always clean and ready to run integration tests.

But over time, new features were added, and so Jikkou was born with the aim to streamline day-to-day operations
on Apache Kafka, ensuring that platform governance is no longer a tedious and boring task for both developers and
administrators.

Today, Jikkou is used in production on several projects, providing efficient management of Kafka resources through
a GitOps approach. The project continues to evolve as an open-source project, as the solutions that have
appeared over time in the Kafka ecosystem do not meet the needs of Kafka developers and administrators as well.
In fact, existing solutions are either designed to work only with Kubernetes, or rely on dedicated services to manage
the state of the solution.

Now, we sincerely believe that Jikkou can play a role in bootstrapping a _self-service platform_ for a Data Mesh 
organization, by unifying the way to manage the various assets required to create and manage a Data Product 
exposed through Apache Kafka.

## Is Jikkou for me ?

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
