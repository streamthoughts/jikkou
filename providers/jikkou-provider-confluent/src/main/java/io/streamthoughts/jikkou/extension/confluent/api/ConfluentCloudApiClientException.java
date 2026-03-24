/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.extension.confluent.api;

import io.streamthoughts.jikkou.core.exceptions.JikkouRuntimeException;

public class ConfluentCloudApiClientException extends JikkouRuntimeException {

    public ConfluentCloudApiClientException(final String message) {
        super(message);
    }

    public ConfluentCloudApiClientException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
