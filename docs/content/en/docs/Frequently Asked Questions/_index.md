---
title: "Frequently Asked Questions"
linkTitle: "Frequently Asked Questions"
weight: 10
description: >

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

#### What is Terraform and how is it typically used?

Terraform ([OpenToFu](https://opentofu.org/)) is widely recognized as the leading solution for infrastructure
provisioning and management. It is commonly used by operations teams for managing cloud infrastructure through
its HCL (HashiCorp Configuration Language) syntax.

#### What are the limitations of Terraform for Kafka Users ?

Many development teams find Terraform challenging to use because:

* They need to learn HCL syntax, which is not commonly known among developers.
* They often lack the necessary permissions to apply configuration files directly.
* They often struggle with Terraform states.

#### How does Jikkou address these limitations?

Jikkou is designed to be a straightforward CLI tool for both developers and operations teams. It simplifies the process
of managing infrastructure, especially for development teams who may not have expertise in HCL or the permissions
required for Terraform.

#### What are the benefits of using Jikkou for Kafka management?

* **On-Premises and Multi-Cloud Support**: Unlike many Terraform providers which focus on cloud-based Kafka services (
  e.g., Confluent Cloud), Jikkou supports on-premises, multi-cloud, and hybrid infrastructures.

* **Versatility**: Jikkou can manage Kafka topics across various environments, including local Kafka clusters in Docker,
  ephemeral clusters in Kubernetes for CI/CD, and production clusters in Aiven Cloud.

* **Auditing and Backup**: Beyond provisioning, Jikkou can audit Kafka platforms for configuration issues and create
  backups of Kafka configurations (Topics, ACLs, Quotas, etc.).
  
There are, of course, many reasons to use Terraform rather than Jikkou and vice versa. As usual, the choice of tool
really depends on your needs, the organization you're in, the skills of the people involved and so on.