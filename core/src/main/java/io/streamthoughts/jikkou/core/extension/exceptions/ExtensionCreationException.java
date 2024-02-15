/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.extension.exceptions;

import io.streamthoughts.jikkou.core.exceptions.JikkouRuntimeException;

/**
 * Indicates that a checked exception was thrown during creation of an extension.
 */
public class ExtensionCreationException extends JikkouRuntimeException {

    /**
     * Creates a new {@link ExtensionCreationException} instance.
     *
     * @param cause the cause.
     */
    public ExtensionCreationException(Throwable cause) {
        super(cause);
    }

    /**
     * Creates a new {@link ExtensionCreationException} instance.
     *
     * @param message the error message.
     */
    public ExtensionCreationException(String message) {
        super(message);
    }
}
