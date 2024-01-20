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
