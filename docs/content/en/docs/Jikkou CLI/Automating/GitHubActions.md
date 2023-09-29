---
title: "Automate Jikkou with GitHub Actions"
linkTitle: "GitHub Actions"
weight: 3
description: >
  Learn Jikkou Setup Github Action in your CI/CD Workflows
---

## Setup Jikkou

The [`streamthoughts/setup-jikkou`](https://github.com/streamthoughts/setup-jikkou) action is a JavaScript action that
sets up Jikkou in your **GitHub Actions** workflow by:

* Downloading a specific version of **Jikkou CLI** and adding it to the `PATH`.
* Configuring **JIKKOU CLI** with a custom configuration file.

After you've used the action, subsequent steps in the same job can run arbitrary Jikkou commands using the GitHub
Actions run syntax. This allows most Jikkou commands to work exactly like they do on your local command line.

### Usage

```yaml
steps:
  - uses: streamthoughts/setup-jikkou@v1
```

A specific version of Jikkou CLI can be installed:

```yaml
steps:
  - uses: streamthoughts/setup-jikkou@v0.1.0
    with:
      jikkou_version: 0.29.0
```

A custom configuration file can be specified:

```yaml
steps:
  - uses: streamthoughts/setup-jikkou@v0.1.0
    with:
      jikkou_config: ./config/jikkouconfig.json
```

### Inputs

This Action additionally supports the following inputs :

| Property	        | Default  | Description                                                                                                                    |
|------------------|----------|--------------------------------------------------------------------------------------------------------------------------------|
| `jikkou_version` | `latest` | The version of Jikkou CLI to install. A value of `latest` will install the latest version of Jikkou CLI.                       |
| `jikkou_config`  |          | The path to the Jikkou CLI config file. If set, Jikkou CLI will be configured through the `JIKKOUCONFIG` environment variable. |
