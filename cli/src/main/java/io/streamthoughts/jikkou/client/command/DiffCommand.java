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
import io.streamthoughts.jikkou.core.models.ApiResourceChangeList;
import io.streamthoughts.jikkou.core.models.HasItems;
import io.streamthoughts.jikkou.core.reconciler.Operation;
import io.streamthoughts.jikkou.core.reconciler.SimpleResourceChangeFilter;
import io.streamthoughts.jikkou.core.repository.LocalResourceRepository;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import org.jetbrains.annotations.NotNull;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;

@Command(name = "diff",
        header = "Show resource changes required by the current resource definitions.",
        description = """
                Generates a speculative reconciliation plan, showing the resource changes Jikkou would apply to reconcile the resource definitions.
                This command does not actually perform the reconciliation actions.
                """)
@Singleton
public class DiffCommand extends CLIBaseCommand implements Callable<Integer> {

    // COMMAND OPTIONS
    @Mixin
    FileOptionsMixin fileOptions;
    @Mixin
    SelectorOptionsMixin selectorOptions;
    @Mixin
    FormatOptionsMixin formatOptions;
    @Mixin
    ConfigOptionsMixin configOptionsMixin;

    @Option(names = {"--" + SimpleResourceChangeFilter.FILTER_RESOURCE_OPS_NAME},
            split = ",",
            description = "Filter out all resources except those corresponding to given operations. Valid values: ${COMPLETION-CANDIDATES}."
    )
    Set<Operation> filterOutAllResourcesExcept = new HashSet<>();

    @Option(names = {"--" + SimpleResourceChangeFilter.FILTER_CHANGE_OP_NAME},
            split = ",",
            description = "Filter out all state-changes except those corresponding to given operations. Valid values: ${COMPLETION-CANDIDATES}."
    )
    Set<Operation> filterOutAllChangesExcept = new HashSet<>();

    @Option(names = {"--list"},
            defaultValue = "false",
            description = "Get resources as ApiResourceChangeList (default: ${DEFAULT-VALUE})."
    )
    private boolean list;

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
            ApiResourceChangeList result = api.getDiff(
                    getResources(),
                    new SimpleResourceChangeFilter()
                            .filterOutAllResourcesExcept(filterOutAllResourcesExcept)
                            .filterOutAllChangesExcept(filterOutAllChangesExcept),
                    getReconciliationContext()
            );
            try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                if (list) {
                    writer.write(formatOptions.format(), result, baos);
                } else {
                    writer.write(formatOptions.format(), result.getItems(), baos);
                }
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
