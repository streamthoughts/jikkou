/*
 * Copyright 2020 StreamThoughts.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
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

import com.fasterxml.jackson.core.JsonProcessingException;
import io.streamthoughts.jikkou.api.control.Change;
import io.streamthoughts.jikkou.api.control.ChangeDescription;
import io.streamthoughts.jikkou.api.control.ChangeResult;
import io.streamthoughts.jikkou.api.control.ChangeType;
import io.streamthoughts.jikkou.api.error.JikkouRuntimeException;
import io.streamthoughts.jikkou.api.io.Jackson;
import java.io.PrintStream;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Helper class pretty print execution results.
 */
public class TextPrinter implements Printer {

    private static final String PADDING = "********************************************************************************";

    private static final String JIKKOU_COLOR_ENABLED = "JIKKOU_COLOR_ENABLED";

    private static final PrintStream PS = System.out;

    private static final String ANSI_RED = "\u001B[31m";
    private static final String ANSI_GREEN = "\u001B[32m";
    private static final String ANSI_WHITE = "\u001B[37m";
    private static final String ANSI_YELLOW = "\u001B[33m";
    private static final String ANSI_BLUE = "\u001B[36m";

    private final boolean printChangeDetail;

    /**
     * Creates a new {@link TextPrinter} instance.
     *
     * @param printChangeDetail  {@code true} if details should be print
     */
    public TextPrinter(boolean printChangeDetail) {
        this.printChangeDetail = printChangeDetail;
    }

    /** {@inheritDoc} **/
    @Override
    public int print(List<ChangeResult<Change>> results,
                     boolean dryRun,
                     long executionTimeMs) {
        int ok = 0;
        int created = 0;
        int changed = 0;
        int deleted = 0;
        int failed = 0;
        for (ChangeResult<?> result : results) {
            final String json;
            try {
                json = Jackson.JSON_OBJECT_MAPPER
                        .writerWithDefaultPrettyPrinter()
                        .writeValueAsString(result);
            } catch (JsonProcessingException e) {
                throw new JikkouRuntimeException(e);
            }

            String color = TextPrinter.ANSI_WHITE;
            ChangeType changeType = result.resource().getChangeType();
            if (result.isChanged()) {
                switch (changeType) {
                    case ADD -> {
                        color = TextPrinter.ANSI_GREEN;
                        created++;
                    }
                    case UPDATE -> {
                        color = TextPrinter.ANSI_YELLOW;
                        changed++;
                    }
                    case DELETE -> {
                        color = TextPrinter.ANSI_RED;
                        deleted++;
                    }
                }
            } else if (result.isFailed()) {
                failed++;
            } else {
                color = TextPrinter.ANSI_BLUE;
                ok++;
            }

            TextPrinter.printTask(result.resource().getChangeType(), result.description(), result.status().name());
            if (printChangeDetail) {
                TextPrinter.PS.printf("%s%s%n", TextPrinter.isColor() ? color : "", json);
            }
        }

        TextPrinter.PS.printf("%sEXECUTION in %s %s%n", TextPrinter.isColor() ? TextPrinter.ANSI_WHITE : "", formatExecutionTime(executionTimeMs), dryRun ? "(DRY_RUN)" : "");
        TextPrinter.PS.printf("%sok : %d, created : %d, altered : %d, deleted : %d failed : %d%n", TextPrinter.isColor() ? TextPrinter.ANSI_WHITE : "", ok, created, changed, deleted, failed);
        return failed > 0 ? 1 : 0;
    }

    private static void printTask(final ChangeType changeType,
                                  final ChangeDescription description,
                                  final String status) {
        String text = description.textual();
        String padding = (text.length() < PADDING.length()) ? PADDING.substring(text.length()) : "";
        PS.printf("%sTASK [%s] %s - %s %s%n", isColor() ? ANSI_WHITE : "", changeType, text, status, padding);
    }

    private static boolean isColor() {
        String enabled = System.getenv(JIKKOU_COLOR_ENABLED);
        return enabled == null || "true".equalsIgnoreCase(enabled);
    }

    private String formatExecutionTime(long execTimeInMillis) {
        long minutes = TimeUnit.MILLISECONDS.toMinutes(execTimeInMillis) % 60;
        long seconds = TimeUnit.MILLISECONDS.toSeconds(execTimeInMillis) % 60;
        long milliseconds = execTimeInMillis % 1000;

        if (minutes == 0) {
            return seconds == 0 ?
                    String.format ("%dms", milliseconds) :
                    String.format ("%ds %dms", seconds, milliseconds);
        }
        return String.format("%dmin %ds %dms", minutes, seconds, milliseconds);
    }
}