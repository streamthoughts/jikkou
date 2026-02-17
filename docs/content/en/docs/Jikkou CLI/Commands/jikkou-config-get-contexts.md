---
title: "jikkou config get-contexts"
linkTitle: "jikkou config get-contexts"
---

List all configured contexts.

## Synopsis

Get all contexts defined in the Jikkou config file. The current context is marked with an asterisk (`*`).

```bash
jikkou config get-contexts
```

## Examples

```bash
# List all contexts
$ jikkou config get-contexts

 NAME
 localhost *
 development
 staging
 production
```

## Options

This command has no specific options.

## Options inherited from parent commands

| Flag | Description |
|------|-------------|
| `--logger-level=<level>` | Log level: TRACE, DEBUG, INFO, WARN, ERROR |

## SEE ALSO

- [jikkou config current-context](../jikkou-config-current-context) - Show the current context details
- [jikkou config use-context](../jikkou-config-use-context) - Switch to a context
- [jikkou config set-context](../jikkou-config-set-context) - Create or update a context
- [jikkou config](../jikkou-config) - Config command overview
