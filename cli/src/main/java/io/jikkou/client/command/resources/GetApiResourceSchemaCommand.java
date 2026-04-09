/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.jikkou.client.command.resources;

import io.jikkou.client.command.CLIBaseCommand;
import io.jikkou.core.JikkouApi;
import io.jikkou.core.io.Jackson;
import io.jikkou.core.models.ApiResourceSchema;
import io.jikkou.core.models.ResourceType;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.concurrent.Callable;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "schema",
        header = "Print the JSON Schema of a resource type",
        description = "Get the JSON Schema for a specific API resource type."
)
@Singleton
public class GetApiResourceSchemaCommand extends CLIBaseCommand implements Callable<Integer> {

    @Option(names = {"--api-version"},
            required = true,
            description = "The API version of the resource (e.g., 'kafka.jikkou.io/v1')."
    )
    public String apiVersion;

    @Option(names = {"--kind"},
            required = true,
            description = "The kind of the resource (e.g., 'KafkaTopic')."
    )
    public String kind;

    @Inject
    private JikkouApi api;

    /** {@inheritDoc} **/
    @Override
    public Integer call() throws Exception {
        ResourceType resourceType = ResourceType.of(kind, apiVersion);
        ApiResourceSchema schema = api.getResourceSchema(resourceType);
        String json = Jackson.JSON_OBJECT_MAPPER
                .writerWithDefaultPrettyPrinter()
                .writeValueAsString(schema.schema());
        System.out.println(json);
        return CommandLine.ExitCode.OK;
    }
}
