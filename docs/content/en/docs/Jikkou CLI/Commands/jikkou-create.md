---
title: "jikkou create"
linkTitle: "jikkou create"
---

Create resources from the resource definition files (only non-existing resources will be created).

## Synopsis

Reconcile the target platform by creating all non-existing resources that are described by the resource definition files
passed as arguments. This command uses the **CREATE** reconciliation mode, meaning only new resources will be created.
Existing resources will not be updated or deleted.

```bash
jikkou create [flags]
```

## Examples

```bash
# Create resources defined in a YAML file
jikkou create -f my-resources.yaml

# Preview what would be created
jikkou create -f my-resources.yaml --dry-run

# Create resources from a directory
jikkou create -f ./resources/

# Create resources with template values
jikkou create -f my-resources.yaml --values-files values.yaml

# Create resources targeting a specific provider
jikkou create -f my-resources.yaml --provider kafka-dev
```

## Options

| Flag | Short | Default | Description |
|------|-------|---------|-------------|
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
- [jikkou update](../jikkou-update) - Create or update resources (UPDATE mode)
- [jikkou delete](../jikkou-delete) - Delete resources (DELETE mode)
- [jikkou diff](../jikkou-diff) - Show changes without applying
