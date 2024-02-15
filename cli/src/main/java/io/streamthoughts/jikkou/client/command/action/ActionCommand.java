/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.client.command.action;

import io.micronaut.context.annotation.Prototype;
import io.streamthoughts.jikkou.client.command.CLIBaseCommand;
import picocli.CommandLine.Command;

@Command
@Prototype
public class ActionCommand extends CLIBaseCommand {}
