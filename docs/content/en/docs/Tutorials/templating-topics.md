---
title: "Generate Topics with Templates"
linkTitle: "Generate Topics with Templates"
weight: 3
description: >
  Learn Jikkou's Jinja templating by generating many Kafka topics from a small values file.
---

Declaring resources one by one gets tedious when you manage dozens of similar topics across teams or
regions. In this tutorial you'll learn Jikkou's **templating** by generating a list of topics from a
compact data file — a single template plus a few values produces many resources.

By the end you'll understand the `values` variable, values files, and how to preview rendered output
before applying it.

## Prerequisites

* You have completed the [Getting Started]({{% relref "get_started.md" %}}) tutorial.
* You have a running Kafka cluster and a configured Jikkou context.

## Step 1 — Define your values

Templating separates *data* from *structure*. Put the data in a values file:

_`file: values.yaml`_

```yaml
topicPrefix: "iot-events"
partitions: 6
replicas: 1
countryCodes:
  - fr
  - be
  - de
  - es
```

## Step 2 — Write the template

Now write a template that loops over the data. Templates use [Jinja](https://jinja.palletsprojects.com/)
syntax, where `{{ ... }}` outputs a value and `{% ... %}` is a control statement. The values from your
file are available under the top-level `values` variable.

_`file: topics.tpl`_

```yaml
apiVersion: "kafka.jikkou.io/v1beta2"
kind: "KafkaTopicList"
items:
{% for country in values.countryCodes %}
  - metadata:
      name: "{{ values.topicPrefix }}-{{ country }}"
    spec:
      partitions: {{ values.partitions }}
      replicationFactor: {{ values.replicas }}
      configs:
        cleanup.policy: "delete"
{% endfor %}
```

## Step 3 — Preview the rendered resources

Always render and inspect the result before touching the cluster. The `validate` command renders the
template and prints the resulting resources:

```bash
jikkou validate --files ./topics.tpl --values-files ./values.yaml
```

You should see four fully-rendered topics — `iot-events-fr`, `iot-events-be`, `iot-events-de`, and
`iot-events-es` — each with 6 partitions.

## Step 4 — Apply the generated topics

Once you're happy with the output, preview the plan and apply it. The same `--values-files` flag is
accepted by `diff`, `apply`, `create`, and `update`:

```bash
jikkou diff  --files ./topics.tpl --values-files ./values.yaml   # preview
jikkou apply --files ./topics.tpl --values-files ./values.yaml   # apply
```

Add a country to `values.yaml`, re-run `diff`, and you'll see Jikkou plan a single new topic — without
touching the template.

## Step 5 — Override values from the command line

You can pass or override individual values with `--set-value` (`-v`), which is handy in CI pipelines:

```bash
jikkou apply --files ./topics.tpl \
  --values-files ./values.yaml \
  --set-value partitions=12
```

You can also read values from the environment inside the template — for example
`{{ system.env.TOPIC_PREFIX | default('iot-events', true) }}`.

## What you learned

* Templating separates **data** (values files) from **structure** (the template).
* `values` exposes your data; `{{ }}` outputs values and `{% %}` drives loops and conditions.
* Render and inspect with `validate` before applying.
* `--values-files` and `--set-value` work with `diff`, `apply`, and the other reconciliation commands.

## Next steps

* Read the [Templates]({{% relref "/docs/Concepts/template.md" %}}) concept for two-phase rendering, `ConfigMap`
  references, and advanced features.
* Manage multiple schemas the same way using a `SchemaRegistrySubjectList` — see
  [Manage Schemas]({{% relref "/docs/How-to Guides/Manage-Schemas.md" %}}).
