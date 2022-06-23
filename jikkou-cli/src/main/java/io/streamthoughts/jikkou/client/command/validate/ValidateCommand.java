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

import io.streamthoughts.jikkou.api.io.ResourceLoader;
import io.streamthoughts.jikkou.api.io.YAMLResourceWriter;
import io.streamthoughts.jikkou.api.io.readers.ResourceReaderOptions;
import io.streamthoughts.jikkou.api.model.NamedValue;
import io.streamthoughts.jikkou.api.model.ResourceList;
import io.streamthoughts.jikkou.client.JikkouConfig;
import io.streamthoughts.jikkou.client.JikkouContext;
import io.streamthoughts.jikkou.client.command.ResourceFileOptionsMixin;
import io.streamthoughts.jikkou.client.command.SetOptionsMixin;
import io.streamthoughts.jikkou.kafka.LegacyKafkaClusterResourceHandler;
import java.io.ByteArrayOutputStream;
import java.util.concurrent.Callable;
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
        header = "Validate your specification file.",
        description = "This command can be used to validate a Kafka Specs file.",
        mixinStandardHelpOptions = true)
public class ValidateCommand implements Callable<Integer> {

    @Mixin
    ResourceFileOptionsMixin specOptions;

    @Mixin
    SetOptionsMixin setOptions;

    public static void main(String[] args) {
        JikkouContext.setConfig(JikkouConfig.load());
        new ValidateCommand().call();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer call() {
        try (var api = JikkouContext.jikkouApi()) {
            ResourceList resources = ResourceLoader
                    .create()
                    .options(new ResourceReaderOptions()
                            .withLabels(NamedValue.setOf(setOptions.clientLabels))
                            .withValues(NamedValue.setOf(setOptions.clientValues))
                            .withPattern(specOptions.pattern)
                            .withTemplatingEnable(true)
                    )
                    .valuesFiles(setOptions.valuesFiles)
                    .load(specOptions.files);

            resources = new LegacyKafkaClusterResourceHandler().handle(resources);

            api.validate(resources)
                    .forEach(resource -> {
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        YAMLResourceWriter.instance().write(resource, baos);
                        System.out.println(baos);
                    });
        }

        return CommandLine.ExitCode.OK;
    }

}
