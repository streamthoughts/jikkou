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

import io.micronaut.context.annotation.Prototype;
import io.streamthoughts.jikkou.client.command.CLIBaseCommand;
import io.streamthoughts.jikkou.client.command.FormatOptionsMixin;
import io.streamthoughts.jikkou.client.command.SelectorOptionsMixin;
import io.streamthoughts.jikkou.core.JikkouApi;
import io.streamthoughts.jikkou.core.config.Configuration;
import io.streamthoughts.jikkou.core.io.writer.ResourceWriter;
import io.streamthoughts.jikkou.core.models.HasMetadata;
import io.streamthoughts.jikkou.core.models.ResourceListObject;
import io.streamthoughts.jikkou.core.models.ResourceType;
import io.streamthoughts.jikkou.core.selectors.ExpressionSelectorFactory;
import io.streamthoughts.jikkou.core.selectors.Selector;
import jakarta.inject.Inject;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Spec;

@Command(
        header = "Display one or many resources",
        description = "Display one or many resources"
)
@Prototype
public class GetResourceCommand extends CLIBaseCommand implements Callable<Integer> {

    // COMMAND OPTIONS
    @Spec
    private CommandSpec commandSpec;
    @Mixin
    SelectorOptionsMixin selectorOptions;
    @Mixin
    FormatOptionsMixin formatOptions;

    @Option(names = { "--list" },
            defaultValue = "false",
            description = "Get resources as ResourceListObject."
    )
    private boolean list = false;

    private final Map<String, Object> options = new HashMap<>();
    private ResourceType resourceType;

    // SERVICES
    @Inject
    JikkouApi api;
    @Inject
    ResourceWriter writer;

    // Picocli require an empty constructor to generate the completion file
    public GetResourceCommand() {}

    /** {@inheritDoc} **/
    @Override
    public Integer call() throws Exception {
        ResourceListObject<HasMetadata> resources = api.getResources(
                resourceType,
                getResourceSelectors(),
                Configuration.from(options)
        );
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

    private List<Selector> getResourceSelectors() {
        return new ExpressionSelectorFactory().make(selectorOptions.expressions);
    }

    public void setResourceType(ResourceType resourceType) {
        this.resourceType = resourceType;
    }

    @SuppressWarnings("unchecked")
    public <T> T addOptions(final String name, final T value) {
        return (T) this.options.put(name,value);
    }
}
