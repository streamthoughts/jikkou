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
jikkou [-hV] [COMMAND]


Jikkou CLI:: A command-line client designed to provide an efficient and easy way to manage, automate, and provision resources for any kafka infrastructure.

Find more information at: https://streamthoughts.github.io/jikkou/.

Options:

  -h, --help      Show this help message and exit.
  -V, --version   Print version information and exit.

Commands:

  create      Create resources from the resource definition files (only non-existing resources will be created).
  delete      Delete resources that are no longer described by the resource definition files.
  update      Create or update resources from the resource definition files
  apply       Update the resources as described by the resource definition files.
  resources   List supported resources
  extensions  List or describe the extensions of Jikkou
  config      Sets or retrieves the configuration of this client
  diff        Display all resource changes.
  validate    Validate resource definition files.
  health      Print or describe health indicators.
  help        Display help information about the specified command.
  get         List and describe all resources of a specific kind.
```

(The output from your current Jikkou version may be different than the above example.)

## Checking Jikkou Version

Run the `jikkou --version` to display your current installation version:

```bash                                                                                                                                                  2 ↵
Jikkou version "0.29.0" 2023-09-29
JVM: 17.0.7 (Oracle Corporation Substrate VM 17.0.7+8-LTS)
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


