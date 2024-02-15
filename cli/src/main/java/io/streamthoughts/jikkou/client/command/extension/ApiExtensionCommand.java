/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.client.command.extension;

import io.streamthoughts.jikkou.client.command.CLIBaseCommand;
import jakarta.inject.Singleton;
import picocli.CommandLine.Command;

@Command(name = "api-extensions",
        header = "Print the supported API extensions",
        description = {
                "List and describe the API extensions supported by the Jikkou CLI or Jikkou API Server (in proxy mode)."
        },
        subcommands = {
                ListExtensionCommand.class,
                GetExtensionCommand.class,
        })
@Singleton
public class ApiExtensionCommand extends CLIBaseCommand {}
