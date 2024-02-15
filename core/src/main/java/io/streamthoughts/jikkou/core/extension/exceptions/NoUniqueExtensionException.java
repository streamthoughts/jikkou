/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.extension.exceptions;

import io.streamthoughts.jikkou.core.exceptions.JikkouRuntimeException;

public final class NoUniqueExtensionException extends JikkouRuntimeException {

    /**
     * Creates a new {@link NoUniqueExtensionException} instance.
     *
     * @param message   the error message.
     */
    public NoUniqueExtensionException(final String message) {
        super(message);
    }
}
