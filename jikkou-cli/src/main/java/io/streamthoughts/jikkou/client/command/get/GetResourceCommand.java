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
import io.streamthoughts.jikkou.client.command.FormatOptionsMixin;
import io.streamthoughts.jikkou.client.command.SelectorOptionsMixin;
import io.streamthoughts.jikkou.core.JikkouApi;
import io.streamthoughts.jikkou.core.config.Configuration;
import io.streamthoughts.jikkou.core.io.writer.ResourceWriter;
import io.streamthoughts.jikkou.core.models.HasMetadata;
import io.streamthoughts.jikkou.core.models.ResourceType;
import io.streamthoughts.jikkou.core.selectors.ExpressionResourceSelectorFactory;
import io.streamthoughts.jikkou.core.selectors.ResourceSelector;
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
@Prototype
public class GetResourceCommand implements Callable<Integer> {

    // COMMAND OPTIONS
    @Spec
    private CommandSpec commandSpec;
    @Mixin
    SelectorOptionsMixin selectorOptions;
    @Mixin
    FormatOptionsMixin formatOptions;

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
        List<HasMetadata> resources = api.getResources(
                resourceType,
                getResourceSelectors(),
                Configuration.from(options)
        );
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            writer.write(formatOptions.format(), resources, baos);
            System.out.println(baos);
            return CommandLine.ExitCode.OK;
        }
    }

    private List<ResourceSelector> getResourceSelectors() {
        return new ExpressionResourceSelectorFactory().make(selectorOptions.expressions);
    }

    public void setResourceType(ResourceType resourceType) {
        this.resourceType = resourceType;
    }

    @SuppressWarnings("unchecked")
    public <T> T addOptions(final String name, final T value) {
        return (T) this.options.put(name,value);
    }
}
