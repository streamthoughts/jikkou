/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.jikkou.client.command.action;

import io.jikkou.client.command.CLIBaseCommand;
import io.micronaut.context.annotation.Prototype;
import picocli.CommandLine.Command;

@Command
@Prototype
public class ActionCommand extends CLIBaseCommand {}
