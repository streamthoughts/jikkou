/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.client.command.validate;

import io.streamthoughts.jikkou.client.command.CLIBaseCommand;
import io.streamthoughts.jikkou.client.command.ConfigOptionsMixin;
import io.streamthoughts.jikkou.client.command.FileOptionsMixin;
import io.streamthoughts.jikkou.client.command.FormatOptionsMixin;
import io.streamthoughts.jikkou.client.command.SelectorOptionsMixin;
import io.streamthoughts.jikkou.core.JikkouApi;
import io.streamthoughts.jikkou.core.ReconciliationContext;
import io.streamthoughts.jikkou.core.io.ResourceLoaderFacade;
import io.streamthoughts.jikkou.core.io.writer.ResourceWriter;
import io.streamthoughts.jikkou.core.models.ApiValidationResult;
import io.streamthoughts.jikkou.core.models.HasItems;
import io.streamthoughts.jikkou.core.models.HasMetadata;
import io.streamthoughts.jikkou.core.models.ResourceListObject;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.Callable;
import org.jetbrains.annotations.NotNull;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;

@Command(name = "validate",
        header = "Check whether the resources definitions meet all validation requirements.",
        description = """
                Validate the resource definition files specified through the command line arguments.
                
                Validate runs all the user-defined validation requirements after performing any relevant resource transformations.
                Validation rules are applied only to resources matching the selectors passed through the command line arguments.
                """
)
@Singleton
public class ValidateCommand extends CLIBaseCommand implements Callable<Integer> {

    // COMMAND OPTIONS
    @Mixin
    FileOptionsMixin fileOptions;
    @Mixin
    SelectorOptionsMixin selectorOptions;
    @Mixin
    FormatOptionsMixin formatOptions;
    @Mixin
    ConfigOptionsMixin configOptionsMixin;

    // SERVICES
    @Inject
    JikkouApi api;
    @Inject
    ResourceLoaderFacade loader;
    @Inject
    ResourceWriter writer;

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer call() throws IOException {
        ApiValidationResult result = api.validate(getResources(), getReconciliationContext());
        if (result.isValid()) {
            ResourceListObject<HasMetadata> resources = result.get();
            try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                writer.write(formatOptions.format(), resources.getItems(), baos);
                System.out.println(baos);
                return CommandLine.ExitCode.OK;
            }
        }
        System.out.println(ValidationErrorsWriter.write(result.errors()));
        return CommandLine.ExitCode.SOFTWARE;
    }

    @NotNull
    private HasItems getResources() {
        return loader.load(fileOptions);
    }

    @NotNull
    private ReconciliationContext getReconciliationContext() {
        return ReconciliationContext.builder()
                .dryRun(true)
                .configuration(configOptionsMixin.getConfiguration())
                .selector(selectorOptions.getResourceSelector())
                .labels(fileOptions.getLabels())
                .annotations(fileOptions.getAnnotations())
                .build();
    }
}
