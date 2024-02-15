/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.client.banner;

import java.io.PrintStream;

public interface Banner {

    enum Mode {
        CONSOLE, LOG, OFF;
    }

    void print(final PrintStream os);
}