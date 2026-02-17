---
title: "jikkou health"
linkTitle: "jikkou health"
---

Print or describe health indicators.

## Synopsis

The `health` command provides information about the health of target environments. It has two subcommands:

- `health get` - Retrieve health status for one or all indicators
- `health get-indicators` - List all available health indicators

## Subcommands

## `jikkou health get`

Get health information for a specific indicator or all indicators.

```bash
jikkou health get <indicator|all> [flags]
```

## Examples

```bash
# Get health for all indicators
jikkou health get all

# Get health for a specific indicator
jikkou health get kafka

# Get health with a custom timeout
jikkou health get all --timeout-ms 5000

# Get health in JSON format
jikkou health get all -o JSON

# Get health targeting a specific provider
jikkou health get kafka --provider kafka-prod
```

## Options

| Flag | Short | Default | Description |
|------|-------|---------|-------------|
| `--output` | `-o` | `YAML` | Output format. Valid values: JSON, YAML |
| `--timeout-ms` | | `2000` | Timeout in milliseconds for retrieving health indicators |
| `--provider` | | | Select a specific provider instance |

The command exits with code 0 if the health status is UP, or a non-zero code otherwise.

---

## `jikkou health get-indicators`

List all available health indicators. This command takes no options and displays the name and description of each
registered health indicator.

```bash
jikkou health get-indicators
```

## Examples

```bash
# List all health indicators
jikkou health get-indicators
```

## Options

This subcommand has no specific options.

## Options inherited from parent commands

| Flag | Description |
|------|-------------|
| `--logger-level=<level>` | Log level: TRACE, DEBUG, INFO, WARN, ERROR |

## SEE ALSO

- [jikkou api-extensions](../jikkou-api-extensions) - List and inspect extensions
- [jikkou server-info](../jikkou-server-info) - Display server information (proxy mode)
