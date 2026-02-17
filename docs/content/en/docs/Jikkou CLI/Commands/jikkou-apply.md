---
title: "jikkou apply"
linkTitle: "jikkou apply"
---

Update the resources aand as described by the resource definition files.

## Synopsis

Reconciles the target platform so that the resources match the resource definition files passed as arguments.
This command uses the **FULL** reconciliation mode, meaning it will create, update, and delete resources as needed
to match the desired state defined in your resource files.

```bash
jikkou apply [flags]
```

## Examples

```bash
# Apply resources defined in a YAML file
jikkou apply -f my-resources.yaml

# Apply resources from a directory
jikkou apply -f ./resources/

# Apply resources with dry-run to preview changes
jikkou apply -f my-resources.yaml --dry-run

# Apply resources with specific selector
jikkou apply -f my-resources.yaml --selector 'metadata.name IN (my-topic)'

# Apply resources with template values
jikkou apply -f my-resources.yaml --values-files values.yaml

# Apply with inline template variable
jikkou apply -f my-resources.yaml -v topicName=my-topic

# Apply with custom labels
jikkou apply -f my-resources.yaml -l environment=production

# Apply with controller options
jikkou apply -f my-resources.yaml --options delete-orphans=true

# Apply targeting a specific provider
jikkou apply -f my-resources.yaml --provider kafka-prod

# Apply with JSON output format
jikkou apply -f my-resources.yaml -o JSON --pretty
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

- [jikkou create](../jikkou-create) - Create resources (CREATE mode only)
- [jikkou update](../jikkou-update) - Create or update resources (UPDATE mode only)
- [jikkou delete](../jikkou-delete) - Delete resources (DELETE mode only)
- [jikkou diff](../jikkou-diff) - Show changes without applying
- [jikkou validate](../jikkou-validate) - Validate resource definitions
