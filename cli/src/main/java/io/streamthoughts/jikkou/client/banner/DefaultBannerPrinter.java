/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.client.banner;

import java.io.PrintStream;
import java.util.Objects;

public class DefaultBannerPrinter implements BannerPrinter {

    private final PrintStream printStream;

    /**
     * Creates a new {@link DefaultBannerPrinter} instance.
     *
     * @param printStream the {@link PrintStream} to be used for printing the {@link Banner}.
     */
    DefaultBannerPrinter(final PrintStream printStream) {
        Objects.requireNonNull(printStream, "printStream cannot be null");
        this.printStream = printStream;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void print(final Banner banner) {
        banner.print(printStream);
    }
}