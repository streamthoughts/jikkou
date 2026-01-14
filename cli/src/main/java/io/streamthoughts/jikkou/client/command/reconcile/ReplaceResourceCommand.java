/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.client.command.reconcile;

import io.streamthoughts.jikkou.client.Jikkou;
import io.streamthoughts.jikkou.client.command.CLIBaseCommand;
import io.streamthoughts.jikkou.client.command.ConfigOptionsMixin;
import io.streamthoughts.jikkou.client.command.ExecOptionsMixin;
import io.streamthoughts.jikkou.client.command.FileOptionsMixin;
import io.streamthoughts.jikkou.client.command.ProviderOptionMixin;
import io.streamthoughts.jikkou.client.command.SelectorOptionsMixin;
import io.streamthoughts.jikkou.client.command.validate.ValidationErrorsWriter;
import io.streamthoughts.jikkou.core.JikkouApi;
import io.streamthoughts.jikkou.core.ReconciliationContext;
import io.streamthoughts.jikkou.core.exceptions.ValidationException;
import io.streamthoughts.jikkou.core.models.ApiChangeResultList;
import io.streamthoughts.jikkou.core.models.HasItems;
import io.streamthoughts.jikkou.core.repository.LocalResourceRepository;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.io.IOException;
import java.util.concurrent.Callable;
import org.jetbrains.annotations.NotNull;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;

@Command(
    name = "replace",
    header = "Replace all resources.",
    description = "Replaces resources by deleting and (re)creating all the resources as defined in the resource descriptor files passed through the arguments."
)
@Singleton
public final class ReplaceResourceCommand extends CLIBaseCommand implements Callable<Integer> {

    // COMMAND OPTIONS
    @Mixin
    ExecOptionsMixin execOptions;
    @Mixin
    FileOptionsMixin fileOptions;
    @Mixin
    SelectorOptionsMixin selectorOptions;
    @Mixin
    ConfigOptionsMixin configOptionsMixin;
    @Mixin
    ProviderOptionMixin providerOptionMixin;
    // SERVICES
    @Inject
    JikkouApi api;

    @Inject
    LocalResourceRepository localResourceRepository;

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer call() throws IOException {
        try {
            HasItems resources = localResourceRepository.all(fileOptions);

            ApiChangeResultList results = api.replace(resources, getReconciliationContext());
            return execOptions.format.print(results, Jikkou.getExecutionTime(), execOptions.pretty);
        } catch (ValidationException exception) {
            System.out.println(ValidationErrorsWriter.write(exception.errors()));
            return CommandLine.ExitCode.SOFTWARE;
        }
    }

    private @NotNull ReconciliationContext getReconciliationContext() {
        return ReconciliationContext.builder()
            .dryRun(isDryRun())
            .configuration(configOptionsMixin.getConfiguration())
            .selector(selectorOptions.getResourceSelector())
            .labels(fileOptions.getLabels())
            .annotations(fileOptions.getAnnotations())
            .providerName(providerOptionMixin.getProvider())
            .build();
    }

    public boolean isDryRun() {
        return execOptions.dryRun;
    }
}
