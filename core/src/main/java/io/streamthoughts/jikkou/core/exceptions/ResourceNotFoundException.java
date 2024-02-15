/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.exceptions;

import org.jetbrains.annotations.NotNull;

public class ResourceNotFoundException extends JikkouRuntimeException {

    /**
     * Creates a new {@link ResourceNotFoundException} instance.
     * @param message   The error message.
     */
    public ResourceNotFoundException(@NotNull final String message) {
        super(message);
    }
}
