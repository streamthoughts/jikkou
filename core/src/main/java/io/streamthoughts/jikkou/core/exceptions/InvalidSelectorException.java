/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.exceptions;

public class InvalidSelectorException extends JikkouRuntimeException {

    public InvalidSelectorException() {
    }

    public InvalidSelectorException(String message) {
        super(message);
    }

    public InvalidSelectorException(String message, Throwable throwable) {
        super(message, throwable);
    }

    public InvalidSelectorException(Throwable cause) {
        super(cause);
    }
}
