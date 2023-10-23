/*
 * Copyright 2022 The original authors
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
package io.streamthoughts.jikkou.client.banner;


import io.streamthoughts.jikkou.core.JikkouInfo;
import java.io.PrintStream;

/**
 * The default {@link Banner} implementation which writes "Kafka Streams".
 */
public class JikkouBanner implements Banner {

    private static final String[] BANNER = {"",
            "       __   __   __  ___  __  ___   ______    __    __  ",
            "      |  | |  | |  |/  / |  |/  /  /  __  \\  |  |  |  | ",
            "      |  | |  | |  '  /  |  '  /  |  |  |  | |  |  |  | ",
            ".--.  |  | |  | |    <   |    <   |  |  |  | |  |  |  | ",
            "|  `--'  | |  | |  .  \\  |  .  \\  |  `--'  | |  `--'  | ",
            " \\______/  |__| |__|\\__\\ |__|\\__\\  \\______/   \\______/ ", ""
    };

    private static final String JIKKOU = " :: Jikkou :: ";

    private static final int STRAP_LINE_SIZE = 55;

    /**
     * {@inheritDoc}
     */
    @Override
    public void print(final PrintStream printStream) {
        for (String line : BANNER) {
            printStream.println(line);
        }

        String version = JikkouInfo.getVersion();
        version = (version != null) ? " (v" + version + ")" : "";
        StringBuilder padding = new StringBuilder();
        while (padding.length() < STRAP_LINE_SIZE - (version.length() + JIKKOU.length())) {
            padding.append(" ");
        }

        printStream.println(JIKKOU + padding + version);
        printStream.println();
    }
}