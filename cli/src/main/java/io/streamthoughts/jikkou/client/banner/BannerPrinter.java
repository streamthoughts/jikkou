/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.client.banner;

public interface BannerPrinter {

    BannerPrinter NO_OP = banner -> { };

    /**
     * Prints the specified {@link Banner}.
     *
     * @param banner    the {@link Banner} instance to print.
     */
    void print(final Banner banner);
}
