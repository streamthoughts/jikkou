/*
 * Copyright 2021 StreamThoughts.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
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
import io.streamthoughts.jikkou.api.control.ExternalResourceController;
import io.streamthoughts.jikkou.api.extensions.ExtensionDescriptor;
import io.streamthoughts.jikkou.api.extensions.ExtensionFactory;
import io.streamthoughts.jikkou.api.model.ResourceType;
import io.streamthoughts.jikkou.client.ClientContext;
import java.util.Collection;
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
public class ListExtensionCommand implements Runnable {

    @Option(names = { "--type"},
            required = false,
            description = "Limit to extensions of the specified type."
    )
    public String type;

    @Option(names = { "--kinds"},
            required = false,
            split = ",",
            description = "Limit to extensions that support the specified resource kind."
    )
    public List<String> kinds = Collections.emptyList();


    /** {@inheritDoc} **/
    @Override
    public void run() {
        ClientContext context = ClientContext.get();

        ExtensionFactory factory = context.getExtensionFactory();
        Collection<ExtensionDescriptor<?>> extensions = factory.allExtensionTypes();

        if (type != null) {
            extensions = extensions
                    .stream()
                    .filter(it -> it.type() != null && it.type().equals(type))
                    .toList();
        }

        if (kinds != null && !kinds.isEmpty()) {
            extensions = extensions
                    .stream()
                    .filter(it -> {
                        List<String> list = it.getSupportedResources().stream().map(ResourceType::getKind).toList();
                        return !Collections.disjoint(list, kinds);
                    })
                    .sorted(Comparator.comparing(ExtensionDescriptor::name))
                    .toList();
        }

        String[][] data = extensions
                .stream()
                .map(descriptor -> new String[]{
                        descriptor.name(),
                        descriptor.type(),
                        String.valueOf(descriptor.isEnabled()),
                        descriptor.getSource(),
                        descriptor.getPrintableSupportedResources(),
                        ExternalResourceController.supportedReconciliationModes(descriptor.clazz())
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
                        new Column().header("TYPE").dataAlign(HorizontalAlign.LEFT),
                        new Column().header("ENABLED").dataAlign(HorizontalAlign.LEFT),
                        new Column().header("SOURCE").dataAlign(HorizontalAlign.LEFT),
                        new Column().header("ACCEPTED RESOURCES").dataAlign(HorizontalAlign.LEFT),
                        new Column().header("ACTIONS").dataAlign(HorizontalAlign.LEFT)
                },
                data);
        System.out.println(table);
    }
}
