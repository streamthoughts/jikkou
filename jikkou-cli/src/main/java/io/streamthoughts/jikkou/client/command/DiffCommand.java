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
package io.streamthoughts.jikkou.client.command;

import io.streamthoughts.jikkou.api.control.Change;
import io.streamthoughts.jikkou.api.io.Jackson;
import io.streamthoughts.jikkou.api.io.YAMLResourceLoader;
import io.streamthoughts.jikkou.api.model.HasItems;
import io.streamthoughts.jikkou.api.model.HasMetadataChange;
import io.streamthoughts.jikkou.api.model.ResourceListObject;
import io.streamthoughts.jikkou.api.template.JinjaResourceTemplateRenderer;
import io.streamthoughts.jikkou.api.template.ResourceTemplateRenderer;
import io.streamthoughts.jikkou.client.ClientContext;
import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.concurrent.Callable;
import org.jetbrains.annotations.NotNull;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;

@Command(name = "diff",
        headerHeading = "Usage:%n%n",
        descriptionHeading = "%nDescription:%n%n",
        parameterListHeading = "%nParameters:%n%n",
        optionListHeading = "%nOptions:%n%n",
        commandListHeading = "%nCommands:%n%n",
        synopsisHeading = "%n",
        header = "Display all resource changes.",
        description = "This command can be used to get all detected changes for the resource definition files.",
        mixinStandardHelpOptions = true)
public class DiffCommand implements Callable<Integer> {

    @Mixin
    FileOptionsMixin fileOptions;

    @Mixin
    SelectorOptionsMixin selectorOptions;

    enum Formats { JSON, YAML }

    @CommandLine.Option(names = { "--output", "-o" },
            defaultValue = "YAML",
            description = "Prints the output in the specified format. Allowed values: json, yaml (default yaml)."
    )
    public Formats format;

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer call() {
        try (var api = ClientContext.get().createApi()) {
            List<ResourceListObject<HasMetadataChange<Change>>> changes = api.getDiff(loadResources(), selectorOptions.getResourceSelectors());
            try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                var objectMapper = switch (format) {
                    case JSON -> Jackson.JSON_OBJECT_MAPPER;
                    case YAML -> Jackson.YAML_OBJECT_MAPPER;
                };
                for (Object item : changes) {
                    objectMapper.writeValue(baos, item);
                }
                System.out.println(baos);
            } catch (Exception e) {
                throw new RuntimeException("Failed to serialize object", e);
            }
        }

        return CommandLine.ExitCode.OK;
    }

    protected @NotNull HasItems loadResources() {
        ResourceTemplateRenderer renderer = new JinjaResourceTemplateRenderer()
                .withPreserveRawTags(false)
                .withFailOnUnknownTokens(false);

        YAMLResourceLoader loader = new YAMLResourceLoader(renderer);
        return loader.load(fileOptions);
    }
}
