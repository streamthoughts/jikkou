---
title: "Overview"
linkTitle: "Overview"
weight: 1
description: >
  Global options, version information, and shell tab-completion.
---

The command line interface to **Jikkou** is the `jikkou` command, which accepts a variety of subcommands such as
`jikkou apply` or `jikkou validate`.

To view a list of the commands available in your current Jikkou version, run `jikkou` with no additional arguments:

```bash
Usage: 
jikkou [-hV] [--logger-level=<level>] [COMMAND]


Jikkou CLI:: A command-line client designed to provide an efficient and easy way to manage, automate, and provision resources.

Find more information at: https://www.jikkou.io/.

OPTIONS:

  -h, --help      Show this help message and exit.
      --logger-level=<level>
                  Specify the log level verbosity to be used while running a command.
                  Valid level values are: TRACE, DEBUG, INFO, WARN, ERROR.
                  For example, `--logger-level=INFO`
  -V, --version   Print version information and exit.

CORE COMMANDS:
  apply                     Update the resources as described by the resource definition files.
  create                    Create resources from the resource definition files (only non-existing resources will be created).
  delete                    Delete resources that are no longer described by the resource definition files.
  diff                      Show resource changes required by the current resource definitions.
  get                       Display one or many specific resources.
  patch                     Execute all changes for the specified reconciliation mode.
  prepare                   Prepare the resource definition files for validation.
  replace                   Replace all resources.
  update                    Create or update resources from the resource definition files
  validate                  Check whether the resources definitions meet all validation requirements.

SYSTEM MANAGEMENT COMMANDS:
  action                    List/execute actions.
  health                    Print or describe health indicators.

ADDITIONAL COMMANDS:
  api-extensions            Print the supported API extensions
  api-resources             Print the supported API resources
  config                    Sets or retrieves the configuration of this client
  generate-completion       Generate bash/zsh completion script for jikkou.
  help                      Display help information about the specified command.

See 'jikkou --help' for more information about a command.
```

(The output from your current Jikkou version may be different than the above example.)

> For detailed options and usage examples for each command, see the [Commands Reference]({{% relref "./Commands" %}}).

## Checking Jikkou Version

Run the `jikkou --version` to display your current installation version:

```bash                                                                                                                                                  2 ↵
Jikkou version "0.37.0" 2026-02-17
JVM: 25.0.1 (GraalVM Community Substrate VM 25.0.1+8)
```

## Shell Tab-completion

It is recommended to install the bash/zsh completion script `jikkou_completion`.

The completion script can be downloaded from the project Github repository:

```bash
wget https://raw.githubusercontent.com/streamthoughts/jikkou/main/jikkou_completion -O jikkou_completion
```

or alternatively, you can run the following command to generate it.

```bash
source <(jikkou generate-completion)
```


