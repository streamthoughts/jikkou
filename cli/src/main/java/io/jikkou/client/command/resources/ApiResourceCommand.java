/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.jikkou.client.command.resources;

import io.jikkou.client.command.CLIBaseCommand;
import jakarta.inject.Singleton;
import picocli.CommandLine.Command;

@Command(name = "api-resources",
        header = "Print the supported API resources",
        description = {
                "List and describe the API resources supported by the Jikkou CLI or Jikkou API Server (in proxy mode)."
        },
        subcommands = {
                ListApiResourcesCommand.class,
                GetApiResourceSchemaCommand.class,
        })
@Singleton
public class ApiResourceCommand extends CLIBaseCommand {}
