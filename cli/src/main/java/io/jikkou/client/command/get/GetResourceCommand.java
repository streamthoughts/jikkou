/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.jikkou.client.command.get;

import io.jikkou.client.command.AbstractApiCommand;
import io.jikkou.client.command.FormatOptionsMixin;
import io.jikkou.client.command.ProviderOptionMixin;
import io.jikkou.client.command.SelectorOptionsMixin;
import io.jikkou.common.utils.Strings;
import io.jikkou.core.GetContext;
import io.jikkou.core.JikkouApi;
import io.jikkou.core.ListContext;
import io.jikkou.core.config.Configuration;
import io.jikkou.core.io.writer.ResourceWriter;
import io.jikkou.core.models.HasMetadata;
import io.jikkou.core.models.ResourceList;
import io.jikkou.core.models.ResourceType;
import io.micronaut.context.annotation.Prototype;
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
    @Mixin
    ProviderOptionMixin providerOptions;
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

    /**
     * Deprecated invocation form (flat subcommand name, e.g. "topics"). {@code null} when invoked via the nested form.
     */
    private String deprecatedForm;

    /**
     * Suggested replacement for the deprecated invocation (e.g. "kafka topics").
     */
    private String deprecatedReplacement;

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
        if (deprecatedForm != null) {
            System.err.println(
                "[DEPRECATED] 'jikkou get " + deprecatedForm +
                "' is deprecated; use 'jikkou get " + deprecatedReplacement +
                "' instead. The flat form will be removed in a future release."
            );
        }
        ResourceList<HasMetadata> resources;
        if (Strings.isNullOrEmpty(name)) {
            ListContext context = ListContext.builder()
                    .selector(selectorOptions.getResourceSelector())
                    .configuration(Configuration.from(options()))
                    .providerName(providerOptions.getProvider())
                    .build();
            resources = api.listResources(type, context);
        } else {
            GetContext getContext = GetContext.builder()
                    .configuration(Configuration.from(options()))
                    .providerName(providerOptions.getProvider())
                    .build();
            HasMetadata resource = api.getResource(type, name, getContext);
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

    /**
     * Tags this command instance as invoked via the deprecated flat form.
     * <p>
     * When set, {@link #call()} prints a one-line deprecation notice to {@code stderr}
     * before executing the normal listing/get logic.
     *
     * @param oldSubcommand the flat subcommand name used, e.g. "topics".
     * @param replacement   the suggested qualified form, e.g. "kafka topics".
     */
    public void setDeprecated(String oldSubcommand, String replacement) {
        this.deprecatedForm = oldSubcommand;
        this.deprecatedReplacement = replacement;
    }

}
