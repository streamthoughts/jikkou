---
tags: [ "concept", "feature", "extension" ]
title: "Jikkou Controllers"
linkTitle: "Controllers"
description: "Learn how Jikkou controllers compute and apply changes to reconcile resources in managed systems."
weight: 9
---

{{% pageinfo color="info" %}}
_**Controllers**_ are used to compute and apply changes required to reconcile resources into a managed
system.
{{% /pageinfo %}}

## Available Controllers

You can list all the available controllers using the Jikkou CLI command:

```bash
jikkou extensions list --type=Controller [-kinds <a resource kind to filter returned results>]
```