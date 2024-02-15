/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.exceptions;

public class ConfigException extends JikkouRuntimeException {

    public ConfigException() {
        super();
    }

    public ConfigException(final String message) {
        super(message);
    }

    public ConfigException(final String message, final Throwable throwable) {
        super(message, throwable);
    }

    public ConfigException(final Throwable cause) {
        super(cause);
    }
}
