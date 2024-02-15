/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.client.command;

import io.streamthoughts.jikkou.client.LoggingMixin;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;

@Command(
        headerHeading = "Usage:%n%n",
        descriptionHeading = "%nDESCRIPTION:%n%n",
        parameterListHeading = "%nPARAMETERS:%n%n",
        optionListHeading = "%nOPTIONS:%n%n",
        commandListHeading = "%nCOMMANDS:%n%n",
        synopsisHeading = "%n",
        mixinStandardHelpOptions = true
)
public class CLIBaseCommand {

    @Mixin
    LoggingMixin loggingMixin;
}
