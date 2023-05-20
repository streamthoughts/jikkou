---
tags: [ "concept", "feature" ]
title: "Selectors"
linkTitle: "Selectors"
weight: 4
---

{{% pageinfo color="info" %}}
You can use _selectors_ to select only a subset of resource objects to describe from your system or for which you want
to perform a reconciliation process.
{{% /pageinfo %}}

## Field Selector (default)

Jikkou provides the built-in `FieldSelector` that allows you to filter resource objects based on a _field key_.

### Selector Expression

The expression below shows you how to select only resource having a label `environement` equals to either `staging` or `production`.

```
metadata.labels.environement IN (staging,production)
```

### Expression Operators

Five kinds of operators are supported:

* **IN**
* **NOTIN**
* **EXISTS**
* **MATCHES**
* **DOESNOTMATCH**

{{% alert title="Using JIKKOU CLI" color="info" %}}
Selectors can be specified via the Jikkou CLI option: `--selector`.
{{% /alert %}}

