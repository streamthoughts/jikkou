/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.client.command.action;

import static picocli.CommandLine.Model.UsageMessageSpec.SECTION_KEY_COMMAND_LIST;
import static picocli.CommandLine.Model.UsageMessageSpec.SECTION_KEY_COMMAND_LIST_HEADING;

import io.micronaut.context.ApplicationContext;
import io.streamthoughts.jikkou.client.command.AbstractCommandLineFactory;
import io.streamthoughts.jikkou.client.renderer.CommandGroupRenderer;
import io.streamthoughts.jikkou.common.utils.Pair;
import io.streamthoughts.jikkou.core.JikkouApi;
import io.streamthoughts.jikkou.core.extension.ExtensionCategory;
import io.streamthoughts.jikkou.core.models.ApiExtension;
import io.streamthoughts.jikkou.core.models.ApiExtensionList;
import io.streamthoughts.jikkou.core.models.ApiExtensionSummary;
import io.streamthoughts.jikkou.core.models.ApiOptionSpec;
import io.streamthoughts.jikkou.core.models.ResourceType;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import picocli.CommandLine;
import picocli.CommandLine.Model.CommandSpec;

@Singleton
public final class ActionCommandLineFactory extends AbstractCommandLineFactory {

    @Inject
    public ActionCommandLineFactory(@NotNull ApplicationContext applicationContext,
                                    @NotNull JikkouApi api) {
        super(applicationContext, api);
    }

    /** {@inheritDoc} **/
    @Override
    public CommandLine createCommandLine() {
        CommandLine cmd = new CommandLine(new ListActionCommand())
                .setCaseInsensitiveEnumValuesAllowed(true)
                .setSubcommandsCaseInsensitive(true);

        ApiExtensionList extensions = api.getApiExtensions(ExtensionCategory.ACTION);

        // Multiplex ApiExtensions for each supported resource type.
        Map<ResourceType, List<ApiExtension>> extensionBySupportedResource = extensions.extensions()
                .stream()
                .map(ApiExtensionSummary::name)
                .map(api::getApiExtension)
                .flatMap(extension -> extension
                        .spec()
                        .resources()
                        .stream()
                        .map(resource -> Pair.of(resource, extension))
                )
                .collect(Collectors.groupingBy(Pair::_1, Collectors.mapping(Pair::_2, Collectors.toList())));

        Map<String, List<String>> sections = new LinkedHashMap<>();
        for (Map.Entry<ResourceType, List<ApiExtension>> entry : extensionBySupportedResource.entrySet()) {
            final ResourceType type = entry.getKey();
            for(ApiExtension apiExtension : entry.getValue()) {
                // Create ACTION TOP Subcommand
                final ActionCommand actionCommand = applicationContext.getBean(ActionCommand.class);
                final String subCommandName = apiExtension.spec().name();
                final CommandLine actionCommandLine = new CommandLine(actionCommand)
                        .setCaseInsensitiveEnumValuesAllowed(true)
                        .setSubcommandsCaseInsensitive(true);

                CommandSpec actionCommandSpec = actionCommandLine.getCommandSpec();
                actionCommandSpec
                        .name(subCommandName)
                        .usageMessage()
                        .header(apiExtension.spec().title())
                        .description(apiExtension.spec().description());

                // Create ACTION EXECUTE Subcommand
                final ExecuteActionCommand executeCommand = applicationContext.getBean(ExecuteActionCommand.class);
                executeCommand.setName(actionCommandSpec.name());

                final CommandLine executeCommandLine = new CommandLine(executeCommand)
                        .setCaseInsensitiveEnumValuesAllowed(true);

                CommandSpec executeCommandSpec = executeCommandLine.getCommandSpec();
                executeCommandSpec
                        .name("execute")
                        .usageMessage()
                        .description(apiExtension.spec().description());
                // Build Options
                List<ApiOptionSpec> optionSpecs = apiExtension.spec().options();
                if (!optionSpecs.isEmpty()) {
                    for (ApiOptionSpec optionSpec : optionSpecs) {
                        executeCommandSpec.addOption(createOptionSpec(optionSpec, executeCommand));
                    }
                }
                actionCommandLine.addSubcommand(executeCommandLine);
                cmd.addSubcommand(actionCommandLine);
                sections.computeIfAbsent("%nACTIONS FOR API GROUP '" + type.group() + "': %n%n", k -> new ArrayList<>())
                        .add(actionCommandLine.getCommandName());
            }
        }
        CommandGroupRenderer renderer = new CommandGroupRenderer(sections);
        cmd.getHelpSectionMap().remove(SECTION_KEY_COMMAND_LIST_HEADING);
        cmd.getHelpSectionMap().put(SECTION_KEY_COMMAND_LIST, renderer);

        return cmd;
    }
}
