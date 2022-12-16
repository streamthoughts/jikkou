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

import io.streamthoughts.jikkou.api.JikkouApi;
import io.streamthoughts.jikkou.api.ReconciliationContext;
import io.streamthoughts.jikkou.api.ReconciliationMode;
import io.streamthoughts.jikkou.api.SimpleJikkouApi;
import io.streamthoughts.jikkou.api.config.Configuration;
import io.streamthoughts.jikkou.api.control.ChangeResult;
import io.streamthoughts.jikkou.api.io.Jackson;
import io.streamthoughts.jikkou.api.io.ResourceLoader;
import io.streamthoughts.jikkou.api.io.readers.ResourceReaderFactory;
import io.streamthoughts.jikkou.api.io.readers.ResourceReaderOptions;
import io.streamthoughts.jikkou.api.model.NamedValue;
import io.streamthoughts.jikkou.api.model.ResourceList;
import io.streamthoughts.jikkou.api.template.JinjaResourceTemplateRenderer;
import io.streamthoughts.jikkou.client.CLIUtils;
import io.streamthoughts.jikkou.client.JikkouContext;
import io.streamthoughts.jikkou.kafka.LegacyKafkaClusterResourceHandler;
import java.util.Collection;
import org.jetbrains.annotations.NotNull;
import picocli.CommandLine;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Spec;

public abstract class BaseResourceCommand extends BaseCommand {

    @Mixin
    ResourceFileOptionsMixin specOptions;

    @Spec
    private CommandSpec spec;

    @Mixin
    SetOptionsMixin options;

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer call() {
        if (!execOptions.yes && !isDryRun()) {
            CLIUtils.askToProceed(spec);
        }

        try (SimpleJikkouApi api = JikkouContext.jikkouApi()) {

            ResourceList resource = loadResources();

            final Collection<ChangeResult<?>> results = execute(api, resource);
            execOptions.format.print(results, isDryRun());
            return CommandLine.ExitCode.OK;
        }
    }

    @NotNull
    protected ResourceList loadResources() {
        ResourceReaderFactory factory = new ResourceReaderFactory()
                .setTemplateEnable(true)
                .setTemplateRenderer(
                        new JinjaResourceTemplateRenderer()
                                .withPreserveRawTags(false)
                                .withFailOnUnknownTokens(false)
                )
                .setObjectMapper(Jackson.YAML_OBJECT_MAPPER);

        ResourceList resources = ResourceLoader
                .create()
                .withResourceReaderFactory(factory)
                .withResourceReaderOptions(new ResourceReaderOptions()
                        .withLabels(NamedValue.setOf(options.clientLabels))
                        .withValues(NamedValue.setOf(options.clientValues))
                        .withPattern(specOptions.pattern)
                )
                .withValuesFiles(options.valuesFiles)
                .load(specOptions.files);

        return new LegacyKafkaClusterResourceHandler().handle(resources);
    }

    public abstract ReconciliationMode getReconciliationMode();

    public abstract Configuration getConfiguration();

    public Collection<ChangeResult<?>> execute(@NotNull JikkouApi api,
                                               @NotNull ResourceList resource) {

        return api.apply(
                resource,
                getReconciliationMode(),
                ReconciliationContext.with(
                        getResourceByName(),
                        getConfiguration(),
                        isDryRun()
                ));
    }

    public boolean isDryRun() {
        return execOptions.dryRun;
    }

}
