/*
 * Copyright 2021 The original authors
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
package io.streamthoughts.jikkou.client.command.extension;

import com.github.freva.asciitable.AsciiTable;
import com.github.freva.asciitable.Column;
import com.github.freva.asciitable.HorizontalAlign;
import io.streamthoughts.jikkou.core.extension.Extension;
import io.streamthoughts.jikkou.core.extension.ExtensionDescriptor;
import io.streamthoughts.jikkou.core.extension.ExtensionDescriptorRegistry;
import io.streamthoughts.jikkou.core.models.ResourceType;
import io.streamthoughts.jikkou.core.reconcilier.Controller;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "list",
        headerHeading = "Usage:%n%n",
        descriptionHeading = "%nDescription:%n%n",
        parameterListHeading = "%nParameters:%n%n",
        optionListHeading = "%nOptions:%n%n",
        commandListHeading = "%nCommands:%n%n",
        synopsisHeading = "%n",
        header = "Get all the extensions.",
        description = "Get all the extensions.",
        mixinStandardHelpOptions = true)
@Singleton
public class ListExtensionCommand implements Runnable {

    @Option(names = {"--type"},
            required = false,
            description = "Limit to extensions of the specified type."
    )
    public String type;

    @Option(names = {"--kinds"},
            required = false,
            split = ",",
            description = "Limit to extensions that support the specified resource kind."
    )
    public List<String> kinds = Collections.emptyList();

    @Inject
    private ExtensionDescriptorRegistry extensionDescriptorRegistry;

    /**
     * {@inheritDoc}
     **/
    @Override
    public void run() {

        List<ExtensionDescriptor<Extension>> descriptors;

        if (type != null) {
            descriptors = extensionDescriptorRegistry.findAllDescriptorsByAlias(type);
        } else {
            descriptors = extensionDescriptorRegistry.findAllDescriptorsByClass(Extension.class);
        }

        if (kinds != null && !kinds.isEmpty()) {
            descriptors = descriptors
                    .stream()
                    .filter(it -> {
                        List<String> list = it.supportedResources().stream().map(ResourceType::getKind).toList();
                        return !Collections.disjoint(list, kinds);
                    })
                    .sorted(Comparator.comparing(ExtensionDescriptor::name))
                    .toList();
        }

        String[][] data = descriptors
                .stream()
                .map(descriptor -> new String[]{
                        descriptor.name(),
                        descriptor.category(),
                        String.valueOf(descriptor.isEnabled()),
                        descriptor.source(),
                        descriptor.printableSupportedResources(),
                        Controller.supportedReconciliationModes(descriptor.type())
                                .stream()
                                .map(Enum::name)
                                .map(String::toLowerCase)
                                .sorted()
                                .collect(Collectors.joining(", "))
                })
                .toArray(String[][]::new);

        String table = AsciiTable.getTable(AsciiTable.NO_BORDERS,
                new Column[]{
                        new Column().header("NAME").dataAlign(HorizontalAlign.LEFT),
                        new Column().header("CATEGORY").dataAlign(HorizontalAlign.LEFT),
                        new Column().header("ENABLED").dataAlign(HorizontalAlign.LEFT),
                        new Column().header("SOURCE").dataAlign(HorizontalAlign.LEFT),
                        new Column().header("ACCEPTED RESOURCES").dataAlign(HorizontalAlign.LEFT),
                        new Column().header("ACTIONS").dataAlign(HorizontalAlign.LEFT)
                },
                data);
        System.out.println(table);
    }
}
