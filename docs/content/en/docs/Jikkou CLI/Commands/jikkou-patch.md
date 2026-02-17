---
title: "jikkou patch"
linkTitle: "jikkou patch"
---

Execute all changes for the specified reconciliation mode.

## Synopsis

Reconcile resources by applying all the changes as defined in the resource descriptor files passed through the
arguments. Unlike the other reconciliation commands (`apply`, `create`, `update`, `delete`), `patch` requires you to
explicitly specify the reconciliation mode using the `--mode` flag.

The `patch` command applies changes to existing resources on the platform by computing a diff between the current
and desired states, then executing only the operations allowed by the selected mode.

```bash
jikkou patch --mode <mode> [flags]
```

## Examples

```bash
# Patch resources using FULL reconciliation mode
jikkou patch --mode FULL -f my-resources.yaml

# Patch resources - create only
jikkou patch --mode CREATE -f my-resources.yaml

# Patch resources - update only
jikkou patch --mode UPDATE -f my-resources.yaml

# Preview patches without applying
jikkou patch --mode FULL -f my-resources.yaml --dry-run

# Patch with a specific provider
jikkou patch --mode UPDATE -f my-resources.yaml --provider kafka-prod
```

## Options

| Flag | Short | Default | Description |
|------|-------|---------|-------------|
| `--mode` | | | **(required)** The reconciliation mode. Valid values: CREATE, DELETE, UPDATE, FULL |
| `--files` | `-f` | | Resource definition file or directory locations (one or more required) |
| `--file-name` | `-n` | `**/*.{yaml,yml}` | Glob pattern to filter resource files when using directories |
| `--values-files` | | | Template values file locations (one or more) |
| `--values-file-name` | | `**/*.{yaml,yml}` | Glob pattern to filter values files |
| `--set-label` | `-l` | | Set labels on resources (key=value, repeatable) |
| `--set-annotation` | | | Set annotations on resources (key=value, repeatable) |
| `--set-value` | `-v` | | Set template variables for the built-in `Values` object (key=value, repeatable) |
| `--selector` | `-s` | | Selector expression for including or excluding resources |
| `--selector-match` | | `ALL` | Selector matching strategy. Valid values: ALL, ANY, NONE |
| `--options` | | | Controller configuration options (key=value, repeatable) |
| `--provider` | | | Select a specific provider instance |
| `--output` | `-o` | `TEXT` | Output format. Valid values: TEXT, COMPACT, JSON, YAML |
| `--pretty` | | `false` | Pretty print JSON output |
| `--dry-run` | | `false` | Execute command in dry-run mode (preview changes without applying) |

## Options inherited from parent commands

| Flag | Description |
|------|-------------|
| `--logger-level=<level>` | Log level: TRACE, DEBUG, INFO, WARN, ERROR |

## SEE ALSO

- [jikkou apply](../jikkou-apply) - Apply all changes (FULL mode)
- [jikkou replace](../jikkou-replace) - Replace resources by deleting and recreating
- [jikkou diff](../jikkou-diff) - Show changes without applying
