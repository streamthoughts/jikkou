/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.jikkou.client.printer;

import static io.jikkou.client.printer.Ansi.isColor;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.jikkou.core.exceptions.JikkouRuntimeException;
import io.jikkou.core.io.Jackson;
import io.jikkou.core.models.ApiChangeResultList;
import io.jikkou.core.models.CoreAnnotations;
import io.jikkou.core.reconciler.ChangeResult;
import io.jikkou.core.reconciler.Operation;
import io.jikkou.core.reconciler.TextDescription;
import java.io.PrintStream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Helper class pretty print execution results.
 */
public class TextPrinter implements Printer {

    private static final String PADDING = "********************************************************************************";

    private final boolean printChangeDetail;

    /**
     * Creates a new {@link TextPrinter} instance.
     *
     * @param printChangeDetail {@code true} if details should be print
     */
    public TextPrinter(boolean printChangeDetail) {
        this.printChangeDetail = printChangeDetail;
    }

    // Resolved at call time so that tests can capture output through System.setOut().
    private static PrintStream out() {
        return System.out;
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public int print(ApiChangeResultList result, long executionTimeMs, boolean pretty) {
        Counts counts = new Counts();

        // Group results by the provider that executed them (multi-provider operations
        // annotate each change with jikkou.io/provider). A single unnamed group keeps
        // the flat single-provider output unchanged.
        Map<String, List<ChangeResult>> byProvider = result.results().stream()
                .collect(Collectors.groupingBy(
                        r -> Optional.ofNullable(r.change())
                                .map(CoreAnnotations::getProvider)
                                .orElse(""),
                        LinkedHashMap::new,
                        Collectors.toList()));

        for (Map.Entry<String, List<ChangeResult>> group : byProvider.entrySet()) {
            String providerName = group.getKey();
            if (!providerName.isEmpty()) {
                printProviderHeader(providerName);
            }
            for (ChangeResult change : group.getValue()) {
                printResult(change, counts);
            }
        }

        out().printf("%sEXECUTION in %s %s%n", isColor() ? Ansi.Color.WHITE : "", formatExecutionTime(executionTimeMs), result.dryRun() ? "(DRY_RUN)" : "");
        out().printf("%sok : %d, created : %d, altered : %d, deleted : %d failed : %d%n", isColor() ? Ansi.Color.WHITE : "", counts.ok, counts.created, counts.changed, counts.deleted, counts.failed);
        return counts.failed > 0 ? 1 : 0;
    }

    private void printResult(final ChangeResult change, final Counts counts) {
        final String json;
        try {
            json = Jackson.JSON_OBJECT_MAPPER
                    .writerWithDefaultPrettyPrinter()
                    .writeValueAsString(change);
        } catch (JsonProcessingException e) {
            throw new JikkouRuntimeException(e);
        }

        String color = Ansi.Color.WHITE;
        Operation operation = change.change().getSpec().getOp();
        if (change.isChanged()) {
            switch (operation) {
                case CREATE -> {
                    color = Ansi.Color.GREEN;
                    counts.created++;
                }
                case UPDATE -> {
                    color = Ansi.Color.YELLOW;
                    counts.changed++;
                }
                case DELETE -> {
                    color = Ansi.Color.RED;
                    counts.deleted++;
                }
            }
        } else if (change.isFailed()) {
            counts.failed++;
        } else {
            color = Ansi.Color.BLUE;
            counts.ok++;
        }

        printTask(change.change().getSpec().getOp(), change.description(), change.status().name());
        if (printChangeDetail) {
            out().printf("%s%s%n", isColor() ? color : "", json);
        }
    }

    private static void printProviderHeader(final String providerName) {
        String text = "PROVIDER [%s] ".formatted(providerName);
        String padding = (text.length() < PADDING.length()) ? PADDING.substring(text.length()) : "";
        out().printf("%s%s%s%n", isColor() ? Ansi.Color.WHITE : "", text, padding);
    }

    private static void printTask(final Operation operation,
                                  final TextDescription description,
                                  final String status) {
        String text = Optional.ofNullable(description).map(TextDescription::textual).orElse("");
        String padding = (text.length() < PADDING.length()) ? PADDING.substring(text.length()) : "";
        out().printf("%sTASK [%s] %s - %s %s%n", isColor() ? Ansi.Color.WHITE : "", operation, text, status, padding);
    }

    private String formatExecutionTime(long execTimeInMillis) {
        long minutes = TimeUnit.MILLISECONDS.toMinutes(execTimeInMillis) % 60;
        long seconds = TimeUnit.MILLISECONDS.toSeconds(execTimeInMillis) % 60;
        long milliseconds = execTimeInMillis % 1000;

        if (minutes == 0) {
            return seconds == 0 ?
                    String.format("%dms", milliseconds) :
                    String.format("%ds %dms", seconds, milliseconds);
        }
        return String.format("%dmin %ds %dms", minutes, seconds, milliseconds);
    }

    private static final class Counts {
        private int ok;
        private int created;
        private int changed;
        private int deleted;
        private int failed;
    }
}
