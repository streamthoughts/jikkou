/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.client.command;

import io.streamthoughts.jikkou.client.command.validate.ValidationErrorsWriter;
import io.streamthoughts.jikkou.core.JikkouApi;
import io.streamthoughts.jikkou.core.ReconciliationContext;
import io.streamthoughts.jikkou.core.exceptions.ValidationException;
import io.streamthoughts.jikkou.core.io.writer.ResourceWriter;
import io.streamthoughts.jikkou.core.models.HasItems;
import io.streamthoughts.jikkou.core.repository.LocalResourceRepository;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.Callable;
import org.jetbrains.annotations.NotNull;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;

@Command(name = "prepare",
        header = "Prepare the resource definition files for validation.",
        description = """
                Prepare the resource definition files specified through the command line arguments for validation.
                """
)
@Singleton
public class PrepareCommand extends CLIBaseCommand implements Callable<Integer> {

    // COMMAND OPTIONS
    @Mixin
    FileOptionsMixin fileOptions;
    @Mixin
    SelectorOptionsMixin selectorOptions;
    @Mixin
    FormatOptionsMixin formatOptions;
    @Mixin
    ConfigOptionsMixin configOptionsMixin;

    // API
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

        try {
            HasItems result = api.prepare(
                    getResources(),
                    getReconciliationContext()
            );
            try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                writer.write(formatOptions.format, result.getItems(), baos);
                System.out.println(baos);
                return CommandLine.ExitCode.OK;
            }
        } catch (ValidationException exception) {
            System.out.println(ValidationErrorsWriter.write(exception.errors()));
            return CommandLine.ExitCode.SOFTWARE;
        }
    }

    @NotNull
    private HasItems getResources() {
        return localResourceRepository.all(fileOptions);
    }

    @NotNull
    private ReconciliationContext getReconciliationContext() {
        return ReconciliationContext
                .builder()
                .dryRun(true)
                .configuration(configOptionsMixin.getConfiguration())
                .selector(selectorOptions.getResourceSelector())
                .labels(fileOptions.getLabels())
                .annotations(fileOptions.getAnnotations())
                .build();
    }
}
