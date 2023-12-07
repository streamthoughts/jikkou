/*
 * Copyright 2023 The original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.streamthoughts.jikkou.client.printer;

import static io.streamthoughts.jikkou.client.printer.Ansi.isColor;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.streamthoughts.jikkou.core.exceptions.JikkouRuntimeException;
import io.streamthoughts.jikkou.core.io.Jackson;
import io.streamthoughts.jikkou.core.models.ApiChangeResultList;
import io.streamthoughts.jikkou.core.reconciler.ChangeResult;
import io.streamthoughts.jikkou.core.reconciler.Operation;
import io.streamthoughts.jikkou.core.reconciler.TextDescription;
import java.io.PrintStream;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Helper class pretty print execution results.
 */
public class TextPrinter implements Printer {

    private static final String PADDING = "********************************************************************************";

    private static final PrintStream PS = System.out;

    private final boolean printChangeDetail;

    /**
     * Creates a new {@link TextPrinter} instance.
     *
     * @param printChangeDetail {@code true} if details should be print
     */
    public TextPrinter(boolean printChangeDetail) {
        this.printChangeDetail = printChangeDetail;
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public int print(ApiChangeResultList result,
                     long executionTimeMs) {
        int ok = 0;
        int created = 0;
        int changed = 0;
        int deleted = 0;
        int failed = 0;
        for (ChangeResult change : result.results()) {
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
                        created++;
                    }
                    case UPDATE -> {
                        color = Ansi.Color.YELLOW;
                        changed++;
                    }
                    case DELETE -> {
                        color = Ansi.Color.RED;
                        deleted++;
                    }
                }
            } else if (change.isFailed()) {
                failed++;
            } else {
                color = Ansi.Color.BLUE;
                ok++;
            }

            TextPrinter.printTask(change.change().getSpec().getOp(), change.description(), change.status().name());
            if (printChangeDetail) {
                TextPrinter.PS.printf("%s%s%n", isColor() ? color : "", json);
            }
        }

        TextPrinter.PS.printf("%sEXECUTION in %s %s%n", isColor() ? Ansi.Color.WHITE : "", formatExecutionTime(executionTimeMs), result.dryRun() ? "(DRY_RUN)" : "");
        TextPrinter.PS.printf("%sok : %d, created : %d, altered : %d, deleted : %d failed : %d%n", isColor() ? Ansi.Color.WHITE : "", ok, created, changed, deleted, failed);
        return failed > 0 ? 1 : 0;
    }

    private static void printTask(final Operation operation,
                                  final TextDescription description,
                                  final String status) {
        String text = Optional.ofNullable(description).map(TextDescription::textual).orElse("");
        String padding = (text.length() < PADDING.length()) ? PADDING.substring(text.length()) : "";
        PS.printf("%sTASK [%s] %s - %s %s%n", isColor() ? Ansi.Color.WHITE : "", operation, text, status, padding);
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
}