/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.jikkou.client.command;

import picocli.CommandLine.Option;

/**
 * Picocli mixin that adds {@code --output / -o} support for commands displaying
 * non-resource data (providers, extensions, API resources).
 *
 * <p>Supports TABLE (default), JSON, and YAML.
 */
public final class OutputFormatMixin {

    @Option(names = {"--output", "-o"},
            defaultValue = "TABLE",
            description = "Prints the output in the specified format. "
                    + "Valid values: ${COMPLETION-CANDIDATES} (default: ${DEFAULT-VALUE})."
    )
    OutputFormat format;

    public OutputFormat format() {
        return format;
    }
}
