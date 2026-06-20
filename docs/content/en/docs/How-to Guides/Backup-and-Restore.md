---
title: "Back Up and Restore Configuration"
linkTitle: "Back Up & Restore"
weight: 50
description: >
  Export the current state of a cluster to YAML for backup or replication, then restore it elsewhere.
---

Because Jikkou is stateless and uses your platform as the source of truth, you can export the live state
of any managed resource to YAML. Those files can be version-controlled, used as a backup, or applied to
another cluster to replicate its configuration.

## Before you begin

* A Jikkou context configured for the source cluster — see [Getting Started]({{% relref "/docs/Tutorials/get_started.md" %}}).

## 1. Export resources to a file

Use `jikkou get <kind>` with YAML output and redirect it to a file:

```bash
jikkou get kafkatopics -o yaml > topics-backup.yaml
```

The same pattern works for any managed resource kind. List the kinds available in your context — and
the exact names accepted by `get` — with:

```bash
jikkou api-resources
```

Then export each kind to build a full snapshot, for example:

```bash
jikkou get kafkatopics -o yaml > topics-backup.yaml
jikkou get kafkaprincipalauthorizations -o yaml > acls-backup.yaml
```

{{% alert title="Tip" color="info" %}}
Add `--default-configs` to `jikkou get kafkatopics` to include built-in default config values in the
export. Use `--selector` to back up only a subset of resources.
{{% /alert %}}

## 2. Restore or replicate

Point Jikkou at the target cluster (e.g. switch context with `jikkou config use-context <name>`), then
preview and apply the exported files:

```bash
jikkou apply --files ./topics-backup.yaml --dry-run   # review
jikkou apply --files ./topics-backup.yaml             # apply
```

This same flow lets you **replicate** configuration from one cluster to another: export from the source,
switch context, and apply to the target.

{{% alert title="Recommendation" color="warning" %}}
When restoring with `apply`, Jikkou reconciles the target to the declared state — resources present on
the target but absent from your files may be altered or deleted. Always run with `--dry-run` first and
scope changes with `--selector`.
{{% /alert %}}

## Automate periodic backups

Run the export commands on a schedule (e.g. a cron job or a CI pipeline) and commit the output to a Git
repository to keep a versioned history of your cluster configuration. See
[Automating]({{% relref "Automating" %}}) for running Jikkou in CI/CD.

## Related

* [`jikkou get` command reference]({{% relref "/docs/Reference/CLI/Commands/jikkou-get.md" %}})
* [Automating Jikkou in CI/CD]({{% relref "Automating" %}})
