/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.jikkou.client.command.validate;

import io.jikkou.client.command.CLIBaseCommand;
import io.jikkou.client.command.ConfigOptionsMixin;
import io.jikkou.client.command.FileOptionsMixin;
import io.jikkou.client.command.FormatOptionsMixin;
import io.jikkou.client.command.ProviderOptionMixin;
import io.jikkou.client.command.SelectorOptionsMixin;
import io.jikkou.core.JikkouApi;
import io.jikkou.core.ReconciliationContext;
import io.jikkou.core.io.writer.ResourceWriter;
import io.jikkou.core.models.ApiValidationResult;
import io.jikkou.core.models.HasItems;
import io.jikkou.core.models.HasMetadata;
import io.jikkou.core.models.ResourceList;
import io.jikkou.core.repository.LocalResourceRepository;
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
    @Mixin
    ProviderOptionMixin providerOptionMixin;
    // SERVICES
    @Inject
    JikkouApi api;
    @Inject
    LocalResourceRepository localResourceRepository;
    @Inject
    ResourceWriter writer;

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer call() throws IOException {
        ApiValidationResult result = api.validate(getResources(), getReconciliationContext());
        if (result.isValid()) {
            ResourceList<HasMetadata> resources = result.get();
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
        return localResourceRepository.all(fileOptions);
    }

    @NotNull
    private ReconciliationContext getReconciliationContext() {
        return ReconciliationContext.builder()
            .dryRun(true)
            .configuration(configOptionsMixin.getConfiguration())
            .selector(selectorOptions.getResourceSelector())
            .labels(fileOptions.getLabels())
            .annotations(fileOptions.getAnnotations())
            .providerName(providerOptionMixin.getProvider())
            .build();
    }
}
