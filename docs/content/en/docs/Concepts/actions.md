---
categories: [ ]
tags: [ "feature", "extensions" ]
title: "Actions"
linkTitle: "Actions"
weight: 12
---

{{% pageinfo color="info" %}}
**_Actions_** allow a user to execute a specific and one-shot operation on resources.
{{% /pageinfo %}}

## Available Actions (CLI)

You can list all the available actions using the Jikkou CLI command:

```bash
jikkou api-extensions list --category=action [-kinds <a resource kind to filter returned results>]
```

## Execution Actions (CLI)

You can execute a specific extension using the Jikkou CLI command:

```bash
jikkou action <ACTION_NAME> execute [<options>]
```
