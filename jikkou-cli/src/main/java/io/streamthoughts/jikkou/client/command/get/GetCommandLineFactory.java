/*
 * Copyright 2023 The original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.streamthoughts.jikkou.client.command.get;

import static picocli.CommandLine.Model.UsageMessageSpec.SECTION_KEY_COMMAND_LIST;
import static picocli.CommandLine.Model.UsageMessageSpec.SECTION_KEY_COMMAND_LIST_HEADING;

import io.micronaut.context.ApplicationContext;
import io.streamthoughts.jikkou.core.JikkouApi;
import io.streamthoughts.jikkou.core.models.ApiResource;
import io.streamthoughts.jikkou.core.models.ApiResourceList;
import io.streamthoughts.jikkou.core.models.ApiResourceVerbOptionList;
import io.streamthoughts.jikkou.core.models.ApiResourceVerbOptionSpec;
import io.streamthoughts.jikkou.core.models.ResourceType;
import io.streamthoughts.jikkou.core.models.Verb;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;
import picocli.CommandLine;
import picocli.CommandLine.Help.Ansi.Text;
import picocli.CommandLine.Help.Column;
import picocli.CommandLine.Help.TextTable;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Model.UsageMessageSpec;

@Singleton
public class GetCommandLineFactory {

    private final ApplicationContext applicationContext;

    private final JikkouApi api;

    @Inject
    public GetCommandLineFactory(@NotNull ApplicationContext applicationContext,
                                 @NotNull JikkouApi api) {
        this.applicationContext = applicationContext;
        this.api = api;
    }

    public CommandLine createCommandLine() {
        CommandLine cmd = new CommandLine(new GetCommand());

        List<ApiResourceList> apiResourceLists = api.listApiResources();
        Map<String, List<String>> sections = new LinkedHashMap<>();

        for (ApiResourceList apiResourceList : apiResourceLists) {
            List<ApiResource> resources = apiResourceList.getResources()
                    .stream()
                    .filter(it -> it.isVerbSupported(Verb.LIST))
                    .toList();
            for (ApiResource resource : resources) {
                ResourceType type = ResourceType.create(resource.kind(), apiResourceList.getGroupVersion());

                // Create command for the current resource
                final GetResourceCommand command = applicationContext.getBean(GetResourceCommand.class);
                command.setResourceType(type);

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
                    for (ApiResourceVerbOptionSpec option : verbOptionList.options()) {
                        spec.addOption(createOptionSpec(option, command)
                        );
                    }
                }
                cmd.addSubcommand(subcommand);
                sections.computeIfAbsent("%nResources for group '" + type.getGroup() + "': %n%n", k -> new ArrayList<>())
                        .add(subcommand.getCommandName());
            }
        }

        CommandGroupRenderer renderer = new CommandGroupRenderer(sections);
        cmd.getHelpSectionMap().remove(SECTION_KEY_COMMAND_LIST_HEADING);
        cmd.getHelpSectionMap().put(SECTION_KEY_COMMAND_LIST, renderer);

        return cmd;
    }

    private CommandLine.Model.OptionSpec createOptionSpec(ApiResourceVerbOptionSpec option,
                                                          GetResourceCommand command) {
        return CommandLine.Model.OptionSpec
                .builder("--" + option.name().replaceAll("\\.", "-"))
                .type(option.typeClass())
                .description(option.description())
                .defaultValue(option.required() ? null : option.defaultValue())
                .required(option.required())
                .setter(new CommandLine.Model.ISetter() {
                    @Override
                    public <T> T set(T value) {
                        return command.addOptions(option.name(), value);
                    }
                })
                .build();
    }

    /**
     * From picocli-examples:
     * <a href="https://github.com/remkop/picocli/blob/main/picocli-examples/src/main/java/picocli/examples/customhelp/GroupingDemo.java">...</a>
     */
    public static class CommandGroupRenderer implements CommandLine.IHelpSectionRenderer {
        private final Map<String, List<String>> sections;

        public CommandGroupRenderer(Map<String, List<String>> sections) {
            this.sections = sections;
        }

        //@Override
        public String render(CommandLine.Help help) {
            if (help.commandSpec().subcommands().isEmpty()) {
                return "";
            }

            StringBuilder result = new StringBuilder();
            sections.forEach((key, value) -> result.append(renderSection(key, value, help)));
            return result.toString();
        }

        private String renderSection(String sectionHeading, List<String> cmdNames, CommandLine.Help help) {
            TextTable textTable = createTextTable(help);

            for (String name : cmdNames) {
                CommandSpec sub = help.commandSpec().subcommands().get(name).getCommandSpec();

                // create comma-separated list of command name and aliases
                String names = sub.names().toString();
                names = names.substring(1, names.length() - 1); // remove leading '[' and trailing ']'

                // description may contain line separators; use Text::splitLines to handle this
                String description = description(sub.usageMessage());
                Text[] lines = help.colorScheme().text(String.format(description)).splitLines();

                for (int i = 0; i < lines.length; i++) {
                    Text cmdNamesText = help.colorScheme().commandText(i == 0 ? names : "");
                    textTable.addRowValues(cmdNamesText, lines[i]);
                }
            }
            return help.createHeading(sectionHeading) + textTable.toString();
        }

        private TextTable createTextTable(CommandLine.Help help) {
            CommandSpec spec = help.commandSpec();
            // prepare layout: two columns
            // the left column overflows, the right column wraps if text is too long
            int commandLength = maxLength(spec.subcommands(), 37);
            TextTable textTable = TextTable.forColumns(help.colorScheme(),
                    new Column(commandLength + 2, 2, Column.Overflow.SPAN),
                    new Column(spec.usageMessage().width() - (commandLength + 2), 2, Column.Overflow.WRAP));
            textTable.setAdjustLineBreaksForWideCJKCharacters(spec.usageMessage().adjustLineBreaksForWideCJKCharacters());
            return textTable;
        }

        private int maxLength(Map<String, CommandLine> subcommands, int max) {
            int result = subcommands.values()
                    .stream().map(cmd -> cmd.getCommandSpec().names().toString().length() - 2)
                    .max(Integer::compareTo)
                    .orElse(max);
            return Math.min(max, result);
        }

        private String description(UsageMessageSpec usageMessage) {
            if (usageMessage.header().length > 0) {
                return usageMessage.header()[0];
            }
            if (usageMessage.description().length > 0) {
                return usageMessage.description()[0];
            }
            return "";
        }
    }
}
