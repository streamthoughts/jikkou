/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.exceptions;

public class JikkouApiException extends JikkouRuntimeException {

    /**
     * Creates a new {@link JikkouApiException} instance.
     *
     * @param message   the detail message.
     */
    public JikkouApiException(final String message) {
        super(message);
    }

    /**
     * Creates a new {@link JikkouApiException} instance.
     *
     * @param message   the detail message.
     * @param throwable the cause.
     */
    public JikkouApiException(final String message, final Throwable throwable) {
        super(message, throwable);
    }

    /**
     * Creates a new {@link JikkouApiException} instance.
     *
     * @param throwable the cause.
     */
    public JikkouApiException(final Throwable throwable) {
        super(throwable);
    }
}
