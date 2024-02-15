/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.http.client.exception;

import io.streamthoughts.jikkou.core.exceptions.JikkouRuntimeException;

public class JikkouApiClientException extends JikkouRuntimeException {

    public JikkouApiClientException() {
        super();
    }

    public JikkouApiClientException(final String message) {
        super(message);
    }

    public JikkouApiClientException(final String message, final Throwable throwable) {
        super(message, throwable);
    }

    public JikkouApiClientException(final Throwable cause) {
        super(cause);
    }
}
