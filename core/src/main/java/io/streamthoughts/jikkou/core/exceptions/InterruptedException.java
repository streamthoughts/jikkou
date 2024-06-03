/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) The original authors
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.streamthoughts.jikkou.core.exceptions;

/**
 * An unchecked wrapper for {@link java.lang.InterruptedException}
 */
public class InterruptedException extends JikkouRuntimeException {

    public InterruptedException(java.lang.InterruptedException cause) {
        super(cause);
        Thread.currentThread().interrupt();
    }
}
