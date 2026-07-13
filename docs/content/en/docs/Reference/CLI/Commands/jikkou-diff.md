---
title: "jikkou diff"
linkTitle: "jikkou diff"
aliases:
  - /docs/jikkou-cli/commands/jikkou-diff/
---

Show resource changes required by the current resource definitions.

## Synopsis

Generates a speculative reconciliation plan, showing the resource changes Jikkou would apply to reconcile the resource
definitions. This command does not actually perform the reconciliation actions.

Use `diff` to preview what changes would be made before running `apply`, `create`, `update`, or `delete`.

```bash
jikkou diff [flags]
```

## Examples

```bash
# Show changes required by a resource definition file
jikkou diff -f my-resources.yaml

# Show only resources that would be created
jikkou diff -f my-resources.yaml --filter-resource-op CREATE

# Show only resources that would be created or deleted
jikkou diff -f my-resources.yaml --filter-resource-op CREATE,DELETE

# Filter changes to show only update operations
jikkou diff -f my-resources.yaml --filter-change-op UPDATE

# Output as a resource list
jikkou diff -f my-resources.yaml --list

# Output in JSON format
jikkou diff -f my-resources.yaml -o JSON

# Diff with a specific provider
jikkou diff -f my-resources.yaml --provider kafka-prod

# Diff with selector
jikkou diff -f my-resources.yaml -s 'metadata.name MATCHES (my-topic-.*)'

# Detect configuration drift in CI: exit with code 3 when any change is pending
jikkou diff -f my-resources.yaml --fail-on-changes
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
| `--filter-resource-op` | | | Filter resources by operation (comma-separated). Valid values: NONE, CREATE, DELETE, REPLACE, UPDATE |
| `--filter-change-op` | | | Filter state changes by operation (comma-separated). Valid values: NONE, CREATE, DELETE, REPLACE, UPDATE |
| `--list` | | `false` | Output resources as an ApiResourceChangeList |
| `--fail-on-changes` | | `false` | Exit with code 3 when the diff contains at least one change. Prints a one-line summary to stderr; stdout stays machine-readable |

## Exit codes

| Code | Meaning |
|------|---------|
| `0` | Success. With `--fail-on-changes`: no changes detected |
| `1` | Validation or execution error |
| `2` | Usage error (invalid flags or arguments) |
| `3` | Changes detected (only with `--fail-on-changes`) |

The drift check is evaluated on the filtered result: combining `--fail-on-changes` with
`--filter-change-op` or `--filter-resource-op` scopes which operations count as drift.
See [Detect configuration drift](/docs/how-to-guides/automating/detect-configuration-drift/)
for a complete CI setup.

## Options inherited from parent commands

| Flag | Description |
|------|-------------|
| `--logger-level=<level>` | Log level: TRACE, DEBUG, INFO, WARN, ERROR |

## SEE ALSO

- [jikkou apply](../jikkou-apply) - Apply all changes
- [jikkou validate](../jikkou-validate) - Validate resource definitions
- [jikkou prepare](../jikkou-prepare) - Prepare resources for validation
