---
title: "jikkou config use-context"
linkTitle: "jikkou config use-context"
---

Switch to a specified context.

## Synopsis

Configures Jikkou to use the specified context. All subsequent commands will use the configuration associated with
this context.

```bash
jikkou config use-context <context-name>
```

## Examples

```bash
# Switch to the production context
$ jikkou config use-context production
Using context 'production'

# If already using the specified context
$ jikkou config use-context production
Already using context production
```

## Arguments

| Argument | Description |
|----------|-------------|
| `context-name` | **(required)** The name of the context to switch to |

## Options

This command has no specific options.

## Options inherited from parent commands

| Flag | Description |
|------|-------------|
| `--logger-level=<level>` | Log level: TRACE, DEBUG, INFO, WARN, ERROR |

## SEE ALSO

- [jikkou config get-contexts](../jikkou-config-get-contexts) - List all available contexts
- [jikkou config current-context](../jikkou-config-current-context) - Display the current context
- [jikkou config set-context](../jikkou-config-set-context) - Create or update a context
- [jikkou config](../jikkou-config) - Config command overview
