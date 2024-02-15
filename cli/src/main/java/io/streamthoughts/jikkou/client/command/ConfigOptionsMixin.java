/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.client.command;

import io.streamthoughts.jikkou.core.config.Configuration;
import java.util.HashMap;
import java.util.Map;
import picocli.CommandLine.Option;

public final class ConfigOptionsMixin {

    @Option(
            names = {"--options"},
            description = "Set the configuration options to be used for computing resource reconciliation (can specify multiple values)"
    )
    public Map<String, Object> options = new HashMap<>();

    public Configuration getConfiguration() {
        return Configuration.from(options);
    }
}
