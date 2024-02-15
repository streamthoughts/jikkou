/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.client.printer;

public final class Ansi {
    private static final String JIKKOU_CLI_NO_COLOR = "JIKKOU_CLI_NO_COLOR";

    public static class Color {
        public static final String RED = "\u001B[31m";
        public static final String GREEN = "\u001B[32m";
        public static final String WHITE = "\u001B[37m";
        public static final String BOLD_WHITE = "\u001B[37;1m";
        public static final String YELLOW = "\u001B[33m";
        public static final String BLUE = "\u001B[36m";
        public static final String DEFAULT = "\u001B[0m";
    }

    public static boolean isColor() {
        String noColor = System.getenv(JIKKOU_CLI_NO_COLOR);
        return noColor == null || "false".equalsIgnoreCase(noColor);
    }

}
