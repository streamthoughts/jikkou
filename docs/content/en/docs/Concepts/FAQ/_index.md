---
title: "Frequently Asked Questions"
linkTitle: "FAQ"
weight: 99
description: >
aliases:
  - /docs/frequently-asked-questions/
---

{{% pageinfo %}}
This section regroups all frequently asked questions about Jikkou.
{{% /pageinfo %}}

### Is Jikkou Free to Use?
Yes, Jikkou is developed and distributed under the [Apache License 2.0](https://spdx.org/licenses/Apache-2.0.html).

###  Can I Use Jikkou with Any Kafka Implementation?
Yes, Jikkou can be used with a wide range of Apache Kafka infrastructures, including:

* [Apache Kafka](https://kafka.apache.org/)
* [Aiven](https://aiven.io/kafka)
* [Amazon MSK](https://aws.amazon.com/fr/msk/)
* [Confluent Cloud](https://www.confluent.io/confluent-cloud/)
* [Redpanda](https://redpanda.com/)

### Why would I use Jikkou over Terraform?

Use Terraform to provision Kafka infrastructure (clusters, networks, credentials); use Jikkou to manage
what lives inside Kafka (topics, ACLs, quotas, schemas, connectors), reconciled statelessly against the
real cluster instead of a state file.

Read the full comparison: [Jikkou vs Terraform]({{< relref "/docs/comparisons/jikkou-vs-terraform" >}}).