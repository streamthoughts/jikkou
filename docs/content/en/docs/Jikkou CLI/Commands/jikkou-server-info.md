---
title: "jikkou server-info"
linkTitle: "jikkou server-info"
---

Display Jikkou API server information.

## Synopsis

Print information about the Jikkou API server. This command is only available when Jikkou is configured in
**proxy mode** (i.e., when `jikkou.proxy.url` is set in your configuration).

```bash
jikkou server-info [flags]
```

## Examples

```bash
# Get server information in YAML format (default)
jikkou server-info

# Get server information in JSON format
jikkou server-info -o JSON
```

## Options

| Flag | Short | Default | Description |
|------|-------|---------|-------------|
| `--output` | `-o` | `YAML` | Output format. Valid values: JSON, YAML |

## Options inherited from parent commands

| Flag | Description |
|------|-------------|
| `--logger-level=<level>` | Log level: TRACE, DEBUG, INFO, WARN, ERROR |

## SEE ALSO

- [jikkou health](../jikkou-health) - Check health of target environments
- [Configuration]({{% relref "../CLI-Configuration" %}}) - Learn how to configure proxy mode
