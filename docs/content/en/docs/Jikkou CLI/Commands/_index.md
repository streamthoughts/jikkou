---
title: "Commands"
linkTitle: "Commands"
weight: 3
description: >
  Comprehensive reference for all Jikkou CLI commands.
---

{{% pageinfo %}}
This section contains the complete reference for every Jikkou CLI command, including synopsis, options, usage examples, and related commands.
{{% /pageinfo %}}

You can also run `jikkou <command> --help` from the terminal to view built-in help for any command.

## Global Options

The following options are available on **all** commands:

| Flag | Description |
|------|-------------|
| `--logger-level=<level>` | Specify the log level verbosity. Valid values: `TRACE`, `DEBUG`, `INFO`, `WARN`, `ERROR` |
| `-h`, `--help` | Show help message and exit |
| `-V`, `--version` | Print version information and exit |

## Core Commands

Commands for reconciling and inspecting resources against your target platform.

### Reconciliation

These commands apply changes to your target platform based on resource definition files.
Each command corresponds to a specific [reconciliation mode]({{% relref "/docs/Concepts/reconciliation" %}}).

| Command | Mode | Description |
|---------|------|-------------|
| [jikkou apply](jikkou-apply) | `FULL` | Create, update, and delete resources to match the desired state |
| [jikkou create](jikkou-create) | `CREATE` | Create only non-existing resources |
| [jikkou update](jikkou-update) | `UPDATE` | Create new and update existing resources (no deletions) |
| [jikkou delete](jikkou-delete) | `DELETE` | Delete resources no longer described in definition files |
| [jikkou patch](jikkou-patch) | _(explicit)_ | Reconcile with an explicitly specified mode |
| [jikkou replace](jikkou-replace) | | Delete and recreate all resources from scratch |

### Inspect & Validate

| Command | Description |
|---------|-------------|
| [jikkou diff](jikkou-diff) | Preview resource changes without applying them |
| [jikkou get](jikkou-get) | Display one or many resources from the target platform |
| [jikkou validate](jikkou-validate) | Check resource definitions against all validation requirements |
| [jikkou prepare](jikkou-prepare) | Render templates and apply transformations without validating |

## System Management Commands

| Command | Description |
|---------|-------------|
| [jikkou action](jikkou-action) | List and execute provider actions |
| [jikkou health](jikkou-health) | Print or describe health indicators for target environments |
| [jikkou server-info](jikkou-server-info) | Display Jikkou API server information _(proxy mode only)_ |

## Configuration & Discovery Commands

| Command | Description |
|---------|-------------|
| [jikkou config](jikkou-config) | Manage CLI configuration contexts |
| [jikkou api-resources](jikkou-api-resources) | List the supported API resource types and their verbs |
| [jikkou api-extensions](jikkou-api-extensions) | List and inspect registered API extensions |
