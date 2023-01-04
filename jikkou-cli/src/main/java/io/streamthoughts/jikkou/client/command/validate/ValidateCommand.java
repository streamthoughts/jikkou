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
package io.streamthoughts.jikkou.client.command.validate;

import io.streamthoughts.jikkou.api.io.YAMLResourceLoader;
import io.streamthoughts.jikkou.api.io.YAMLResourceWriter;
import io.streamthoughts.jikkou.api.model.ResourceList;
import io.streamthoughts.jikkou.api.template.JinjaResourceTemplateRenderer;
import io.streamthoughts.jikkou.api.template.ResourceTemplateRenderer;
import io.streamthoughts.jikkou.client.JikkouContext;
import io.streamthoughts.jikkou.client.command.FileOptionsMixin;
import io.streamthoughts.jikkou.kafka.LegacyKafkaClusterResourceHandler;
import java.io.ByteArrayOutputStream;
import java.util.concurrent.Callable;
import org.jetbrains.annotations.NotNull;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;

@Command(name = "validate",
        headerHeading = "Usage:%n%n",
        descriptionHeading = "%nDescription:%n%n",
        parameterListHeading = "%nParameters:%n%n",
        optionListHeading = "%nOptions:%n%n",
        commandListHeading = "%nCommands:%n%n",
        synopsisHeading = "%n",
        header = "Validate your resource definition files.",
        description = "This command can be used to validate resource definition file.",
        mixinStandardHelpOptions = true)
public class ValidateCommand implements Callable<Integer> {

    @Mixin
    FileOptionsMixin fileOptions;

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer call() {
        try (var api = JikkouContext.jikkouApi()) {
            ResourceList resources = loadResources();

            api.validate(resources)
                    .forEach(resource -> {
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        YAMLResourceWriter.instance().write(resource, baos);
                        System.out.println(baos);
                    });
        }

        return CommandLine.ExitCode.OK;
    }

    protected @NotNull ResourceList loadResources() {
        ResourceTemplateRenderer renderer = new JinjaResourceTemplateRenderer()
                .withPreserveRawTags(false)
                .withFailOnUnknownTokens(false);

        YAMLResourceLoader loader = new YAMLResourceLoader(renderer);
        ResourceList resources = loader.load(fileOptions);

        return new LegacyKafkaClusterResourceHandler().handle(resources);
    }

}
