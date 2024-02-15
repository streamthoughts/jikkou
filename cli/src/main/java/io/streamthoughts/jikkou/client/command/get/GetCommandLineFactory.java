/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.client.command.get;

import static picocli.CommandLine.Model.UsageMessageSpec.SECTION_KEY_COMMAND_LIST;
import static picocli.CommandLine.Model.UsageMessageSpec.SECTION_KEY_COMMAND_LIST_HEADING;

import io.micronaut.context.ApplicationContext;
import io.streamthoughts.jikkou.client.command.AbstractCommandLineFactory;
import io.streamthoughts.jikkou.client.renderer.CommandGroupRenderer;
import io.streamthoughts.jikkou.core.JikkouApi;
import io.streamthoughts.jikkou.core.models.ApiOptionSpec;
import io.streamthoughts.jikkou.core.models.ApiResource;
import io.streamthoughts.jikkou.core.models.ApiResourceList;
import io.streamthoughts.jikkou.core.models.ApiResourceVerbOptionList;
import io.streamthoughts.jikkou.core.models.ResourceType;
import io.streamthoughts.jikkou.core.models.Verb;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;
import picocli.CommandLine;
import picocli.CommandLine.Model.CommandSpec;

@Singleton
public final class GetCommandLineFactory extends AbstractCommandLineFactory {

    @Inject
    public GetCommandLineFactory(@NotNull ApplicationContext applicationContext,
                                 @NotNull JikkouApi api) {
        super(applicationContext, api);
    }

    public CommandLine createCommandLine() {
        CommandLine cmd = new CommandLine(new GetCommand());

        List<ApiResourceList> apiResourceLists = api.listApiResources();
        Map<String, List<String>> sections = new LinkedHashMap<>();
        for (ApiResourceList apiResourceList : apiResourceLists) {
            List<ApiResource> resources = apiResourceList.resources()
                    .stream()
                    .filter(it -> it.isVerbSupported(Verb.LIST))
                    .toList();
            for (ApiResource resource : resources) {
                ResourceType type = ResourceType.of(resource.kind(), apiResourceList.groupVersion());

                // Create command for the current resource
                final GetResourceCommand command = applicationContext.getBean(GetResourceCommand.class);
                command.setType(type);

                // Create subcommand
                final CommandLine subcommand = new CommandLine(command);
                CommandSpec spec = subcommand.getCommandSpec();

                final String subCommandName = resource.name();
                spec.name(subCommandName)
                        .usageMessage()
                        .header(String.format("Get all '%s' resources.", resource.kind()))
                        .description(String.format(
                                        "Use jikkou get %s when you want to describe the state of all resources of type '%s'.",
                                        subCommandName,
                                        resource.kind()
                                )
                        );

                spec.aliases(resource.shortNames().toArray(new String[0]));
                Optional<ApiResourceVerbOptionList> optional = resource.getVerbOptionList(Verb.LIST);
                if (optional.isPresent()) {
                    ApiResourceVerbOptionList verbOptionList = optional.get();
                    for (ApiOptionSpec option : verbOptionList.options()) {
                        spec.addOption(createOptionSpec(option, command));
                    }
                }
                if (resource.isVerbSupported(Verb.GET)) {
                    spec.addOption(CommandLine.Model.OptionSpec
                            .builder("--name")
                            .hasInitialValue(false)
                            .paramLabel("<name>")
                            .type(String.class)
                            .description("The name of the resource.")
                            .required(false)
                            .setter(new CommandLine.Model.ISetter() {
                                @Override
                                public <T> T set(T value) {
                                    String str = Optional.ofNullable(value).map(Objects::toString).orElse(null);
                                    command.setName(str);
                                    return null;
                                }
                            })
                            .build());
                }
                cmd.addSubcommand(subcommand);
                sections.computeIfAbsent("%nCOMMANDS FOR API GROUP '" + type.group() + "': %n%n", k -> new ArrayList<>())
                        .add(subcommand.getCommandName());
            }
        }

        CommandGroupRenderer renderer = new CommandGroupRenderer(sections);
        cmd.getHelpSectionMap().remove(SECTION_KEY_COMMAND_LIST_HEADING);
        cmd.getHelpSectionMap().put(SECTION_KEY_COMMAND_LIST, renderer);

        return cmd;
    }
}
