---
title: "Annotations"
linkTitle: "Annotations"
weight: 60
description: >
  Learn how to use the metadata annotations provided by the extensions for Confluent Cloud.
---

{{% pageinfo %}}
Here, you will find information about the annotations provided by the Confluent Cloud extension for Jikkou.
{{% /pageinfo %}}

### List of built-in annotations

#### `confluent.cloud/role-binding-id`

Used by Jikkou.

The annotation is automatically added by Jikkou to store the Confluent Cloud role binding ID (e.g., `rb-NBl9kE`).
This ID is used internally when deleting role bindings.
