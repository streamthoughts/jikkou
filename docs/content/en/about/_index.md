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

[Jikkou](https://github.com/streamthoughts/jikkou) is an open-source tool to help you automate the
management of the configurations that live on your [Apache Kafka](https://kafka.apache.org/documentation/) clusters.
It was developed by Kafka ❤️ to make daily operations on an Apache Kafka cluster simpler for both **developers** and **administrators**.

It can efficiently manage configuration changes for **Topics**, **ACLs**, **Quotas** and more with the use of **_resource specification files_**.
It is also applicable to quickly replicate the configuration of a production cluster to another with a few command lines or to initialize a new cluster for testing purpose.

<h3>The main usage scenarios</h3>

* Create new resource entities on an Apache Kafka cluster (i.e., _Topics_, _ACLs_, and _Quotas_).
* Update the configurations of existing resource entities.
* Delete resource entities which are not anymore managed.
* Describe all the configuration defined for Brokers.
* Describe all the configuration defined for _Topics_, _ACLs_, and _Quotas_.

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