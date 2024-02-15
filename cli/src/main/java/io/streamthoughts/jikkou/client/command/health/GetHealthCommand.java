/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.client.command.health;

import io.streamthoughts.jikkou.client.command.CLIBaseCommand;
import io.streamthoughts.jikkou.core.JikkouApi;
import io.streamthoughts.jikkou.core.health.HealthStatus;
import io.streamthoughts.jikkou.core.io.Jackson;
import io.streamthoughts.jikkou.core.models.ApiHealthResult;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.Callable;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(name = "get",
        header = "Print health indicators.",
        description = "This command can be used to get information about the health of target environments."
)
@Singleton
public class GetHealthCommand extends CLIBaseCommand implements Callable<Integer> {

    public static final String HEALTH_INDICATOR_ALL = "all";

    enum Formats { JSON, YAML }

    @Option(names = { "--output", "-o" },
            defaultValue = "YAML",
            description = "Prints the output in the specified format. Valid values: ${COMPLETION-CANDIDATES} (default: ${DEFAULT-VALUE})."
    )
    Formats format;

    @Option(names = "--timeout-ms",
            defaultValue = "2000",
            description = "Timeout in milliseconds for retrieving health indicators (default: ${DEFAULT-VALUE}).")
    long timeoutMs;

    @Parameters(
            paramLabel = "HEALTH_INDICATOR",
            description = "Name of the health indicator (use 'all' to get all indicators).")
    String indicator;

    @Inject
    private JikkouApi api;

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer call() throws IOException {
        Duration timeout = Duration.ofMillis(timeoutMs);
        ApiHealthResult result;
        if (indicator.equalsIgnoreCase(HEALTH_INDICATOR_ALL)) {
            result = api.getApiHealth(timeout);
        } else {
            result = api.getApiHealth(indicator, timeout);
        }

        if (result != null) {
            try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                switch (format) {
                    case JSON -> Jackson.JSON_OBJECT_MAPPER.writeValue(baos, result);
                    case YAML -> Jackson.YAML_OBJECT_MAPPER.writeValue(baos, result);
                }
                System.out.println(baos);
            }
            return result.status().equals(HealthStatus.UP) ?
                    CommandLine.ExitCode.OK :
                    CommandLine.ExitCode.SOFTWARE;
        }

        return CommandLine.ExitCode.OK;
    }
}
