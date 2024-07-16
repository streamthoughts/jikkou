/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.exceptions;

/**
 * Exception to be thrown for invalid Jikkou configuration.
 */
public class InvalidConfigException extends JikkouRuntimeException {

    public InvalidConfigException() {
        super();
    }

    public InvalidConfigException(final String message) {
        super(message);
    }

    public InvalidConfigException(final String message, final Throwable throwable) {
        super(message, throwable);
    }

    public InvalidConfigException(final Throwable cause) {
        super(cause);
    }
}
