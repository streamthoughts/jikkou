---
title: "Preview Changes and Detect Drift"
linkTitle: "Preview & Detect Drift"
weight: 2
description: >
  Learn how Jikkou reconciles desired state with reality — preview changes with diff, apply safely with
  dry-run, and detect configuration drift.
---

In this tutorial you'll learn the mental model at the heart of Jikkou: **reconciliation**. You declare
the *desired state* of your resources, and Jikkou compares it with the *actual state* on your cluster and
computes the changes needed to make them match.

By the end you'll know how to preview changes, apply them safely, and detect when a cluster has *drifted*
away from your declared configuration.

## Prerequisites

* You have completed the [Getting Started]({{% relref "get_started.md" %}}) tutorial.
* You have a running Kafka cluster and a configured Jikkou context.

## Step 1 — Declare a topic

Create a resource file describing a single topic:

_`file: orders.yaml`_

```yaml
apiVersion: "kafka.jikkou.io/v1beta2"
kind: "KafkaTopic"
metadata:
  name: "orders"
spec:
  partitions: 3
  replicationFactor: 1
  configs:
    cleanup.policy: "delete"
    retention.ms: 86400000
```

## Step 2 — Preview the plan with `diff`

Before changing anything, ask Jikkou what it *would* do. The `diff` command produces a speculative
reconciliation plan without touching the cluster:

```bash
jikkou diff -f ./orders.yaml
```

Because the topic doesn't exist yet, the plan reports a `CREATE` operation. `diff` is your safety net —
use it before every `apply`.

## Step 3 — Apply the change

Now create the topic:

```bash
jikkou apply -f ./orders.yaml
```

Run `diff` again:

```bash
jikkou diff -f ./orders.yaml
```

This time there are **no changes** — the actual state already matches your desired state. This is what
reconciliation looks like when everything is in sync.

## Step 4 — Detect drift

"Drift" happens when the cluster changes outside of Jikkou. Let's simulate it by editing your desired
state instead: change `retention.ms` to `3600000` and add a new config in `orders.yaml`:

```yaml
spec:
  partitions: 3
  replicationFactor: 1
  configs:
    cleanup.policy: "delete"
    retention.ms: 3600000          # changed
    max.message.bytes: 20971520    # added
```

Preview the drift:

```bash
jikkou diff -f ./orders.yaml
```

Jikkou now reports an `UPDATE` operation describing exactly which config keys differ. You can narrow the
output to specific operations:

```bash
jikkou diff -f ./orders.yaml --filter-change-op UPDATE
```

## Step 5 — Apply safely with `--dry-run`

`apply` also supports `--dry-run`, which runs the full plan without committing it — useful in scripts and
CI where you want the apply command's exact behavior:

```bash
jikkou apply -f ./orders.yaml --dry-run   # preview, no changes
jikkou apply -f ./orders.yaml             # commit the changes
```

## Step 6 — Understand deletion behavior

`apply` reconciles to the declared state. By default it will not delete topics that are missing from your
files unless you opt in. To delete the topic you created, add the delete annotation:

```yaml
apiVersion: "kafka.jikkou.io/v1beta2"
kind: "KafkaTopic"
metadata:
  name: "orders"
  annotations:
    jikkou.io/delete: true
spec:
  partitions: 3
  replicationFactor: 1
```

Preview, then apply:

```bash
jikkou diff -f ./orders.yaml          # shows a DELETE operation
jikkou apply -f ./orders.yaml --dry-run
jikkou apply -f ./orders.yaml
```

{{% alert title="Recommendation" color="warning" %}}
In production, always preview with `diff` or `--dry-run`, and scope changes with `--selector` so you only
affect the resources you intend to.
{{% /alert %}}

## What you learned

* Jikkou reconciles **desired state** (your YAML) with **actual state** (the cluster).
* `diff` previews the plan; `--dry-run` runs `apply` without committing.
* Re-running with no changes is a no-op — that's how you confirm there's no drift.
* Deletion is opt-in via the `jikkou.io/delete` annotation.

## Next steps

* Learn the [Reconciliation]({{% relref "/docs/Concepts/reconciliation.md" %}}) concept in depth.
* Generate many resources at once in the [Generate Topics with Templates]({{% relref "templating-topics.md" %}}) tutorial.
* Enforce rules automatically with [governance policies]({{% relref "/docs/How-to Guides/Enforce-Policies.md" %}}).
