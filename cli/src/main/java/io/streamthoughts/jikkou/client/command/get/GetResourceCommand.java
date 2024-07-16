/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.client.command.get;

import io.micronaut.context.annotation.Prototype;
import io.streamthoughts.jikkou.client.command.AbstractApiCommand;
import io.streamthoughts.jikkou.client.command.FormatOptionsMixin;
import io.streamthoughts.jikkou.client.command.SelectorOptionsMixin;
import io.streamthoughts.jikkou.common.utils.Strings;
import io.streamthoughts.jikkou.core.JikkouApi;
import io.streamthoughts.jikkou.core.config.Configuration;
import io.streamthoughts.jikkou.core.io.writer.ResourceWriter;
import io.streamthoughts.jikkou.core.models.HasMetadata;
import io.streamthoughts.jikkou.core.models.ResourceList;
import io.streamthoughts.jikkou.core.models.ResourceType;
import jakarta.inject.Inject;
import java.io.ByteArrayOutputStream;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;

@Command(
        header = "Display one or many resources",
        description = "Display one or many resources"
)
@Prototype
public class GetResourceCommand extends AbstractApiCommand {

    // COMMAND OPTIONS
    @Mixin
    SelectorOptionsMixin selectorOptions;
    @Mixin
    FormatOptionsMixin formatOptions;
    @Option(names = {"--list"},
            defaultValue = "false",
            description = "Get resources as ResourceListObject (default: ${DEFAULT-VALUE})."
    )
    private boolean list;

    /**
     * The resource name (optional).
     */
    private String name;

    /**
     * The resource type.
     */
    private ResourceType type;

    // SERVICES
    @Inject
    JikkouApi api;
    @Inject
    ResourceWriter writer;

    // Picocli require an empty constructor to generate the completion file
    public GetResourceCommand() {
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public Integer call() throws Exception {
        ResourceList<HasMetadata> resources;
        if (Strings.isBlank(name)) {
            resources = api.listResources(
                    type,
                    selectorOptions.getResourceSelector(),
                    Configuration.from(options())
            );
        } else {
            HasMetadata resource = api.getResource(
                    type,
                    name,
                    Configuration.from(options())
            );
            resources = ResourceList.of(resource);
        }
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            if (list) {
                writer.write(formatOptions.format(), resources, baos);
            } else {
                writer.write(formatOptions.format(), resources.getItems(), baos);
            }
            System.out.println(baos);
            return CommandLine.ExitCode.OK;
        }
    }

    /**
     * Sets the resource name.
     * @param name The resource name.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Sets the resource type.
     * @param type The resource type.
     */
    public void setType(ResourceType type) {
        this.type = type;
    }

}
