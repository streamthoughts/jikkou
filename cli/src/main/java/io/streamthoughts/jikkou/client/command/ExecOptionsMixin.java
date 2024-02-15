/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.client.command;

import io.streamthoughts.jikkou.client.printer.Printers;
import picocli.CommandLine.Option;

public class ExecOptionsMixin {

    @Option(names = { "--output", "-o" },
            defaultValue = "TEXT",
            description = "Prints the output in the specified format. Valid values: ${COMPLETION-CANDIDATES} (default: ${DEFAULT-VALUE})."
    )
    public Printers format;

    @Option(names = "--dry-run",
            description = "Execute command in Dry-Run mode."
    )
    public boolean dryRun;

}