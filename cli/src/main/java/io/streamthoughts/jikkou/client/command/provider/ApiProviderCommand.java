/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.client.command.provider;

import io.streamthoughts.jikkou.client.command.CLIBaseCommand;
import jakarta.inject.Singleton;
import picocli.CommandLine.Command;

@Command(name = "api-providers",
        header = "Print the registered API providers",
        description = {
                "List the API providers registered by the Jikkou CLI or Jikkou API Server (in proxy mode)."
        },
        subcommands = {
                ListProviderCommand.class,
                GetProviderCommand.class,
        })
@Singleton
public class ApiProviderCommand extends CLIBaseCommand {}
