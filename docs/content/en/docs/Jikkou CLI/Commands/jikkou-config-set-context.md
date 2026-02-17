---
title: "jikkou config set-context"
linkTitle: "jikkou config set-context"
---

Configure a context with the provided arguments.

## Synopsis

Configures the specified context with the provided arguments. A context stores a reference to a configuration file
and/or inline configuration properties. If the context already exists, it will be updated.

After setting a context, use `jikkou config use-context` to switch to it.

```bash
jikkou config set-context <context-name> [flags]
```

## Examples

```bash
# Create a context with inline Kafka bootstrap server
jikkou config set-context localhost \
  --config-props=provider.kafka.config.client.bootstrap.servers=localhost:9092

# Create a context pointing to a configuration file
jikkou config set-context production --config-file=/path/to/production.conf

# Create a context with a properties file
jikkou config set-context staging --config-props-file=/path/to/staging.properties

# Create a context with provider-scoped properties
jikkou config set-context dev \
  --provider kafka \
  --config-props=client.bootstrap.servers=kafka-dev:9092

# Create a context with a config prefix
jikkou config set-context dev \
  --config-prefix=provider.kafka.config \
  --config-props=client.bootstrap.servers=kafka-dev:9092
```

## Options

| Flag | Default | Description |
|------|---------|-------------|
| `--config-file` | | Path to a Jikkou configuration file |
| `--config-props` | | Inline configuration properties as key=value pairs (repeatable) |
| `--config-props-file` | | Path(s) to one or more configuration properties files (repeatable) |
| `--provider` | | Name of the provider to which this configuration should be attached |
| `--config-prefix` | | Prefix to apply to all configuration property keys |

## Arguments

| Argument | Description |
|----------|-------------|
| `context-name` | **(required)** The name of the context to configure |

## Options inherited from parent commands

| Flag | Description |
|------|-------------|
| `--logger-level=<level>` | Log level: TRACE, DEBUG, INFO, WARN, ERROR |

## SEE ALSO

- [jikkou config use-context](../jikkou-config-use-context) - Switch to a context
- [jikkou config get-contexts](../jikkou-config-get-contexts) - List all contexts
- [jikkou config](../jikkou-config) - Config command overview
