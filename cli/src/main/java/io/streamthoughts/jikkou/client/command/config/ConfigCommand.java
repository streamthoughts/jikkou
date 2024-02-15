/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.client.command.config;

import io.streamthoughts.jikkou.client.command.CLIBaseCommand;
import jakarta.inject.Singleton;
import picocli.CommandLine.Command;

@Command(name = "config",
        subcommands = {
                ViewCommand.class,
                SetContextCommand.class,
                GetContextsCommand.class,
                CurrentContextCommand.class,
                UseContextCommand.class},
        description = "Sets or retrieves the configuration of this client"

)
@Singleton
public class ConfigCommand extends CLIBaseCommand { }
