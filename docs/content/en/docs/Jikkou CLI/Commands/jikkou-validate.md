---
title: "jikkou validate"
linkTitle: "jikkou validate"
---

Check whether the resources definitions meet all validation requirements.

## Synopsis

Validate the resource definition files specified through the command line arguments.

Validate runs all the user-defined validation requirements after performing any relevant resource transformations.
Validation rules are applied only to resources matching the selectors passed through the command line arguments.

If validation passes, the command outputs the validated resources. If validation fails, the command prints the
validation errors and exits with a non-zero status code.

```bash
jikkou validate [flags]
```

## Examples

```bash
# Validate resources defined in a YAML file
jikkou validate -f my-resources.yaml

# Validate resources from a directory
jikkou validate -f ./resources/

# Validate with a selector to filter specific resources
jikkou validate -f my-resources.yaml -s 'metadata.name MATCHES (my-topic-.*)'

# Validate with template values
jikkou validate -f my-resources.yaml --values-files values.yaml

# Validate with JSON output
jikkou validate -f my-resources.yaml -o JSON

# Validate targeting a specific provider
jikkou validate -f my-resources.yaml --provider kafka-prod
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

- [jikkou prepare](../jikkou-prepare) - Prepare resources for validation
- [jikkou apply](../jikkou-apply) - Apply changes after validation
- [jikkou diff](../jikkou-diff) - Show changes without applying
