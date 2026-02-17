---
title: "jikkou replace"
linkTitle: "jikkou replace"
---

Replace all resources.

## Synopsis

Replaces resources by deleting and (re)creating all the resources as defined in the resource descriptor files passed
through the arguments. Unlike `apply`, which performs incremental updates, `replace` performs a full replacement of
resources: existing resources are deleted first and then recreated from scratch.

Use `replace` when you need to ensure resources exactly match the desired state without any leftover configuration
from previous versions.

```bash
jikkou replace [flags]
```

## Examples

```bash
# Replace resources defined in a YAML file
jikkou replace -f my-resources.yaml

# Preview what would be replaced
jikkou replace -f my-resources.yaml --dry-run

# Replace resources from a directory
jikkou replace -f ./resources/

# Replace resources targeting a specific provider
jikkou replace -f my-resources.yaml --provider kafka-prod

# Replace with JSON output
jikkou replace -f my-resources.yaml -o JSON --pretty
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
- [jikkou patch](../jikkou-patch) - Patch resources with a specified mode
- [jikkou diff](../jikkou-diff) - Show changes without applying
