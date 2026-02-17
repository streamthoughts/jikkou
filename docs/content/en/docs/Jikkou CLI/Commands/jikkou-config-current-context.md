---
title: "jikkou config current-context"
linkTitle: "jikkou config current-context"
---

Display the current context.

## Synopsis

Displays the current context used by Jikkou CLI, including the configuration file path and inline configuration
properties associated with it.

```bash
jikkou config current-context
```

## Examples

```bash
# Show the current context
$ jikkou config current-context
Using context 'localhost'

 KEY          VALUE
 ConfigFile
 ConfigProps  {"provider.kafka.config.client.bootstrap.servers":"localhost:9092"}
```

## Options

This command has no specific options.

## Options inherited from parent commands

| Flag | Description |
|------|-------------|
| `--logger-level=<level>` | Log level: TRACE, DEBUG, INFO, WARN, ERROR |

## SEE ALSO

- [jikkou config get-contexts](../jikkou-config-get-contexts) - List all contexts
- [jikkou config use-context](../jikkou-config-use-context) - Switch to a different context
- [jikkou config view](../jikkou-config-view) - Show the merged configuration
- [jikkou config](../jikkou-config) - Config command overview
