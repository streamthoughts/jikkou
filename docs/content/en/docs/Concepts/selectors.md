---
tags: [ "concept", "feature" ]
title: "Selectors"
linkTitle: "Selectors"
weight: 4
---

{{% pageinfo color="info" %}}
**Selectors** allow you to include or exclude resource objects when returned or reconciled by Jikkou.
{{% /pageinfo %}}

## Overview

Selectors filter which resources Jikkou operates on. They are passed via the `--selector` (or `-s`) CLI option and
can be used with any command that accepts resources, including `get`, `apply`, `create`, `update`, `delete`, `diff`,
`validate`, and `prepare`.

You can specify multiple selectors by repeating the `--selector` flag, and control how they are combined using the
`--selector-match` option.

## Selector Expression Syntax

A selector expression follows one of two formats:

- With an explicit selector type prefix:

```
<SELECTOR_TYPE>: <EXPRESSION>
```

- Using the default `field` selector (the prefix can be omitted):

```
<KEY> <OPERATOR> <VALUE>
<KEY> <OPERATOR> (VALUE1, VALUE2, ...)
```

## Supported Selectors

### Field (default)

The `field` selector filters resources based on any field in the resource object, using dot-notation to navigate
nested properties.

```text
field: <KEY> <OPERATOR> <VALUE>
field: <KEY> <OPERATOR> (VALUE1, VALUE2, ...)
```

Since `field` is the default selector type, the `field:` prefix can be omitted.

#### Examples

```bash
# Select resources by name
jikkou get kafkatopics -s 'metadata.name IN (my-topic, other-topic)'

# Select by kind
jikkou get kafkatopics -s 'kind IN (KafkaTopic)'

# Select by label (using field selector)
jikkou get kafkatopics -s 'metadata.labels.environment IN (staging, production)'

# Select topics matching a regex pattern
jikkou get kafkatopics -s 'metadata.name MATCHES (^public-.*)'

# Exclude internal topics
jikkou get kafkatopics -s 'metadata.name DOESNOTMATCH (^__.*)'

# With explicit field: prefix (equivalent to above)
jikkou get kafkatopics -s 'field: metadata.name MATCHES (^public-.*)'
```

### Label

The `label` selector provides a shorthand to filter resources by their metadata labels, without needing the full
`metadata.labels.` path prefix.

```text
label: <LABEL_KEY> <OPERATOR> <VALUE>
label: <LABEL_KEY> <OPERATOR> (VALUE1, VALUE2, ...)
```

#### Examples

```bash
# Select resources with a specific label value
jikkou get kafkatopics -s 'label: environment IN (staging, production)'

# Select resources that have a specific label (regardless of value)
jikkou get kafkatopics -s 'label: team EXISTS'

# Select resources that do NOT have a specific label
jikkou get kafkatopics -s 'label: deprecated DOESNOTEXIST'
```

### Expression _(since Jikkou v0.36)_

The `expr` selector enables complex filtering using the
[Common Expression Language (CEL)](https://cel.dev/). This is the most powerful selector type, supporting conditions
on nested fields, arrays, maps, and computed expressions.

The resource being evaluated is available as the `resource` variable.

```text
expr: <CEL_EXPRESSION>
```

#### Examples

```bash
# Select resources with a label value in a list
jikkou get kafkatopics \
  -s 'expr: has(resource.metadata.labels.env) && resource.metadata.labels.env in ["staging", "production"]'

# Select Kafka topics with at least 12 partitions
jikkou get kafkatopics \
  -s 'expr: resource.kind == "KafkaTopic" && resource.spec.partitions >= 12'

# Select resources missing a specific annotation
jikkou get kafkatopics \
  -s 'expr: !has(resource.metadata.annotations["mycompany.io/owner"])'
```

{{% alert title="Tip" color="info" %}}
Use the `expr` selector when you need advanced filtering logic that goes beyond simple key-operator-value matching,
such as numeric comparisons, boolean combinations, or presence checks on nested maps.
{{% /alert %}}

## Expression Operators

The following operators are available for `field` and `label` selectors:

| Operator        | Description                                                  |
|-----------------|--------------------------------------------------------------|
| `IN`            | Match if the value is in the given list                      |
| `NOTIN`         | Match if the value is **not** in the given list              |
| `EXISTS`        | Match if the field or label exists                           |
| `DOESNOTEXIST`  | Match if the field or label does **not** exist               |
| `MATCHES`       | Match if the value matches a regular expression              |
| `DOESNOTMATCH`  | Match if the value does **not** match the regular expression |

{{% alert title="Note" color="info" %}}
These operators are **not** available for the `expr` selector, which uses
[CEL syntax](https://cel.dev/) instead.
{{% /alert %}}

## Matching Strategies

When specifying multiple selectors, use `--selector-match` to control how they are combined:

| Strategy | Behavior                                                   |
|----------|------------------------------------------------------------|
| `ALL`    | The resource must match **all** selectors (logical AND).   |
| `ANY`    | The resource must match **at least one** selector (logical OR). |
| `NONE`   | The resource must match **none** of the selectors (logical NOT). |

The default strategy is `ALL`.

### Examples

```bash
# Match resources whose name starts with "__" OR is exactly "_schemas" (ANY)
jikkou get kafkatopics \
  --selector 'metadata.name MATCHES (^__.*)' \
  --selector 'metadata.name IN (_schemas)' \
  --selector-match ANY

# Match resources that have both labels (ALL - default)
jikkou get kafkatopics \
  -s 'label: environment IN (production)' \
  -s 'label: team EXISTS'

# Exclude resources matching any selector (NONE)
jikkou get kafkatopics \
  -s 'metadata.name MATCHES (^__.*)' \
  -s 'metadata.name IN (_schemas)' \
  --selector-match NONE
```

## SEE ALSO

- [jikkou get]({{% relref "../Jikkou CLI/Commands/jikkou-get" %}}) - Display resources with selectors
- [jikkou apply]({{% relref "../Jikkou CLI/Commands/jikkou-apply" %}}) - Apply resources with selectors
- [jikkou diff]({{% relref "../Jikkou CLI/Commands/jikkou-diff" %}}) - Diff resources with selectors
- [Labels and annotations]({{% relref "./labels-and-annotations" %}}) - Using labels for resource organization
