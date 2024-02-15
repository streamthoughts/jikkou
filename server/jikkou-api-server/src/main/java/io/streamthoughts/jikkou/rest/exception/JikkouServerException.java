/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.rest.exception;

import io.streamthoughts.jikkou.core.exceptions.JikkouRuntimeException;

public class JikkouServerException extends JikkouRuntimeException {

    public JikkouServerException() {
        super();
    }

    public JikkouServerException(final String message) {
        super(message);
    }

    public JikkouServerException(final String message, final Throwable throwable) {
        super(message, throwable);
    }

    public JikkouServerException(final Throwable cause) {
        super(cause);
    }
}
