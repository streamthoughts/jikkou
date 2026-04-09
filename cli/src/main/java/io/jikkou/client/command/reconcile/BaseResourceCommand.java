/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.jikkou.client.command.reconcile;

import io.jikkou.client.Jikkou;
import io.jikkou.client.command.CLIBaseCommand;
import io.jikkou.client.command.ConfigOptionsMixin;
import io.jikkou.client.command.ExecOptionsMixin;
import io.jikkou.client.command.FileOptionsMixin;
import io.jikkou.client.command.ProviderOptionMixin;
import io.jikkou.client.command.ProviderResolver;
import io.jikkou.client.command.SelectorOptionsMixin;
import io.jikkou.client.command.validate.ValidationErrorsWriter;
import io.jikkou.client.printer.Printers;
import io.jikkou.core.JikkouApi;
import io.jikkou.core.ReconciliationContext;
import io.jikkou.core.ReconciliationMode;
import io.jikkou.core.config.Configuration;
import io.jikkou.core.exceptions.ValidationException;
import io.jikkou.core.models.ApiChangeResultList;
import io.jikkou.core.models.HasItems;
import io.jikkou.core.repository.LocalResourceRepository;
import jakarta.inject.Inject;
import java.io.IOException;
import java.util.concurrent.Callable;
import org.jetbrains.annotations.NotNull;
import picocli.CommandLine;
import picocli.CommandLine.Mixin;

public abstract class BaseResourceCommand extends CLIBaseCommand implements Callable<Integer> {

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

    @Inject
    Configuration configuration;

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer call() throws IOException {
        try {
            ApiChangeResultList results = api.reconcile(
                    getResources(),
                    getReconciliationMode(),
                    getReconciliationContext()
            );
            Printers printers = execOptions.format;
            return printers.print(results, Jikkou.getExecutionTime(), execOptions.pretty);
        } catch (ValidationException exception) {
            System.out.println(ValidationErrorsWriter.write(exception.errors()));
            return CommandLine.ExitCode.SOFTWARE;
        }
    }

    private @NotNull ReconciliationContext getReconciliationContext() {
        return new ProviderResolver(configuration).buildReconciliationContext(
                providerOptionMixin, configOptionsMixin, selectorOptions, fileOptions, isDryRun());
    }

    protected @NotNull HasItems getResources() {
        return localResourceRepository.all(fileOptions);
    }

    protected abstract @NotNull ReconciliationMode getReconciliationMode();

    public boolean isDryRun() {
        return execOptions.dryRun;
    }
}
