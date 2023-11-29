---
title: "Basic CLI Features"
linkTitle: "Basic CLI Features"
weight: 1
description: 
---

{{% pageinfo %}}
**Hands-on:** Try the Jikkou: [Get Started tutorials]({{% relref "../Tutorials/get_started.md" %}}).
{{% /pageinfo %}}

The command line interface to **Jikkou** is the `jikkou` command, which accepts a variety of subcommands such as 
`jikkou apply` or `jikkou validate`.

To view a list of the commands available in your current Jikkou version, run `jikkou` with no additional arguments:

```bash
Usage: 
jikkou [-hV] [--logger-level=<level>] [COMMAND]


Jikkou CLI:: A command-line client designed to provide an efficient and easy way to manage, automate, and provision all the assets of your data infrastructure.

Find more information at: https://streamthoughts.github.io/jikkou/.

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
  diff                      Show changes required by the current resource definitions.
  get                       Display one or many specific resources.
  prepare                   Prepare the resource definition files for validation.
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
```

(The output from your current Jikkou version may be different than the above example.)

## Checking Jikkou Version

Run the `jikkou --version` to display your current installation version:

```bash                                                                                                                                                  2 ↵
Jikkou version "0.32.0" 2023-11-28
JVM: 21.0.1 (GraalVM Community Substrate VM 21.0.1+12)
```

## Shell Tab-completion

It is recommended to install the bash/zsh completion script `jikkou_completion`.

The completion script can be downloaded from the project Github repository:

```bash
wget https://raw.githubusercontent.com/streamthoughts/jikkou/main/jikkou_completion . jikkou_completion
```

or alternatively, you can run the following command to generate it.

```bash
source <(jikkou generate-completion)
```


