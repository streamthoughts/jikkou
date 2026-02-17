---
title: "jikkou update"
linkTitle: "jikkou update"
---

Create or update resources from the resource definition files.

## Synopsis

Reconcile the target platform by creating or updating resources that are described by the resource definition files
passed as arguments. This command uses the **UPDATE** reconciliation mode, meaning new resources will be created and
existing resources will be updated. Resources that exist on the platform but are not described in the resource files
will not be deleted.

```bash
jikkou update [flags]
```

## Examples

```bash
# Update resources defined in a YAML file
jikkou update -f my-resources.yaml

# Preview what would be created or updated
jikkou update -f my-resources.yaml --dry-run

# Update resources from a directory
jikkou update -f ./resources/

# Update resources with selector to filter specific resources
jikkou update -f my-resources.yaml -s 'metadata.name MATCHES (my-topic-.*)'

# Update resources targeting a specific provider
jikkou update -f my-resources.yaml --provider kafka-prod
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
- [jikkou delete](../jikkou-delete) - Delete resources (DELETE mode)
- [jikkou diff](../jikkou-diff) - Show changes without applying
