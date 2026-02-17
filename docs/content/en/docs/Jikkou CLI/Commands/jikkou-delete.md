---
title: "jikkou delete"
linkTitle: "jikkou delete"
---

Delete resources that are no longer described by the resource definition files.

## Synopsis

Reconcile the target platform by deleting all existing resources that are no longer described by the resource definition
files passed as arguments. This command uses the **DELETE** reconciliation mode, meaning only deletions will be
performed. No resources will be created or updated.

```bash
jikkou delete [flags]
```

## Examples

```bash
# Delete resources no longer defined in a YAML file
jikkou delete -f my-resources.yaml

# Preview what would be deleted
jikkou delete -f my-resources.yaml --dry-run

# Delete resources from a directory
jikkou delete -f ./resources/

# Delete with selector to target specific resources
jikkou delete -f my-resources.yaml -s 'metadata.name IN (old-topic)'

# Delete resources targeting a specific provider
jikkou delete -f my-resources.yaml --provider kafka-dev
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
- [jikkou create](../jikkou-create) - Create resources only (CREATE mode)
- [jikkou update](../jikkou-update) - Create or update resources (UPDATE mode)
- [jikkou diff](../jikkou-diff) - Show changes without applying
