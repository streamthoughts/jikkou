/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.client.command.get;

import io.streamthoughts.jikkou.client.command.CLIBaseCommand;
import picocli.CommandLine.Command;

@Command(name = "get",
        header = "Display one or many specific resources.",
        description = "Use this command to display the current state of all the resources of a specific kind.")
public class GetCommand extends CLIBaseCommand {}
