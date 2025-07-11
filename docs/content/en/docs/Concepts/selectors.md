---
tags: [ "concept", "feature" ]
title: "Selectors"
linkTitle: "Selectors"
weight: 4
---

{{% pageinfo color="info" %}}
**Selectors** allow you to include or exclude resource objects when returned or reconciled by Jikkou.
{{% /pageinfo %}}

## Selector Expressions

Selectors are passed to Jikkou using the `--selector` CLI option. 
They allow you to define filtering logic to target specific resources during execution.

Selector expressions follow one of the following formats:

- With an explicit selector:

```
<SELECTOR>: <EXPRESSION>
```

- Using the default `field` selector (shorthand):

```
<KEY> <OPERATOR> VALUE`
<KEY> <OPERATOR> (VALUE1[, VALUE2, ...])
```

> **Note:** The `field:` selector is the default, so it can be omitted when using field-based expressions.

## Supported Selectors

### Field (default)

The `field` selector filters resources based on arbitrary fields in the resource object.

```text
field: <KEY> <OPERATOR> VALUE
field: <KEY> <OPERATOR> (VALUE1, VALUE2, ...)
```

#### Examples

Select resources with a label `environment` equal to `staging` or `production`:

```
field: metadata.labels.environement IN (staging, production)
```

or omitting the selector:

```
 metadata.labels.environement IN (staging, production)
```

### Label

The `label` selector provides a convenient way to filter resources by their labels.
This is often simpler and more intuitive than referencing label fields directly via the `field` selector.

#### Examples

Select resources with a label `environment` equal to `staging` or `production`:

```
label: environement IN (staging, production)
```

### Expression (_since: Jikkou v0.36_)

The `expr` selector allows complex filtering using the [Common Expression Language (CEL)](https://cel.dev/), providing
powerful and expressive syntax for filtering by any resource attribute, including arrays and maps.

#### Examples

Select resources with the label `env` in `staging` or `production`:

```
expr: has(resource.metadata.labels.env) && resource.metadata.labels.env in ['staging', 'production']
```

Select Kafka topics with at least 12 partitions:

```
expr: resource.kind == 'KafkaTopic' && resource.spec.partitions >= 12
```

Select resources missing a specific annotation:

```
expr: !has(resource.metadata.annotations['mycompany.io/owner'])
```

{{% alert title="Tip" color="info" %}}
Use the `expr` selector when you need advanced filtering logic, or when targeting nested fields, maps, or arrays.
{{% /alert %}}

## Expression Operators

The following operators are supported for both `field` and `label` selectors:

| Operator       | Description                                                 |
|----------------|-------------------------------------------------------------|
| `IN`           | Match if the field/label value is in the given list         |
| `NOTIN`        | Match if the field/label value is **not** in the given list |
| `EXISTS`       | Match if the field/label is present                         |
| `MATCHES`      | Match if the field/label value matches a regular expression |
| `DOESNOTMATCH` | Match if the field/label value does **not** match the regex |

> Note: These operators are **not** available for the `expr` selector, which uses CEL syntax instead.

## Matching Strategies

When specifying multiple selectors, you can define how they are logically combined using the `--selector-match` option.

Available strategies:

- `ALL`: The resource must match **all** selector expressions (logical AND).
- `ANY`: The resource must match **at least one** selector (logical OR).
- `NONE`: The resource must **not match any** selector (logical NOT).

#### Examples

```bash
jikkou get kafkatopics \
--selector 'metadata.name MATCHES (^__.*)' \
--selector 'metadata.name IN (_schemas)' \
--selector-match ANY
```



