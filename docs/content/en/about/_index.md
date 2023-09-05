---
title: About Jikkou
linkTitle: About
description: >
  What is Jikkou and why using it ?
weight: 20
menu:
  main:
    weight: 20
ui.breadcrumb_disable: false
---

{{< blocks/cover title="" image_anchor="bottom" height="min" class="text-left">}}
{{< page/header >}}
{{< /blocks/cover >}}

<div class="container l-container--padded">

<div class="row">
{{< page/toc collapsed=true placement="inline" >}}
</div>

<div class="row mt-5 mb-5">
<div class="col-12 col-lg-8">

**https://github.com/streamthoughts/jikkou[Jikkou]** (jikkō / 実行) is an open-source tool designed to provide an efficient and easy way to
manage, automate, and provision resource configurations for Kafka, Schema Registry, etc.

Developed by Kafka ❤️, Jikkou aims to streamline daily operations on https://kafka.apache.org/documentation/[Apache Kafka],
ensuring that platform governance is no longer a boring and tedious task for both **Developers** and **Administrators**.

Jikkou enables a declarative management approach of **Topics**, **ACLs**, **Quotas**, **Schema** and even more with the use of YAML files called **_Resource Definitions_**.

Taking inspiration from `kubectl` and Kubernetes resource definition files, Jikkou offers an intuitive and user-friendly approach to configuration management.

Jikkou can be used on on-premise Apache Kafka, https://aiven.io/kafka[Aiven], and https://www.confluent.io/confluent-cloud/[Confluent Cloud].

<h3>The main usage scenarios</h3>

* Create new resource entities for Apache Kafka (i.e., _Topics_, _ACLs_, and _Quotas_).
* Update the configurations of existing resource entities.
* Delete resource entities which are not anymore managed.
* Describe all the configuration defined for Brokers.
* Describe all the configuration defined for _Topics_, _ACLs_, and _Quotas_.
* Replicate configurations of a production cluster to another with a few command lines.
* Initialize a new cluster for testing purpose.

<h3>Core features that make it awesome</h3>

* Simple command line interface (CLI) for end user.
* Simple Java API on top of the Kafka's [Java AdminClient](https://kafka.apache.org/30/javadoc/org/apache/kafka/clients/admin/Admin.html).
* Completely stateless and thus does not store any state (Basically: _Your kafka cluster is the state of Jikkou_).
* Pluggable validation rules to ensure that resources meet your requirement before being created or updated ona target cluster.
* Pluggable resource manager to extend Jikkou with cloud managed services for Apache Kafka which are supported out-of-the-box.
* Simple templating mechanism using [Jinja](https://jinja.palletsprojects.com/en/3.0.x/) notation.

<h3>The story behind Jikkou</h3>
Jikkou was initially created as a side project to help the development teams to quickly re-create topics on Apache Kafka clusters used for testing purpose.
The goal was to ensure that environments was always cleanup and ready for running integration tests. Over time, new features have been added to Jikkou so that it can also be used by Kafka administrators on Kafka environments.


Today we continue to make the tool evolve in open-source because we find that the solutions that have appeared over time in the Kafka ecosystem do not respond as well to Kafka developers and administrators.
In addition, existing solutions are either designed to work only with Kubernetes or rely on dedicated services to manage the state of the solution.

<h3>Credits</h3>
Images used on this website documentation:

* Page Cover: Photo by [Anton Ivanov](https://unsplash.com/es/@bradanton?utm_source=unsplash&utm_medium=referral&utm_content=creditCopyText) on [Unsplash](https://unsplash.com/).



</div>
{{< page/toc placement="sidebar" >}}
</div>

{{< page/page-meta-links >}}

</div>