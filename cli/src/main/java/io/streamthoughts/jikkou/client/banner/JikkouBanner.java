/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
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