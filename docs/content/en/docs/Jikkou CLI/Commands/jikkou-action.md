---
title: "jikkou action"
linkTitle: "jikkou action"
---

List and execute actions.

## Synopsis

The `action` command manages Jikkou actions. Actions are named operations that can be executed on the target platform.
Like the `get` command, `action` uses dynamic subcommands based on the actions available from your configured providers.

Each action is registered as a subcommand under `jikkou action`, and has an `execute` subcommand to run it.

```bash
jikkou action <action-name> execute [flags]
```

## Examples

```bash
# List all available actions (shown as subcommands in help)
jikkou action --help

# Execute an action
jikkou action KafkaConsumerGroupsResetOffsets execute --options topic=my-topic --options group=my-group

# Execute an action with YAML output
jikkou action KafkaConsumerGroupsResetOffsets execute -o YAML

# Execute an action targeting a specific provider
jikkou action KafkaConsumerGroupsResetOffsets execute --provider kafka-prod --options topic=my-topic
```

## Options (execute subcommand)

| Flag | Short | Default | Description |
|------|-------|---------|-------------|
| `--output` | `-o` | `YAML` | Output format. Valid values: JSON, YAML |
| `--provider` | | | Select a specific provider instance |

Additional options may be available depending on the action. These are dynamically registered based on the
action's `ApiOptionSpec` definitions. Use `jikkou api-extensions get <action-name>` to view options for a
specific action.

## Options inherited from parent commands

| Flag | Description |
|------|-------------|
| `--logger-level=<level>` | Log level: TRACE, DEBUG, INFO, WARN, ERROR |

## SEE ALSO

- [jikkou api-extensions](../jikkou-api-extensions) - List and inspect available extensions
- [jikkou get](../jikkou-get) - Display resources from the platform
