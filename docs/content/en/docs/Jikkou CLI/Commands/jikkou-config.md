---
title: "jikkou config"
linkTitle: "jikkou config"
---

Sets or retrieves the configuration of this client.

## Synopsis

The `config` command manages Jikkou CLI configuration contexts. A context defines the configuration settings
(config file path, inline properties, provider bindings) used when running Jikkou commands.

Configuration is stored in the Jikkou config file, located by default at `$HOME/.jikkou/config`.

## Subcommands

| Command | Description |
|---------|-------------|
| [jikkou config set-context](../jikkou-config-set-context) | Configure a context with the provided arguments |
| [jikkou config get-contexts](../jikkou-config-get-contexts) | List all configured contexts |
| [jikkou config current-context](../jikkou-config-current-context) | Display the current context |
| [jikkou config use-context](../jikkou-config-use-context) | Switch to a specified context |
| [jikkou config view](../jikkou-config-view) | Show merged configuration settings |

## Options inherited from parent commands

| Flag | Description |
|------|-------------|
| `--logger-level=<level>` | Log level: TRACE, DEBUG, INFO, WARN, ERROR |

## SEE ALSO

- [Configuration]({{% relref "../CLI-Configuration" %}}) - Learn how to configure Jikkou CLI
