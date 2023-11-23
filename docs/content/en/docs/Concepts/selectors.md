---
tags: [ "concept", "feature" ]
title: "Selectors"
linkTitle: "Selectors"
weight: 4
---

{{% pageinfo color="info" %}}
**Selectors** allows you to include or exclude some resource objects from being returned or reconciled by Jikkou.
{{% /pageinfo %}}

## Selector Expressions

Selectors are passed as arguments to Jikkou as expression strings in the following form: 

* `<SELECTOR>: <KEY> <OPERATOR> VALUE`
* `<SELECTOR>: <KEY> <OPERATOR> (VALUE[, VALUES])`

or (using default field selector):

* `<KEY> <OPERATOR> VALUE`
* `<KEY> <OPERATOR> (VALUE[, VALUES])`

### Selectors

#### Field (default)

Jikkou packs with a built-in `FieldSelector` allowing to filter resource objects based on a _field key_.

For example, the expression below shows you how to select only resource having a label `environement` equals to
either `staging` or `production`.

```
metadata.labels.environement IN (staging, production)
```

_Note: In the above example, we have omitted the selector because `field` is the default selector._

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

### Matching Strategies

Jikkou allows you to use multiple selector expressions. To indicate how these expressions are to be combined, you can pass one of the following matching strategies:

* `ALL`: A resource is selected if it matches all selectors.
* `ANY`: A resource is selected if it matches one of the selectors.
* `NONE`: A resource is selected if it matches none of the selectors.

*Example:*

```bash
jikkou get kafkatopics \
--selector 'metadata.name MATCHES (^__.*)' \
--selector 'metadata.name IN (_schemas)' \
--selector-match ANY
```

