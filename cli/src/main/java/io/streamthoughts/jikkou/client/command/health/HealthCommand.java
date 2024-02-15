/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.client.command.health;

import io.streamthoughts.jikkou.client.command.CLIBaseCommand;
import picocli.CommandLine.Command;

@Command(name = "health",
        header = "Print or describe health indicators.",
        description = "This command can be used to describe all resources of a given kind",
        subcommands = {
                GetHealthCommand.class,
                GetHealthIndicatorsCommand.class
        })
public class HealthCommand extends CLIBaseCommand {
}
