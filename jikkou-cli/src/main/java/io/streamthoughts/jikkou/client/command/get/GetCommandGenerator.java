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
import io.streamthoughts.jikkou.core.config.ConfigPropertyDescriptor;
import io.streamthoughts.jikkou.core.extension.ExtensionDescriptor;
import io.streamthoughts.jikkou.core.extension.ExtensionDescriptorRegistry;
import io.streamthoughts.jikkou.core.models.ResourceType;
import io.streamthoughts.jikkou.core.resource.ResourceCollector;
import io.streamthoughts.jikkou.core.resource.ResourceDescriptor;
import io.streamthoughts.jikkou.runtime.JikkouContext;
import jakarta.inject.Singleton;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import picocli.CommandLine;
import picocli.CommandLine.Help.Ansi.Text;
import picocli.CommandLine.Help.Column;
import picocli.CommandLine.Help.TextTable;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Model.UsageMessageSpec;

@Singleton
public class GetCommandGenerator {

    private final ApplicationContext applicationContext;

    private final ExtensionDescriptorRegistry extensionDescriptorRegistry;
    private final JikkouContext context;

    public GetCommandGenerator(@NotNull ApplicationContext applicationContext,
                               @Nullable JikkouContext context,
                               @NotNull ExtensionDescriptorRegistry registry) {
        this.applicationContext = applicationContext;
        this.extensionDescriptorRegistry = registry;
        this.context = context;
    }

    public CommandLine createGetCommandLine() {
        CommandLine cmd = new CommandLine(new GetCommand());

        Collection<ExtensionDescriptor<ResourceCollector>> descriptors = extensionDescriptorRegistry
                .findAllDescriptorsByClass(ResourceCollector.class);

        Map<String, List<String>> sections = new LinkedHashMap<>();
        for (ExtensionDescriptor<ResourceCollector> descriptor : descriptors) {
            Class<ResourceCollector> type = descriptor.type();
            List<ResourceType> resources = descriptor.supportedResources();
            for (ResourceType resource : resources) {
                GetResourceCommand command = applicationContext.getBean(GetResourceCommand.class);
                command.setResourceType(resource);
                CommandLine subcommand = new CommandLine(command);
                ResourceDescriptor resourceDescriptor = context.getResourceContext().getResourceDescriptorByType(resource);
                CommandSpec spec = subcommand.getCommandSpec();
                String subCommandName = resourceDescriptor.pluralName().orElse(resourceDescriptor.singularName());
                spec.name(subCommandName)
                        .usageMessage()
                        .header(String.format("Get all '%s' resources.", resource.getKind()))
                        .description(String.format(
                                        "Use jikkou get %s when you want to describe the state of all resources of type '%s'.",
                                        subCommandName,
                                        resource.getKind()
                                )
                        );

                spec.aliases(resourceDescriptor.shortNames().toArray(new String[0]));

                List<ConfigPropertyDescriptor> configs = ResourceCollector.getConfigPropertyDescriptors(type);
                for (ConfigPropertyDescriptor config : configs) {
                    spec.addOption(createOptionSpec(config, command)
                    );
                }
                cmd.addSubcommand(subcommand);
                sections.computeIfAbsent("%nResources for group '" + resourceDescriptor.group() + "': %n%n", k -> new ArrayList<>())
                        .add(subcommand.getCommandName());
            }
        }

        CommandGroupRenderer renderer = new CommandGroupRenderer(sections);
        cmd.getHelpSectionMap().remove(SECTION_KEY_COMMAND_LIST_HEADING);
        cmd.getHelpSectionMap().put(SECTION_KEY_COMMAND_LIST, renderer);

        return cmd;
    }

    private CommandLine.Model.OptionSpec createOptionSpec(ConfigPropertyDescriptor config, GetResourceCommand command) {
        return CommandLine.Model.OptionSpec
                .builder("--" + config.name().replaceAll("\\.", "-"))
                .type(config.type())
                .description(config.description())
                .defaultValue(config.isRequired() ? null : config.defaultValue())
                .required(config.isRequired())
                .setter(new CommandLine.Model.ISetter() {
                    @Override
                    public <T> T set(T value) {
                        return command.addOptions(config.name(), value);
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
