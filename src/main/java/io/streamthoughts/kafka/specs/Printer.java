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
package io.streamthoughts.kafka.specs;

import com.fasterxml.jackson.core.JsonProcessingException;

import java.io.PrintStream;
import java.util.Collection;

/**
 * Helper class pretty print execution results.
 */
public class Printer {

    private static final String PADDING = "********************************************************************************";

    private static final String KAFKA_SPECS_COLOR_ENABLED = "KAFKA_SPECS_COLOR_ENABLED";

    private static final PrintStream PS = System.out;

    private static final String ANSI_RED = "\u001B[31m";
    private static final String ANSI_GREEN = "\u001B[32m";
    private static final String ANSI_WHITE = "\u001B[37m";
    private static final String ANSI_YELLOW = "\u001B[33m";

    /**
     * Print the specified execution results to stdout and terminate the application with the appropriate exit code.
     *
     * @param results   the execution results to print.
     * @param verbose   print details.
     */
    public static <T> int print(final Collection<OperationResult<T>> results,
                                final boolean verbose,
                                final boolean dryRun) {
        int ok = 0;
        int changed = 0;
        int failed = 0;
        for (OperationResult<?> r : results) {
            final String json;
            try {
                json = Jackson.JSON_OBJECT_MAPPER
                        .writerWithDefaultPrettyPrinter()
                        .writeValueAsString(r);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
            String color;
            if (r.isFailed()) {
                color = ANSI_RED;
                failed++;
            }
            else if (r.isChanged()) {
                color = ANSI_YELLOW;
                changed++;
            } else {
                color = ANSI_GREEN;
                ok++;
            }
            printTask(r.description(), r.status().name());
            if (verbose) {
                PS.printf("%s%s\n", isColor() ? color : "", json);
            }
        }

        PS.printf("%sEXECUTION in %s %s\n", isColor() ? ANSI_WHITE : "", KafkaSpecs.getExecutionTime(), dryRun ? "(DRY_RUN)" : "");
        PS.printf("%sok : %d, changed : %d, failed : %d\n", isColor() ? ANSI_WHITE : "", ok, changed, failed);
        return failed > 0 ? 1 : 0;
    }

    private static void printTask(final Description description, final String status) {
        String text = description.textDescription();
        String padding =  (text.length() < PADDING.length()) ? PADDING.substring(text.length()) : "";
        PS.printf("%sTASK [%s] %s - %s %s\n", isColor() ? ANSI_WHITE : "", description.operation().name(), text, status, padding);
    }

    private static boolean isColor() {
        String enabled = System.getenv(KAFKA_SPECS_COLOR_ENABLED);
        return enabled == null || "true".equalsIgnoreCase(enabled);
    }

}