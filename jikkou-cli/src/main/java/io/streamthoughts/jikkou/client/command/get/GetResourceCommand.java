/*
 * Copyright 2023 StreamThoughts.
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
package io.streamthoughts.jikkou.client.command.get;

import io.streamthoughts.jikkou.api.JikkouApi;
import io.streamthoughts.jikkou.api.config.Configuration;
import io.streamthoughts.jikkou.api.io.YAMLResourceWriter;
import io.streamthoughts.jikkou.api.model.HasMetadata;
import io.streamthoughts.jikkou.api.model.ResourceType;
import io.streamthoughts.jikkou.api.selector.ExpressionResourceSelectorFactory;
import io.streamthoughts.jikkou.api.selector.ResourceSelector;
import io.streamthoughts.jikkou.client.ClientContext;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Spec;

@Command(headerHeading = "Usage:%n%n",
        descriptionHeading = "%nDescription:%n%n",
        parameterListHeading = "%nParameters:%n%n",
        optionListHeading = "%nOptions:%n%n",
        commandListHeading = "%nCommands:%n%n",
        synopsisHeading = "%n",
        header = "Display one or many resources",
        description = "Display one or many resources",
        mixinStandardHelpOptions = true
)
public class GetResourceCommand implements Callable<Integer> {

    @Spec
    private CommandSpec commandSpec;

    @Option(names = { "--selector", "-s" },
            description = "The selector expression use for including or excluding resources.")
    private final List<String> selectors = new ArrayList<>();

    private final Map<String, Object> options = new HashMap<>();

    private ResourceType resourceType;

    public GetResourceCommand(ResourceType resourceType) {
        this.resourceType = resourceType;
    }

    // Picocli require an empty constructor to generate the completion file
    public GetResourceCommand() {}

    /** {@inheritDoc} **/
    @Override
    public Integer call() throws Exception {
        List<ResourceSelector> resourceSelectors = new ExpressionResourceSelectorFactory().make(selectors);
        try {
            try(JikkouApi api = ClientContext.get().createApi()) {
                List<HasMetadata> resources = api.getResources(
                        resourceType,
                        resourceSelectors,
                        Configuration.from(options)
                );
                try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                    YAMLResourceWriter.instance().write(resources, baos);
                    System.out.println(baos);
                    return CommandLine.ExitCode.OK;
                }
            }

        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public void setResourceType(ResourceType resourceType) {
        this.resourceType = resourceType;
    }

    @SuppressWarnings("unchecked")
    public <T> T addOptions(final String name, final T value) {
        return (T) this.options.put(name,value);
    }
}
