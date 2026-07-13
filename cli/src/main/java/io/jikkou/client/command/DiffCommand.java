/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.jikkou.client.command;

import io.jikkou.client.command.validate.ValidationErrorsWriter;
import io.jikkou.core.JikkouApi;
import io.jikkou.core.ReconciliationContext;
import io.jikkou.core.config.Configuration;
import io.jikkou.core.exceptions.ValidationException;
import io.jikkou.core.io.writer.ResourceWriter;
import io.jikkou.core.models.ApiResourceChangeList;
import io.jikkou.core.models.HasItems;
import io.jikkou.core.models.change.ResourceChange;
import io.jikkou.core.reconciler.Operation;
import io.jikkou.core.reconciler.SimpleResourceChangeFilter;
import io.jikkou.core.repository.LocalResourceRepository;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import java.util.stream.Stream;
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

    /**
     * Exit code returned when {@code --fail-on-changes} is set and the diff contains at least one change.
     * Distinct from {@code 1} (validation/execution error) and {@code 2} (usage error).
     *
     * @since 1.1.0
     */
    public static final int EXIT_CODE_CHANGES_DETECTED = 3;

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

    @Option(names = {"--fail-on-changes"},
        defaultValue = "false",
        description = "Exit with code 3 when the diff contains at least one change, i.e., any operation" +
            " other than NONE. Useful for detecting configuration drift in CI (default: ${DEFAULT-VALUE})."
    )
    private boolean failOnChanges;

    // API
    @Inject
    JikkouApi api;
    @Inject
    LocalResourceRepository localResourceRepository;
    @Inject
    ResourceWriter writer;
    @Inject
    Configuration configuration;

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
                if (failOnChanges) {
                    List<ResourceChange> changes = result.getItems();
                    System.err.println(summarize(changes));
                    return hasChanges(changes) ? EXIT_CODE_CHANGES_DETECTED : CommandLine.ExitCode.OK;
                }
                return CommandLine.ExitCode.OK;
            }
        } catch (ValidationException exception) {
            System.out.println(ValidationErrorsWriter.write(exception.errors()));
            return CommandLine.ExitCode.SOFTWARE;
        }
    }

    static boolean hasChanges(final List<ResourceChange> changes) {
        return changes.stream().anyMatch(change -> change.getSpec().getOp().isChanged());
    }

    static String summarize(final List<ResourceChange> changes) {
        Map<Operation, Long> counts = changes.stream()
            .map(change -> change.getSpec().getOp())
            .filter(Operation::isChanged)
            .collect(Collectors.groupingBy(op -> op, () -> new EnumMap<>(Operation.class), Collectors.counting()));

        long total = counts.values().stream().mapToLong(Long::longValue).sum();
        if (total == 0) {
            return "No changes detected.";
        }
        String details = Stream.of(Operation.CREATE, Operation.UPDATE, Operation.DELETE, Operation.REPLACE)
            .filter(counts::containsKey)
            .map(op -> counts.get(op) + " " + op.name())
            .collect(Collectors.joining(", "));
        return "%d change%s detected: %s".formatted(total, total > 1 ? "s" : "", details);
    }

    @NotNull
    private HasItems getResources() {
        return localResourceRepository.all(fileOptions);
    }

    @NotNull
    private ReconciliationContext getReconciliationContext() {
        return new ProviderResolver(configuration).buildReconciliationContext(
                providerOptionMixin, configOptionsMixin, selectorOptions, fileOptions, true);
    }
}
