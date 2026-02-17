---
title: "jikkou config view"
linkTitle: "jikkou config view"
---

Show merged configuration settings.

## Synopsis

Show the merged Jikkou configuration settings for the current context (or a named context). The configuration is
rendered in HOCON format, showing all resolved values from the config file, inline properties, and defaults.

Use `--debug` to see the origin of each setting, or `--comments` to include human-written comments from the
configuration files.

```bash
jikkou config view [flags]
```

## Examples

```bash
# View the current configuration
jikkou config view

# View configuration for a specific context
jikkou config view --name production

# View configuration with origin comments (useful for debugging)
jikkou config view --debug

# View configuration with human-written comments
jikkou config view --comments
```

## Options

| Flag | Default | Description |
|------|---------|-------------|
| `--name` | | The name of the context configuration to view (defaults to current context) |
| `--debug` | `false` | Print configuration with the origin of each setting as comments |
| `--comments` | `false` | Print configuration with human-written comments |

## Options inherited from parent commands

| Flag | Description |
|------|-------------|
| `--logger-level=<level>` | Log level: TRACE, DEBUG, INFO, WARN, ERROR |

## SEE ALSO

- [jikkou config current-context](../jikkou-config-current-context) - Display the current context
- [jikkou config set-context](../jikkou-config-set-context) - Create or update a context
- [jikkou config](../jikkou-config) - Config command overview
