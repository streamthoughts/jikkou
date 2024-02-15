/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.exceptions;

/**
 * Throws when a resource cannot be processed.
 */
public class InvalidResourceException extends JikkouRuntimeException {

    public InvalidResourceException(final String message) {
        super(message);
    }

    public InvalidResourceException(final String message, final Throwable throwable) {
        super(message, throwable);
    }
}
