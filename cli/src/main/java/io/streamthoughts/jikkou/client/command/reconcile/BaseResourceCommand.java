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
import io.streamthoughts.jikkou.client.command.SelectorOptionsMixin;
import io.streamthoughts.jikkou.client.command.validate.ValidationErrorsWriter;
import io.streamthoughts.jikkou.client.printer.Printers;
import io.streamthoughts.jikkou.core.JikkouApi;
import io.streamthoughts.jikkou.core.ReconciliationContext;
import io.streamthoughts.jikkou.core.ReconciliationMode;
import io.streamthoughts.jikkou.core.exceptions.ValidationException;
import io.streamthoughts.jikkou.core.io.ResourceLoaderFacade;
import io.streamthoughts.jikkou.core.models.ApiChangeResultList;
import io.streamthoughts.jikkou.core.models.HasItems;
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

    // SERVICES
    @Inject
    JikkouApi api;
    @Inject
    ResourceLoaderFacade loader;
    
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
        return ReconciliationContext.builder()
                .dryRun(isDryRun())
                .configuration(configOptionsMixin.getConfiguration())
                .selector(selectorOptions.getResourceSelector())
                .labels(fileOptions.getLabels())
                .annotations(fileOptions.getAnnotations())
                .build();
    }

    protected @NotNull HasItems getResources() {
        return loader.load(fileOptions);
    }

    protected abstract @NotNull ReconciliationMode getReconciliationMode();

    public boolean isDryRun() {
        return execOptions.dryRun;
    }
}
