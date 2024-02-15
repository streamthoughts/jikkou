/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.kafka.connect.api;

import java.util.Arrays;
import java.util.Locale;
import org.jetbrains.annotations.NotNull;

public enum AuthMethod {
    INVALID,
    BASICAUTH,
    NONE;

    public static AuthMethod getForNameIgnoreCase(final @NotNull String str) {
        return Arrays.stream(AuthMethod.values())
                .filter(e -> e.name().equals(str.toUpperCase(Locale.ROOT)))
                .findFirst()
                .orElse(AuthMethod.INVALID);
    }
}
