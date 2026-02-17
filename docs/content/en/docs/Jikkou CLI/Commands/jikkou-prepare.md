---
title: "jikkou prepare"
linkTitle: "jikkou prepare"
---

Prepare the resource definition files for validation.

## Synopsis

Prepare the resource definition files specified through the command line arguments for validation. This command applies
all configured transformations to the resource definitions and outputs the result, without running validation rules or
performing reconciliation.

Use `prepare` to inspect how your resources look after template rendering and transformations, before validation and
reconciliation happen.

```bash
jikkou prepare [flags]
```

## Examples

```bash
# Prepare resources from a YAML file
jikkou prepare -f my-resources.yaml

# Prepare resources from a directory
jikkou prepare -f ./resources/

# Prepare with template values
jikkou prepare -f my-resources.yaml --values-files values.yaml

# Prepare with JSON output
jikkou prepare -f my-resources.yaml -o JSON

# Prepare with selector
jikkou prepare -f my-resources.yaml -s 'metadata.name IN (my-topic)'

# Prepare targeting a specific provider
jikkou prepare -f my-resources.yaml --provider kafka-prod
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
| `--output` | `-o` | `YAML` | Output format. Valid values: JSON, YAML |

## Options inherited from parent commands

| Flag | Description |
|------|-------------|
| `--logger-level=<level>` | Log level: TRACE, DEBUG, INFO, WARN, ERROR |

## SEE ALSO

- [jikkou validate](../jikkou-validate) - Validate prepared resources
- [jikkou apply](../jikkou-apply) - Apply changes
- [jikkou diff](../jikkou-diff) - Show changes without applying
