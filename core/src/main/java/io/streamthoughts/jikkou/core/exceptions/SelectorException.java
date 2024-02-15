/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.exceptions;

public class SelectorException extends JikkouRuntimeException {

    public SelectorException() {
    }

    public SelectorException(String message) {
        super(message);
    }

    public SelectorException(String message, Throwable throwable) {
        super(message, throwable);
    }

    public SelectorException(Throwable cause) {
        super(cause);
    }
}
