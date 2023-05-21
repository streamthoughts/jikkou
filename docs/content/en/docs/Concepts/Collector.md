---
tags: [ "concept", "feature", "extension" ]
title: "External Resource Collectors"
linkTitle: "Collectors"
weight: 8
---

{{% pageinfo color="info" %}}
_**ExternalResourceCollectors**_ are used to collect and describe all entities that exist into your system for a
specific resource type.
{{% /pageinfo %}}

## Available Collectors

You can list all the available collectors using the Jikkou CLI command:

```bash
jikkou extensions list --type=ExternalResourceCollector [-kinds <a resource kind to filter returned results>]
```