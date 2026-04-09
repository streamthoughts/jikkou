/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.jikkou.client.command.action;

import io.jikkou.client.command.CLIBaseCommand;
import picocli.CommandLine.Command;

@Command(name = "action",
        header = "List/execute actions.",
        description = "Use this command to execute a specific action.")
public class ListActionCommand extends CLIBaseCommand {}
