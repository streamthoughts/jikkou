/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.exceptions;

/**
 * Top-level exception for Kafka Specs.
 */
public class JikkouRuntimeException extends RuntimeException {

    public JikkouRuntimeException() {
        super();
    }

    public JikkouRuntimeException(final String message) {
        super(message);
    }

    public JikkouRuntimeException(final String message, final Throwable throwable) {
        super(message, throwable);
    }

    public JikkouRuntimeException(final Throwable cause) {
        super(cause);
    }

}
