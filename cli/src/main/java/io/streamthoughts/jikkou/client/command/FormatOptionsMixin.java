/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.client.command;

import io.streamthoughts.jikkou.core.io.writer.ResourceWriter;
import picocli.CommandLine.Option;

public final class FormatOptionsMixin {

    @Option(names = { "--output", "-o" },
            defaultValue = "YAML",
            description = "Prints the output in the specified format. Valid values: ${COMPLETION-CANDIDATES} (default: ${DEFAULT-VALUE})."
    )
    ResourceWriter.Format format;

    public ResourceWriter.Format format() {
        return format;
    }
}
