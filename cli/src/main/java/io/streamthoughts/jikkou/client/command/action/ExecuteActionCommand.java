/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.client.command.action;

import io.micronaut.context.annotation.Prototype;
import io.streamthoughts.jikkou.client.command.AbstractApiCommand;
import io.streamthoughts.jikkou.core.JikkouApi;
import io.streamthoughts.jikkou.core.config.Configuration;
import io.streamthoughts.jikkou.core.io.Jackson;
import io.streamthoughts.jikkou.core.models.ApiActionResultSet;
import jakarta.inject.Inject;
import java.io.ByteArrayOutputStream;
import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(
        name = "execute",
        header = "Execute the action."
)
@Prototype
public class ExecuteActionCommand extends AbstractApiCommand {

    /**
     * The action name.
     */
    private String name;

    // SERVICES
    @Inject
    JikkouApi api;

    enum Format {
        JSON, YAML
    }

    @CommandLine.Option(names = {"--output", "-o"},
            defaultValue = "YAML",
            description = "Prints the output in the specified format. Allowed values: ${COMPLETION-CANDIDATES} (default YAML)."
    )

    private Format format;

    // Picocli require an empty constructor to generate the completion file
    public ExecuteActionCommand() {
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public Integer call() throws Exception {
        ApiActionResultSet<?> results = api.execute(name, Configuration.from(options()));
        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            switch (format) {
                case JSON -> Jackson.JSON_OBJECT_MAPPER
                        .writerWithDefaultPrettyPrinter()
                        .writeValue(os, results);
                case YAML -> Jackson.YAML_OBJECT_MAPPER
                        .writerWithDefaultPrettyPrinter()
                        .writeValue(os, results);
            }
            System.out.println(os);
            return CommandLine.ExitCode.OK;
        }
    }

    /**
     * Sets the resource name.
     *
     * @param name The resource name.
     */
    public void setName(String name) {
        this.name = name;
    }
}
